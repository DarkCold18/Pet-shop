package com.example.Pet.shop.controllers;

import com.example.Pet.shop.models.Category;
import com.example.Pet.shop.models.Product;
import com.example.Pet.shop.repo.CategoryRepository;
import com.example.Pet.shop.repo.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        repoProduct.save(product);
        return "redirect:/shop/product";
    }

    @GetMapping("/shop/product/{id}/edit")
    public String editProduct(@PathVariable(value = "id") Long id, Model model) {
        Optional<Product> optionalProduct = repoProduct.findById(id);
        if (optionalProduct.isEmpty()) {
            return "redirect:/shop/product";
        }
        model.addAttribute("product", optionalProduct.get());
        model.addAttribute("categories", repoCategory.findAll());
        return "edit-product";
    }

    @PostMapping("/shop/product/{id}/edit")
    public String updateProduct(@PathVariable(value = "id") Long id,
                                @RequestParam String name,
                                @RequestParam("image") MultipartFile mainImageFile,
                                @RequestParam(value = "images", required = false) List<MultipartFile> additionalImageFiles,
                                @RequestParam String short_description,
                                @RequestParam String full_description,
                                @RequestParam double price,
                                @RequestParam Long categoryId,
                                @RequestParam(required = false) String sizes) throws IOException {

        Product product = repoProduct.findById(id).orElseThrow();
        Category category = repoCategory.findById(categoryId).orElseThrow();

        product.setName(name);
        product.setCategory(category);
        product.setPrice(price);
        product.setShort_description(short_description);
        product.setFull_description(full_description);
        if (!mainImageFile.isEmpty()) {
            String mainImageName = saveImage(mainImageFile);
            product.setImage(mainImageName);
        }
        List<String> additionalImageNames = new ArrayList<>();
        for (MultipartFile file : additionalImageFiles) {
            if (!file.isEmpty()) {
                String imageName = saveImage(file);
                additionalImageNames.add(imageName);
            }
        }
        if (!additionalImageNames.isEmpty()) {
            product.setImages(additionalImageNames);
        }
        List<String> sizeList = convertSizesStringToList(sizes);
        product.setAvailableSizes(sizeList);
        repoProduct.save(product);
        return "redirect:/shop/product";
    }

    @PostMapping("/shop/product/{id}/delete")
    public String deleteProduct(@PathVariable(value = "id") Long id) {
        Product product = repoProduct.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        repoProduct.delete(product);
        return "redirect:/shop/product";
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