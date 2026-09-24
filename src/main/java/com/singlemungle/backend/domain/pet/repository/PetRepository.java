package com.singlemungle.backend.domain.pet.repository;

import java.util.List;
import java.util.Optional;

import com.singlemungle.backend.domain.pet.entity.Pet;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findByUserId(Long userId);

    Optional<Pet> findByIdAndUserId(Long id, Long userId);
}
