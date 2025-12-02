package app.budget.service;

import app.budget.model.Budget;
import app.budget.repository.BudgetRepository;
import app.exception.UserNotFoundException;
import app.transactions.model.Category;
import app.transactions.service.TransactionService;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.wallet.model.Wallet;
import app.web.dto.BudgetDto;
import app.web.dto.BudgetPageData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceUTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private BudgetService budgetService;

    private User testUser;
    private Wallet testWallet;
    private UUID userId;
    private UUID walletId;
    private Budget testBudget;
    private BudgetDto budgetDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        walletId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .build();

        testWallet = Wallet.builder()
                .id(walletId)
                .name("Default")
                .income(BigDecimal.ZERO)
                .expense(BigDecimal.ZERO)
                .balance(BigDecimal.ZERO)
                .user(testUser)
                .build();

        testUser.setWallet(testWallet);

        testBudget = Budget.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .category(Category.FOOD)
                .amount(new BigDecimal("500.00"))
                .month(YearMonth.now().getMonthValue())
                .year(YearMonth.now().getYear())
                .build();

        budgetDto = BudgetDto.builder()
                .category(Category.FOOD)
                .amount(new BigDecimal("500.00"))
                .month(YearMonth.now().getMonthValue())
                .year(YearMonth.now().getYear())
                .build();
    }

    @Test
    void should_CreateNewBudget_When_BudgetDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findByUserAndCategoryAndYearAndMonth(
                testUser, budgetDto.getCategory(), budgetDto.getYear(), budgetDto.getMonth()))
                .thenReturn(Optional.empty());
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);

        Budget result = budgetService.createOrUpdateBudget(userId, budgetDto);

        assertNotNull(result);
        assertEquals(testBudget, result);
        verify(userRepository).findById(userId);
        verify(budgetRepository).findByUserAndCategoryAndYearAndMonth(
                testUser, budgetDto.getCategory(), budgetDto.getYear(), budgetDto.getMonth());
        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    void should_UpdateExistingBudget_When_BudgetAlreadyExists() {
        BigDecimal newAmount = new BigDecimal("600.00");
        budgetDto.setAmount(newAmount);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findByUserAndCategoryAndYearAndMonth(
                testUser, budgetDto.getCategory(), budgetDto.getYear(), budgetDto.getMonth()))
                .thenReturn(Optional.of(testBudget));
        when(budgetRepository.save(testBudget)).thenReturn(testBudget);

        Budget result = budgetService.createOrUpdateBudget(userId, budgetDto);

        assertNotNull(result);
        assertEquals(newAmount, testBudget.getAmount());
        verify(userRepository).findById(userId);
        verify(budgetRepository).findByUserAndCategoryAndYearAndMonth(
                testUser, budgetDto.getCategory(), budgetDto.getYear(), budgetDto.getMonth());
        verify(budgetRepository).save(testBudget);
    }

    @Test
    void should_ThrowUserNotFoundException_When_UserDoesNotExist() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> budgetService.createOrUpdateBudget(userId, budgetDto));

        verify(userRepository).findById(userId);
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void should_DeleteBudget_When_UserIsAuthorized() {
        UUID budgetId = testBudget.getId();
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(testBudget));
        doNothing().when(budgetRepository).delete(testBudget);

        assertDoesNotThrow(() -> budgetService.deleteBudget(budgetId, userId));

        verify(budgetRepository).findById(budgetId);
        verify(budgetRepository).delete(testBudget);
    }

    @Test
    void should_ThrowIllegalArgumentException_When_BudgetNotFound() {
        UUID budgetId = UUID.randomUUID();
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> budgetService.deleteBudget(budgetId, userId));

        verify(budgetRepository).findById(budgetId);
        verify(budgetRepository, never()).delete(any(Budget.class));
    }

    @Test
    void should_ThrowSecurityException_When_DifferentUserTriesToDelete() {
        UUID budgetId = testBudget.getId();
        UUID differentUserId = UUID.randomUUID();
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(testBudget));

        assertThrows(SecurityException.class,
                () -> budgetService.deleteBudget(budgetId, differentUserId));

        verify(budgetRepository).findById(budgetId);
        verify(budgetRepository, never()).delete(any(Budget.class));
    }

    @Test
    void should_ReturnBudgetPageData_When_MonthAndYearProvided() {
        int month = 6;
        int year = 2024;
        YearMonth yearMonth = YearMonth.of(year, month);

        List<Budget> budgets = List.of(testBudget);
        Map<Category, BigDecimal> categoryExpenses = new HashMap<>();
        categoryExpenses.put(Category.FOOD, new BigDecimal("200.00"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findByUserAndYearAndMonth(testUser, year, month))
                .thenReturn(budgets);
        when(transactionService.getCategoryTotalsForMonth(walletId, yearMonth))
                .thenReturn(categoryExpenses);
        when(transactionService.getTotalExpensesForMonth(walletId, yearMonth))
                .thenReturn(new BigDecimal("200.00"));

        BudgetPageData result = budgetService.getBudgetPageData(userId, month, year);

        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(budgets, result.getBudgets());
        assertEquals(new BigDecimal("500.00"), result.getTotalBudget());
        assertEquals(new BigDecimal("200.00"), result.getTotalSpent());
        assertEquals(new BigDecimal("300.00"), result.getTotalRemaining());
        assertEquals(yearMonth, result.getCurrentMonth());
        assertNotNull(result.getCurrentMonthName());
        assertNotNull(result.getPreviousMonth());
        assertNotNull(result.getNextMonth());
        verify(userRepository, atLeastOnce()).findById(userId);
    }

    @Test
    void should_ReturnBudgetPageDataForCurrentMonth_When_MonthAndYearNotProvided() {
        YearMonth currentMonth = YearMonth.now();
        List<Budget> budgets = List.of(testBudget);
        Map<Category, BigDecimal> categoryExpenses = new HashMap<>();
        categoryExpenses.put(Category.FOOD, new BigDecimal("100.00"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findByUserAndYearAndMonth(
                testUser, currentMonth.getYear(), currentMonth.getMonthValue()))
                .thenReturn(budgets);
        when(transactionService.getCategoryTotalsForMonth(walletId, currentMonth))
                .thenReturn(categoryExpenses);
        when(transactionService.getTotalExpensesForMonth(walletId, currentMonth))
                .thenReturn(new BigDecimal("100.00"));

        BudgetPageData result = budgetService.getBudgetPageData(userId, null, null);

        assertNotNull(result);
        assertEquals(currentMonth, result.getCurrentMonth());
        assertEquals(new BigDecimal("500.00"), result.getTotalBudget());
        assertEquals(new BigDecimal("100.00"), result.getTotalSpent());
        verify(userRepository, atLeastOnce()).findById(userId);
    }

    @Test
    void should_ReturnEmptyBudgetPageData_When_NoBudgetsExist() {
        YearMonth currentMonth = YearMonth.now();
        List<Budget> emptyBudgets = new ArrayList<>();
        Map<Category, BigDecimal> emptyExpenses = new HashMap<>();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findByUserAndYearAndMonth(
                testUser, currentMonth.getYear(), currentMonth.getMonthValue()))
                .thenReturn(emptyBudgets);
        when(transactionService.getCategoryTotalsForMonth(walletId, currentMonth))
                .thenReturn(emptyExpenses);
        when(transactionService.getTotalExpensesForMonth(walletId, currentMonth))
                .thenReturn(BigDecimal.ZERO);

        BudgetPageData result = budgetService.getBudgetPageData(userId, null, null);

        assertNotNull(result);
        assertTrue(result.getBudgets().isEmpty());
        assertEquals(BigDecimal.ZERO, result.getTotalBudget());
        assertEquals(BigDecimal.ZERO, result.getTotalSpent());
        assertEquals(BigDecimal.ZERO, result.getTotalRemaining());
        verify(userRepository, atLeastOnce()).findById(userId);
    }

    @Test
    void should_ThrowUserNotFoundException_When_UserNotFoundForBudgetPageData() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> budgetService.getBudgetPageData(userId, null, null));

        verify(userRepository).findById(userId);
    }

    @Test
    void should_ReturnZeroSpent_When_WalletIsNull() {
        testUser.setWallet(null);
        YearMonth currentMonth = YearMonth.now();
        List<Budget> budgets = List.of(testBudget);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findByUserAndYearAndMonth(
                testUser, currentMonth.getYear(), currentMonth.getMonthValue()))
                .thenReturn(budgets);

        BudgetPageData result = budgetService.getBudgetPageData(userId, null, null);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getTotalSpent());
        verify(userRepository, atLeastOnce()).findById(userId);
        verify(transactionService, never()).getTotalExpensesForMonth(any(), any());
    }

    @Test
    void should_MarkBudgetAsOverBudget_When_ExpensesExceedBudget() {
        YearMonth currentMonth = YearMonth.now();
        List<Budget> budgets = List.of(testBudget);
        Map<Category, BigDecimal> categoryExpenses = new HashMap<>();
        categoryExpenses.put(Category.FOOD, new BigDecimal("600.00")); // Over budget

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findByUserAndYearAndMonth(
                testUser, currentMonth.getYear(), currentMonth.getMonthValue()))
                .thenReturn(budgets);
        when(transactionService.getCategoryTotalsForMonth(walletId, currentMonth))
                .thenReturn(categoryExpenses);
        when(transactionService.getTotalExpensesForMonth(walletId, currentMonth))
                .thenReturn(new BigDecimal("600.00"));

        BudgetPageData result = budgetService.getBudgetPageData(userId, null, null);

        assertNotNull(result);
        assertEquals(new BigDecimal("500.00"), result.getTotalBudget());
        assertEquals(new BigDecimal("600.00"), result.getTotalSpent());
        assertEquals(new BigDecimal("-100.00"), result.getTotalRemaining());
        assertTrue(result.getBudgetInfo().get(Category.FOOD).isOverBudget());
        verify(userRepository, atLeastOnce()).findById(userId);
    }

    @Test
    void should_ReturnBudgetPageData_When_MultipleBudgetsExist() {
        YearMonth currentMonth = YearMonth.now();
        Budget budget1 = Budget.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .category(Category.FOOD)
                .amount(new BigDecimal("500.00"))
                .month(currentMonth.getMonthValue())
                .year(currentMonth.getYear())
                .build();

        Budget budget2 = Budget.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .category(Category.TRANSPORT)
                .amount(new BigDecimal("300.00"))
                .month(currentMonth.getMonthValue())
                .year(currentMonth.getYear())
                .build();

        List<Budget> budgets = List.of(budget1, budget2);
        Map<Category, BigDecimal> categoryExpenses = new HashMap<>();
        categoryExpenses.put(Category.FOOD, new BigDecimal("200.00"));
        categoryExpenses.put(Category.TRANSPORT, new BigDecimal("150.00"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findByUserAndYearAndMonth(
                testUser, currentMonth.getYear(), currentMonth.getMonthValue()))
                .thenReturn(budgets);
        when(transactionService.getCategoryTotalsForMonth(walletId, currentMonth))
                .thenReturn(categoryExpenses);
        when(transactionService.getTotalExpensesForMonth(walletId, currentMonth))
                .thenReturn(new BigDecimal("350.00"));

        BudgetPageData result = budgetService.getBudgetPageData(userId, null, null);

        assertNotNull(result);
        assertEquals(2, result.getBudgets().size());
        assertEquals(new BigDecimal("800.00"), result.getTotalBudget());
        assertEquals(new BigDecimal("350.00"), result.getTotalSpent());
        assertEquals(new BigDecimal("450.00"), result.getTotalRemaining());
        assertEquals(2, result.getBudgetInfo().size());
        verify(userRepository, atLeastOnce()).findById(userId);
    }

    @Test
    void should_ReturnZeroSpentForCategory_When_NoExpensesForCategory() {
        YearMonth currentMonth = YearMonth.now();
        List<Budget> budgets = List.of(testBudget);
        Map<Category, BigDecimal> categoryExpenses = new HashMap<>();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findByUserAndYearAndMonth(
                testUser, currentMonth.getYear(), currentMonth.getMonthValue()))
                .thenReturn(budgets);
        when(transactionService.getCategoryTotalsForMonth(walletId, currentMonth))
                .thenReturn(categoryExpenses);
        when(transactionService.getTotalExpensesForMonth(walletId, currentMonth))
                .thenReturn(BigDecimal.ZERO);

        BudgetPageData result = budgetService.getBudgetPageData(userId, null, null);

        assertNotNull(result);
        assertEquals(new BigDecimal("500.00"), result.getTotalBudget());
        assertEquals(BigDecimal.ZERO, result.getTotalSpent());
        assertEquals(new BigDecimal("500.00"), result.getTotalRemaining());
        assertNotNull(result.getBudgetInfo().get(Category.FOOD));
        assertEquals(BigDecimal.ZERO, result.getBudgetInfo().get(Category.FOOD).getSpent());
        assertFalse(result.getBudgetInfo().get(Category.FOOD).isOverBudget());
        verify(userRepository, atLeastOnce()).findById(userId);
    }
}
