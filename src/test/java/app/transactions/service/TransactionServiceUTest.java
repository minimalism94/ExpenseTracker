package app.transactions.service;

import app.exception.CustomException;
import app.transactions.model.Category;
import app.transactions.model.Transaction;
import app.transactions.model.Type;
import app.transactions.repository.TransactionRepository;
import app.user.model.User;
import app.wallet.model.Wallet;
import app.wallet.repository.WalletRepository;
import app.web.dto.TopCategories;
import app.web.dto.TransactionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Wallet testWallet;
    private User testUser;
    private UUID walletId;
    private UUID userId;
    private Transaction testTransaction;
    private TransactionDto transactionDto;

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
                .income(new BigDecimal("1000.00"))
                .expense(new BigDecimal("200.00"))
                .balance(new BigDecimal("800.00"))
                .user(testUser)
                .transactions(new ArrayList<>())
                .build();

        testTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("50.00"))
                .date(LocalDateTime.now())
                .type(Type.EXPENSE)
                .category(Category.FOOD)
                .description("Test transaction")
                .wallet(testWallet)
                .build();

        transactionDto = TransactionDto.builder()
                .amount(new BigDecimal("100.00"))
                .date(LocalDateTime.now())
                .type(Type.EXPENSE)
                .category(Category.FOOD)
                .description("Test expense")
                .build();
    }

    @Test
    void should_SaveTransaction_When_ValidTransactionProvided() {
        when(transactionRepository.save(testTransaction)).thenReturn(testTransaction);

        Transaction result = transactionService.saveTransaction(testTransaction);

        assertNotNull(result);
        assertEquals(testTransaction, result);
        verify(transactionRepository).save(testTransaction);
    }

    @Test
    void should_ReturnAllTransactionsSortedByDate_When_FindAllCalled() {
        List<Transaction> transactions = Arrays.asList(testTransaction);
        when(transactionRepository.findAll(any(Sort.class))).thenReturn(transactions);

        List<Transaction> result = transactionService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transactionRepository).findAll(any(Sort.class));
    }

    @Test
    void should_ProcessExpenseTransaction_When_SufficientBalanceExists() {
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(testWallet));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        transactionService.processTransaction(transactionDto, userId);

        assertEquals(new BigDecimal("300.00"), testWallet.getExpense());
        assertEquals(new BigDecimal("700.00"), testWallet.getBalance());
        verify(walletRepository).findByUserId(userId);
        verify(transactionRepository).save(any(Transaction.class));
        verify(walletRepository).save(testWallet);
    }

    @Test
    void should_ThrowIllegalArgumentException_When_InsufficientBalanceForExpense() {
        transactionDto.setAmount(new BigDecimal("1000.00"));
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(testWallet));


        assertThrows(IllegalArgumentException.class, 
                () -> transactionService.processTransaction(transactionDto, userId));

        verify(walletRepository).findByUserId(userId);
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void should_ProcessIncomeTransaction_When_ValidIncomeProvided() {
        transactionDto.setType(Type.INCOME);
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(testWallet));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        transactionService.processTransaction(transactionDto, userId);

        assertEquals(new BigDecimal("1100.00"), testWallet.getIncome());
        assertEquals(new BigDecimal("900.00"), testWallet.getBalance());
        verify(walletRepository).findByUserId(userId);
        verify(transactionRepository).save(any(Transaction.class));
        verify(walletRepository).save(testWallet);
    }

    @Test
    void should_ThrowCustomException_When_WalletNotFound() {
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());


        assertThrows(CustomException.class, 
                () -> transactionService.processTransaction(transactionDto, userId));

        verify(walletRepository).findByUserId(userId);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void should_ReturnTopThreeCategoriesWithPercentages_When_MultipleCategoriesExist() {
        TopCategories top1 = new TopCategories(Category.FOOD, new BigDecimal("200.00"), 0);
        TopCategories top2 = new TopCategories(Category.TRANSPORT, new BigDecimal("150.00"), 0);
        TopCategories top3 = new TopCategories(Category.UTILITIES, new BigDecimal("100.00"), 0);
        List<TopCategories> rawTop = Arrays.asList(top1, top2, top3, 
                new TopCategories(Category.ENTERTAINMENT, new BigDecimal("50.00"), 0));

        when(transactionRepository.topCategories(walletId)).thenReturn(rawTop);

        List<TopCategories> result = transactionService.getTopCategories(walletId);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(Category.FOOD, result.get(0).getCategory());
        assertTrue(result.get(0).getPercent() > 0);
        verify(transactionRepository).topCategories(walletId);
    }

    @Test
    void should_ReturnEmptyList_When_NoCategoriesExist() {
        when(transactionRepository.topCategories(walletId)).thenReturn(Collections.emptyList());

        List<TopCategories> result = transactionService.getTopCategories(walletId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(transactionRepository).topCategories(walletId);
    }

    @Test
    void should_ReturnAllExpenseCategories_When_Requested() {
        List<TopCategories> categories = Arrays.asList(
                new TopCategories(Category.FOOD, new BigDecimal("200.00"), 0)
        );
        when(transactionRepository.topCategories(walletId)).thenReturn(categories);

        List<TopCategories> result = transactionService.getAllExpenseCategories(walletId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(transactionRepository).topCategories(walletId);
    }

    @Test
    void should_ReturnCurrentMonthTransactionsOnly_When_MultipleMonthsExist() {
        LocalDateTime now = LocalDateTime.now();
        Transaction currentMonthTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("50.00"))
                .date(now)
                .type(Type.EXPENSE)
                .category(Category.FOOD)
                .description("Current month")
                .wallet(testWallet)
                .build();

        Transaction oldTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("30.00"))
                .date(now.minusMonths(2))
                .type(Type.EXPENSE)
                .category(Category.FOOD)
                .description("Old transaction")
                .wallet(testWallet)
                .build();

        testWallet.getTransactions().add(currentMonthTransaction);
        testWallet.getTransactions().add(oldTransaction);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));

        List<Transaction> result = transactionService.getCurrentMonthTransactions(walletId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(currentMonthTransaction, result.get(0));
        verify(walletRepository).findById(walletId);
    }

    @Test
    void should_ThrowCustomException_When_WalletNotFoundForCurrentMonthTransactions() {
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());


        assertThrows(CustomException.class, 
                () -> transactionService.getCurrentMonthTransactions(walletId));

        verify(walletRepository).findById(walletId);
    }

    @Test
    void should_ReturnCategoryTotalsForMonth_When_MultipleTransactionsExist() {
        YearMonth yearMonth = YearMonth.now();
        LocalDateTime now = LocalDateTime.now();
        
        Transaction transaction1 = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .date(now)
                .type(Type.EXPENSE)
                .category(Category.FOOD)
                .wallet(testWallet)
                .build();

        Transaction transaction2 = Transaction.builder()
                .amount(new BigDecimal("50.00"))
                .date(now)
                .type(Type.EXPENSE)
                .category(Category.FOOD)
                .wallet(testWallet)
                .build();

        Transaction transaction3 = Transaction.builder()
                .amount(new BigDecimal("75.00"))
                .date(now)
                .type(Type.EXPENSE)
                .category(Category.TRANSPORT)
                .wallet(testWallet)
                .build();

        testWallet.getTransactions().add(transaction1);
        testWallet.getTransactions().add(transaction2);
        testWallet.getTransactions().add(transaction3);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));

        Map<Category, BigDecimal> result = transactionService.getCategoryTotalsForMonth(walletId, yearMonth);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(new BigDecimal("150.00"), result.get(Category.FOOD));
        assertEquals(new BigDecimal("75.00"), result.get(Category.TRANSPORT));
        verify(walletRepository).findById(walletId);
    }

    @Test
    void should_ReturnTotalExpensesForCurrentMonth_When_ExpenseTransactionsExist() {
        LocalDateTime now = LocalDateTime.now();
        Transaction expense1 = Transaction.builder()
                .amount(new BigDecimal("50.00"))
                .date(now)
                .type(Type.EXPENSE)
                .category(Category.FOOD)
                .wallet(testWallet)
                .build();

        Transaction expense2 = Transaction.builder()
                .amount(new BigDecimal("30.00"))
                .date(now)
                .type(Type.EXPENSE)
                .category(Category.TRANSPORT)
                .wallet(testWallet)
                .build();

        Transaction income = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .date(now)
                .type(Type.INCOME)
                .category(Category.OTHER)
                .wallet(testWallet)
                .build();

        testWallet.getTransactions().add(expense1);
        testWallet.getTransactions().add(expense2);
        testWallet.getTransactions().add(income);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));

        BigDecimal result = transactionService.getTotalExpensesForCurrentMonth(walletId);

        assertNotNull(result);
        assertEquals(new BigDecimal("80.00"), result);
        verify(walletRepository).findById(walletId);
    }

    @Test
    void should_ReturnZero_When_NoExpensesForCurrentMonth() {
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));

        BigDecimal result = transactionService.getTotalExpensesForCurrentMonth(walletId);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result);
        verify(walletRepository).findById(walletId);
    }

    @Test
    void should_ReturnTotalExpensesForSpecificMonth_When_ExpensesExist() {
        YearMonth yearMonth = YearMonth.now();
        LocalDateTime now = LocalDateTime.now();
        
        Transaction expense = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .date(now)
                .type(Type.EXPENSE)
                .category(Category.FOOD)
                .wallet(testWallet)
                .build();

        testWallet.getTransactions().add(expense);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));

        BigDecimal result = transactionService.getTotalExpensesForMonth(walletId, yearMonth);

        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result);
        verify(walletRepository).findById(walletId);
    }

    @Test
    void should_ReturnTotalIncomeForCurrentMonth_When_IncomeTransactionsExist() {
        LocalDateTime now = LocalDateTime.now();
        Transaction income1 = Transaction.builder()
                .amount(new BigDecimal("500.00"))
                .date(now)
                .type(Type.INCOME)
                .category(Category.OTHER)
                .wallet(testWallet)
                .build();

        Transaction income2 = Transaction.builder()
                .amount(new BigDecimal("300.00"))
                .date(now)
                .type(Type.INCOME)
                .category(Category.OTHER)
                .wallet(testWallet)
                .build();

        Transaction expense = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .date(now)
                .type(Type.EXPENSE)
                .category(Category.FOOD)
                .wallet(testWallet)
                .build();

        testWallet.getTransactions().add(income1);
        testWallet.getTransactions().add(income2);
        testWallet.getTransactions().add(expense);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));

        BigDecimal result = transactionService.getTotalIncomeForCurrentMonth(walletId);

        assertNotNull(result);
        assertEquals(new BigDecimal("800.00"), result);
        verify(walletRepository).findById(walletId);
    }

    @Test
    void should_ReturnBiggestExpense_When_MultipleExpensesExist() {
        LocalDateTime now = LocalDateTime.now();
        Transaction expense1 = Transaction.builder()
                .amount(new BigDecimal("50.00"))
                .date(now)
                .type(Type.EXPENSE)
                .category(Category.FOOD)
                .wallet(testWallet)
                .build();

        Transaction expense2 = Transaction.builder()
                .amount(new BigDecimal("150.00"))
                .date(now)
                .type(Type.EXPENSE)
                .category(Category.TRANSPORT)
                .wallet(testWallet)
                .build();

        testWallet.getTransactions().add(expense1);
        testWallet.getTransactions().add(expense2);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));

        Transaction result = transactionService.getBiggestExpenseForCurrentMonth(walletId);

        assertNotNull(result);
        assertEquals(new BigDecimal("150.00"), result.getAmount());
        assertEquals(Category.TRANSPORT, result.getCategory());
        verify(walletRepository).findById(walletId);
    }

    @Test
    void should_ReturnNull_When_NoExpensesForCurrentMonth() {
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));

        Transaction result = transactionService.getBiggestExpenseForCurrentMonth(walletId);

        assertNull(result);
        verify(walletRepository).findById(walletId);
    }

    @Test
    void should_ReturnFormattedCategoryName_When_BiggestExpenseExists() {
        LocalDateTime now = LocalDateTime.now();
        Transaction biggestExpense = Transaction.builder()
                .amount(new BigDecimal("200.00"))
                .date(now)
                .type(Type.EXPENSE)
                .category(Category.ENTERTAINMENT)
                .wallet(testWallet)
                .build();

        testWallet.getTransactions().add(biggestExpense);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));

        String result = transactionService.getBiggestExpenseCategoryName(walletId);

        assertNotNull(result);
        assertEquals("Entertainment", result);
        verify(walletRepository).findById(walletId);
    }

    @Test
    void should_ReturnNull_When_NoExpensesForCategoryName() {
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));

        String result = transactionService.getBiggestExpenseCategoryName(walletId);

        assertNull(result);
        verify(walletRepository).findById(walletId);
    }

    @Test
    void should_ReturnNull_When_CategoryIsNull() {
        LocalDateTime now = LocalDateTime.now();
        Transaction expense = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .date(now)
                .type(Type.EXPENSE)
                .category(null)
                .wallet(testWallet)
                .build();

        testWallet.getTransactions().add(expense);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));

        String result = transactionService.getBiggestExpenseCategoryName(walletId);

        assertNull(result);
        verify(walletRepository).findById(walletId);
    }

    @Test
    void should_ReturnExpenseHistoryByDay_When_ExpensesExist() {
        LocalDateTime now = LocalDateTime.now();
        Transaction expense1 = Transaction.builder()
                .amount(new BigDecimal("50.00"))
                .date(now)
                .type(Type.EXPENSE)
                .category(Category.FOOD)
                .wallet(testWallet)
                .build();

        Transaction expense2 = Transaction.builder()
                .amount(new BigDecimal("30.00"))
                .date(now)
                .type(Type.EXPENSE)
                .category(Category.TRANSPORT)
                .wallet(testWallet)
                .build();

        testWallet.getTransactions().add(expense1);
        testWallet.getTransactions().add(expense2);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));

        Map<String, BigDecimal> result = transactionService.getExpenseHistoryByDay(walletId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(walletRepository).findById(walletId);
    }

    @Test
    void should_ReturnCategoryNamesSortedByAmount_When_ExpensesExist() {
        LocalDateTime now = LocalDateTime.now();
        Transaction expense1 = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .date(now)
                .type(Type.EXPENSE)
                .category(Category.FOOD)
                .wallet(testWallet)
                .build();

        Transaction expense2 = Transaction.builder()
                .amount(new BigDecimal("50.00"))
                .date(now)
                .type(Type.EXPENSE)
                .category(Category.TRANSPORT)
                .wallet(testWallet)
                .build();

        testWallet.getTransactions().add(expense1);
        testWallet.getTransactions().add(expense2);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));

        List<String> result = transactionService.getCategoryNamesForCurrentMonth(walletId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("FOOD", result.get(0));
        verify(walletRepository, atLeastOnce()).findById(walletId);
    }

    @Test
    void should_ReturnEmptyList_When_NoExpensesForCategoryNames() {
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));

        List<String> result = transactionService.getCategoryNamesForCurrentMonth(walletId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(walletRepository, atLeastOnce()).findById(walletId);
    }

    @Test
    void should_ReturnCategoryPercentages_When_ExpensesExist() {
        LocalDateTime now = LocalDateTime.now();
        Transaction expense1 = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .date(now)
                .type(Type.EXPENSE)
                .category(Category.FOOD)
                .wallet(testWallet)
                .build();

        Transaction expense2 = Transaction.builder()
                .amount(new BigDecimal("50.00"))
                .date(now)
                .type(Type.EXPENSE)
                .category(Category.TRANSPORT)
                .wallet(testWallet)
                .build();

        testWallet.getTransactions().add(expense1);
        testWallet.getTransactions().add(expense2);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));

        List<Integer> result = transactionService.getCategoryPercentsForCurrentMonth(walletId);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.get(0) > result.get(1));
        verify(walletRepository, atLeastOnce()).findById(walletId);
    }

    @Test
    void should_ReturnEmptyList_When_NoExpensesForCategoryPercentages() {
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));

        List<Integer> result = transactionService.getCategoryPercentsForCurrentMonth(walletId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(walletRepository, atLeastOnce()).findById(walletId);
    }

    @Test
    void should_ReturnCategoryAmountsSortedByValue_When_ExpensesExist() {
        LocalDateTime now = LocalDateTime.now();
        Transaction expense1 = Transaction.builder()
                .amount(new BigDecimal("100.00"))
                .date(now)
                .type(Type.EXPENSE)
                .category(Category.FOOD)
                .wallet(testWallet)
                .build();

        Transaction expense2 = Transaction.builder()
                .amount(new BigDecimal("50.00"))
                .date(now)
                .type(Type.EXPENSE)
                .category(Category.TRANSPORT)
                .wallet(testWallet)
                .build();

        testWallet.getTransactions().add(expense1);
        testWallet.getTransactions().add(expense2);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(testWallet));

        List<BigDecimal> result = transactionService.getCategoryAmountsForCurrentMonth(walletId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(new BigDecimal("100.00"), result.get(0));
        assertEquals(new BigDecimal("50.00"), result.get(1));
        verify(walletRepository, atLeastOnce()).findById(walletId);
    }

    @Test
    void should_DeleteTransaction_When_UserIsAuthorized() {
        UUID transactionId = testTransaction.getId();
        testWallet.getTransactions().add(testTransaction);
        
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        assertDoesNotThrow(() -> transactionService.deleteTransaction(transactionId, userId));

        assertFalse(testWallet.getTransactions().contains(testTransaction));
        verify(transactionRepository).findById(transactionId);
        verify(walletRepository).save(testWallet);
    }

    @Test
    void should_ThrowIllegalArgumentException_When_TransactionNotFound() {
        UUID transactionId = UUID.randomUUID();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());


        assertThrows(IllegalArgumentException.class, 
                () -> transactionService.deleteTransaction(transactionId, userId));

        verify(transactionRepository).findById(transactionId);
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void should_ThrowSecurityException_When_WalletIsNull() {
        UUID transactionId = testTransaction.getId();
        testTransaction.setWallet(null);
        
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));


        assertThrows(SecurityException.class, 
                () -> transactionService.deleteTransaction(transactionId, userId));

        verify(transactionRepository).findById(transactionId);
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void should_ThrowSecurityException_When_UserIsNull() {
        UUID transactionId = testTransaction.getId();
        testWallet.setUser(null);
        testTransaction.setWallet(testWallet);
        
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));


        assertThrows(SecurityException.class, 
                () -> transactionService.deleteTransaction(transactionId, userId));

        verify(transactionRepository).findById(transactionId);
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void should_ThrowSecurityException_When_DifferentUserTriesToDelete() {
        UUID transactionId = testTransaction.getId();
        UUID differentUserId = UUID.randomUUID();
        testTransaction.setWallet(testWallet);
        
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));


        assertThrows(SecurityException.class, 
                () -> transactionService.deleteTransaction(transactionId, differentUserId));

        verify(transactionRepository).findById(transactionId);
        verify(walletRepository, never()).save(any(Wallet.class));
    }
}
