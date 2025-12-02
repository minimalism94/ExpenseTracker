package app.user.service;

import app.confg.BeanConfiguration;
import app.exception.UserNotFoundException;
import app.exception.UsernameAlreadyExistException;
import app.notification.service.NotificationService;
import app.security.UserData;
import app.subscription.service.SubscriptionsService;
import app.user.model.Country;
import app.user.model.Role;
import app.user.model.User;
import app.user.model.UserVersion;
import app.user.repository.UserRepository;
import app.wallet.service.WalletService;
import app.web.dto.RegisterRequest;
import app.web.dto.UserEditRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private SubscriptionsService subscriptionsService;

    @Mock
    private BeanConfiguration beanConfiguration;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UUID testUserId;
    private RegisterRequest registerRequest;
    private UserEditRequest userEditRequest;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(testUserId)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .role(Role.USER)
                .country(Country.BULGARIA)
                .userVersion(UserVersion.BASIC)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        registerRequest = RegisterRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("password123")
                .country(Country.BULGARIA)
                .build();

        userEditRequest = UserEditRequest.builder()
                .username("updateduser")
                .firstName("Updated")
                .lastName("Name")
                .email("updated@example.com")
                .country(Country.BULGARIA)
                .build();
    }

    @Test
    void should_RegisterUser_When_ValidRequestProvided() {
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
        when(beanConfiguration.passwordEncoder()).thenReturn(passwordEncoder);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        assertDoesNotThrow(() -> userService.register(registerRequest));

        verify(userRepository).findByUsername(registerRequest.getUsername());
        verify(userRepository).save(any(User.class));
        verify(walletService).createDefaultWallet(any(User.class));
        verify(subscriptionsService).createDefaultSubscription(any(User.class));
        verify(notificationService).upsertPreference(any(UUID.class), eq(false), anyString());
    }

    @Test
    void should_ThrowUsernameAlreadyExistException_When_UsernameAlreadyExists() {
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.of(testUser));


        assertThrows(UsernameAlreadyExistException.class, () -> userService.register(registerRequest));

        verify(userRepository).findByUsername(registerRequest.getUsername());
        verify(userRepository, never()).save(any(User.class));
        verify(walletService, never()).createDefaultWallet(any(User.class));
    }

    @Test
    void should_ReturnUserDetails_When_UsernameExists() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserDetails result = userService.loadUserByUsername("testuser");

        assertNotNull(result);
        assertInstanceOf(UserData.class, result);
        assertEquals("testuser", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void should_ThrowUserNotFoundException_When_UsernameDoesNotExist() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());


        assertThrows(UserNotFoundException.class, () -> userService.loadUserByUsername("nonexistent"));

        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void should_ReturnUser_When_UserIdExists() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        User result = userService.getById(testUserId);

        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findById(testUserId);
    }

    @Test
    void should_ThrowUserNotFoundException_When_UserIdDoesNotExist() {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());


        assertThrows(UserNotFoundException.class, () -> userService.getById(nonExistentId));

        verify(userRepository).findById(nonExistentId);
    }

    @Test
    void should_ReturnAllUsers_When_UsersExist() {
        User user1 = User.builder()
                .id(UUID.randomUUID())
                .username("user1")
                .build();
        User user2 = User.builder()
                .id(UUID.randomUUID())
                .username("user2")
                .build();
        List<User> users = Arrays.asList(user1, user2);

        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void should_SaveUser_When_ValidUserProvided() {
        when(userRepository.save(testUser)).thenReturn(testUser);

        User result = userService.save(testUser);

        assertNotNull(result);
        assertEquals(testUser, result);
        verify(userRepository).save(testUser);
    }

    @Test
    void should_ChangeRoleToAdmin_When_UserHasUserRole() {
        testUser.setRole(Role.USER);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.setRole(testUserId);

        assertEquals(Role.ADMIN, testUser.getRole());
        verify(userRepository).findById(testUserId);
        verify(userRepository).save(testUser);
    }

    @Test
    void should_ChangeRoleToUser_When_UserHasAdminRole() {
        testUser.setRole(Role.ADMIN);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.setRole(testUserId);

        assertEquals(Role.USER, testUser.getRole());
        verify(userRepository).findById(testUserId);
        verify(userRepository).save(testUser);
    }

    @Test
    void should_ThrowUserNotFoundException_When_UserNotFoundForSetRole() {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());


        assertThrows(UserNotFoundException.class, () -> userService.setRole(nonExistentId));

        verify(userRepository).findById(nonExistentId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void should_DeleteUser_When_ValidUserIdProvided() {
        doNothing().when(userRepository).deleteById(testUserId);

        assertDoesNotThrow(() -> userService.delete(testUserId));

        verify(userRepository).deleteById(testUserId);
    }

    @Test
    void should_DeactivateUser_When_UserIsActive() {
        testUser.setActive(true);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.setActive(testUserId);

        assertFalse(testUser.isActive());
        verify(userRepository).findById(testUserId);
        verify(userRepository).save(testUser);
    }

    @Test
    void should_ActivateUser_When_UserIsInactive() {
        testUser.setActive(false);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.setActive(testUserId);

        assertTrue(testUser.isActive());
        verify(userRepository).findById(testUserId);
        verify(userRepository).save(testUser);
    }

    @Test
    void should_ThrowUserNotFoundException_When_UserNotFoundForSetActive() {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());


        assertThrows(UserNotFoundException.class, () -> userService.setActive(nonExistentId));

        verify(userRepository).findById(nonExistentId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void should_UpdateUserDetails_When_ValidRequestProvided() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.editUserDetails(testUserId, userEditRequest);

        assertEquals("updateduser", testUser.getUsername());
        assertEquals("Updated", testUser.getFirstName());
        assertEquals("updated@example.com", testUser.getEmail());
        assertNotNull(testUser.getUpdatedOn());
        verify(userRepository).findById(testUserId);
        verify(userRepository).save(testUser);
    }

    @Test
    void should_UpdateNotificationPreference_When_EmailIsProvided() {
        userEditRequest.setEmail("newemail@example.com");
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.editUserDetails(testUserId, userEditRequest);

        verify(notificationService).upsertPreference(testUserId, true, "newemail@example.com");
    }

    @Test
    void should_ThrowNullPointerException_When_EmailIsNull() {
        userEditRequest.setEmail(null);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));


        assertThrows(NullPointerException.class, () -> userService.editUserDetails(testUserId, userEditRequest));

        verify(userRepository).findById(testUserId);
        verify(userRepository, never()).save(any(User.class));
        verify(notificationService, never()).upsertPreference(any(UUID.class), anyBoolean(), anyString());
    }

    @Test
    void should_DisableNotificationPreference_When_EmailIsBlank() {
        userEditRequest.setEmail("   "); // Blank email
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.editUserDetails(testUserId, userEditRequest);

        verify(notificationService).upsertPreference(testUserId, false, null);
    }

    @Test
    void should_ThrowUserNotFoundException_When_UserNotFoundForEditDetails() {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());


        assertThrows(UserNotFoundException.class, () -> userService.editUserDetails(nonExistentId, userEditRequest));

        verify(userRepository).findById(nonExistentId);
        verify(userRepository, never()).save(any(User.class));
    }
}
