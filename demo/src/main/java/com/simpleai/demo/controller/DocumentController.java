package com.simpleai.demo.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.simpleai.demo.common.Result;
import com.simpleai.demo.service.DocumentService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/doc")
public class DocumentController {

    /** RAG 关闭时该 Bean 不存在，用 ObjectProvider 避免启动失败 */
    private final ObjectProvider<DocumentService> documentServiceProvider;

    public DocumentController(ObjectProvider<DocumentService> documentServiceProvider) {
        this.documentServiceProvider = documentServiceProvider;
    }

    /** 文档入库，需要 doc:upload 权限（guest 账号没有，可用于演示 403） */
    @SaCheckPermission("doc:upload")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        DocumentService documentService = documentServiceProvider.getIfAvailable();
        if (documentService == null) {
            return Result.fail(503, "知识库问答模块未启用（app.rag.enabled=false）");
        }
        if (file.isEmpty()) {
            return Result.fail(400, "文件为空");
        }
        String name = file.getOriginalFilename();
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        int chunks = documentService.ingest(name, content);
        return Result.ok("已入库 " + chunks + " 个片段：" + name);
    }
}
