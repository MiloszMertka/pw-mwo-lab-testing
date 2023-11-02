package com.example.lab2.service;

import com.example.lab2.dto.ProductDto;
import com.example.lab2.model.Brand;
import com.example.lab2.model.Category;
import com.example.lab2.model.Product;
import com.example.lab2.repository.BrandRepository;
import com.example.lab2.repository.CategoryRepository;
import com.example.lab2.repository.ProductRepository;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final Validator validator;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapProductToProductDto)
                .toList();
    }

    public ProductDto getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::mapProductToProductDto)
                .orElseThrow();
    }

    public void createProduct(ProductDto productDto) {
        validateProductDto(productDto);
        validateProductNameIsNotTaken(productDto.name());
        final var product = Product.builder()
                .name(productDto.name())
                .description(productDto.description())
                .price(productDto.price())
                .quantity(productDto.quantity())
                .category(getCategoryById(productDto.categoryId()))
                .brand(getBrandById(productDto.brandId()))
                .build();
        productRepository.save(product);
    }

    public void updateProduct(Long id, ProductDto productDto) {
        validateProductDto(productDto);
        final var product = productRepository.findById(id).orElseThrow();
        product.setName(productDto.name());
        product.setDescription(productDto.description());
        product.setPrice(productDto.price());
        product.setQuantity(productDto.quantity());
        product.setCategory(getCategoryById(productDto.categoryId()));
        product.setBrand(getBrandById(productDto.brandId()));
        productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    private ProductDto mapProductToProductDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .brandId(product.getBrand().getId())
                .categoryId(product.getCategory().getId())
                .build();
    }

    private void validateProductDto(ProductDto productDto) {
        final var violations = validator.validate(productDto);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private void validateProductNameIsNotTaken(String name) {
        if (productRepository.existsByName(name)) {
            throw new IllegalStateException("Product name is already taken: " + name);
        }
    }

    private Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElseThrow();
    }

    private Brand getBrandById(Long id) {
        return brandRepository.findById(id).orElseThrow();
    }

}
