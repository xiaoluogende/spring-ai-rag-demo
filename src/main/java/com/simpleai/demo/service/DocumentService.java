package com.simpleai.demo.service;

public interface DocumentService {

    /**
     * 把文本切分、向量化后写入知识库
     * @return 切出的片段数量
     */
    int ingest(String fileName, String content);
}
