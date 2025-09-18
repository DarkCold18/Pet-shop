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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        if(!repoProduct.existsById(id)) {
            return "redirect:/shop";
        }
        Optional<Product> product = repoProduct.findById(id);
        ArrayList<Product> prod = new ArrayList<>();
        product.ifPresent(prod::add);
        model.addAttribute("product", prod);
        return "product-detail";
    }
    @GetMapping("/shop/product/add")
    public String addProduct(Model model) {
        model.addAttribute("categories", repoCategory.findAll());
        return "add-product";
    }
    @PostMapping("/shop/product/add")
    public String saveProduct(@RequestParam String name, @RequestParam("image") MultipartFile file,
                               @RequestParam String short_description, @RequestParam String full_description,
                               @RequestParam double price, @RequestParam Long categoryId , Model model) throws IOException {
        StringBuilder fileName=new StringBuilder();
        Path fileNameAndpath = Paths.get(UPLOAD_DIRECTORY, file.getOriginalFilename());
        fileName.append(file.getOriginalFilename());
        Files.write(fileNameAndpath, file.getBytes());
        String image=fileName.toString();
        Category category = repoCategory.findById(categoryId).orElseThrow();
        Product product=new Product(name,price, image, short_description, full_description, category);
        repoProduct.save(product);
        return "redirect:/shop/product";
    }
    @GetMapping("/shop/product/{id}/edit")
    public String editProduct(@PathVariable(value = "id") Long id, Model model) {
        if(!repoProduct.existsById(id)) {
            return "redirect:/shop/product";
        }
        Optional<Product> product = repoProduct.findById(id);
        ArrayList<Product> prod = new ArrayList<>();
        product.ifPresent(prod::add);
        model.addAttribute("product", prod);
        List<Category> categories = repoCategory.findAll();
        model.addAttribute("categories", categories);
        return "edit-product";
    }
    @PostMapping("/shop/product/{id}/edit")
    public String updateProduct(@PathVariable(value = "id") Long id, @RequestParam String name, @RequestParam("image") MultipartFile file,
                              @RequestParam String short_description, @RequestParam String full_description,
                              @RequestParam double price, @RequestParam Long categoryId , Model model) throws IOException {
        StringBuilder fileName = new StringBuilder();
        Path fileNameAndpath = Paths.get(UPLOAD_DIRECTORY, file.getOriginalFilename());
        fileName.append(file.getOriginalFilename());
        Files.write(fileNameAndpath, file.getBytes());
        String image = fileName.toString();
        Category category = repoCategory.findById(categoryId).orElseThrow();
        Product product = repoProduct.findById(id).orElseThrow();
        product.setName(name);
        product.setCategory(category);
        product.setPrice(price);
        product.setImage(image);
        product.setShort_description(short_description);
        product.setFull_description(full_description);
        repoProduct.save(product);
        return "redirect:/shop/product";
    }
    @GetMapping("/shop/product/{id}/delete")
    public String deleteProduct(@PathVariable(value = "id") Long id, Model model) {
        Product product = repoProduct.findById(id).orElseThrow();
        repoProduct.delete(product);
        return "redirect:/shop/product";
    }
}
