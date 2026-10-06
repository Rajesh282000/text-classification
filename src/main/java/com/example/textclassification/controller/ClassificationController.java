package com.example.textclassification.controller;

import com.example.textclassification.dto.ClassificationRequest;
import com.example.textclassification.dto.ClassificationResponse;
import com.example.textclassification.service.ClassificationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ClassificationController {
    private final ClassificationService classificationService;

    public ClassificationController(ClassificationService classificationService) {
        this.classificationService = classificationService;
    }

    @PostMapping("/classify")
    public ClassificationResponse classify(
            @Valid @RequestBody ClassificationRequest request) {
        return classificationService.classify(request);
    }
}
