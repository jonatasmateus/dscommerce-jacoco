package com.devsuperior.dscommerce.services;

import com.devsuperior.dscommerce.entities.User;
import com.devsuperior.dscommerce.factory.UserDetailsFactory;
import com.devsuperior.dscommerce.factory.UserFactory;
import com.devsuperior.dscommerce.projections.UserDetailsProjection;
import com.devsuperior.dscommerce.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService service;

    @Mock
    private UserRepository repository;

    private String existingUsername, nonExistingUsername;
    private User user;
    private List<UserDetailsProjection> userDetails;

    @BeforeEach
    void setUp() {
        existingUsername = "maria@gmail.com";
        nonExistingUsername = "user@gmail.com";
        user = UserFactory.createCustomClientUser(1L, existingUsername);
        userDetails = UserDetailsFactory.createCustomAdminUser(existingUsername);

        Mockito.when(repository.searchUserAndRolesByEmail(existingUsername)).thenReturn(userDetails);
        Mockito.when(repository.searchUserAndRolesByEmail(nonExistingUsername)).thenReturn(new ArrayList<>());
    }

    @Test
    void loadUserByUsernameShouldReturnUserDetailsWhenUserExists() {
         UserDetails result = service.loadUserByUsername(existingUsername);

         assertNotNull(result);
         assertEquals(existingUsername, result.getUsername());
    }

    @Test
    void loadUserByUsernameShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExist() {
        assertThrows(UsernameNotFoundException.class, () -> {
            service.loadUserByUsername(nonExistingUsername);
        });
    }
}