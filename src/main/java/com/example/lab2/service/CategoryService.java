package com.example.lab2.service;

import com.example.lab2.dto.CategoryDto;
import com.example.lab2.model.Category;
import com.example.lab2.repository.CategoryRepository;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final Validator validator;

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapCategoryToCategoryDto)
                .toList();
    }

    public CategoryDto getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(this::mapCategoryToCategoryDto)
                .orElseThrow();
    }

    public void createCategory(CategoryDto categoryDto) {
        validateCategoryDto(categoryDto);
        validateCategoryNameIsNotTaken(categoryDto.name());
        final var category = Category.builder()
                .name(categoryDto.name())
                .parentCategory(getParentCategoryById(categoryDto.parentCategoryId()))
                .build();
        categoryRepository.save(category);
    }

    public void updateCategory(Long id, CategoryDto categoryDto) {
        validateCategoryDto(categoryDto);
        final var category = categoryRepository.findById(id).orElseThrow();
        category.setName(categoryDto.name());
        category.setParentCategory(getParentCategoryById(categoryDto.parentCategoryId()));
        categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    private void validateCategoryDto(CategoryDto categoryDto) {
        final var violations = validator.validate(categoryDto);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private CategoryDto mapCategoryToCategoryDto(Category category) {
        final var parentCategory = category.getParentCategory();
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .parentCategoryId(parentCategory == null ? null : parentCategory.getId())
                .build();
    }

    private void validateCategoryNameIsNotTaken(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new IllegalStateException("Category name is already taken: " + name);
        }
    }

    private Category getParentCategoryById(Long id) {
        return id == null ? null : categoryRepository.findById(id).orElseThrow();
    }

}
