package com.example.Pet.shop.controllers;

import com.example.Pet.shop.models.AppUser;
import com.example.Pet.shop.models.Pet;
import com.example.Pet.shop.models.Product;
import com.example.Pet.shop.repo.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal; // Обов'язковий імпорт
import java.util.List;

@Controller
public class PageController {

    // Додав слово final для безпеки (рекомендовано в Spring)
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PetRepository petRepository;

    // ВИПРАВЛЕНО: Тепер конструктор приймає всі 3 репозиторії
    public PageController(ProductRepository productRepository,
                          UserRepository userRepository,
                          PetRepository petRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.petRepository = petRepository;
    }

    // ВИПРАВЛЕНО: Додано Principal principal у параметри методу
    @GetMapping("/")
    public String home(Model model, Principal principal) {
        List<Product> products = productRepository.findByRecommendedTrue();
        model.addAttribute("products", products);
        model.addAttribute("title", "Головна");

        if (principal != null) {
            AppUser user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                List<Pet> pets = petRepository.findByUser(user);
                model.addAttribute("pets", pets);
            }
        }

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