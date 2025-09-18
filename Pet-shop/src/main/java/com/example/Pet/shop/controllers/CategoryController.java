package com.example.Pet.shop.controllers;

import com.example.Pet.shop.models.Category;
import com.example.Pet.shop.repo.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.awt.font.MultipleMaster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Controller
public class CategoryController {
    @Autowired
    private CategoryRepository repoCategory;
    public static String UPLOAD_DIRECTORY = "D:/Практика2025/Pet shop/Pet-shop/src/main/resources/static/images";


    @GetMapping("/shop")
    public String categoryList(Model model) {
        Iterable<Category> categories;
        categories = repoCategory.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("title", "Зоомагазин");
        return "shop";
    }
    @GetMapping("/shop/add-category")
    public String addCategory(Model model) {
        model.addAttribute("title", "Додати категорію");
        return "add-category";
    }

    @PostMapping("/shop/add-category")
    public String saveCategory(@RequestParam String name,@RequestParam("image") MultipartFile file,
                               @RequestParam String description,  Model model) throws IOException {
        StringBuilder fileName=new StringBuilder();
        Path fileNameAndpath = Paths.get(UPLOAD_DIRECTORY, file.getOriginalFilename());
        fileName.append(file.getOriginalFilename());
        Files.write(fileNameAndpath, file.getBytes());
        String image=fileName.toString();
        Category category=new Category(name,image,description);
        repoCategory.save(category);
        model.addAttribute("title", "Зоомагазин");
        return "redirect:/shop";
    }
    @GetMapping("/shop/category/{id}")
    public String viewCategory(@PathVariable(value = "id") Long id, Model model) {
        if(!repoCategory.existsById(id)) {
            model.addAttribute("title", "Зоомагазин");
            return "redirect:/shop";
        }
        Optional<Category> category = repoCategory.findById(id);
        ArrayList<Category> categ = new ArrayList<>();
        category.ifPresent(categ::add);
        model.addAttribute("category", categ);
        return "category";
    }
    @GetMapping("/shop/category/{id}/delete")
    public String deleteCategory(@PathVariable(value = "id") Long id, Model model) {
        Category category = repoCategory.findById(id).orElseThrow();
        repoCategory.delete(category);
        model.addAttribute("title", "Зоомагазин");
        return "redirect:/shop";
    }
    @GetMapping("/shop/category/{id}/edit")
    public String editCategory(@PathVariable(value = "id") Long id, Model model) {
        if(!repoCategory.existsById(id)) {
            return "redirect:/shop";
        }
        Optional<Category> category = repoCategory.findById(id);
        ArrayList<Category> cat = new ArrayList<>();
        category.ifPresent(cat::add);
        model.addAttribute("category", cat);
        model.addAttribute("title", "Редагування категорії");
        return "edit-category";
    }
    @PostMapping("/shop/update-category/{id}")
    public String saveEditCategory(@PathVariable(value = "id") Long id,@RequestParam String name,@RequestParam("image") MultipartFile file,
                               @RequestParam String description,  Model model) throws IOException {
        Category category = repoCategory.findById(id).orElseThrow();
        category.setName(name);
        if (file != null && !file.isEmpty()) {
            StringBuilder fileName = new StringBuilder();
            Path fileNameAndpath = Paths.get(UPLOAD_DIRECTORY, file.getOriginalFilename());
            fileName.append(file.getOriginalFilename());
            Files.write(fileNameAndpath, file.getBytes());
            String image = fileName.toString();
            category.setImage(image);
        }
        category.setId(id);
        category.setDescription(description);
        repoCategory.save(category);
        model.addAttribute("title", "Зоомагазин");
        return "redirect:/shop";
    }
}
