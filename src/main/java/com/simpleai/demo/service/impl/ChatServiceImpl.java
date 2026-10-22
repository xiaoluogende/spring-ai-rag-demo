package com.simpleai.demo.service.impl;

import com.simpleai.demo.dto.ChatAnswer;
import com.simpleai.demo.entity.ChatRecord;
import com.simpleai.demo.service.ChatRecordService;
import com.simpleai.demo.service.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {



    private final ChatClient chatClient;
    private final ChatRecordService chatRecordService;
    private final Resource systemPrompt;

    public ChatServiceImpl(ChatClient.Builder builder,
                           ChatRecordService chatRecordService,
                           @Value("classpath:prompts/system-chat.st") Resource systemPrompt) {
        this.chatClient = builder.build();
        this.chatRecordService = chatRecordService;
        this.systemPrompt = systemPrompt;
    }

    @Override
    public ChatAnswer chat(String question) {
        long start = System.currentTimeMillis();

        String answer = chatClient.prompt()
                .system(systemPrompt)
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
