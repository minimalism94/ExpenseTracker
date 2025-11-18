package app.wallet.model;

import app.transactions.model.Transaction;
import app.user.model.User;
import jakarta.persistence.*;

import lombok.*;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private BigDecimal income;
    @Column(nullable = false)
    private BigDecimal expense;
    @Column(nullable = false)
    private BigDecimal balance;
    @Column(nullable = false)
    private Currency currency;

    @OneToOne
    private User user;

    @OneToMany(mappedBy = "wallet", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Transaction> transactions;
}
