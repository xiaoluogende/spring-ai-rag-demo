package com.simpleai.demo.service;

import com.simpleai.demo.dto.ChatAnswer;

public interface RagService {

    /** 知识库检索增强问答 */
    ChatAnswer ask(String question);
}
