package com.example.Pet.shop.models;

import jakarta.persistence.*;

@Entity
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // DOG, CAT, BIRD, RODENT
    private String name;
    private String breed;
    private Double age;
    private Double weight;
    private String healthFocus; // ACTIVE, DIGESTION, SKIN, TEETH

    // Зв'язок: У одного користувача може бути багато тваринок
    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    public Pet() {}

    public Pet(String type, String name, String breed, Double age, Double weight, String healthFocus, AppUser user) {
        this.type = type;
        this.name = name;
        this.breed = breed;
        this.age = age;
        this.weight = weight;
        this.healthFocus = healthFocus;
        this.user = user;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }
    public Double getAge() { return age; }
    public void setAge(Double age) { this.age = age; }
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    public String getHealthFocus() { return healthFocus; }
    public void setHealthFocus(String healthFocus) { this.healthFocus = healthFocus; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
}