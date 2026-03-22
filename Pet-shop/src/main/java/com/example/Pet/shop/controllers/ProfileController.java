package com.example.Pet.shop.controllers;

import com.example.Pet.shop.models.AppUser;
import com.example.Pet.shop.models.Order;
import com.example.Pet.shop.models.OrderItem;
import com.example.Pet.shop.models.Product;
import com.example.Pet.shop.repo.OrderRepository;
import com.example.Pet.shop.repo.ProductRepository;
import com.example.Pet.shop.repo.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ProfileController {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public ProfileController(ProductRepository productRepository, UserRepository userRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
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
    @GetMapping("/profile/analytics")
    public String analyticsPage(Model model, Principal principal) {

        Optional<AppUser> user = userRepository.findByUsername(principal.getName());
        model.addAttribute("user", user);

        List<Order> allOrders = orderRepository.findAll();
        List<Product> allProducts = productRepository.findAll();

        // Середній чек
        double totalRevenue = allOrders.stream().mapToDouble(Order::getTotal).sum();
        double averageCheck = allOrders.isEmpty() ? 0 : totalRevenue / allOrders.size();
        model.addAttribute("averageCheck", Math.round(averageCheck * 100.0) / 100.0);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalOrders", allOrders.size());

        // Топ товарів, що найбільше продаються
        Map<Product, Integer> productSales = new HashMap<>();
        for (Order o : allOrders) {
            for (OrderItem item : o.getItems()) {
                productSales.put(item.getProduct(),
                        productSales.getOrDefault(item.getProduct(), 0) + item.getQuality());
            }
        }

        List<Map.Entry<Product, Integer>> topProducts = productSales.entrySet().stream()
                .sorted(Map.Entry.<Product, Integer>comparingByValue().reversed())
                .limit(5) // Берем Топ-5
                .collect(Collectors.toList());
        model.addAttribute("topProducts", topProducts);

        // Динаміка продажів за місяцями
        Map<String, Double> salesByMonth = new TreeMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (Order o : allOrders) {
            if (o.getOrderDate() != null) {
                String month = o.getOrderDate().format(formatter);
                salesByMonth.put(month, salesByMonth.getOrDefault(month, 0.0) + o.getTotal());
            }
        }
        model.addAttribute("salesByMonth", salesByMonth);

        // Прогноз попиту та розумні попередження
        // Вважаємо продажі за останні 30 днів
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        Map<Product, Integer> lastMonthSales = new HashMap<>();
        for (Order o : allOrders) {
            if (o.getOrderDate() != null && o.getOrderDate().isAfter(thirtyDaysAgo)) {
                for (OrderItem item : o.getItems()) {
                    lastMonthSales.put(item.getProduct(),
                            lastMonthSales.getOrDefault(item.getProduct(), 0) + item.getQuality());
                }
            }
        }

        //якщо на складі менше товару, ніж ми продали за минулий місяць
        List<Map<String, Object>> forecastWarnings = new ArrayList<>();
        for (Product p : allProducts) {
            int soldLastMonth = lastMonthSales.getOrDefault(p, 0);
            if (soldLastMonth > 0 && p.getQuantity() <= soldLastMonth) {
                Map<String, Object> warning = new HashMap<>();
                warning.put("productName", p.getName());
                warning.put("stock", p.getQuantity());
                warning.put("monthlyDemand", soldLastMonth);
                forecastWarnings.add(warning);
            }
        }
        model.addAttribute("forecastWarnings", forecastWarnings);

        // Отримати останні 5 замовлень для столу
        List<Order> recentOrders = allOrders.stream()
                .sorted(Comparator.comparing(Order::getId).reversed())
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("recentOrders", recentOrders);
        return "analytics";
    }
}