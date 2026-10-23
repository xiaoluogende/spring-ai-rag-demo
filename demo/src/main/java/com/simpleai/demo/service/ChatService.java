package com.simpleai.demo.service;

import com.simpleai.demo.dto.ChatAnswer;

/**
 * 问答业务接口。
 * 后续 RAG 知识库问答在此扩展，Controller 不感知具体实现。
 */
public interface ChatService {

    /** 普通大模型对话 */
    ChatAnswer chat(String question);
}

