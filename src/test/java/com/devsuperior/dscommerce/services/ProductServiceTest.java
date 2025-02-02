package com.devsuperior.dscommerce.services;

import com.devsuperior.dscommerce.dto.ProductDTO;
import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.factory.ProductFactory;
import com.devsuperior.dscommerce.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService service;

    @Mock
    private ProductRepository repository;

    private long existingId, nonExistingId;
    private String productName;
    private Product product;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = 2L;
        productName = "PlayStation 5";
        product = ProductFactory.createProduct(productName);

        Mockito.when(repository.findById(existingId)).thenReturn(Optional.of(product));
    }

    @Test
    void findByIdShouldReturnProductDTOWhenIdExists() {
        ProductDTO result = service.findById(existingId);

        assertNotNull(result);
        assertEquals(existingId, result.getId());
        assertEquals(productName, result.getName());
    }
}