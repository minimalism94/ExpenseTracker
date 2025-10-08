package app.subscription.model;

import app.user.model.User;

import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private BigDecimal amount;
    private LocalDate nextPaymentDate;
    private String frequency;

    @ManyToOne
    private User user;
}