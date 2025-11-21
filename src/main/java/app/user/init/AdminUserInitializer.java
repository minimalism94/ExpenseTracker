package app.user.init;

import app.notification.service.NotificationService;
import app.subscription.service.SubscriptionsService;
import app.user.model.Country;
import app.user.model.Role;
import app.user.model.User;
import app.user.model.UserVersion;
import app.user.repository.UserRepository;
import app.wallet.service.WalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@Slf4j
public class AdminUserInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;
    private final SubscriptionsService subscriptionsService;
    private final NotificationService notificationService;

    public AdminUserInitializer(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                WalletService walletService,
                                SubscriptionsService subscriptionsService,
                                NotificationService notificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletService = walletService;
        this.subscriptionsService = subscriptionsService;
        this.notificationService = notificationService;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(1)
    @Transactional
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

            log.info("Default admin user created successfully - Username: '{}', Password: '{}', Email: '{}'", 
                    username, password, email);
        } else {
            log.debug("Database already contains users. Skipping admin user initialization.");
        }
    }
}

