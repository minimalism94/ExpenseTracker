package app.budget.model;

import app.transactions.model.Category;
import app.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "budgets", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "category", "month", "year"})
})
public class Budget {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)

    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private int month;
    
    @Column(nullable = false)
    private int year;
    
    public YearMonth getYearMonth() {
        return YearMonth.of(year, month);
    }
    
    public void setYearMonth(YearMonth yearMonth) {
        this.year = yearMonth.getYear();
        this.month = yearMonth.getMonthValue();
    }
}

