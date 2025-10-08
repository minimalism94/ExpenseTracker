package app.budget.model;

import app.category.model.Category;
import app.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private BigDecimal limit;
    private String period;

    @ManyToOne
    private User user;

    @ManyToOne
    private Category category;

    // Getters and setters
}
