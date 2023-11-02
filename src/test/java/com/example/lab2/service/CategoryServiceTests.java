package com.example.lab2.service;

import com.example.lab2.dto.CategoryDto;
import com.example.lab2.model.Category;
import com.example.lab2.repository.CategoryRepository;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTests {

    @Mock
    private CategoryRepository categoryRepository;
    private CategoryService categoryService;

    static Stream<CategoryDto> createInvalidCategoryArgs() {
        return Stream.of(
                CategoryDto.builder()
                        .name(" ")
                        .parentCategoryId(-1L)
                        .build(),
                CategoryDto.builder()
                        .name("")
                        .parentCategoryId(null)
                        .build(),
                CategoryDto.builder()
                        .name(null)
                        .parentCategoryId(1L)
                        .build(),
                CategoryDto.builder()
                        .name("Category")
                        .parentCategoryId(0L)
                        .build()
        );
    }

    @BeforeEach
    void setUp() {
        final var validatorFactory = Validation.buildDefaultValidatorFactory();
        final var validator = validatorFactory.getValidator();
        categoryService = new CategoryService(categoryRepository, validator);
    }

    @Test
    void whenGetAllCategories_thenReturnsListOfCategories() {
        System.out.println("Testing getAllCategories method");
        final var rootCategory = new Category(1L, "Category 1", null);
        final List<Category> categories = List.of(
                rootCategory,
                new Category(2L, "Category 2", rootCategory)
        );
        given(categoryRepository.findAll()).willReturn(categories);
        assertThat(categoryService.getAllCategories()).containsExactly(
                new CategoryDto(1L, "Category 1", null),
                new CategoryDto(2L, "Category 2", 1L)
        );
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 2L, 3L, Long.MAX_VALUE})
    void givenCategoryId_whenGetCategoryById_thenReturnsCategory(Long id) {
        System.out.println("Testing getCategoryById method with id = " + id);
        final var categoryName = "Category " + id;
        given(categoryRepository.findById(id)).willReturn(Optional.of(new Category(id, categoryName, null)));
        final var categoryDto = categoryService.getCategoryById(id);
        assertThat(categoryDto).isEqualTo(new CategoryDto(id, categoryName, null));
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 0L, -1L, Long.MIN_VALUE})
    void givenCategoryWithGivenIdDoesNotExist_whenGetCategoryById_thenThrowsException(Long id) {
        System.out.println("Testing getCategoryById method with non-existent id = " + id);
        given(categoryRepository.findById(id)).willReturn(Optional.empty());
        assertThatThrownBy(() -> categoryService.getCategoryById(id)).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void givenCategoryDto_whenCreateCategory_thenSavesNewCategory() {
        System.out.println("Testing createCategory method");
        final var categoryDto = CategoryDto.builder()
                .name("Category")
                .parentCategoryId(null)
                .build();
        categoryService.createCategory(categoryDto);
        verify(categoryRepository).save(ArgumentMatchers.eq(new Category(null, categoryDto.name(), null)));
    }

    @ParameterizedTest
    @MethodSource("createInvalidCategoryArgs")
    void givenInvalidCategoryData_whenCreateCategory_thenThrowsException(CategoryDto categoryDto) {
        System.out.println("Testing createCategory method with " + categoryDto);
        assertThatThrownBy(() -> categoryService.createCategory(categoryDto)).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void givenCategoryNameIsTaken_whenCreateCategory_thenThrowsException() {
        System.out.println("Testing createCategory method with taken category name");
        final var categoryName = "Category 1";
        given(categoryRepository.existsByName(categoryName)).willReturn(true);
        assertThatThrownBy(() -> categoryService.createCategory(new CategoryDto(null, categoryName, null))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void givenCategoryData_whenUpdateCategory_thenSavesUpdatedCategory() {
        System.out.println("Testing updateCategory method");
        final var id = 1L;
        final var categoryName = "Category 1";
        given(categoryRepository.findById(id)).willReturn(Optional.of(new Category(id, categoryName, null)));
        final var newCategoryName = "Category 2";
        categoryService.updateCategory(id, new CategoryDto(id, newCategoryName, null));
        verify(categoryRepository).save(ArgumentMatchers.eq(new Category(id, newCategoryName, null)));
    }

    @ParameterizedTest
    @MethodSource("createInvalidCategoryArgs")
    void givenInvalidCategoryData_whenUpdateCategory_thenThrowsException(CategoryDto categoryDto) {
        System.out.println("Testing updateCategory method with " + categoryDto);
        final var id = 1L;
        assertThatThrownBy(() -> categoryService.updateCategory(id, categoryDto)).isInstanceOf(ConstraintViolationException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 0L, -1L, Long.MIN_VALUE})
    void givenCategoryWithGivenIdDoesNotExist_whenUpdateCategory_thenThrowsException(Long id) {
        System.out.println("Testing updateCategory method with non-existent id = " + id);
        given(categoryRepository.findById(id)).willReturn(Optional.empty());
        assertThatThrownBy(() -> categoryService.updateCategory(id, new CategoryDto(id, "Category", null))).isInstanceOf(NoSuchElementException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 2L, 3L, Long.MAX_VALUE})
    void givenCategoryId_whenDeleteCategory_thenDeletesCategory(Long id) {
        System.out.println("Testing deleteCategory method with id = " + id);
        categoryService.deleteCategory(id);
        verify(categoryRepository).deleteById(id);
    }

}
