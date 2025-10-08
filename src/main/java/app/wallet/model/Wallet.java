package app.wallet.model;

import app.transactions.model.Transaction;
import app.user.model.User;
import jakarta.persistence.*;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private BigDecimal balance;
    private String currency;

    @ManyToOne
    private User user;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL)
    private List<Transaction> transactions;
}
