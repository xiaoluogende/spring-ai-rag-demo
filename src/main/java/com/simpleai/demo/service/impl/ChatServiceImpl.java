package com.simpleai.demo.service.impl;

import com.simpleai.demo.dto.ChatAnswer;
import com.simpleai.demo.entity.ChatRecord;
import com.simpleai.demo.service.ChatRecordService;
import com.simpleai.demo.service.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    private static final String SYSTEM_PROMPT = """
            你是一位严谨的技术助手。回答必须满足以下要求：
            1. 全程使用简体中文。
            2. 输出必须是合法的 Markdown，禁止使用 HTML 标签。
            3. 结构：第一句直接给出结论，然后分点展开说明。
            4. 合理使用二级标题(##)、有序列表、行内代码 `code` 和代码块。
            5. 不要输出"好的""当然可以"这类开场白，也不要输出总结性客套话。
            6. 如果信息不足以确定答案，必须明确说明"不确定"，禁止编造。
            """;

    private final ChatClient chatClient;
    private final ChatRecordService chatRecordService;

    public ChatServiceImpl(ChatClient.Builder builder, ChatRecordService chatRecordService) {
        this.chatClient = builder.build();
        this.chatRecordService = chatRecordService;
    }

    @Override
    public ChatAnswer chat(String question) {
        long start = System.currentTimeMillis();

        String answer = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(question)
                .call()
                .content();

        long costMs = System.currentTimeMillis() - start;

        saveRecord(question, answer);

        return new ChatAnswer(answer, List.of(), costMs);
    }

    private void saveRecord(String question, String answer) {
        ChatRecord record = new ChatRecord();
        record.setQuestion(question);
        record.setAnswer(answer);
        record.setCreateTime(LocalDateTime.now());
        chatRecordService.save(record);
    }
}
