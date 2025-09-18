package com.example.Pet.shop.models;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String image;
    private String description;

    //1 to all
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products;

    public Category() {

    }
    public Category(String name) {
        this.name = name;
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

    public Category(String name, String image, String description) {
        this.name = name;
        this.image = image;
        this.description = description;
    }

    public Category(String name, String image, String description, List<Product> products) {
        this.name = name;
        this.image = image;
        this.description = description;
        this.products = products;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List <Product> getProducts() {return products;}
    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
