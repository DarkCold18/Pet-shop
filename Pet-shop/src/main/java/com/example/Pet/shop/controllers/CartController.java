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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    // Ініціалізація нового кошика для користувача
    @ModelAttribute("cart")
    public List<CartItem> Cart() {
        return new ArrayList<>();
    }

    // Відображення сторінки кошика
    @GetMapping({"", "/"})
    public String viewCart(@ModelAttribute("cart") List<CartItem> cart, Model model, Principal principal) {
        // Розрахунок загальної вартості товарів у кошику
        double total = 0;
        for (CartItem item : cart) {
            total += item.getProduct().getPrice() * item.getQuality();
        }

        if (principal != null) {
            AppUser appUser = userRepository.findByUsername(principal.getName()).orElseThrow();
            model.addAttribute("user", appUser);
            // Розрахунок кількості бонусів для використання
            int bonusToUse = (int) Math.min(appUser.getBonusPoints(), total);
            model.addAttribute("bonusToUse", bonusToUse);
            model.addAttribute("discountedTotal", total - bonusToUse);
        } else {
            model.addAttribute("discountedTotal", total);
            model.addAttribute("bonusToUse", 0);
        }
        model.addAttribute("cartTotal", total);
        model.addAttribute("cartItems", cart);
        return "cart";
    }

    @PostMapping("/add/{id}")
    public String addToCart(@PathVariable Long id,
                            @RequestParam(defaultValue = "1") int quantity,
                            @ModelAttribute("cart") List<CartItem> cart,
                            RedirectAttributes redirectAttributes) {

        Product product = productRepository.findById(id).orElse(null);
        // Перевірка існування товару
        if (product == null) {
            redirectAttributes.addFlashAttribute("error_message", "Товар не знайдено.");
            return "redirect:/";
        }
        // Перевірка наявності необхідної кількості товару на складі
        if (product.getQuantity() < quantity) {
            redirectAttributes.addFlashAttribute("error_message",
                    "На складі недостатньо товару. Доступно: " + product.getQuantity() + " шт.");
            return "redirect:/";
        }
        // Оновлення залишку товару на складі
        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);
        boolean found = false;
        for (CartItem item : cart) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.setQuality(item.getQuality() + quantity);
                found = true;
                break;
            }
        }
        if (!found) {
            cart.add(new CartItem(product, quantity));
        }

        return "redirect:/cart/";
    }

    @PostMapping("/remove/{id}")
    public String removeFromCart(@PathVariable Long id, @ModelAttribute("cart") List<CartItem> cart) {
        CartItem itemToRemove = null;
        for (CartItem item : cart) {
            if (item.getProduct().getId().equals(id)) {
                itemToRemove = item;
                break;
            }
        }
        if (itemToRemove != null) {
            Product product = itemToRemove.getProduct();
            int quantityToReturn = itemToRemove.getQuality();
            // Повернення товару на склад після видалення з кошика
            product.setQuantity(product.getQuantity() + quantityToReturn);
            productRepository.save(product);
            cart.remove(itemToRemove);
        }
        return "redirect:/cart/";
    }
    @PostMapping("/clear")
    public String clearCart(@ModelAttribute("cart") List<CartItem> cart) {
        // Повернення всіх товарів із кошика на склад
        for (CartItem item : cart) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuality());
            productRepository.save(product);
        }
        cart.clear();
        return "redirect:/cart/";
    }
    @PostMapping("/update")
    @ResponseBody
    public Map<String, Object> updateCartItemQuantity(@RequestParam Long id,
                                                      @RequestParam int delta,
                                                      @ModelAttribute("cart") List<CartItem> cart) {
        Map<String, Object> response = new HashMap<>();

        for (CartItem item : cart) {
            // Пошук товару для оновлення кількості
            if (item.getProduct().getId().equals(id)) {
                Product product = item.getProduct();
                int currentCartQuantity = item.getQuality();
                int newCartQuantity = currentCartQuantity + delta;
                int stockAfterChange = product.getQuantity() - delta;
                if (newCartQuantity <= 0) {
                    response.put("error", "Кількість не може бути менше одиниці.");
                    response.put("newQuantity", currentCartQuantity);
                    return response;
                }
                // Перевірка наявності товару на складі перед збільшенням кількості
                if (delta > 0 && stockAfterChange < 0) {
                    response.put("error", "На складі недостатньо товару.");
                    response.put("newQuantity", currentCartQuantity);
                    return response;
                }
                product.setQuantity(stockAfterChange);
                productRepository.save(product);
                item.setQuality(newCartQuantity);
                response.put("success", true);
                response.put("itemId", id);
                response.put("newQuantity", newCartQuantity);
                response.put("itemTotal", newCartQuantity * product.getPrice());
                // Перерахунок загальної вартості кошика
                double total = cart.stream()
                        .mapToDouble(i -> i.getProduct().getPrice() * i.getQuality())
                        .sum();
                response.put("cartTotal", total);

                return response;
            }
        }

        response.put("error", "Товар не знайдено в кошику.");
        return response;
    }
}