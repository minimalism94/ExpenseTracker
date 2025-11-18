package app.user.service;

import app.confg.BeanConfiguration;
import app.confg.SecurityConfig;
import app.exception.CustomException;
import app.notification.service.NotificationService;
import app.security.UserData;
import app.subscription.model.Subscription;
import app.subscription.service.SubscriptionsService;
import app.user.model.Country;
import app.user.model.Role;
import app.user.model.User;
import app.user.repository.UserRepository;
import app.wallet.service.WalletService;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import app.web.dto.UserEditRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Slf4j
@Service


public class UserService implements UserDetailsService {



    private final UserRepository userRepository;

    private final WalletService walletService;
    private final SubscriptionsService subscriptionsService;
    private final SecurityConfig securityConfig;
    private final BeanConfiguration beanConfiguration;
    private final NotificationService notificationService;

    @Autowired
    public UserService(UserRepository userRepository, BeanConfiguration beanConfiguration, WalletService walletService, SubscriptionsService subscriptionsService, SecurityConfig securityConfig, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.walletService = walletService;
        this.subscriptionsService = subscriptionsService;
        this.securityConfig = securityConfig;
        this.beanConfiguration = beanConfiguration;
        this.notificationService = notificationService;
    }



    @Transactional
    public void register( RegisterRequest registerRequest) {
        Optional<User> optionalUser = userRepository.findByUsername(registerRequest.getUsername());
        if (optionalUser.isPresent()) {
            throw new CustomException("User with %s already exists".formatted(registerRequest.getUsername()));
        }
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(beanConfiguration.passwordEncoder().encode(registerRequest.getPassword()))
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .country(registerRequest.getCountry())
                .role(Role.USER)
                .userVersion(app.user.model.UserVersion.BASIC)
                .build();
        user = userRepository.save(user);
        walletService.createDefaultWallet(user);
        subscriptionsService.createDefaultSubscription(user);
        log.info("User [%s] registered successfully".formatted(user.getUsername()));
        notificationService.upsertPreference(user.getId(), false, user.getEmail());
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Username not found"));

        return new UserData(user.getId(), user.getUsername(), user.getPassword(), user.getEmail(), user.getRole(), user.isActive());
    }

    public User getById(UUID id) {

        Optional<User> user = userRepository.findById(id);

        user.orElseThrow(() -> new CustomException("User with id [%s] does not exist.".formatted(id)));

        return user.get();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void setRole(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new CustomException("User not found"));
        if (user.getRole() == Role.ADMIN) {
            user.setRole(Role.USER);
        } else {
            user.setRole(Role.ADMIN);
        }
        userRepository.save(user);
    }

    public void delete(UUID id) {
        userRepository.deleteById(id);
    }

    public void setActive(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new CustomException("User not found"));
        user.setActive(!user.isActive());
        userRepository.save(user);
    }


        public void editUserDetails(UUID id, UserEditRequest dto) {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new CustomException("User not found with id [%s]".formatted(id)));
                if (dto.getEmail() != null || dto.getEmail().isBlank()) {
                    notificationService.upsertPreference(user.getId(), true, user.getEmail());
                }else {
                    notificationService.upsertPreference(user.getId(), false, null);
                }
            user.setUsername(dto.getUsername());
            user.setFirstName(dto.getFirstName());
            user.setLastName(dto.getLastName());
            user.setEmail(dto.getEmail());
            user.setProfilePicture(dto.getProfilePicture());
            user.setCountry(dto.getCountry());
            user.setUpdatedOn(LocalDateTime.now());

            userRepository.save(user);
        }
    }


