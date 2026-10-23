package com.simpleai.demo.service.impl;

import com.simpleai.demo.service.DocumentService;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 由 {@link com.simpleai.demo.config.RagConfiguration} 装配，
 * 不标 @Service，以便受 app.rag.enabled 控制。
 */
public class DocumentServiceImpl implements DocumentService {

    /** 百炼 embedding 单次最多 10 条文本 */
    private static final int BATCH_SIZE = 10;

    private final VectorStore vectorStore;
    private final TokenTextSplitter splitter;

    public DocumentServiceImpl(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.splitter = TokenTextSplitter.builder()
                .withChunkSize(800)
                .withMinChunkSizeChars(200)
                .withMinChunkLengthToEmbed(10)
                .withMaxNumChunks(10000)
                .withKeepSeparator(true)
                .build();
    }

    @Override
    public int ingest(String fileName, String content) {
        Document source = new Document(content, Map.of("source", fileName));
        List<Document> chunks = splitter.apply(List.of(source));

        for (int i = 0; i < chunks.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, chunks.size());
            List<Document> batch = new ArrayList<>(chunks.subList(i, end));
            vectorStore.add(batch);
        }
        return chunks.size();
    }
}
