package com.example.product.service;

import com.example.product.dto.ProductRequest;
import com.example.product.model.Product;
import com.example.product.repository.ProductRepository;
import com.example.shared.exception.BusinessException;
import com.example.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void create_WhenNameExists_ShouldThrowException() {
        ProductRequest request = new ProductRequest();
        request.setName("Existing Product");

        when(productRepository.existsByName("Existing Product")).thenReturn(true);

        assertThrows(BusinessException.class, () -> productService.create(request));
        verify(productRepository, never()).save(any());
    }

    @Test
    void create_WhenValid_ShouldSaveProduct() {
        ProductRequest request = new ProductRequest();
        request.setName("New Product");
        request.setPrice(BigDecimal.valueOf(29.99));
        request.setCategory("Electronics");

        when(productRepository.existsByName(anyString())).thenReturn(false);
        Product savedProduct = Product.builder()
                .id(1L)
                .name("New Product")
                .price(BigDecimal.valueOf(29.99))
                .category("Electronics")
                .build();
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        Product result = productService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("New Product", result.getName());
        assertEquals(BigDecimal.valueOf(29.99), result.getPrice());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void findById_WhenNotFound_ShouldThrowException() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.findById(1L));
    }
}
