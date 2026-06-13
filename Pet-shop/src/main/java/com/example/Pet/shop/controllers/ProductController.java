package com.example.Pet.shop.controllers;

import com.example.Pet.shop.models.Category;
import com.example.Pet.shop.models.Product;
import com.example.Pet.shop.repo.CategoryRepository;
import com.example.Pet.shop.repo.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ProductController {
    @Autowired
    private CategoryRepository repoCategory;
    @Autowired
    private ProductRepository repoProduct;

    public static String UPLOAD_DIRECTORY = "D:/Практика2025/Pet shop/Pet-shop/src/main/resources/static/images";

    // Відображення каталогу товарів з підтримкою пошуку, фільтрації та сортування
    @GetMapping("/shop")
    public String shopPage(
            @RequestParam(value = "category", required = false) Long categoryId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sort", required = false, defaultValue = "featured") String sort,
            Model model) {

        List<Category> categories = repoCategory.findAll();
        model.addAttribute("categories", categories);

        // Визначення поточної вибраної категорії
        Category currentCategory = null;
        if (categoryId != null) {
            currentCategory = repoCategory.findById(categoryId).orElse(null);
        }
        model.addAttribute("currentCategory", currentCategory);
        List<Product> products = repoProduct.findAll();

        // Фильтр по категории
        if (categoryId != null) {
            products = products.stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList());
        }

        // Пошук товарів за назвою
        if (keyword != null && !keyword.trim().isEmpty()) {
            // Фільтрація товарів за вибраною категорією
            products = products.stream()
                    .filter(p -> p.getName().toLowerCase().contains(keyword.toLowerCase().trim()))
                    .collect(Collectors.toList());
        }

        // Сортування товарів за обраним критерієм
        if (sort != null) {
            switch (sort) {
                case "price-asc":
                    products.sort(Comparator.comparing(Product::getPrice));
                    break;
                case "price-desc":
                    products.sort(Comparator.comparing(Product::getPrice).reversed());
                    break;
                case "name-asc":
                    products.sort(Comparator.comparing(Product::getName));
                    break;
            }
        }

        model.addAttribute("products", products);
        model.addAttribute("title", "Магазин");

        return "shop";
    }

    @GetMapping("/shop/product")
    public String productList(Model model) {
        List<Product> products;
        products = repoProduct.findAll();
        model.addAttribute("products", products);
        return "allproducts";
    }
    @GetMapping("/shop/{id}")
    public String productCategoryList(@PathVariable(value = "id") Long id, Model model) {
        Optional<Category> category = repoCategory.findById(id);
        List<Product> products = repoProduct.findByCategoryId(id);
        model.addAttribute("category", category.get().getName());
        model.addAttribute("products", products);
        return "category-products";
    }
    @GetMapping("/shop/product/{id}")
    public String productDetail(@PathVariable(value = "id") Long id, Model model) {
        Optional<Product> optionalProduct = repoProduct.findById(id);
        if (optionalProduct.isEmpty()) {
            return "redirect:/shop";
        }
        model.addAttribute("product", optionalProduct.get());
        return "product-detail";
    }

    @GetMapping("/shop/product/add")
    public String addProduct(Model model) {
        model.addAttribute("categories", repoCategory.findAll());
        model.addAttribute("product", new Product());
        return "add-product";
    }
    @PostMapping("/shop/product/add")
    public String saveProduct(@RequestParam String name,
                              @RequestParam("image") MultipartFile mainImageFile,
                              @RequestParam(value = "images", required = false) List<MultipartFile> additionalImageFiles,
                              @RequestParam String short_description,
                              @RequestParam String full_description,
                              @RequestParam double price,
                              @RequestParam Long categoryId,
                              @RequestParam(required = false) String sizes) throws IOException {

        Category category = repoCategory.findById(categoryId).orElseThrow();
        Product product = new Product(name, price, "", short_description, full_description, category);

        String mainImageName = saveImage(mainImageFile);
        if (mainImageName != null) {
            product.setImage(mainImageName);
        }

        List<String> additionalImageNames = new ArrayList<>();
        for (MultipartFile file : additionalImageFiles) {
            String imageName = saveImage(file);
            if (imageName != null) {
                additionalImageNames.add(imageName);
            }
        }
        product.setImages(additionalImageNames);
        List<String> sizeList = convertSizesStringToList(sizes);
        product.setAvailableSizes(sizeList);
        product.generateSmartInfoIfEmpty();
        repoProduct.save(product);
        return "redirect:/shop";
    }

    @GetMapping("/shop/product/{id}/edit")
    public String editProduct(@PathVariable(value = "id") Long id, Model model) {
        Optional<Product> optionalProduct = repoProduct.findById(id);
        if (optionalProduct.isEmpty()) {
            return "redirect:/shop";
        }
        model.addAttribute("product", optionalProduct.get());
        model.addAttribute("categories", repoCategory.findAll());
        return "edit-product";
    }

    @PostMapping("/shop/product/{id}/edit")
    public String updateProduct(@PathVariable("id") Long id,
                                @ModelAttribute("product") Product updatedProduct, // Привязываем данные из формы к объекту
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                @RequestParam(value = "categoryId", required = false) Long categoryId) throws IOException {

        Product product = repoProduct.findById(id).orElseThrow();

        product.setName(updatedProduct.getName());
        product.setPrice(updatedProduct.getPrice());
        product.setQuantity(updatedProduct.getQuantity());

        if (categoryId != null) {
            Category category = repoCategory.findById(categoryId).orElse(null);
            product.setCategory(category);
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            String mainImageName = saveImage(imageFile);
            product.setImage(mainImageName);
        }

        repoProduct.save(product);

        return "redirect:/inventory";
    }

    @PostMapping("/shop/product/{id}/delete")
    public String deleteProduct(@PathVariable(value = "id") Long id) {
        Product product = repoProduct.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        repoProduct.delete(product);
        return "redirect:/shop";
    }

    private String saveImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }
        String originalFilename = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFilename;
        Path fileNameAndPath = Paths.get(UPLOAD_DIRECTORY, uniqueFileName);
        Files.write(fileNameAndPath, file.getBytes());
        return uniqueFileName;
    }
    private List<String> convertSizesStringToList(String sizes) {
        if (sizes == null || sizes.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(sizes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}