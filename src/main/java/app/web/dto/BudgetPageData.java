package app.web.dto;

import app.budget.model.Budget;
import app.transactions.model.Category;
import app.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BudgetPageData {
    private User user;
    private List<Budget> budgets;
    private Map<Category, BudgetInfo> budgetInfo;
    private List<Category> allCategories;
    private Set<Category> categoriesWithBudgets;
    private BigDecimal totalBudget;
    private BigDecimal totalSpent;
    private BigDecimal totalRemaining;
    private YearMonth currentMonth;
    private String currentMonthName;
    private YearMonth previousMonth;
    private YearMonth nextMonth;
}

