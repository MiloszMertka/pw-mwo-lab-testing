package com.example.lab2.service;

import com.example.lab2.dto.BrandDto;
import com.example.lab2.model.Brand;
import com.example.lab2.repository.BrandRepository;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;
    private final Validator validator;

    public List<BrandDto> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(this::mapBrandToBrandDto)
                .toList();
    }

    public BrandDto getBrandById(Long id) {
        return brandRepository.findById(id)
                .map(this::mapBrandToBrandDto)
                .orElseThrow();
    }

    public void createBrand(BrandDto brandDto) {
        validateBrandDto(brandDto);
        validateBrandNameIsNotTaken(brandDto.name());
        final var brand = Brand.builder()
                .name(brandDto.name())
                .build();
        brandRepository.save(brand);
    }

    public void updateBrand(Long id, BrandDto brandDto) {
        validateBrandDto(brandDto);
        final var brand = brandRepository.findById(id).orElseThrow();
        brand.setName(brandDto.name());
        brandRepository.save(brand);
    }

    public void deleteBrand(Long id) {
        brandRepository.deleteById(id);
    }

    private void validateBrandDto(BrandDto brandDto) {
        final var violations = validator.validate(brandDto);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private BrandDto mapBrandToBrandDto(Brand brand) {
        return BrandDto.builder()
                .id(brand.getId())
                .name(brand.getName())
                .build();
    }

    private void validateBrandNameIsNotTaken(String name) {
        if (brandRepository.existsByName(name)) {
            throw new IllegalStateException("Brand name is already taken: " + name);
        }
    }

}
