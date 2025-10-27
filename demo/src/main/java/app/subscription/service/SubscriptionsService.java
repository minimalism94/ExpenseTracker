package app.subscription.service;

import app.exception.CustomException;
import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionType;
import app.subscription.repository.SubscriptionsRepository;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.EditSubscriptionDto;
import app.web.dto.SubscriptionDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionsService {

    private final SubscriptionsRepository subscriptionsRepository;
    private final UserRepository userRepository;

    @Autowired
    public SubscriptionsService(SubscriptionsRepository subscriptionsRepository, UserRepository userRepository) {
        this.subscriptionsRepository = subscriptionsRepository;
        this.userRepository = userRepository;
    }

    public void createDefaultSubscription(User user) {
        Subscription subscription = Subscription.builder()
                .user(user)
                .name("Figma")
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

    public void deleteById(UUID id) {
        Subscription subscription = subscriptionsRepository.findById(id)
                .orElseThrow(() -> new CustomException("Subscription not found"));

        subscriptionsRepository.delete(subscription);
    }
}
