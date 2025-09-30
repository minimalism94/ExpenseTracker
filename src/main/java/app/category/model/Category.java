package app.category.model;


import app.subscription.model.SubscriptionStatus;
import app.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
    public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private User owner;
    @Column(nullable = false)
    private BigDecimal amount;
    @Column(nullable = false)
    private LocalDate date;
    @Column(nullable = true)
    private String note;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExpenseCategory category;
}
