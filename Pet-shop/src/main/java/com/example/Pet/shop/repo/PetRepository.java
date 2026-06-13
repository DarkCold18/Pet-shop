package com.example.Pet.shop.repo;

import com.example.Pet.shop.models.AppUser;
import com.example.Pet.shop.models.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {
    // Метод для пошуку всіх тваринок конкретного користувача
    List<Pet> findByUser(AppUser user);
}