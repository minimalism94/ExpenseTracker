package app.scheduler;

import app.scheduler.config.CronExpressions;
import app.subscription.model.Subscription;
import app.subscription.repository.SubscriptionsRepository;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class SubscriptionScheduler {

    private static final String DEFAULT_SUBSCRIPTION_SUBJECT = "Expire subscription ";

    private final SubscriptionsRepository subscriptionsRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public SubscriptionScheduler(SubscriptionsRepository subscriptionsRepository, UserRepository userRepository,
                                 NotificationService notificationService) {
        this.subscriptionsRepository = subscriptionsRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = CronExpressions.DAILY_AT_9AM)
    @Transactional
    public void notifyExpiringSubscriptions() {
        log.info("Starting expiring subscriptions notification...");
        
        try {
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

                notificationService.send(user.getId(), subject, bodyBuilder.toString());
            }
            
            log.info("Completed expiring subscriptions notification");
        } catch (Exception e) {
            log.error("Error in notifyExpiringSubscriptions", e);
        }
    }
}

