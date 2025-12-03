package app.user.service;

import app.exception.UserNotFoundException;
import app.exception.UsernameAlreadyExistException;
import app.user.model.Country;
import app.user.model.Role;
import app.user.model.User;
import app.user.model.UserVersion;
import app.user.repository.UserRepository;
import app.wallet.model.Wallet;
import app.wallet.repository.WalletRepository;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceITest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @BeforeEach
    void setUp() {
        walletRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void should_ThrowException_When_UsernameAlreadyExists() {
        // Given
        User existingUser = User.builder()
                .username("existinguser")
                .email("existing@example.com")
                .password("password")
                .isActive(true)
                .role(Role.USER)
                .country(Country.BULGARIA)
                .userVersion(UserVersion.BASIC)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        userRepository.save(existingUser);

        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("existinguser")
                .email("newemail@example.com")
                .password("password123")
                .country(Country.BULGARIA)
                .build();

        // When & Then
        assertThrows(UsernameAlreadyExistException.class, () -> {
            userService.register(registerRequest);
        });
    }

    @Test
    void should_LoadUserByUsername_When_UserExists() {
        // Given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .isActive(true)
                .role(Role.USER)
                .country(Country.BULGARIA)
                .userVersion(UserVersion.BASIC)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        user = userRepository.save(user);

        // When
        UserDetails userDetails = userService.loadUserByUsername("testuser");

        // Then
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void should_ThrowException_When_UsernameNotFound() {
        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            userService.loadUserByUsername("nonexistent");
        });
    }

    @Test
    void should_GetUserById_When_UserExists() {
        // Given
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .isActive(true)
                .role(Role.USER)
                .country(Country.BULGARIA)
                .userVersion(UserVersion.BASIC)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        user = userRepository.save(user);

        // When
        User foundUser = userService.getById(user.getId());

        // Then
        assertNotNull(foundUser);
        assertEquals(user.getId(), foundUser.getId());
        assertEquals("testuser", foundUser.getUsername());
    }

    @Test
    void should_ThrowException_When_UserIdNotFound() {
        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            userService.getById(java.util.UUID.randomUUID());
        });
    }

    @Test
    void should_GetAllUsers_When_UsersExist() {
        // Given
        User user1 = User.builder()
                .username("user1")
                .email("user1@example.com")
                .password("password")
                .isActive(true)
                .role(Role.USER)
                .country(Country.BULGARIA)
                .userVersion(UserVersion.BASIC)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        User user2 = User.builder()
                .username("user2")
                .email("user2@example.com")
                .password("password")
                .isActive(true)
                .role(Role.USER)
                .country(Country.BULGARIA)
                .userVersion(UserVersion.BASIC)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        userRepository.save(user1);
        userRepository.save(user2);

        // When
        var allUsers = userService.getAllUsers();

        // Then
        assertNotNull(allUsers);
        assertTrue(allUsers.size() >= 2);
    }
}




