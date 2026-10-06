package com.example.textclassification.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(
            MethodArgumentNotValidException ex) {

        return Map.of(
                "error",
                "Invalid request",
                "message",
                "Text must not be blank"
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Map<String, String> handleAIException(
            IllegalStateException ex) {

        logger.error("AI classification error", ex);

        return Map.of(
                "error",
                "AI classification failed",
                "message",
                "Unable to classify the text using the AI service"
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleGeneralException(
            Exception ex) {

        logger.error("Unexpected application error", ex);

        return Map.of(
                "error",
                "Internal server error",
                "message",
                "An unexpected error occurred"
        );
    }
}