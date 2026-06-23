package com.example.Pet.shop.controllers;

import com.example.Pet.shop.models.AppUser;
import com.example.Pet.shop.models.CartItem;
import com.example.Pet.shop.models.Order;
import com.example.Pet.shop.models.OrderItem;
import com.example.Pet.shop.repo.OrderRepository;
import com.example.Pet.shop.repo.UserRepository;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PaymentController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;
    private final String stripeSecretKey = "";


    @PostMapping("/create-payment-intent")
    @ResponseBody
    public ResponseEntity<Map<String, String>> createPaymentIntent(@RequestBody Map<String, Object> data) {
        Stripe.apiKey = stripeSecretKey;
        try {
            // Отримуємо суму з фронтенду
            double amount = Double.parseDouble(data.get("amount").toString());
            long amountInCents = Math.round(amount * 100);

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("uah")
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            // Віддаємо clientSecret на фронтенд
            Map<String, String> responseData = new HashMap<>();
            responseData.put("clientSecret", intent.getClientSecret());

            return ResponseEntity.ok(responseData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }


    // Збереження даних
    @PostMapping("/checkout/process")
    public String processPayment(@RequestParam String name,
                                 @RequestParam String phone,
                                 @RequestParam String email,
                                 @RequestParam String address,
                                 @RequestParam String paymentMethod,
                                 @RequestParam Double amount,
                                 @RequestParam(required = false) String stripePaymentId,
                                 java.security.Principal principal,
                                 jakarta.servlet.http.HttpSession session) {

        if (principal != null) {
            AppUser user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                //  Оновлюємо дані клієнта
                user.setFullName(name);
                user.setPhone(phone);
                user.setEmail(email);
                user.setAddress(address);

                //  Створюємо замовлення
                Order order = new Order();
                order.setTotal(amount);
                order.setOrderDate(java.time.LocalDateTime.now());
                order.setUser(user);

                if (stripePaymentId != null && !stripePaymentId.isEmpty()) {
                    order.setStripePaymentId(stripePaymentId);
                }
                List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

                if (cart != null && !cart.isEmpty()) {
                    List<OrderItem> orderItems = new ArrayList<>();

                    for (CartItem cartItem : cart) {
                        OrderItem orderItem = new OrderItem();
                        orderItem.setProduct(cartItem.getProduct());
                        orderItem.setQuality(cartItem.getQuality());
                        orderItem.setOrder(order);

                        orderItems.add(orderItem);
                    }
                    // Кладемо всі сформовані товари в замовлення
                    order.setItems(orderItems);
                }

                //  Зберігаємо все в базу
                userRepository.save(user);
                orderRepository.save(order);
                session.removeAttribute("cart");
            }
        }
        return "redirect:/payment/success";
    }

    @GetMapping("/payment/success")
    public String paymentSuccess() {
        return "success-page";
    }

    @GetMapping("/payment/cancel")
    public String paymentCancel() {
        return "redirect:/cart";
    }
}