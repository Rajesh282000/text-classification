package com.example.textclassification.dto;

public record ClassificationResponse(
        String category,
        double confidence
) {}
