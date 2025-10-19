package com.example.Pet.shop.controllers;

import com.example.Pet.shop.models.AppUser;
import com.example.Pet.shop.models.CartItem;
import com.example.Pet.shop.models.Order;
import com.example.Pet.shop.models.OrderItem;
import com.example.Pet.shop.repo.OrderRepository;
import com.example.Pet.shop.repo.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderController {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderController(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/checkout")
    public String processCheckout(HttpSession session, Principal principal,
                                  Model model,
                                  @RequestParam(value = "useBonus", required = false) String useBonus) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

        if (principal == null) {
            model.addAttribute("error", "Будь ласка, авторизуйтесь для оформлення замовлення.");
            return "redirect:/login";
        }

        if (cart == null || cart.isEmpty()) {
            model.addAttribute("error", "Кошик порожній");
            return "cart";
        }

        AppUser user = userRepository.findByUsername(principal.getName()).orElseThrow();

        double total = cart.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuality())
                .sum();

        double totalAfterDiscount = total;
        int currentBonus = user.getBonusPoints();
        int bonusUsed = 0;

        boolean useBonusFlag = useBonus != null;

        if (useBonusFlag && currentBonus > 0) {
            double bonusValue = Math.min(totalAfterDiscount, currentBonus);
            totalAfterDiscount -= bonusValue;
            bonusUsed = (int) bonusValue;
        }

        int newBonusEarned = (int) (totalAfterDiscount * 0.05);


        int finalBonusBalance = currentBonus - bonusUsed + newBonusEarned;
        user.setBonusPoints(finalBonusBalance);


        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setTotal(totalAfterDiscount);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart) {
            OrderItem item = new OrderItem();
            item.setProduct(cartItem.getProduct());
            item.setQuality(cartItem.getQuality());
            item.setPrice(cartItem.getProduct().getPrice());
            item.setOrder(order);
            orderItems.add(item);
        }
        order.setItems(orderItems);

        orderRepository.save(order);
        userRepository.save(user);
        session.removeAttribute("cart");

        model.addAttribute("message", "Замовлення успішно оформлено! Бали оновлено.");

        return "order-configuration";
    }

    @GetMapping("/orders")
    public String userOrders(Principal principal ,Model model) {

        if (principal == null) {
            return "redirect:/login";
        }

        AppUser user = userRepository.findByUsername(principal.getName()).orElseThrow();


        List<Order> orders = orderRepository.findByUser(user);


        model.addAttribute("orders", orders);
        model.addAttribute("title", "Історія покупок");

        return "order-history";
    }
}