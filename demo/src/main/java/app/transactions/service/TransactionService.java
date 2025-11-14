package app.transactions.service;

import app.exception.CustomException;
import app.transactions.model.Category;
import app.transactions.model.Transaction;
import app.transactions.model.Type;
import app.transactions.repository.TransactionRepository;
import app.user.model.User;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.wallet.repository.WalletRepository;
import app.web.dto.TopCategories;
import app.web.dto.TransactionDto;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service

public class TransactionService {


    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;


    @Autowired
    public TransactionService(TransactionRepository transactionRepository, WalletRepository walletRepository) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
    }

    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public List<Transaction> findAll() {
        return transactionRepository.findAll(Sort.by(Sort.Direction.DESC, "date"));
    }


    public void processTransaction(TransactionDto dto, UUID userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException("Wallet not found"));

        BigDecimal amount = dto.getAmount();

        if (dto.getType() == Type.EXPENSE) {
            if (wallet.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException("Insufficient balance for this expense.");
            }
            wallet.setExpense(wallet.getExpense().add(amount));
            wallet.setBalance(wallet.getBalance().subtract(amount));
        } else if (dto.getType() == Type.INCOME) {
            wallet.setIncome(wallet.getIncome().add(amount));
            wallet.setBalance(wallet.getBalance().add(amount));
        }

        Transaction transaction = Transaction.builder()
                .amount(amount)
                .date(dto.getDate())
                .type(dto.getType())
                .category(dto.getCategory())
                .description(dto.getDescription())
                .wallet(wallet)
                .build();

        transactionRepository.save(transaction);
        walletRepository.save(wallet);
    }


    public List<TopCategories> getTopCategories(UUID walletId) {
        List<TopCategories> rawTop = transactionRepository.topCategories(walletId);


        List<TopCategories> top3 = rawTop.stream()
                .limit(3)
                .collect(Collectors.toList());

        BigDecimal total = calculateTotalAmount(top3);

        calculatePercents(top3, total);

        return top3;
    }

    public List<TopCategories> getAllExpenseCategories(UUID walletId) {
        return transactionRepository.topCategories(walletId);
    }

    public List<Transaction> getCurrentMonthTransactions(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new CustomException("Wallet not found"));
        
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.plusMonths(1).atDay(1).atStartOfDay();
        
        return wallet.getTransactions().stream()
                .filter(t -> !t.getDate().isBefore(monthStart) && t.getDate().isBefore(monthEnd))
                .sorted((t1, t2) -> t2.getDate().compareTo(t1.getDate()))
                .collect(Collectors.toList());
    }

    public List<Transaction> getCurrentMonthExpenseTransactions(UUID walletId) {
        return getCurrentMonthTransactions(walletId).stream()
                .filter(t -> t.getType() == Type.EXPENSE)
                .collect(Collectors.toList());
    }

    public Map<Category, BigDecimal> getCategoryTotals(UUID walletId) {
        List<Transaction> expenseTransactions = getCurrentMonthExpenseTransactions(walletId);
        Map<Category, BigDecimal> categoryTotals = new HashMap<>();
        
        for (Transaction t : expenseTransactions) {
            categoryTotals.merge(t.getCategory(), t.getAmount(), BigDecimal::add);
        }
        
        return categoryTotals;
    }

    public BigDecimal getTotalExpensesForCurrentMonth(UUID walletId) {
        return getCurrentMonthExpenseTransactions(walletId).stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalIncomeForCurrentMonth(UUID walletId) {
        return getCurrentMonthTransactions(walletId).stream()
                .filter(t -> t.getType() == Type.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Transaction getBiggestExpenseForCurrentMonth(UUID walletId) {
        return getCurrentMonthExpenseTransactions(walletId).stream()
                .max(Comparator.comparing(Transaction::getAmount))
                .orElse(null);
    }

    public String getBiggestExpenseCategoryName(UUID walletId) {
        Transaction biggestExpense = getBiggestExpenseForCurrentMonth(walletId);
        
        if (biggestExpense != null && biggestExpense.getCategory() != null) {
            String categoryName = biggestExpense.getCategory().name();
            return categoryName.substring(0, 1) + categoryName.substring(1).toLowerCase();
        }
        
        return null;
    }

    public Map<String, BigDecimal> getExpenseHistoryByDay(UUID walletId) {
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.plusMonths(1).atDay(1).atStartOfDay();
        
        List<Transaction> expenseTransactions = getCurrentMonthExpenseTransactions(walletId);
        Map<String, BigDecimal> expenseHistory = new LinkedHashMap<>();
        
        LocalDate startDate = monthStart.toLocalDate();
        LocalDate endDate = monthEnd.toLocalDate().minusDays(1);
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            LocalDateTime dayStart = currentDate.atStartOfDay();
            LocalDateTime dayEnd = currentDate.plusDays(1).atStartOfDay();
            
            BigDecimal dayExpenses = expenseTransactions.stream()
                    .filter(t -> !t.getDate().isBefore(dayStart) && t.getDate().isBefore(dayEnd))
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);
            expenseHistory.put(currentDate.format(formatter), dayExpenses);
            currentDate = currentDate.plusDays(1);
        }
        
        return expenseHistory;
    }

    public List<String> getCategoryNamesForCurrentMonth(UUID walletId) {
        Map<Category, BigDecimal> categoryTotals = getCategoryTotals(walletId);
        BigDecimal totalExpenses = getTotalExpensesForCurrentMonth(walletId);
        
        List<String> categoryNames = new ArrayList<>();
        
        if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
            categoryTotals.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .forEach(entry -> categoryNames.add(entry.getKey().getName()));
        }
        
        return categoryNames;
    }

    public List<Integer> getCategoryPercentsForCurrentMonth(UUID walletId) {
        Map<Category, BigDecimal> categoryTotals = getCategoryTotals(walletId);
        BigDecimal totalExpenses = getTotalExpensesForCurrentMonth(walletId);
        
        List<Integer> categoryPercents = new ArrayList<>();
        
        if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
            categoryTotals.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .forEach(entry -> {
                        int percent = entry.getValue()
                                .multiply(BigDecimal.valueOf(100))
                                .divide(totalExpenses, 0, RoundingMode.HALF_UP)
                                .intValue();
                        categoryPercents.add(percent);
                    });
        }
        
        return categoryPercents;
    }

    public List<BigDecimal> getCategoryAmountsForCurrentMonth(UUID walletId) {
        Map<Category, BigDecimal> categoryTotals = getCategoryTotals(walletId);
        List<BigDecimal> categoryAmounts = new ArrayList<>();
        
        categoryTotals.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .forEach(entry -> categoryAmounts.add(entry.getValue()));
        
        return categoryAmounts;
    }

    private BigDecimal calculateTotalAmount(List<TopCategories> categories) {
        return categories.stream()
                .map(TopCategories::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void calculatePercents(List<TopCategories> categories, BigDecimal total) {
        for (TopCategories dto : categories) {
            int percent = total.compareTo(BigDecimal.ZERO) > 0
                    ? dto.getTotalAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(total, 0, RoundingMode.HALF_UP)
                    .intValue()
                    : 0;
            dto.setPercent(percent);
        }

    }

    @Transactional
    public void deleteTransaction(UUID transactionId, UUID userId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        Wallet wallet = transaction.getWallet();
        if (wallet == null || wallet.getUser() == null || !wallet.getUser().getId().equals(userId)) {
            throw new SecurityException("You are not authorized to delete this transaction");
        }

        wallet.getTransactions().remove(transaction);
        walletRepository.save(wallet);
    }





}

