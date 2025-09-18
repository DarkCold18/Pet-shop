package com.example.Pet.shop.controllers;

import com.example.Pet.shop.models.Category;
import com.example.Pet.shop.repo.CategoryRepository;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {
    private CategoryRepository categoryRepository;
    public GlobalControllerAdvice(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    @ModelAttribute("categs")
    public List<Category> categs(){
        return categoryRepository.findAll();
    }
}
