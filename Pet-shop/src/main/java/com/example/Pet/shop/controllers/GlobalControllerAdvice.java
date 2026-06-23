package com.example.Pet.shop.controllers;

import com.example.Pet.shop.models.CartItem;
import com.example.Pet.shop.models.Category;
import com.example.Pet.shop.repo.CategoryRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final CategoryRepository categoryRepository;

    public GlobalControllerAdvice(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Автоматичне додавання списку категорій до всіх сторінок застосунку
    @ModelAttribute("categs")
    public List<Category> categs() {
        return categoryRepository.findAll();
    }

    // НОВЕ: Автоматичне додавання кількості товарів у кошику для всіх сторінок
    @ModelAttribute("cartItemCount")
    public int getCartItemCount(HttpSession session) {
        // Дістаємо кошик із сесії
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {
            return 0; // Якщо кошик порожній, повертаємо 0
        }

        // Рахуємо загальну кількість усіх товарів (штук)
        return cart.stream().mapToInt(CartItem::getQuality).sum();
    }
}