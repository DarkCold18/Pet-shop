package com.example.Pet.shop.controllers;

import com.example.Pet.shop.models.AppUser;
import com.example.Pet.shop.models.Product;
import com.example.Pet.shop.repo.ProductRepository;
import com.example.Pet.shop.repo.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ProfileController {
    private final ProductRepository productRepository;

    public ProfileController(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/inventory")
    public String inventoryPage(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        model.addAttribute("title", "Inventory Management");
        return "inventory";
    }
    // оновлення кількості (тільки адмін)
    @PostMapping("/inventory/update/{id}")
    public String updateQuantity(@PathVariable Long id,
                                 @RequestParam("quantity") int quantity) {
        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            product.setQuantity(quantity);
            productRepository.save(product);
        }
        return "redirect:/inventory";
    }

    @PostMapping("/shop/product/{id}")
    public String buyProduct(@PathVariable Long id,
                             @RequestParam(defaultValue = "1") int count) {
        Product product = productRepository.findById(id).orElse(null);
        if (product != null && product.getQuantity() >= count) {
            product.setQuantity(product.getQuantity() - count);
            productRepository.save(product);
        }
        return "redirect:/shop";
    }
}