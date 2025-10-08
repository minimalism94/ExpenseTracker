package app.transactions.model;
import app.category.model.Category;
import app.user.model.User;
import app.wallet.model.Wallet;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;
    private LocalDateTime date;
    private String paymentMethod;
    private String status;

    @ManyToOne
    private User user;

    @ManyToOne
    private Wallet wallet;

    @ManyToOne
    private Category category;
}