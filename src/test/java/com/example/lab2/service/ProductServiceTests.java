package com.example.lab2.service;

import com.example.lab2.dto.ProductDto;
import com.example.lab2.model.Brand;
import com.example.lab2.model.Category;
import com.example.lab2.model.Product;
import com.example.lab2.repository.BrandRepository;
import com.example.lab2.repository.CategoryRepository;
import com.example.lab2.repository.ProductRepository;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.CsvFileSource;
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
public class ProductServiceTests {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BrandRepository brandRepository;
    private ProductService productService;

    static Stream<ProductDto> createInvalidProductArgs() {
        return Stream.of(
                ProductDto.builder()
                        .name(" ")
                        .description(null)
                        .price(0.99)
                        .quantity(-1)
                        .categoryId(-1L)
                        .brandId(null)
                        .build(),
                ProductDto.builder()
                        .name("")
                        .description("")
                        .price(0.0)
                        .quantity(0)
                        .categoryId(null)
                        .brandId(0L)
                        .build(),
                ProductDto.builder()
                        .name(null)
                        .description("Description")
                        .price(-1.0)
                        .quantity(1)
                        .categoryId(1L)
                        .brandId(1L)
                        .build()
        );
    }

    @BeforeEach
    void setUp() {
        final var validatorFactory = Validation.buildDefaultValidatorFactory();
        final var validator = validatorFactory.getValidator();
        productService = new ProductService(productRepository, validator, categoryRepository, brandRepository);
    }

    @Test
    void whenGetAllProducts_thenReturnsListOfProducts() {
        System.out.println("Testing getAllProducts method");
        final List<Product> products = List.of(
                Product.builder()
                        .name("Product 1")
                        .description("Description 1")
                        .price(0.99)
                        .quantity(1)
                        .category(new Category(1L, "Category 1", null))
                        .brand(new Brand(1L, "Brand 1"))
                        .build()
        );
        given(productRepository.findAll()).willReturn(products);
        assertThat(productService.getAllProducts()).containsExactly(
                ProductDto.builder()
                        .name("Product 1")
                        .description("Description 1")
                        .price(0.99)
                        .quantity(1)
                        .categoryId(1L)
                        .brandId(1L)
                        .build()
        );
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 2L, 3L, Long.MAX_VALUE})
    void givenProductId_whenGetProductById_thenReturnsProduct(Long id) {
        System.out.println("Testing getProductById method with id = " + id);
        final var productName = "Product " + id;
        given(productRepository.findById(id)).willReturn(Optional.of(
                Product.builder()
                        .name("Product 1")
                        .description("Description 1")
                        .price(0.99)
                        .quantity(1)
                        .category(new Category(1L, "Category 1", null))
                        .brand(new Brand(1L, "Brand 1"))
                        .build())
        );
        final var productDto = productService.getProductById(id);
        assertThat(productDto).isEqualTo(
                ProductDto.builder()
                        .name("Product 1")
                        .description("Description 1")
                        .price(0.99)
                        .quantity(1)
                        .categoryId(1L)
                        .brandId(1L)
                        .build()
        );
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 0L, -1L, Long.MIN_VALUE})
    void givenProductWithGivenIdDoesNotExist_whenGetProductById_thenThrowsException(Long id) {
        System.out.println("Testing getProductById method with non-existent id = " + id);
        given(productRepository.findById(id)).willReturn(Optional.empty());
        assertThatThrownBy(() -> productService.getProductById(id)).isInstanceOf(NoSuchElementException.class);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/productArgs.csv", numLinesToSkip = 1)
    void givenProductData_whenCreateProduct_thenSavesNewProduct(ArgumentsAccessor arguments) {
        final var productDto = ProductDto.builder()
                .name(arguments.getString(0))
                .description(arguments.getString(1))
                .price(arguments.getDouble(2))
                .quantity(arguments.getInteger(3))
                .categoryId(arguments.getLong(4))
                .brandId(arguments.getLong(5))
                .build();
        System.out.println("Testing createProduct method with " + productDto);
        given(categoryRepository.findById(productDto.categoryId())).willReturn(Optional.of(new Category(productDto.categoryId(), null, null)));
        given(brandRepository.findById(productDto.brandId())).willReturn(Optional.of(new Brand(productDto.brandId(), null)));
        productService.createProduct(productDto);
        verify(productRepository).save(ArgumentMatchers.eq(
                Product.builder()
                        .name(productDto.name())
                        .description(productDto.description())
                        .price(productDto.price())
                        .quantity(productDto.quantity())
                        .category(new Category(productDto.categoryId(), null, null))
                        .brand(new Brand(productDto.brandId(), null))
                        .build()
        ));
    }

    @ParameterizedTest
    @MethodSource("createInvalidProductArgs")
    void givenInvalidProductData_whenCreateProduct_thenThrowsException(ProductDto productDto) {
        System.out.println("Testing createProduct method with " + productDto);
        assertThatThrownBy(() -> productService.createProduct(productDto)).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void givenProductNameIsTaken_whenCreateProduct_thenThrowsException() {
        System.out.println("Testing createProduct method with taken product name");
        final var productName = "Product 1";
        given(productRepository.existsByName(productName)).willReturn(true);
        assertThatThrownBy(() -> productService.createProduct(
                ProductDto.builder()
                        .name(productName)
                        .description("Description 1")
                        .price(0.99)
                        .quantity(1)
                        .categoryId(1L)
                        .brandId(1L)
                        .build()
        )).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void givenProductData_whenUpdateProduct_thenSavesUpdatedProduct() {
        System.out.println("Testing updateProduct method");
        final var id = 1L;
        final var productName = "Product 1";
        final var category = new Category(1L, "Category 1", null);
        final var brand = new Brand(1L, "Brand 1");
        final var product = Product.builder()
                .name(productName)
                .description("Description 1")
                .price(0.99)
                .quantity(1)
                .category(category)
                .brand(brand)
                .build();
        given(categoryRepository.findById(product.getCategory().getId())).willReturn(Optional.of(category));
        given(brandRepository.findById(product.getCategory().getId())).willReturn(Optional.of(brand));
        given(productRepository.findById(id)).willReturn(Optional.of(product));
        final var newProductName = "Product 2";
        productService.updateProduct(id, ProductDto.builder()
                .name(productName)
                .description("Description 1")
                .price(0.99)
                .quantity(1)
                .categoryId(1L)
                .brandId(1L)
                .build());
        product.setName(newProductName);
        verify(productRepository).save(ArgumentMatchers.eq(product));
    }

    @ParameterizedTest
    @MethodSource("createInvalidProductArgs")
    void givenInvalidProductData_whenUpdateProduct_thenThrowsException(ProductDto productDto) {
        System.out.println("Testing updateProduct method with " + productDto);
        final var id = 1L;
        assertThatThrownBy(() -> productService.updateProduct(id, productDto)).isInstanceOf(ConstraintViolationException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 0L, -1L, Long.MIN_VALUE})
    void givenProductWithGivenIdDoesNotExist_whenUpdateProduct_thenThrowsException(Long id) {
        System.out.println("Testing updateProduct method with non-existent id = " + id);
        given(productRepository.findById(id)).willReturn(Optional.empty());
        assertThatThrownBy(() -> productService.updateProduct(id, ProductDto.builder()
                .name("Product 1")
                .description("Description 1")
                .price(0.99)
                .quantity(1)
                .categoryId(1L)
                .brandId(1L)
                .build())
        ).isInstanceOf(NoSuchElementException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 2L, 3L, Long.MAX_VALUE})
    void givenProductId_whenDeleteProduct_thenDeletesProduct(Long id) {
        System.out.println("Testing deleteProduct method with id = " + id);
        productService.deleteProduct(id);
        verify(productRepository).deleteById(id);
    }

}
