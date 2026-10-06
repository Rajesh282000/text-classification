package com.example.textclassification.service;

import com.example.textclassification.dto.ClassificationRequest;
import com.example.textclassification.dto.ClassificationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class ClassificationService {

    private static final Logger logger =
            LoggerFactory.getLogger(ClassificationService.class);

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public ClassificationService(
            ObjectMapper objectMapper,
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model:gemini-2.5-flash}") String model) {

        this.objectMapper = objectMapper;
        this.model = model;

        this.restClient = RestClient.builder()
                .baseUrl(GEMINI_URL)
                .defaultHeader(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .defaultHeader(
                        "x-goog-api-key",
                        apiKey
                )
                .build();
    }

    public ClassificationResponse classify(
            ClassificationRequest request) {

        long startTime = System.currentTimeMillis();

        String prompt = """
                Classify the following text into exactly one of these categories:
                Complaint, Query, Feedback, Other.

                Return ONLY valid JSON in this exact format:

                {
                  "category": "Complaint",
                  "confidence": 0.95
                }

                Rules:
                - category must be exactly one of:
                  Complaint, Query, Feedback, Other
                - confidence must be a number between 0 and 1
                - do not return markdown
                - do not return any additional text

                Text:
                %s
                """.formatted(request.text());

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt)
                                )
                        )
                ),
                "generationConfig", Map.of(
                        "responseMimeType", "application/json"
                )
        );

        JsonNode response;

        try {

            response = restClient.post()
                    .uri("models/" + model + ":generateContent")
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

        } finally {

            long endTime = System.currentTimeMillis();

            logger.info(
                    "Gemini API response time: {} ms",
                    endTime - startTime
            );
        }

        if (response == null) {
            throw new IllegalStateException(
                    "Empty response from Gemini"
            );
        }

        String outputText = extractOutputText(response);

        try {

            JsonNode result =
                    objectMapper.readTree(outputText);

            String category =
                    result.path("category").asText();

            double confidence =
                    result.path("confidence").asDouble();

            if (!isValidCategory(category)) {

                throw new IllegalStateException(
                        "Gemini returned invalid category"
                );
            }

            if (confidence < 0 || confidence > 1) {

                throw new IllegalStateException(
                        "Gemini returned invalid confidence"
                );
            }

            return new ClassificationResponse(
                    category,
                    confidence
            );

        } catch (Exception e) {

            logger.error(
                    "Failed to parse Gemini classification response",
                    e
            );

            throw new IllegalStateException(
                    "Invalid response received from AI model"
            );
        }
    }

    private String extractOutputText(JsonNode response) {

        JsonNode candidates =
                response.path("candidates");

        if (!candidates.isArray()
                || candidates.isEmpty()) {

            throw new IllegalStateException(
                    "Gemini returned no candidates"
            );
        }

        JsonNode parts =
                candidates.get(0)
                        .path("content")
                        .path("parts");

        if (!parts.isArray()
                || parts.isEmpty()) {

            throw new IllegalStateException(
                    "Gemini returned no content"
            );
        }

        String text =
                parts.get(0)
                        .path("text")
                        .asText();

        if (text == null || text.isBlank()) {

            throw new IllegalStateException(
                    "Gemini returned empty text"
            );
        }

        return text;
    }

    private boolean isValidCategory(String category) {

        return category.equals("Complaint")
                || category.equals("Query")
                || category.equals("Feedback")
                || category.equals("Other");
    }
}