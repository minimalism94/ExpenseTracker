package app.subscription.model;


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
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private  SubscriptionPeriod period;
    @Column(nullable = false)
    private LocalDate expiryOn;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionType type;
    @Column(nullable = false)
    private BigDecimal price;


    @ManyToOne
    private User user;

}
