package com.simpleai.demo.controller;

import com.simpleai.demo.common.Result;
import com.simpleai.demo.service.DocumentService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/doc")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Result.fail(400, "文件为空");
        }
        String name = file.getOriginalFilename();
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        int chunks = documentService.ingest(name, content);
        return Result.ok("已入库 " + chunks + " 个片段：" + name);
    }
}
