package app.subscription.service;

import app.exception.CustomException;
import app.exception.UserNotFoundException;
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
import app.web.dto.mapper.DtoMapper;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
                .name("Just Testing Subscription")
                .period(SubscriptionPeriod.MONTHLY)
                .expiryOn(LocalDate.now().plusMonths(1))
                .type(SubscriptionType.DEFAULT)
                .price(new BigDecimal("150"))
                        .build();

        subscriptionsRepository.save(subscription);

    }

    public List<Subscription> getByUsername(String username) {
        return subscriptionsRepository.findAllByUser_UsernameOrderByExpiryOnAsc(username).stream()
                .filter(s -> s.getPaidDate() == null)
                .collect(Collectors.toList());
    }

    public List<Subscription> getPaidSubscriptionsForCurrentMonth(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        YearMonth currentMonth = YearMonth.now();
        
        return user.getSubscriptions().stream()
                .filter(s -> s.getPaidDate() != null)
                .filter(s -> {
                    YearMonth paidMonth = YearMonth.from(s.getPaidDate());
                    return paidMonth.equals(currentMonth);
                })
                .sorted((s1, s2) -> s2.getPaidDate().compareTo(s1.getPaidDate()))
                .collect(Collectors.toList());
    }

    public void saveSubscription(@Valid SubscriptionDto dto, String name) {
        User user = userRepository.findByUsername(name)
                .orElseThrow(() -> new UserNotFoundException(name));

        Subscription subscription = DtoMapper.mapSubscriptionDtoToEntity(dto, user);

        subscriptionsRepository.save(subscription);
    }

    @Transactional
    public void deleteById(UUID id) {
        Subscription subscription = subscriptionsRepository.findById(id)
                .orElseThrow(() -> new CustomException("Subscription not found"));

        User user = subscription.getUser();
        if (user != null) {
            subscription.setUser(null);
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
        
        subscription.setPaidDate(LocalDate.now());
        subscriptionsRepository.save(subscription);
        walletRepository.save(wallet);
    }

}
