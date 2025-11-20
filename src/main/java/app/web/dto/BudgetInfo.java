package app.web.dto;

import app.budget.model.Budget;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BudgetInfo {
    private Budget budget;
    private BigDecimal spent;
    private BigDecimal remaining;
    private BigDecimal percentage;
    private boolean isOverBudget;
}

