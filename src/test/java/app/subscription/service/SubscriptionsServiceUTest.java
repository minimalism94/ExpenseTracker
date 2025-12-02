package app.subscription.service;

import app.exception.CustomException;
import app.exception.UserNotFoundException;
import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionType;
import app.subscription.repository.SubscriptionsRepository;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.wallet.model.Wallet;
import app.wallet.repository.WalletRepository;
import app.web.dto.SubscriptionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionsServiceUTest {

    @Mock
    private SubscriptionsRepository subscriptionsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private SubscriptionsService subscriptionsService;

    private User testUser;
    private Wallet testWallet;
    private Subscription testSubscription;
    private SubscriptionDto subscriptionDto;
    private UUID userId;
    private UUID walletId;
    private UUID subscriptionId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        walletId = UUID.randomUUID();
        subscriptionId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .build();

        testWallet = Wallet.builder()
                .id(walletId)
                .name("Default")
                .income(BigDecimal.ZERO)
                .expense(BigDecimal.ZERO)
                .balance(new BigDecimal("500.00"))
                .user(testUser)
                .build();

        testUser.setWallet(testWallet);

        testSubscription = Subscription.builder()
                .id(subscriptionId)
                .user(testUser)
                .name("Netflix")
                .period(SubscriptionPeriod.MONTHLY)
                .expiryOn(LocalDate.now().plusMonths(1))
                .type(SubscriptionType.DEFAULT)
                .price(new BigDecimal("15.99"))
                .paidDate(null)
                .build();

        subscriptionDto = SubscriptionDto.builder()
                .name("Spotify Premium")
                .period(SubscriptionPeriod.MONTHLY)
                .expiryOn(LocalDate.now().plusMonths(1))
                .type(SubscriptionType.PREMIUM)
                .price(new BigDecimal("9.99"))
                .build();
    }

    @Test
    void should_CreateDefaultSubscription_When_ValidUserProvided() {
        when(subscriptionsRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        subscriptionsService.createDefaultSubscription(testUser);

        ArgumentCaptor<Subscription> subscriptionCaptor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionsRepository).save(subscriptionCaptor.capture());

        Subscription savedSubscription = subscriptionCaptor.getValue();
        assertEquals("Just Testing Subscription", savedSubscription.getName());
        assertEquals(SubscriptionPeriod.MONTHLY, savedSubscription.getPeriod());
        assertEquals(SubscriptionType.DEFAULT, savedSubscription.getType());
        assertEquals(new BigDecimal("150"), savedSubscription.getPrice());
        assertEquals(testUser, savedSubscription.getUser());
        assertNotNull(savedSubscription.getExpiryOn());
    }

    @Test
    void should_ReturnUnpaidSubscriptions_When_UsernameExists() {
        Subscription unpaidSubscription1 = Subscription.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .name("Netflix")
                .paidDate(null)
                .build();

        Subscription unpaidSubscription2 = Subscription.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .name("Spotify")
                .paidDate(null)
                .build();

        Subscription paidSubscription = Subscription.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .name("Gym")
                .paidDate(LocalDate.now())
                .build();

        List<Subscription> allSubscriptions = List.of(unpaidSubscription1, unpaidSubscription2, paidSubscription);
        when(subscriptionsRepository.findAllByUser_UsernameOrderByExpiryOnAsc("testuser"))
                .thenReturn(allSubscriptions);

        List<Subscription> result = subscriptionsService.getByUsername("testuser");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(s -> s.getPaidDate() == null));
        verify(subscriptionsRepository).findAllByUser_UsernameOrderByExpiryOnAsc("testuser");
    }

    @Test
    void should_ReturnEmptyList_When_NoUnpaidSubscriptions() {
        Subscription paidSubscription = Subscription.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .name("Gym")
                .paidDate(LocalDate.now())
                .build();

        when(subscriptionsRepository.findAllByUser_UsernameOrderByExpiryOnAsc("testuser"))
                .thenReturn(List.of(paidSubscription));

        List<Subscription> result = subscriptionsService.getByUsername("testuser");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(subscriptionsRepository).findAllByUser_UsernameOrderByExpiryOnAsc("testuser");
    }

    @Test
    void should_ReturnPaidSubscriptionsForCurrentMonth_When_SubscriptionsExist() {
        YearMonth currentMonth = YearMonth.now();
        LocalDate currentMonthDate = LocalDate.now();

        Subscription paidThisMonth = Subscription.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .name("Netflix")
                .paidDate(currentMonthDate)
                .build();

        Subscription paidLastMonth = Subscription.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .name("Spotify")
                .paidDate(currentMonthDate.minusMonths(1))
                .build();

        Subscription unpaid = Subscription.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .name("Gym")
                .paidDate(null)
                .build();

        testUser.setSubscriptions(new ArrayList<>(List.of(paidThisMonth, paidLastMonth, unpaid)));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        List<Subscription> result = subscriptionsService.getPaidSubscriptionsForCurrentMonth(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(paidThisMonth, result.get(0));
        verify(userRepository).findById(userId);
    }

    @Test
    void should_ThrowUserNotFoundException_When_UserNotFoundForPaidSubscriptions() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> subscriptionsService.getPaidSubscriptionsForCurrentMonth(userId));

        verify(userRepository).findById(userId);
    }

    @Test
    void should_SaveSubscription_When_ValidDtoAndUsernameProvided() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(subscriptionsRepository.save(any(Subscription.class))).thenReturn(testSubscription);

        subscriptionsService.saveSubscription(subscriptionDto, "testuser");

        verify(userRepository).findByUsername("testuser");
        verify(subscriptionsRepository).save(any(Subscription.class));
    }

    @Test
    void should_ThrowUserNotFoundException_When_UsernameNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> subscriptionsService.saveSubscription(subscriptionDto, "nonexistent"));

        verify(userRepository).findByUsername("nonexistent");
        verify(subscriptionsRepository, never()).save(any(Subscription.class));
    }

    @Test
    void should_DeleteSubscription_When_SubscriptionExists() {
        testUser.setSubscriptions(new ArrayList<>(List.of(testSubscription)));
        when(subscriptionsRepository.findById(subscriptionId)).thenReturn(Optional.of(testSubscription));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(subscriptionsRepository).delete(testSubscription);

        subscriptionsService.deleteById(subscriptionId);

        verify(subscriptionsRepository).findById(subscriptionId);
        verify(userRepository).save(testUser);
        verify(subscriptionsRepository).delete(testSubscription);
    }

    @Test
    void should_ThrowCustomException_When_SubscriptionNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(subscriptionsRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(CustomException.class,
                () -> subscriptionsService.deleteById(nonExistentId));

        verify(subscriptionsRepository).findById(nonExistentId);
        verify(subscriptionsRepository, never()).delete(any(Subscription.class));
    }

    @Test
    void should_DeleteSubscription_When_UserIsNull() {
        testSubscription.setUser(null);
        when(subscriptionsRepository.findById(subscriptionId)).thenReturn(Optional.of(testSubscription));
        doNothing().when(subscriptionsRepository).delete(testSubscription);

        subscriptionsService.deleteById(subscriptionId);

        verify(subscriptionsRepository).findById(subscriptionId);
        verify(userRepository, never()).save(any(User.class));
        verify(subscriptionsRepository).delete(testSubscription);
    }

    @Test
    void should_PaySubscription_When_SufficientBalanceExists() {
        when(subscriptionsRepository.findById(subscriptionId)).thenReturn(Optional.of(testSubscription));
        when(subscriptionsRepository.save(any(Subscription.class))).thenReturn(testSubscription);
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        subscriptionsService.paySubscription(subscriptionId, userId);

        assertEquals(LocalDate.now(), testSubscription.getPaidDate());
        assertEquals(new BigDecimal("484.01"), testWallet.getBalance());
        assertEquals(new BigDecimal("15.99"), testWallet.getExpense());
        verify(subscriptionsRepository).findById(subscriptionId);
        verify(subscriptionsRepository).save(testSubscription);
        verify(walletRepository).save(testWallet);
    }

    @Test
    void should_ThrowIllegalArgumentException_When_SubscriptionNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(subscriptionsRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> subscriptionsService.paySubscription(nonExistentId, userId));

        verify(subscriptionsRepository).findById(nonExistentId);
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void should_ThrowCustomException_When_WalletIsNull() {
        testUser.setWallet(null);
        when(subscriptionsRepository.findById(subscriptionId)).thenReturn(Optional.of(testSubscription));

        assertThrows(CustomException.class,
                () -> subscriptionsService.paySubscription(subscriptionId, userId));

        verify(subscriptionsRepository).findById(subscriptionId);
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void should_ThrowCustomException_When_UserIsNull() {
        testWallet.setUser(null);
        when(subscriptionsRepository.findById(subscriptionId)).thenReturn(Optional.of(testSubscription));

        assertThrows(CustomException.class,
                () -> subscriptionsService.paySubscription(subscriptionId, userId));

        verify(subscriptionsRepository).findById(subscriptionId);
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void should_ThrowCustomException_When_DifferentUserTriesToPay() {
        UUID differentUserId = UUID.randomUUID();
        when(subscriptionsRepository.findById(subscriptionId)).thenReturn(Optional.of(testSubscription));

        assertThrows(CustomException.class,
                () -> subscriptionsService.paySubscription(subscriptionId, differentUserId));

        verify(subscriptionsRepository).findById(subscriptionId);
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void should_ThrowCustomException_When_InsufficientBalance() {
        testWallet.setBalance(new BigDecimal("10.00"));
        when(subscriptionsRepository.findById(subscriptionId)).thenReturn(Optional.of(testSubscription));

        assertThrows(CustomException.class,
                () -> subscriptionsService.paySubscription(subscriptionId, userId));

        verify(subscriptionsRepository).findById(subscriptionId);
        verify(walletRepository, never()).save(any(Wallet.class));
    }
}

