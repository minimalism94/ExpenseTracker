package app.security;

import app.user.model.Country;
import app.user.model.Role;
import app.user.model.User;
import app.user.model.UserVersion;
import app.user.repository.UserRepository;
import app.wallet.service.WalletService;
import app.subscription.service.SubscriptionsService;
import app.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final WalletService walletService;
    private final SubscriptionsService subscriptionsService;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserRepository userRepository,
                                   WalletService walletService,
                                   SubscriptionsService subscriptionsService,
                                   NotificationService notificationService,
                                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.walletService = walletService;
        this.subscriptionsService = subscriptionsService;
        this.notificationService = notificationService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");
        String providerId = (String) attributes.get("sub");
        
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    String[] nameParts = name != null ? name.split(" ", 2) : new String[]{"User", ""};
                    String username = email.split("@")[0] + "_" + UUID.randomUUID().toString().substring(0, 8);
                    
                    while (userRepository.findByUsername(username).isPresent()) {
                        username = email.split("@")[0] + "_" + UUID.randomUUID().toString().substring(0, 8);
                    }
                    
                    String oauthPassword = passwordEncoder.encode("OAUTH2_USER_" + UUID.randomUUID());
                    
                    User newUser = User.builder()
                            .username(username)
                            .email(email)
                            .firstName(nameParts[0])
                            .lastName(nameParts.length > 1 ? nameParts[1] : "")
                            .profilePicture(picture)
                            .password(oauthPassword)
                            .provider(provider)
                            .providerId(providerId)
                            .isActive(true)
                            .createdOn(LocalDateTime.now())
                            .updatedOn(LocalDateTime.now())
                            .role(Role.USER)
                            .country(Country.BULGARIA)
                            .userVersion(UserVersion.BASIC)
                            .monthlyReportEmailEnabled(false)
                            .build();
                    
                    newUser = userRepository.save(newUser);
                    walletService.createDefaultWallet(newUser);
                    subscriptionsService.createDefaultSubscription(newUser);
                    notificationService.upsertPreference(newUser.getId(), false, newUser.getEmail());
                    
                    log.info("New OAuth2 user created: {} via {}", email, provider);
                    return newUser;
                });
        
        if (user.getProvider() == null) {
            user.setProvider(provider);
            user.setProviderId(providerId);
            if (user.getProfilePicture() == null && picture != null) {
                user.setProfilePicture(picture);
            }
            userRepository.save(user);
        }
        
        Collection<? extends GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
        
        return new CustomOAuth2User(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                authorities,
                attributes
        );
    }
}

