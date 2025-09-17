package com.recipeai.recipebackend.controller;

import com.recipeai.recipebackend.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController

@RequestMapping("/api/recipes")
public class Homecontroller {

    @Autowired
    private RecipeService
    recipeService;
    @GetMapping
    public ResponseEntity<String> defaultRecipes() {
        return ResponseEntity.ok("Recipe API is working.");
    }


    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }

    // Basic suggest dishes (normal mode)
    @PostMapping("/suggest-dishes")
    public String suggestDishes(@RequestBody Map<String, String> request) {
        String ingredients = request.get("ingredients");
        String feature = request.get("feature");
        return recipeService.generateDishIdeas(ingredients);
    }

    // Suggest dishes with filter (for feature page)
    @PostMapping("/suggest-dishes-filtered")
    public String suggestDishesWithFilter(@RequestBody Map<String, String> request) {
        String ingredients = request.get("ingredients");
        String filter = request.get("filter");
        return recipeService.generateDishIdeasWithFilter(ingredients, filter);
    }

    @PostMapping("/get-recipe")
    public String getRecipe(@RequestBody Map<String, String> request) {
        String dish = request.get("dish");
        return recipeService.generateRecipeForDish(dish);
    }

    // Remix recipe (button below recipe)
    @PostMapping("/remix-recipe")
    public String remixRecipe(@RequestBody Map<String, String> request) {
        String dish = request.get("dish");
        return recipeService.remixDishIdea(dish);
    }
}




