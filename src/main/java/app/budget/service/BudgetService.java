package app.budget.service;

import app.budget.model.Budget;
import app.budget.repository.BudgetRepository;
import app.exception.UserNotFoundException;
import app.transactions.model.Category;
import app.transactions.service.TransactionService;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.BudgetDto;
import app.web.dto.BudgetInfo;
import app.web.dto.BudgetPageData;
import app.wallet.model.Wallet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BudgetService {
    
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;
    private final UserService userService;
    
    public BudgetService(BudgetRepository budgetRepository, 
                        UserRepository userRepository,
                        TransactionService transactionService,
                        UserService userService) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
        this.transactionService = transactionService;
        this.userService = userService;
    }
    
    public Budget createOrUpdateBudget(UUID userId, BudgetDto budgetDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        Optional<Budget> existingBudget = budgetRepository.findByUserAndCategoryAndYearAndMonth(
            user, budgetDto.getCategory(), budgetDto.getYear(), budgetDto.getMonth()
        );
        
        if (existingBudget.isPresent()) {
            Budget budget = existingBudget.get();
            budget.setAmount(budgetDto.getAmount());
            return budgetRepository.save(budget);
        } else {
            Budget budget = Budget.builder()
                    .user(user)
                    .category(budgetDto.getCategory())
                    .amount(budgetDto.getAmount())
                    .year(budgetDto.getYear())
                    .month(budgetDto.getMonth())
                    .build();
            return budgetRepository.save(budget);
        }
    }
    
    public List<Budget> getBudgetsForMonth(UUID userId, YearMonth yearMonth) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        return budgetRepository.findByUserAndYearAndMonth(
            user, yearMonth.getYear(), yearMonth.getMonthValue()
        );
    }
    
    public void deleteBudget(UUID budgetId, UUID userId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found"));
        
        if (!budget.getUser().getId().equals(userId)) {
            throw new SecurityException("You are not authorized to delete this budget");
        }
        
        budgetRepository.delete(budget);
    }
    
    public Map<Category, BudgetInfo> getBudgetInfoForMonth(UUID userId, YearMonth yearMonth) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        Wallet wallet = user.getWallet();
        if (wallet == null) {
            return new HashMap<>();
        }
        
        List<Budget> budgets = getBudgetsForMonth(userId, yearMonth);
        Map<Category, BigDecimal> categoryExpenses = transactionService.getCategoryTotalsForMonth(wallet.getId(), yearMonth);
        
        Map<Category, BudgetInfo> budgetInfoMap = new HashMap<>();
        
        for (Budget budget : budgets) {
            BigDecimal spent = categoryExpenses.getOrDefault(budget.getCategory(), BigDecimal.ZERO);
            BigDecimal remaining = budget.getAmount().subtract(spent);
            BigDecimal percentage = budget.getAmount().compareTo(BigDecimal.ZERO) > 0
                    ? spent.multiply(BigDecimal.valueOf(100))
                            .divide(budget.getAmount(), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            
            boolean isOverBudget = spent.compareTo(budget.getAmount()) > 0;
            
            budgetInfoMap.put(budget.getCategory(), BudgetInfo.builder()
                    .budget(budget)
                    .spent(spent)
                    .remaining(remaining)
                    .percentage(percentage)
                    .isOverBudget(isOverBudget)
                    .build());
        }
        
        return budgetInfoMap;
    }
    
    public BudgetPageData getBudgetPageData(UUID userId, Integer month, Integer year) {
        YearMonth currentMonth = YearMonth.now();
        if (month != null && year != null) {
            currentMonth = YearMonth.of(year, month);
        }
        
        User user = userService.getById(userId);
        
        List<Budget> budgets = getBudgetsForMonth(userId, currentMonth);
        Map<Category, BudgetInfo> budgetInfo = getBudgetInfoForMonth(userId, currentMonth);
        BigDecimal totalBudget = getTotalBudgetForMonth(userId, currentMonth);
        BigDecimal totalSpent = getTotalSpentForMonth(userId, currentMonth);
        BigDecimal totalRemaining = totalBudget.subtract(totalSpent);
        
        List<Category> allCategories = Arrays.asList(Category.values());
        Set<Category> categoriesWithBudgets = budgets.stream()
                .map(Budget::getCategory)
                .collect(Collectors.toSet());
        
        return BudgetPageData.builder()
                .user(user)
                .budgets(budgets)
                .budgetInfo(budgetInfo)
                .allCategories(allCategories)
                .categoriesWithBudgets(categoriesWithBudgets)
                .totalBudget(totalBudget)
                .totalSpent(totalSpent)
                .totalRemaining(totalRemaining)
                .currentMonth(currentMonth)
                .currentMonthName(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")))
                .previousMonth(currentMonth.minusMonths(1))
                .nextMonth(currentMonth.plusMonths(1))
                .build();
    }
    
    public BigDecimal getTotalBudgetForMonth(UUID userId, YearMonth yearMonth) {
        List<Budget> budgets = getBudgetsForMonth(userId, yearMonth);
        return budgets.stream()
                .map(Budget::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal getTotalSpentForMonth(UUID userId, YearMonth yearMonth) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        Wallet wallet = user.getWallet();
        if (wallet == null) {
            return BigDecimal.ZERO;
        }
        
        return transactionService.getTotalExpensesForMonth(wallet.getId(), yearMonth);
    }
    
}

