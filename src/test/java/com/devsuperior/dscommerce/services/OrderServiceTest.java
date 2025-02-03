package com.devsuperior.dscommerce.services;

import com.devsuperior.dscommerce.dto.OrderDTO;
import com.devsuperior.dscommerce.entities.Order;
import com.devsuperior.dscommerce.entities.OrderItem;
import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.entities.User;
import com.devsuperior.dscommerce.factory.OrderFactory;
import com.devsuperior.dscommerce.factory.ProductFactory;
import com.devsuperior.dscommerce.factory.UserFactory;
import com.devsuperior.dscommerce.repositories.OrderItemRepository;
import com.devsuperior.dscommerce.repositories.OrderRepository;
import com.devsuperior.dscommerce.repositories.ProductRepository;
import com.devsuperior.dscommerce.services.exceptions.ForbiddenException;
import com.devsuperior.dscommerce.services.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService service;

    @Mock
    private OrderRepository repository;

    @Mock
    private AuthService authService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private UserService userService;

    private Long existingOrderId, nonExistingOrderId;
    private Long existingProductId, nonExistingProductId;
    private Order order;
    private OrderDTO orderDTO;
    private User admin, client;
    private Product product;

    @BeforeEach
    void setUp() {
        existingOrderId = 1L;
        nonExistingOrderId = 2L;

        existingProductId = 1L;
        nonExistingProductId = 2L;

        admin = UserFactory.createCustomAdminUser(1L, "Jef");
        client = UserFactory.createCustomClientUser(2L, "Bob");

        order = OrderFactory.createOrder(client);
        orderDTO = new OrderDTO(order);

        product = ProductFactory.createProduct();

        // Mocks para findById()
        Mockito.when(repository.findById(existingOrderId)).thenReturn(Optional.of(order));
        Mockito.when(repository.findById(nonExistingOrderId)).thenReturn(Optional.empty());

        // Mocks para insert()
        Mockito.when(productRepository.getReferenceById(existingProductId)).thenReturn(product);
        Mockito.when(productRepository.getReferenceById(nonExistingProductId)).thenThrow(EntityNotFoundException.class);
        Mockito.when(repository.save(any())).thenReturn(order);
        Mockito.when(orderItemRepository.saveAll(any())).thenReturn(new ArrayList<>(order.getItems()));
    }

    @Test
    void findByIdShouldReturnOrderDTOWhenIdExistsAndAdminIsLogged() {
        Mockito.doNothing().when(authService).validateSelfOrAdmin(any());
        OrderDTO result = service.findById(existingOrderId);

        assertNotNull(result);
        assertEquals(existingOrderId, result.getId());
    }

    @Test
    void findByIdShouldReturnOrderDTOWhenIdExistsAndSelfClientIsLogged() {
        Mockito.doNothing().when(authService).validateSelfOrAdmin(any());
        OrderDTO result = service.findById(existingOrderId);

        assertNotNull(result);
        assertEquals(existingOrderId, result.getId());
    }

    @Test
    void findByIdShouldThrowsForbiddenExceptionWhenIdExistsAndOtherClientLogged() {
        Mockito.doThrow(ForbiddenException.class).when(authService).validateSelfOrAdmin(any());
        assertThrows(ForbiddenException.class, () -> {
           service.findById(existingOrderId);
        });
    }

    @Test
    void findByIdShouldThrowsResourceNotFoundExceptionWhenIdDoesNotExist() {
        Mockito.doNothing().when(authService).validateSelfOrAdmin(any());
        assertThrows(ResourceNotFoundException.class, () -> {
            service.findById(nonExistingOrderId);
        });
    }

    @Test
    void insertShouldReturnOrderDTOWhenAdminLogged() {
        Mockito.when(userService.authenticated()).thenReturn(admin);
        OrderDTO result = service.insert(orderDTO);

        assertNotNull(result);
    }

    @Test
    void insertShouldReturnOrderDTOWhenClientLogged() {
        Mockito.when(userService.authenticated()).thenReturn(client);
        OrderDTO result = service.insert(orderDTO);

        assertNotNull(result);
    }

    @Test
    void insertShouldThrowsUsernameNotFoundExceptionWhenUserNotLogged() {
        Mockito.doThrow(UsernameNotFoundException.class).when(userService).authenticated();

        order.setClient(new User());
        orderDTO = new OrderDTO(order);

        assertThrows(UsernameNotFoundException.class, () -> {
            service.insert(orderDTO);
        });
    }

    @Test
    void insertShouldThrowsEntityNotFoundExceptionWhenOrderProductIdDoesNotExist() {
        Mockito.when(userService.authenticated()).thenReturn(client);

        product.setId(nonExistingOrderId);
        OrderItem orderItem = new OrderItem(order, product, 2, 10.0);
        order.getItems().add(orderItem);

        orderDTO = new OrderDTO(order);

        assertThrows(EntityNotFoundException.class, () -> {
           service.insert(orderDTO);
        });
    }
}