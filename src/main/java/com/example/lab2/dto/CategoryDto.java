package com.example.lab2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CategoryDto(
        Long id,
        @NotBlank @Size(max = 255) String name,
        @Positive Long parentCategoryId) {

}
