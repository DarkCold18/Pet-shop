package com.example.Pet.shop.models;

import jakarta.persistence.*;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String kilk;
    private double price;
    private String korm;
    private String toys;
    private String image;
    private String short_description;
    private String full_description;
    private boolean recommended;
    private int quantity;

    //all to 1
    @ManyToOne
    @JoinColumn(name ="category_id")
    private Category category;

    public Product() {

    }

    public Product(String name, String kilk, double price, Category category) {
        this.name = name;
        this.kilk = kilk;
        this.price = price;
        this.category = category;
    }

    public Product(String name, String kilk, double price, String korm, String toys, String image, String short_description, String full_description, Category category, boolean recommended) {
        this.name = name;
        this.kilk = kilk;
        this.price = price;
        this.korm = korm;
        this.toys = toys;
        this.image = image;
        this.short_description = short_description;
        this.full_description = full_description;
        this.category = category;
        this.recommended = recommended;
    }
    public Product(String name, double price, String image, String short_description, String full_description, boolean recommended, Category category) {
        this.name = name;
        this.recommended=recommended;
        this.price = price;
        this.image = image;
        this.short_description = short_description;
        this.full_description = full_description;
        this.category = category;
    }

    public Product(String name, double price, String image, String short_description, String full_description,Category category) {
        this.name = name;

        this.price = price;
        this.image = image;
        this.short_description = short_description;
        this.full_description = full_description;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public boolean isRecommended() {
        return recommended;
    }

    public void setRecommended(boolean recommended) {
        this.recommended = recommended;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKilk() {
        return kilk;
    }

    public void setKilk(String kilk) {
        this.kilk = kilk;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String  getKorm() {
        return korm;
    }

    public void setKorm(String korm) {
        this.korm = korm;
    }

    public String getToys() {
        return toys;
    }

    public void setToys(String toys) {
        this.toys = toys;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getShort_description() {
        return short_description;
    }

    public void setShort_description(String short_description) {
        this.short_description = short_description;
    }

    public String getFull_description() {
        return full_description;
    }

    public void setFull_description(String full_description) {
        this.full_description = full_description;
    }
    public Category getCategory() {
        return category;
    }
    public void setCategory(Category category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}