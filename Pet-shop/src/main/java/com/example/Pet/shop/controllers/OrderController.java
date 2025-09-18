package com.example.Pet.shop.controllers;

import com.example.Pet.shop.models.AppUser;
import com.example.Pet.shop.models.CartItem;
import com.example.Pet.shop.models.Order;
import com.example.Pet.shop.models.OrderItem;
import com.example.Pet.shop.repo.OrderRepository;
import com.example.Pet.shop.repo.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.apache.catalina.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class OrderController {
    private final OrderRepository orderRepository;

    UserRepository userRepository;

    public OrderController(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }
    @PostMapping("/checkout")
    public String processCheckout(HttpSession session, Principal principal,
                                  Model model,
                                  @RequestParam(value = "useBonus", required = false) String useBonus) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            model.addAttribute("error", "Кошик порожній");
            return "cart";
        }

        AppUser user = userRepository.findByUsername(principal.getName()).orElseThrow();
        double total = cart.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuality())
                .sum();

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());

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

        int currentBonus = user.getBonusPoints();
        boolean useBonusFlag = useBonus != null;

        if (useBonusFlag && currentBonus > 0) {
            // списуємо бонуси до суми total, максимум currentBonus
            double bonusValue = Math.min(total, currentBonus);
            total -= bonusValue;
            user.setBonusPoints(currentBonus - (int) bonusValue);
        }

        // нараховуємо нові бонуси
        int newBonus = (int) (total * 0.05); // 5% від оплаченої суми
        user.setBonusPoints(user.getBonusPoints() + newBonus);

        order.setTotal(total);
        orderRepository.save(order);
        userRepository.save(user);

        session.removeAttribute("cart");
        model.addAttribute("message", "Замовлення оформлено. Бали оновлено.");

        return "order-configuration";
    }
    /* @PostMapping("/checkout")
    public String processCheckout(HttpSession session, Principal principal, Model model) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            model.addAttribute("error", "Кошик порожній");
            return "cart";
        }
        double total = cart.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuality())
                .sum();
        AppUser user = userRepository.findByUsername(principal.getName()).orElseThrow();

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());

        List<OrderItem> orderItems = new ArrayList<>();
        double tot=0;

        for (CartItem cartItem : cart) {
            OrderItem item = new OrderItem();
            item.setProduct(cartItem.getProduct());
            item.setQuality(cartItem.getQuality());
            item.setPrice(cartItem.getProduct().getPrice());
            item.setOrder(order);
            orderItems.add(item);

            tot+=cartItem.getProduct().getPrice()*cartItem.getQuality();
        }
        order.setItems(orderItems);
        order.setTotal(tot);

        orderRepository.save(order);
        session.removeAttribute("cart");
        model.addAttribute("message", "Замовлення оформлено. Бали нараховані");
        return "order-configuration";
    } */
    @GetMapping("/orders")
    public String userOrders(Principal principal ,Model model) {
        AppUser user = userRepository.findByUsername(principal.getName()).orElseThrow();
        List<Order> orders = orderRepository.findByUser(user);
        model.addAttribute("orders", orders);
        model.addAttribute("title", "Історія покупок");
        return "order-history";
    }

}
