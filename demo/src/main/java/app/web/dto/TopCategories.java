package app.web.dto;

import app.transactions.model.Category;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class TopCategories {

    private Category category;
    private BigDecimal totalAmount;
    private int percent;

    public TopCategories(Category category, BigDecimal totalAmount, int percent) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.percent = percent;
    }
}
