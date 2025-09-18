package com.example.Pet.shop.repo;

import com.example.Pet.shop.models.AppUser;
import com.example.Pet.shop.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(AppUser user);
}
