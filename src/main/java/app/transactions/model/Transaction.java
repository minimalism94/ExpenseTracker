    package app.transactions.model;
    import app.user.model.User;
    import app.wallet.model.Wallet;
    import jakarta.persistence.*;
    import lombok.*;
    import java.math.BigDecimal;
    import java.time.LocalDate;
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
        private LocalDate date;
        @Enumerated(EnumType.STRING)
        private Type type;
        @Enumerated(EnumType.STRING)
        private Category categoryType;
        private String description;


        @ManyToOne
        private User user;

        @ManyToOne
        private Wallet wallet;


    }