package com.example.Pet.shop.controllers;

import com.example.Pet.shop.models.Product;
import com.example.Pet.shop.repo.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class PageController {
    private ProductRepository productRepository;

    public PageController(ProductRepository productRepository){
        this.productRepository=productRepository;
    }
    @GetMapping("/")
    public String home(Model model) {
        List<Product> products= productRepository.findByRecommendedTrue();
        model.addAttribute("products",products);
        model.addAttribute("title","Головна");
        return "home";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "Про нас");
        return "about";
    }
    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("title", "Контакти");
        return "contact";
    }
}
