package app.subscription.service;

import app.exception.CustomException;
import app.notification.service.NotificationService;
import app.security.UserData;
import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionType;
import app.subscription.repository.SubscriptionsRepository;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.wallet.repository.WalletRepository;
import app.web.dto.EditSubscriptionDto;
import app.web.dto.SubscriptionDto;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SubscriptionsService {

    private static final String DEFAULT_SUBSCRIPTION_SUBJECT = "Expire subscription ";
    private static final String DEFAULT_SUBSCRIPTION_BODY = "You subscription %s will expire in 3 days, and you will need to pay %.2f BGN, ! ";

    private final SubscriptionsRepository subscriptionsRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final NotificationService notificationService;

    @Autowired
    public SubscriptionsService(SubscriptionsRepository subscriptionsRepository, UserRepository userRepository, WalletRepository walletRepository, NotificationService notificationService) {
        this.subscriptionsRepository = subscriptionsRepository;
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.notificationService = notificationService;
    }

    public void createDefaultSubscription(User user) {
        Subscription subscription = Subscription.builder()
                .user(user)
                .name("Just Testing Subscription")
                .period(SubscriptionPeriod.MONTHLY)
                .expiryOn(LocalDate.now().plusMonths(1))
                .type(SubscriptionType.DEFAULT)
                .price(new BigDecimal("150"))
                        .build();

        subscriptionsRepository.save(subscription);

    }

    public List<Subscription> getByUsername(String username) {
        return subscriptionsRepository.findAllByUser_UsernameOrderByExpiryOnAsc(username);
    }

    public void saveSubscription(@Valid SubscriptionDto dto, String name) {
        User user = userRepository.findByUsername(name)
                .orElseThrow(() -> new CustomException("User not found: " + name));

        Subscription subscription = new Subscription();
        subscription.setName(dto.getName());
        subscription.setPeriod(dto.getPeriod());
        subscription.setExpiryOn(dto.getExpiryOn());
        subscription.setType(dto.getType());
        subscription.setPrice(dto.getPrice());
        subscription.setUser(user);

        subscriptionsRepository.save(subscription);
    }

    @Transactional
    public void deleteById(UUID id) {
        Subscription subscription = subscriptionsRepository.findById(id)
                .orElseThrow(() -> new CustomException("Subscription not found"));

        User user = subscription.getUser();
        if (user != null) {
            user.removeSubscription(subscription);
            userRepository.save(user);
        }
        
        subscriptionsRepository.delete(subscription);
    }

    @Transactional
    public void paySubscription(UUID subscriptionId, UUID userId) {
        Subscription subscription = subscriptionsRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        User user = subscription.getUser();
        Wallet wallet = user.getWallet();

        if (wallet == null || wallet.getUser() == null || !wallet.getUser().getId().equals(userId)) {
            throw new CustomException("You are not authorized to pay this subscription");
        }

        BigDecimal amount = subscription.getPrice();
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new CustomException("Insufficient balance for this subscription.");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setExpense(wallet.getExpense().add(amount));
        
        // Mark subscription as paid (don't remove it, just set paidDate)
        subscription.setPaidDate(LocalDate.now());
        subscriptionsRepository.save(subscription);
        walletRepository.save(wallet);
    }
   // @Scheduled(cron = "0 * * * * *", zone = "Europe/Sofia")
    //@Scheduled(cron = "0 0 8 * * MON", zone = "Europe/Sofia")
    public void notifyExpiringSubscriptions() {
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(7);


        List<User> users = userRepository.findAll();

        for (User user : users) {

            List<Subscription> expiring = subscriptionsRepository
                    .findByUser_IdAndExpiryOnBeforeOrderByExpiryOn(user.getId(), limit);

            if (expiring.isEmpty()) {
                continue;
            }

            String subject = DEFAULT_SUBSCRIPTION_SUBJECT + user.getUsername();

            StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append("Здравей, ").append(user.getUsername()).append("!\n\n");
            bodyBuilder.append("Следните абонаменти ти изтичат тази седмица:\n");

            for (Subscription s : expiring) {
                bodyBuilder.append(String.format("- %s: %.2f BGN (изтича на %s)\n",
                        s.getName(), s.getPrice(), s.getExpiryOn()));
            }

            bodyBuilder.append("\nАко не желаете да получавате отново известие, моля влезте в профила си и деактивирайте услугата!");

            notificationService.send(user.getId() ,subject, bodyBuilder.toString());
        }
    }

}
