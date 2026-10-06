package com.example.textclassification.dto;

import jakarta.validation.constraints.NotBlank;

public record ClassificationRequest(
        @NotBlank(message = "text must not be blank")
        String text
) {}
