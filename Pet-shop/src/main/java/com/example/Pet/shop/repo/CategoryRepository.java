package com.example.Pet.shop.repo;

import com.example.Pet.shop.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Long id(Long id);
}
