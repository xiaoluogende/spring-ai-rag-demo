package com.simpleai.demo.service.impl;

import com.simpleai.demo.dto.ChatAnswer;
import com.simpleai.demo.entity.ChatRecord;
import com.simpleai.demo.service.ChatRecordService;
import com.simpleai.demo.service.RagService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 由 {@link com.simpleai.demo.config.RagConfiguration} 装配，
 * 不在类上标 @Service —— 组件扫描不处理 @ConditionalOnProperty，
 * 只有通过 @Bean 方法创建才能受 app.rag.enabled 控制。
 */
public class RagServiceImpl implements RagService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ChatRecordService chatRecordService;
    private final Resource ragSystemPrompt;
    private final int topK;
    private final double similarityThreshold;

    public RagServiceImpl(ChatClient.Builder builder,
                          VectorStore vectorStore,
                          ChatRecordService chatRecordService,
                          @Value("classpath:prompts/rag-system.st") Resource ragSystemPrompt,
                          @Value("${app.rag.top-k:4}") int topK,
                          @Value("${app.rag.similarity-threshold:0.2}") double similarityThreshold) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
        this.chatRecordService = chatRecordService;
        this.ragSystemPrompt = ragSystemPrompt;
        this.topK = topK;
        this.similarityThreshold = similarityThreshold;
    }

    @Override
    public ChatAnswer ask(String question) {
        long start = System.currentTimeMillis();

        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(topK)
                        .similarityThreshold(similarityThreshold)
                        .build());

        // 检索不到 → 不调模型，直接返回。这是防幻觉的第一道闸门
        if (docs == null || docs.isEmpty()) {
            return new ChatAnswer(
                    """
                    ## 没有找到相关依据

                    知识库中没有与这个问题匹配的内容，我不会在缺乏依据的情况下编造答案。

                    可能的原因：
                    1. 相关文档还没有导入知识库
                    2. 换个说法可能更容易匹配到
                    """,
                    List.of(),
                    System.currentTimeMillis() - start);
        }

        String context = docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        String answer = chatClient.prompt()
                .system(ragSystemPrompt)
                .user(u -> u.text("""
                        【参考资料】
                        {context}

                        【问题】
                        {question}
                        """).param("context", context).param("question", question))
                .call()
                .content();

        Map<String, Document> bestByDoc = new LinkedHashMap<>();
        for (Document d : docs) {
            String src = String.valueOf(d.getMetadata().getOrDefault("source", "unknown"));
            bestByDoc.merge(src, d, (a, b) -> scoreOf(a) >= scoreOf(b) ? a : b);
        }

        List<ChatAnswer.Reference> references = bestByDoc.entrySet().stream()
                .map(e -> new ChatAnswer.Reference(
                        e.getKey(),
                        snippet(e.getValue().getText()),
                        scoreOf(e.getValue())))
                .toList();

        saveRecord(question, answer);

        return new ChatAnswer(answer, references, System.currentTimeMillis() - start);
    }

    private String snippet(String text) {
        String flat = text.replaceAll("\\s+", " ").trim();
        return flat.length() <= 80 ? flat : flat.substring(0, 80) + "...";
    }

    private void saveRecord(String question, String answer) {
        ChatRecord record = new ChatRecord();
        record.setQuestion(question);
        record.setAnswer(answer);
        record.setCreateTime(LocalDateTime.now());
        chatRecordService.save(record);
    }
    private double scoreOf(Document d) {
        return d.getScore() == null ? 0.0 : d.getScore();
    }

}

