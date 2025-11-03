package app.subscription.service;

import app.exception.CustomException;
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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SubscriptionsService {

    private final SubscriptionsRepository subscriptionsRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    @Autowired
    public SubscriptionsService(SubscriptionsRepository subscriptionsRepository, UserRepository userRepository, WalletRepository walletRepository) {
        this.subscriptionsRepository = subscriptionsRepository;
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
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

    @Transactional
    public void paySubscription(UUID subscriptionId, UUID userId) {
        Subscription subscription = subscriptionsRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
        User user = subscription.getUser();

        Wallet wallet = subscription.getUser().getWallet();
        if (wallet == null || wallet.getUser() == null || !wallet.getUser().getId().equals(userId)) {
            throw new CustomException("You are not authorized to pay this subscription");
        }
        BigDecimal amount = subscription.getPrice();

        if (wallet.getBalance().compareTo(amount) >= 0) {
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setExpense(wallet.getExpense().add(amount));} else {
            throw new CustomException("Insufficient balance for this subscription.");
        }
        walletRepository.save(wallet);
        //TODO по изчистен вариант
        user.getSubscriptions().remove(subscription);
        subscription.setUser(null);

    }
}
