package com.example.Pet.shop.controllers;

import com.example.Pet.shop.models.AppUser;
import com.example.Pet.shop.models.CartItem;
import com.example.Pet.shop.models.Product;
import com.example.Pet.shop.repo.ProductRepository;
import com.example.Pet.shop.repo.UserRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cart")
@SessionAttributes("cart")
public class CartController {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    public CartController(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @ModelAttribute("cart")
    public List<CartItem> Cart() {
        return new ArrayList<>();
    }
    @GetMapping("/")
    public String viewCart(@ModelAttribute("cart") List<CartItem> cart, Model model, Principal principal) {
        double total=cart.stream()
                .mapToDouble(item ->item.getProduct().getPrice() * item.getQuality())
                .sum();
        if (principal != null) {
            AppUser appUser = userRepository.findByUsername(principal.getName()).orElseThrow();
            model.addAttribute("user", appUser);
            int bonusToUse = (int) Math.min(appUser.getBonusPoints(), total);
            model.addAttribute("bonusToUse", bonusToUse);
            model.addAttribute("discountedTotal", total - bonusToUse);
        } else {
            model.addAttribute("discountedTotal", total);
            model.addAttribute("bonusToUse", 0);
        }
        model.addAttribute("total", total);
        return "cart";
    }
    @PostMapping("/add/{id}")
    public String addToCart(@PathVariable Long id, @RequestParam(defaultValue = "1") int quantity,
                            @ModelAttribute("cart") List<CartItem> cart) {
        Product product = productRepository.findById(id).orElseThrow();
        for (CartItem item : cart) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.setQuality(item.getQuality() + quantity);

            }
        }
        cart.add(new CartItem(product, quantity));
        return "redirect:/cart/";
    }
    @PostMapping("/remove/{id}")
    public String removeFromCart(@PathVariable Long id, @ModelAttribute("cart") List<CartItem> cart) {
        cart.removeIf(item -> item.getProduct().getId().equals(id));
        return "redirect:/cart/";
    }
    @PostMapping("/clear")
    public String clearCart(@ModelAttribute("cart") List<CartItem> cart) {
        cart.clear();
        return "redirect:/cart/";
    }
}
