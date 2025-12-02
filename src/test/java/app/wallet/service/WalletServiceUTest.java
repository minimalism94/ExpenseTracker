package app.wallet.service;

import app.transactions.service.TransactionService;
import app.user.model.Country;
import app.user.model.Role;
import app.user.model.User;
import app.user.model.UserVersion;
import app.wallet.model.Wallet;
import app.wallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceUTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private WalletService walletService;

    private User testUser;
    private Wallet testWallet;
    private UUID userId;
    private UUID walletId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        walletId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .isActive(true)
                .role(Role.USER)
                .country(Country.BULGARIA)
                .userVersion(UserVersion.BASIC)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        testWallet = Wallet.builder()
                .id(walletId)
                .name("Default")
                .income(new BigDecimal("0"))
                .expense(new BigDecimal("0"))
                .balance(new BigDecimal("100"))
                .currency(Currency.getInstance("BGN"))
                .user(testUser)
                .build();
    }

    @Test
    void should_CreateDefaultWallet_When_ValidUserProvided() {
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        walletService.createDefaultWallet(testUser);

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());

        Wallet savedWallet = walletCaptor.getValue();
        assertEquals("Default", savedWallet.getName());
        assertEquals(BigDecimal.ZERO, savedWallet.getIncome());
        assertEquals(BigDecimal.ZERO, savedWallet.getExpense());
        assertEquals(new BigDecimal("100"), savedWallet.getBalance());
        assertEquals(Currency.getInstance("BGN"), savedWallet.getCurrency());
        assertEquals(testUser, savedWallet.getUser());
    }

    @Test
    void should_ReturnWallet_When_WalletIdExists() {
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));

        Wallet result = walletService.getById(walletId);

        assertNotNull(result);
        assertEquals(walletId, result.getId());
        assertEquals("Default", result.getName());
        assertEquals(testUser, result.getUser());
        verify(walletRepository).findById(walletId);
    }

    @Test
    void should_ThrowRuntimeException_When_WalletNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(walletRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> walletService.getById(nonExistentId));

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Wallet by id"));
        assertTrue(exception.getMessage().contains(nonExistentId.toString()));
        verify(walletRepository).findById(nonExistentId);
    }

    @Test
    void should_CreateWalletWithCorrectDefaultValues_When_UserProvided() {
        Wallet savedWallet = Wallet.builder()
                .id(walletId)
                .name("Default")
                .income(BigDecimal.ZERO)
                .expense(BigDecimal.ZERO)
                .balance(new BigDecimal("100"))
                .currency(Currency.getInstance("BGN"))
                .user(testUser)
                .build();

        when(walletRepository.save(any(Wallet.class))).thenReturn(savedWallet);

        walletService.createDefaultWallet(testUser);

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());

        Wallet capturedWallet = walletCaptor.getValue();
        assertEquals("Default", capturedWallet.getName());
        assertEquals(new BigDecimal("0"), capturedWallet.getIncome());
        assertEquals(new BigDecimal("0"), capturedWallet.getExpense());
        assertEquals(new BigDecimal("100"), capturedWallet.getBalance());
        assertEquals(Currency.getInstance("BGN"), capturedWallet.getCurrency());
    }
}

