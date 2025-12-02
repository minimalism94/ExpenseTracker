package app.user.init;

import app.notification.service.NotificationService;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionType;
import app.subscription.service.SubscriptionsService;
import app.transactions.model.Category;
import app.transactions.model.Type;
import app.transactions.service.TransactionService;
import app.user.model.Country;
import app.user.model.Role;
import app.user.model.User;
import app.user.model.UserVersion;
import app.user.repository.UserRepository;
import app.wallet.service.WalletService;
import app.web.dto.SubscriptionDto;
import app.web.dto.TransactionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
@ConditionalOnProperty(name = "app.admin.initializer.enabled", havingValue = "true", matchIfMissing = true)
public class AdminUserInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;
    private final SubscriptionsService subscriptionsService;
    private final NotificationService notificationService;
    private final TransactionService transactionService;

    public AdminUserInitializer(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                WalletService walletService,
                                SubscriptionsService subscriptionsService,
                                NotificationService notificationService,
                                TransactionService transactionService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletService = walletService;
        this.subscriptionsService = subscriptionsService;
        this.notificationService = notificationService;
        this.transactionService = transactionService;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(1)
    public void initializeAdminUser() {
        if (userRepository.count() == 0) {

            String username = "admin";
            String password = "admin";
            String email = "admin@gmail.com";

            User adminUser = User.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .firstName("Admin")
                    .lastName("User")
                    .isActive(true)
                    .createdOn(LocalDateTime.now())
                    .updatedOn(LocalDateTime.now())
                    .role(Role.ADMIN)
                    .country(Country.BULGARIA)
                    .userVersion(UserVersion.BASIC)
                    .monthlyReportEmailEnabled(false)
                    .build();

            adminUser = userRepository.save(adminUser);
            walletService.createDefaultWallet(adminUser);
            subscriptionsService.createDefaultSubscription(adminUser);
            notificationService.upsertPreference(adminUser.getId(), false, adminUser.getEmail());

            createDefaultSubscriptions(adminUser);
            createDefaultTransactions(adminUser);

            log.info("Default admin user created successfully - Username: '{}', Password: '{}', Email: '{}'",
                    username, password, email);
        } else {
            log.debug("Database already contains users. Skipping admin user initialization.");
        }
    }

    private void createDefaultSubscriptions(User user) {
        SubscriptionDto subscription1 = new SubscriptionDto();
        subscription1.setName("Netflix");
        subscription1.setPeriod(SubscriptionPeriod.MONTHLY);
        subscription1.setExpiryOn(LocalDate.now().plusMonths(1));
        subscription1.setType(SubscriptionType.DEFAULT);
        subscription1.setPrice(new BigDecimal("15.99"));
        subscriptionsService.saveSubscription(subscription1, user.getUsername());

        SubscriptionDto subscription2 = new SubscriptionDto();
        subscription2.setName("Spotify Premium");
        subscription2.setPeriod(SubscriptionPeriod.MONTHLY);
        subscription2.setExpiryOn(LocalDate.now().plusMonths(1));
        subscription2.setType(SubscriptionType.PREMIUM);
        subscription2.setPrice(new BigDecimal("9.99"));
        subscriptionsService.saveSubscription(subscription2, user.getUsername());

        SubscriptionDto subscription3 = new SubscriptionDto();
        subscription3.setName("Gym Membership");
        subscription3.setPeriod(SubscriptionPeriod.MONTHLY);
        subscription3.setExpiryOn(LocalDate.now().plusMonths(1));
        subscription3.setType(SubscriptionType.ULTIMATE);
        subscription3.setPrice(new BigDecimal("50.00"));
        subscriptionsService.saveSubscription(subscription3, user.getUsername());
    }

    private void createDefaultTransactions(User user) {
        UUID userId = user.getId();

        TransactionDto income1 = new TransactionDto();
        income1.setAmount(new BigDecimal("2000.00"));
        income1.setDate(LocalDateTime.now().minusDays(10));
        income1.setType(Type.INCOME);
        income1.setCategory(Category.OTHER);
        income1.setDescription("Monthly salary");
        transactionService.processTransaction(income1, userId);

        TransactionDto income2 = new TransactionDto();
        income2.setAmount(new BigDecimal("150.00"));
        income2.setDate(LocalDateTime.now().minusDays(8));
        income2.setType(Type.INCOME);
        income2.setCategory(Category.OTHER);
        income2.setDescription("Freelance work");
        transactionService.processTransaction(income2, userId);

        TransactionDto expense1 = new TransactionDto();
        expense1.setAmount(new BigDecimal("25.50"));
        expense1.setDate(LocalDateTime.now().minusDays(2));
        expense1.setType(Type.EXPENSE);
        expense1.setCategory(Category.FOOD);
        expense1.setDescription("Grocery shopping");
        transactionService.processTransaction(expense1, userId);

        TransactionDto expense2 = new TransactionDto();
        expense2.setAmount(new BigDecimal("45.00"));
        expense2.setDate(LocalDateTime.now().minusDays(5));
        expense2.setType(Type.EXPENSE);
        expense2.setCategory(Category.TRANSPORT);
        expense2.setDescription("Gas refill");
        transactionService.processTransaction(expense2, userId);

        TransactionDto expense3 = new TransactionDto();
        expense3.setAmount(new BigDecimal("120.00"));
        expense3.setDate(LocalDateTime.now().minusDays(7));
        expense3.setType(Type.EXPENSE);
        expense3.setCategory(Category.UTILITIES);
        expense3.setDescription("Electricity bill");
        transactionService.processTransaction(expense3, userId);
    }
}

