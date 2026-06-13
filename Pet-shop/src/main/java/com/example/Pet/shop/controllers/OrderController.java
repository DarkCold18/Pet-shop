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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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


    @RequestMapping(value = "/checkout", method = {RequestMethod.GET, RequestMethod.POST})
    public String showCheckoutPage(HttpSession session, Principal principal, Model model,
                                   @RequestParam(value = "useBonus", required = false) String useBonus) {
        if (principal == null) {
            return "redirect:/login";
        }

        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            return "redirect:/cart/";
        }

        AppUser user = userRepository.findByUsername(principal.getName()).orElseThrow();

        // Якщо користувач поставив галочку "Використати бонуси" в кошику
        if (useBonus != null) {
            session.setAttribute("useBonus", true);
        }

        // Передаємо товари в HTML
        model.addAttribute("cartItems", cart);

        // Рахуємо суму
        double total = cart.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuality())
                .sum();

        // Якщо бонуси активовані, одразу показуємо суму зі знижкою праворуч
        if (Boolean.TRUE.equals(session.getAttribute("useBonus"))) {
            double bonusValue = Math.min(total, user.getBonusPoints());
            total -= bonusValue;
        }

        model.addAttribute("cartTotal", total);

        return "order-configuration";
    }


    @PostMapping("/orders/place")
    public String placeOrder(HttpSession session, Principal principal,
                             @RequestParam String fullName,
                             @RequestParam String phone,
                             @RequestParam String address,
                             @RequestParam String paymentMethod,
                             Model model) {

        if (principal == null) {
            return "redirect:/login";
        }

        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            return "redirect:/cart/";
        }

        AppUser user = userRepository.findByUsername(principal.getName()).orElseThrow();

        double total = cart.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuality())
                .sum();

        double totalAfterDiscount = total;
        int currentBonus = user.getBonusPoints();
        int bonusUsed = 0;

        if (Boolean.TRUE.equals(session.getAttribute("useBonus")) && currentBonus > 0) {
            double bonusValue = Math.min(totalAfterDiscount, currentBonus);
            totalAfterDiscount -= bonusValue;
            bonusUsed = (int) bonusValue;
            session.removeAttribute("useBonus");
        }

        int newBonusEarned = (int) (totalAfterDiscount * 0.05);
        user.setBonusPoints(currentBonus - bonusUsed + newBonusEarned);

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

        cart.clear();

        // Формуємо повідомлення
        String msg = "Замовлення успішно оформлено!";
        if (bonusUsed > 0) {
            msg += " Списано " + bonusUsed + " бонусів.";
        }
        msg += " Нараховано " + newBonusEarned + " нових бонусів.";

        // Передаємо повідомлення напряму в поточний шаблон
        model.addAttribute("message", msg);
        model.addAttribute("title", "Замовлення оформлено");
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