package app.user.model;

import app.subscription.model.Subscription;
import app.transactions.model.Transaction;
import app.wallet.model.Wallet;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    private String firstName;

    private String lastName;

    private String profilePicture;


    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;
    private boolean isActive;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Country country = Country.BULGARIA;
    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER)
    private Wallet wallet;




    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<Subscription> subscriptions;

}
