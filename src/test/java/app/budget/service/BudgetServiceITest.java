package app.budget.service;

import app.budget.model.Budget;
import app.budget.repository.BudgetRepository;
import app.exception.UserNotFoundException;
import app.transactions.model.Category;
import app.user.model.Country;
import app.user.model.Role;
import app.user.model.User;
import app.user.model.UserVersion;
import app.user.repository.UserRepository;
import app.wallet.model.Wallet;
import app.wallet.repository.WalletRepository;
import app.web.dto.BudgetDto;
import app.web.dto.BudgetPageData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BudgetServiceITest {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    private User testUser;
    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        budgetRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();

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

        testWallet = Wallet.builder()
                .user(testUser)
                .name("Default")
                .income(new BigDecimal("0"))
                .expense(new BigDecimal("0"))
                .balance(new BigDecimal("1000"))
                .build();
        testWallet = walletRepository.save(testWallet);
        testUser.setWallet(testWallet);
        userRepository.save(testUser);
    }

    @Test
    void should_CreateBudget_When_ValidDtoProvided() {
        // Given
        BudgetDto dto = new BudgetDto();
        dto.setCategory(Category.FOOD);
        dto.setAmount(new BigDecimal("500"));
        dto.setMonth(LocalDateTime.now().getMonthValue());
        dto.setYear(LocalDateTime.now().getYear());

        // When
        Budget created = budgetService.createOrUpdateBudget(testUser.getId(), dto);

        // Then
        assertNotNull(created.getId());
        assertEquals(Category.FOOD, created.getCategory());
        assertEquals(new BigDecimal("500"), created.getAmount());
        assertEquals(testUser.getId(), created.getUser().getId());

        Optional<Budget> saved = budgetRepository.findById(created.getId());
        assertTrue(saved.isPresent());
    }

    @Test
    void should_ThrowException_When_UserIdNotFound() {
        // Given
        BudgetDto dto = new BudgetDto();
        dto.setCategory(Category.FOOD);
        dto.setAmount(new BigDecimal("500"));
        dto.setMonth(LocalDateTime.now().getMonthValue());
        dto.setYear(LocalDateTime.now().getYear());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            budgetService.createOrUpdateBudget(java.util.UUID.randomUUID(), dto);
        });
    }

    @Test
    void should_DeleteBudget_When_ValidIdProvided() {
        // Given
        BudgetDto dto = new BudgetDto();
        dto.setCategory(Category.FOOD);
        dto.setAmount(new BigDecimal("500"));
        dto.setMonth(LocalDateTime.now().getMonthValue());
        dto.setYear(LocalDateTime.now().getYear());
        Budget created = budgetService.createOrUpdateBudget(testUser.getId(), dto);

        // When
        budgetService.deleteBudget(created.getId(), testUser.getId());

        // Then
        Optional<Budget> deleted = budgetRepository.findById(created.getId());
        assertFalse(deleted.isPresent(), "Budget should be deleted");
    }

    @Test
    void should_ThrowException_When_DeletingBudgetOfDifferentUser() {
        // Given
        BudgetDto dto = new BudgetDto();
        dto.setCategory(Category.FOOD);
        dto.setAmount(new BigDecimal("500"));
        dto.setMonth(LocalDateTime.now().getMonthValue());
        dto.setYear(LocalDateTime.now().getYear());
        Budget created = budgetService.createOrUpdateBudget(testUser.getId(), dto);

        User otherUser = User.builder()
                .username("otheruser")
                .email("other@example.com")
                .password("password")
                .isActive(true)
                .role(Role.USER)
                .country(Country.BULGARIA)
                .userVersion(UserVersion.BASIC)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        User savedOtherUser = userRepository.save(otherUser);
        final java.util.UUID otherUserId = savedOtherUser.getId();

        // When & Then
        assertThrows(SecurityException.class, () -> {
            budgetService.deleteBudget(created.getId(), otherUserId);
        });
    }
}

