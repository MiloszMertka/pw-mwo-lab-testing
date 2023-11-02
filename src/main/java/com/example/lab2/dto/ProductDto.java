package com.example.lab2.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record ProductDto(
        Long id,
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 2000) String description,
        @NotNull @Positive Double price,
        @NotNull @PositiveOrZero Integer quantity,
        @NotNull @Positive Long categoryId,
        @NotNull @Positive Long brandId) {

}
