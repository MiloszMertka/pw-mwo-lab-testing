package com.example.lab2.repository;

import com.example.lab2.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    boolean existsByName(String name);

}
