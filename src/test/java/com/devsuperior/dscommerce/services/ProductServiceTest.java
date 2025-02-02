package com.devsuperior.dscommerce.services;

import com.devsuperior.dscommerce.dto.ProductDTO;
import com.devsuperior.dscommerce.dto.ProductMinDTO;
import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.factory.ProductFactory;
import com.devsuperior.dscommerce.repositories.ProductRepository;
import com.devsuperior.dscommerce.services.exceptions.DatabaseException;
import com.devsuperior.dscommerce.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService service;

    @Mock
    private ProductRepository repository;

    private long existingId, nonExistingId, dependentId;
    private String productName;
    private Product product;
    private ProductDTO productDTO;
    private PageImpl<Product> page;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = 2L;
        dependentId = 3L;
        productName = "PlayStation 5";
        product = ProductFactory.createProduct(productName);
        productDTO = new ProductDTO(product);
        page = new PageImpl<>(List.of(product));

        // Mocks para findById()
        Mockito.when(repository.findById(existingId)).thenReturn(Optional.of(product));
        Mockito.when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Mock para findAll()
        Mockito.when(repository.searchByName(any(), (Pageable) any())).thenReturn(page);

        // Mock para insert()
        Mockito.when(repository.save(any())).thenReturn(product);

        // Mocks para update()
        Mockito.when(repository.getReferenceById(existingId)).thenReturn(product);
        Mockito.when(repository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);

        // Mocks para delete()
        Mockito.when(repository.existsById(existingId)).thenReturn(true);
        Mockito.when(repository.existsById(dependentId)).thenReturn(true);
        Mockito.when(repository.existsById(nonExistingId)).thenReturn(false);

        Mockito.doNothing().when(repository).deleteById(existingId);
        Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
    }

    @Test
    void findByIdShouldReturnProductDTOWhenIdExists() {
        ProductDTO result = service.findById(existingId);

        assertNotNull(result);
        assertEquals(existingId, result.getId());
        assertEquals(productName, result.getName());
    }

    @Test
    void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        assertThrows(ResourceNotFoundException.class, () -> {
            service.findById(nonExistingId);
        });
    }

    @Test
    void findAllShouldReturnPagedProductMinDTO() {
        Pageable pageable = PageRequest.of(0, 12);
        Page<ProductMinDTO> result = service.findAll(productName, pageable);

        assertNotNull(result);
        assertEquals(1, result.getSize());
        assertEquals(productName, result.iterator().next().getName());
    }

    @Test
    void insertShouldReturnProductDTO() {
        ProductDTO result = service.insert(productDTO);

        assertNotNull(result);
        assertEquals(existingId, result.getId());
    }

    @Test
    void updateShouldSaveAndReturnProductDTO() {
        ProductDTO result = service.update(existingId, productDTO);

        verify(repository).save(any());
        assertNotNull(result);
        assertEquals(existingId, result.getId());
    }

    @Test
    void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        assertThrows(ResourceNotFoundException.class, () -> {
            service.update(nonExistingId, productDTO);
        });
    }

    @Test
    void deleteShouldDoNothingWhenIdExists() {
        assertDoesNotThrow(() -> {
            service.delete(existingId);
        });
    }

    @Test
    void deleteShouldThrowResourceNotFoundWhenIdDoesNotExist() {
        assertThrows(ResourceNotFoundException.class, () -> {
           service.delete(nonExistingId);
        });
    }

    @Test
    void deleteShouldThrowDatabaseExceptionWhenIdIsDependent() {
        assertThrows(DatabaseException.class, () -> {
            service.delete(dependentId);
        });
    }
}