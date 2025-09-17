package com.recipeai.recipebackend.controller;
import org.springframework.http.MediaType;
import com.recipeai.recipebackend.service.VisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/vision")
@CrossOrigin(origins = "http://localhost:3000")
public class VisionController {

    @Autowired
    private VisionService visionService;
    @PostMapping("/ingredients")
    public ResponseEntity<List<String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            List<String> ingredients = visionService.extractIngredients(file);
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON) // ← force JSON
                    .body(ingredients);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(500)
                    .contentType(MediaType.APPLICATION_JSON) // optional: still return JSON
                    .body(List.of("Error analyzing image: " + e.getMessage()));
        }
    }
    }

