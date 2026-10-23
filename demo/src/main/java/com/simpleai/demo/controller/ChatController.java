package com.simpleai.demo.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.simpleai.demo.common.Result;
import com.simpleai.demo.dto.ChatAnswer;
import com.simpleai.demo.service.ChatService;
import com.simpleai.demo.service.RagService;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/chat")
public class ChatController {

    /**
     * RAG 相关 Bean 在 app.rag.enabled=false 时不会被创建，
     * 用 ObjectProvider 延迟获取，避免构造器注入失败导致整个应用起不来。
     */
    private static final String RAG_DISABLED = "知识库问答模块未启用（app.rag.enabled=false）";

    private final ChatService chatService;
    private final ObjectProvider<EmbeddingModel> embeddingModelProvider;
    private final ObjectProvider<RagService> ragServiceProvider;

    public ChatController(ChatService chatService,
                          ObjectProvider<EmbeddingModel> embeddingModelProvider,
                          ObjectProvider<RagService> ragServiceProvider) {
        this.chatService = chatService;
        this.embeddingModelProvider = embeddingModelProvider;
        this.ragServiceProvider = ragServiceProvider;
    }

    /** 普通大模型对话，不依赖 RAG 模块 */
    @GetMapping("/ask")
    public Result<ChatAnswer> ask(@RequestParam String question) {
        return Result.ok(chatService.chat(question));
    }

    /** 知识库检索增强问答，需要 rag:ask 权限 */
    @SaCheckPermission("rag:ask")
    @GetMapping("/rag")
    public Result<ChatAnswer> rag(@RequestParam String question) {
        RagService ragService = ragServiceProvider.getIfAvailable();
        if (ragService == null) {
            return Result.fail(503, RAG_DISABLED);
        }
        return Result.ok(ragService.ask(question));
    }

    /** 调试用：验证向量化是否正常 */
    @GetMapping("/debug-embed")
    public Result<String> debugEmbed(@RequestParam String text) {
        EmbeddingModel embeddingModel = embeddingModelProvider.getIfAvailable();
        if (embeddingModel == null) {
            return Result.fail(503, RAG_DISABLED);
        }
        float[] vector = embeddingModel.embed(text);
        return Result.ok("维度=" + vector.length
                + "，前 5 个值=" + Arrays.toString(Arrays.copyOf(vector, 5)));
    }
}
