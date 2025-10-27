    package app.transactions.model;
    import app.user.model.User;
    import app.wallet.model.Wallet;
    import jakarta.persistence.*;
    import lombok.*;
    import java.math.BigDecimal;
    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.util.UUID;

    @Entity
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class Transaction {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;
        @Column(nullable = false)
        private BigDecimal amount;
        @Column(nullable = false)
        private LocalDateTime date;
        @Column(nullable = false)
        @Enumerated(EnumType.STRING)
        private Type type;
        @Column(nullable = false)
        @Enumerated(EnumType.STRING)
        private Category category;
        @Column(nullable = false)
        private String description;


        @ManyToOne
        private User user;
// Трябва ли тук да имам релация само с user или и С WALLET
        @ManyToOne
        private Wallet wallet;


    }