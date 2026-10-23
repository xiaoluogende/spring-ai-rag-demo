package com.simpleai.demo.dto;

import java.util.List;

public record ChatAnswer(
        String answer,
        List<Reference> references,
        long costMs
) {
    public record Reference(String docName, String snippet, double score) {
    }
}
