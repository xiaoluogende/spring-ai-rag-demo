package com.simpleai.demo.controller;

import com.simpleai.demo.common.Result;
import com.simpleai.demo.dto.ChatAnswer;
import com.simpleai.demo.service.ChatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/ask")
    public Result<ChatAnswer> ask(@RequestParam String question) {
        return Result.ok(chatService.chat(question));
    }
}
