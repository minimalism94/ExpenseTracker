package app.wallet.service;

import app.user.model.Country;
import app.user.model.Role;
import app.user.model.User;
import app.user.model.UserVersion;
import app.user.repository.UserRepository;
import app.wallet.model.Wallet;
import app.wallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class WalletServiceITest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        walletRepository.deleteAll();
        userRepository.deleteAll();

        // Create a test user
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("password123")
                .isActive(true)
                .role(Role.USER)
                .country(Country.BULGARIA)
                .userVersion(UserVersion.BASIC)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .monthlyReportEmailEnabled(false)
                .build();

        testUser = userRepository.save(testUser);
    }

    @Test
    void should_CreateDefaultWallet_When_ValidUserProvided() {

        walletService.createDefaultWallet(testUser);

        Optional<Wallet> savedWalletOpt = walletRepository.findByUserId(testUser.getId());
        assertTrue(savedWalletOpt.isPresent(), "Wallet should be saved in database");

        Wallet savedWallet = savedWalletOpt.get();
        assertNotNull(savedWallet.getId(), "Wallet should have an ID");
        assertEquals("Default", savedWallet.getName(), "Wallet name should be 'Default'");
        assertEquals(BigDecimal.ZERO, savedWallet.getIncome(), "Wallet income should be 0");
        assertEquals(BigDecimal.ZERO, savedWallet.getExpense(), "Wallet expense should be 0");
        assertEquals(new BigDecimal("100"), savedWallet.getBalance(), "Wallet balance should be 100");
        assertEquals(Currency.getInstance("BGN"), savedWallet.getCurrency(), "Wallet currency should be BGN");
        assertNotNull(savedWallet.getUser(), "Wallet should have a user");
        assertEquals(testUser.getId(), savedWallet.getUser().getId(), "Wallet should be associated with the correct user");
    }

    @Test
    void should_PersistWalletInDatabase_When_CreateDefaultWalletCalled() {

        long walletCountBefore = walletRepository.count();

        walletService.createDefaultWallet(testUser);

        long walletCountAfter = walletRepository.count();
        assertEquals(walletCountBefore + 1, walletCountAfter, "Wallet count should increase by 1");

        Wallet savedWallet = walletRepository.findByUserId(testUser.getId())
                .orElseThrow(() -> new AssertionError("Wallet should exist in database"));

        assertEquals("Default", savedWallet.getName());
        assertEquals(0, savedWallet.getIncome().compareTo(BigDecimal.ZERO));
        assertEquals(0, savedWallet.getExpense().compareTo(BigDecimal.ZERO));
        assertEquals(0, savedWallet.getBalance().compareTo(new BigDecimal("100")));
        assertEquals(Currency.getInstance("BGN"), savedWallet.getCurrency());
    }

}

