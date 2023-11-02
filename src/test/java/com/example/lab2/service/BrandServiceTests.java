package com.example.lab2.service;

import com.example.lab2.dto.BrandDto;
import com.example.lab2.model.Brand;
import com.example.lab2.repository.BrandRepository;
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
public class BrandServiceTests {

    @Mock
    private BrandRepository brandRepository;
    private BrandService brandService;

    static Stream<BrandDto> createBrandArgs() {
        return Stream.of(
                BrandDto.builder()
                        .name("Brand 1")
                        .build(),
                BrandDto.builder()
                        .name("Brand 2")
                        .build()
        );
    }

    static Stream<BrandDto> createInvalidBrandArgs() {
        return Stream.of(
                BrandDto.builder()
                        .name(" ")
                        .build(),
                BrandDto.builder()
                        .name("")
                        .build(),
                BrandDto.builder()
                        .name(null)
                        .build()
        );
    }

    @BeforeEach
    void setUp() {
        final var validatorFactory = Validation.buildDefaultValidatorFactory();
        final var validator = validatorFactory.getValidator();
        brandService = new BrandService(brandRepository, validator);
    }

    @Test
    void whenGetAllBrands_thenReturnsListOfBrands() {
        System.out.println("Testing getAllBrands method");
        final List<Brand> brands = List.of(
                new Brand(1L, "Brand 1"),
                new Brand(2L, "Brand 2")
        );
        given(brandRepository.findAll()).willReturn(brands);
        assertThat(brandService.getAllBrands()).containsExactly(
                new BrandDto(1L, "Brand 1"),
                new BrandDto(2L, "Brand 2")
        );
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 2L, 3L, Long.MAX_VALUE})
    void givenBrandId_whenGetBrandById_thenReturnsBrand(Long id) {
        System.out.println("Testing getBrandById method with id = " + id);
        final var brandName = "Brand " + id;
        given(brandRepository.findById(id)).willReturn(Optional.of(new Brand(id, brandName)));
        final var brandDto = brandService.getBrandById(id);
        assertThat(brandDto).isEqualTo(new BrandDto(id, brandName));
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 0L, -1L, Long.MIN_VALUE})
    void givenBrandWithGivenIdDoesNotExist_whenGetBrandById_thenThrowsException(Long id) {
        System.out.println("Testing getBrandById method with non-existent id = " + id);
        given(brandRepository.findById(id)).willReturn(Optional.empty());
        assertThatThrownBy(() -> brandService.getBrandById(id)).isInstanceOf(NoSuchElementException.class);
    }

    @ParameterizedTest
    @MethodSource("createBrandArgs")
    void givenBrandData_whenCreateBrand_thenSavesNewBrand(BrandDto brandDto) {
        System.out.println("Testing createBrand method with " + brandDto);
        brandService.createBrand(brandDto);
        verify(brandRepository).save(ArgumentMatchers.eq(new Brand(null, brandDto.name())));
    }

    @ParameterizedTest
    @MethodSource("createInvalidBrandArgs")
    void givenInvalidBrandData_whenCreateBrand_thenThrowsException(BrandDto brandDto) {
        System.out.println("Testing createBrand method with " + brandDto);
        assertThatThrownBy(() -> brandService.createBrand(brandDto)).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void givenBrandNameIsTaken_whenCreateBrand_thenThrowsException() {
        System.out.println("Testing createBrand method with taken brand name");
        final var brandName = "Brand 1";
        given(brandRepository.existsByName(brandName)).willReturn(true);
        assertThatThrownBy(() -> brandService.createBrand(new BrandDto(null, brandName))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void givenBrandData_whenUpdateBrand_thenSavesUpdatedBrand() {
        System.out.println("Testing updateBrand method");
        final var id = 1L;
        final var brandName = "Brand 1";
        given(brandRepository.findById(id)).willReturn(Optional.of(new Brand(id, brandName)));
        final var newBrandName = "Brand 2";
        brandService.updateBrand(id, new BrandDto(id, newBrandName));
        verify(brandRepository).save(ArgumentMatchers.eq(new Brand(id, newBrandName)));
    }

    @ParameterizedTest
    @MethodSource("createInvalidBrandArgs")
    void givenInvalidBrandData_whenUpdateBrand_thenThrowsException(BrandDto brandDto) {
        System.out.println("Testing updateBrand method with " + brandDto);
        final var id = 1L;
        assertThatThrownBy(() -> brandService.updateBrand(id, brandDto)).isInstanceOf(ConstraintViolationException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 0L, -1L, Long.MIN_VALUE})
    void givenBrandWithGivenIdDoesNotExist_whenUpdateBrand_thenThrowsException(Long id) {
        System.out.println("Testing updateBrand method with non-existent id = " + id);
        given(brandRepository.findById(id)).willReturn(Optional.empty());
        assertThatThrownBy(() -> brandService.updateBrand(id, new BrandDto(id, "Brand"))).isInstanceOf(NoSuchElementException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 2L, 3L, Long.MAX_VALUE})
    void givenBrandId_whenDeleteBrand_thenDeletesBrand(Long id) {
        System.out.println("Testing deleteBrand method with id = " + id);
        brandService.deleteBrand(id);
        verify(brandRepository).deleteById(id);
    }

}
