package com.simpleai.demo.controller;

import com.simpleai.demo.common.Result;
import com.simpleai.demo.dto.ChatAnswer;
import com.simpleai.demo.service.ChatService;
import com.simpleai.demo.service.RagService;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    private final EmbeddingModel embeddingModel;
    private final RagService ragService;

    public ChatController(ChatService chatService, EmbeddingModel embeddingModel, RagService ragService) {
        this.chatService = chatService;
        this.embeddingModel = embeddingModel;
        this.ragService = ragService;

    }

    @GetMapping("/ask")
    public Result<ChatAnswer> ask(@RequestParam String question) {
        return Result.ok(chatService.chat(question));
    }

    @GetMapping("/debug-embed")
    public Result<String> debugEmbed(@RequestParam String text) {
        float[] vector = embeddingModel.embed(text);
        return Result.ok("维度=" + vector.length
                + "，前 5 个值=" + Arrays.toString(Arrays.copyOf(vector, 5)));
    }
    @GetMapping("/rag")
    public Result<ChatAnswer> rag(@RequestParam String question) {
        return Result.ok(ragService.ask(question));
    }

}
