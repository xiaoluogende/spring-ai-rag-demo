package com.simpleai.demo.controller;

import com.simpleai.demo.entity.ChatRecord;
import com.simpleai.demo.service.ChatRecordService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatClient chatClient;
    private final ChatRecordService chatRecordService;

    // 构造注入
    public ChatController(ChatClient.Builder builder, ChatRecordService chatRecordService) {
        this.chatClient = builder.build();
        this.chatRecordService = chatRecordService;
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String question) {
        // 调用DeepSeek
        String answer = chatClient.prompt(question).call().content();

        // 组装记录，入库
        ChatRecord record = new ChatRecord();
        record.setQuestion(question);
        record.setAnswer(answer);
        record.setCreateTime(LocalDateTime.now());
        chatRecordService.save(record);

        return answer;
    }
}
