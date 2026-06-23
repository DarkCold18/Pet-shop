package com.example.Pet.shop.models;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    private String password;private String fullName;
    private String phone;
    private String address;
    private String email;
    private int bonusPoints=0;


    public Long getId() {
        return id;
    }
    public AppUser() {

    }

    public AppUser(String username, String password, List<String> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    public AppUser(String username, String password) {
        this.username = username;
        this.password = password;

    }
    public String getUsername() {
        return username;
    }

    public int getBonusPoints() {
        return bonusPoints;
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public void setBonusPoints(int bonusPoints) {
        this.bonusPoints = bonusPoints;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "app_user_roles",
            joinColumns = @JoinColumn(name = "app_user_id"))
    @Column(name = "role")
    private List<String> roles = new ArrayList<>();
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pet> pets = new ArrayList<>();

    public List<Pet> getPets() {
        return pets;
    }

    public void setPets(List<Pet> pets) {
        this.pets = pets;
    }
    //  Автоматичний підрахунок загальної кількості замовлень
    public int getTotalOrdersCount() {
        return orders != null ? orders.size() : 0;
    }

    //  Автоматичний підрахунок усієї витраченої суми
    public double getTotalSpentAmount() {
        if (orders == null || orders.isEmpty()) {
            return 0.0;
        }
        return orders.stream()
                .mapToDouble(Order::getTotal)
                .sum();
    }
}
