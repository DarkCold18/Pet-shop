package com.example.Pet.shop.controllers;

import com.example.Pet.shop.models.*;
import com.example.Pet.shop.repo.*;
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
    private final CategoryRepository categoryRepository;
    private final PetRepository petRepository;

    public ProfileController(ProductRepository productRepository, UserRepository userRepository, OrderRepository orderRepository, CategoryRepository categoryRepository, PetRepository petRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.categoryRepository = categoryRepository;
        this.petRepository = petRepository;
    }

    @GetMapping("/inventory")
    public String inventoryPage(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        // Формування списку товарів із низьким залишком на складі
        List<Product> lowStockProducts = products.stream()
                .filter(p -> p.getQuantity() <= 5)
                .collect(Collectors.toList());
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("title", "Inventory Management");
        return "inventory";
    }
    // оновлення кількості (тільки адмін)
    @PostMapping("/inventory/update/{id}")
    public String updateQuantity(@PathVariable Long id,
                                 @RequestParam("quantity") int quantity) {
        // Пошук товару за ідентифікатором
        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            // Встановлення нової кількості товару
            product.setQuantity(quantity);
            productRepository.save(product);
        }
        return "redirect:/inventory";
    }
    @PostMapping("/shop/product/adjust/{id}")
    public String adjustProductQuantity(@PathVariable Long id,
                                        @RequestParam("amount") int amount) {
        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            // Збільшуємо поточну кількість на (+5 або +20)
            product.setQuantity(product.getQuantity() + amount);
            productRepository.save(product);
        }
        return "redirect:/inventory";
    }
    @PostMapping("/shop/product/{id}")
    public String buyProduct(@PathVariable Long id,
                             @RequestParam(defaultValue = "1") int count) {
        Product product = productRepository.findById(id).orElse(null);
        if (product != null && product.getQuantity() >= count) {
            // Зменшення залишку товару після покупки
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
        //  продажі за останні 30 днів
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

        // Отримати останні 5 замовлень
        List<Order> recentOrders = allOrders.stream()
                .sorted(Comparator.comparing(Order::getId).reversed())
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("recentOrders", recentOrders);
        return "analytics";
    }
    @GetMapping("/profile/pet/add")
    public String showPetQuiz(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }
        model.addAttribute("title", "Підбір раціону");
        model.addAttribute("products", productRepository.findAll());
        return "pet-quiz";
    }

    @PostMapping("/profile/pet/add")
    public String savePetProfile(Principal principal,
                                 @RequestParam String type,
                                 @RequestParam String name,
                                 @RequestParam(required = false) String breed,
                                 @RequestParam Double age,
                                 @RequestParam Double weight,
                                 @RequestParam String healthFocus) {

        if (principal == null) {
            return "redirect:/login";
        }

        // Знаходимо юзера
        AppUser user = userRepository.findByUsername(principal.getName()).orElseThrow();

        // Створюємо тваринку
        Pet pet = new Pet(type, name, breed, age, weight, healthFocus, user);
        petRepository.save(pet);

        return "redirect:/profile";
    }
    //  Сторінка редагування
    @GetMapping("/profile/pet/edit/{id}")
    public String editPetForm(@PathVariable Long id, Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        AppUser user = userRepository.findByUsername(principal.getName()).orElseThrow();
        Pet pet = petRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Невірний ID тваринки: " + id));

        // Перевірка по логіну
        if (!pet.getUser().getUsername().equals(user.getUsername())) {
            return "redirect:/profile";
        }

        model.addAttribute("pet", pet);
        model.addAttribute("title", "Редагування профілю");
        return "pet-edit";
    }

    //  Збереження відредагованих даних
    @PostMapping("/profile/pet/edit/{id}")
    public String updatePetProfile(@PathVariable Long id,
                                   @RequestParam String type,
                                   @RequestParam String name,
                                   @RequestParam(required = false) String breed,
                                   @RequestParam Double age,
                                   @RequestParam Double weight,
                                   @RequestParam String healthFocus,
                                   Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        AppUser user = userRepository.findByUsername(principal.getName()).orElseThrow();
        Pet pet = petRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Невірний ID тваринки: " + id));

        // Оновлюємо тільки якщо тваринка належить користувачу
        if (pet.getUser().getUsername().equals(user.getUsername())) {
            pet.setType(type);
            pet.setName(name);
            pet.setBreed(breed);
            pet.setAge(age);
            pet.setWeight(weight);
            pet.setHealthFocus(healthFocus);

            petRepository.save(pet);
        }

        return "redirect:/profile";
    }

    //  Видалення тваринки
    @PostMapping("/profile/pet/delete/{id}")
    public String deletePet(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        AppUser user = userRepository.findByUsername(principal.getName()).orElseThrow();
        Pet pet = petRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Невірний ID тваринки: " + id));
        if (pet.getUser().getUsername().equals(user.getUsername())) {
            petRepository.delete(pet);
        }

        return "redirect:/profile";
    }
    @GetMapping("/customers")
    public String showCustomersBase(Model model) {
        // Отримуємо всіх користувачів з бази даних
        List<AppUser> customers = userRepository.findAll();

        model.addAttribute("customers", customers);
        model.addAttribute("title", "База клієнтів");

        return "customers";
    }
    @GetMapping("/crm-dashboard")
    public String showCrmDashboard(Model model) {
        List<Order> allOrders = orderRepository.findAll();
        long totalCustomers = userRepository.count();

        model.addAttribute("orders", allOrders);
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalOrders", allOrders.size());
        model.addAttribute("title", "Головна панель CRM");

        return "crm-dashboard";
    }
    @PostMapping("/crm-dashboard/order/status")
    public String updateOrderStatus(@RequestParam Long orderId, @RequestParam String newStatus, @RequestParam(defaultValue = "false") boolean urgent) {
        // Знаходимо замовлення за його ID
        Order order = orderRepository.findById(orderId).orElse(null);

        if (order != null) {
            order.setStatus(newStatus);
            order.setUrgent(urgent);
            orderRepository.save(order); // Зберігаємо в базу
        }

        return "redirect:/crm-dashboard";
    }

}