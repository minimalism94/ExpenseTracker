package app.category.model;


import app.budget.model.Budget;
import app.transactions.model.Transaction;
import app.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Budget> budgets;
}