package app.web;

import app.security.UserData;
import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionType;
import app.transactions.model.Category;
import app.transactions.service.TransactionService;
import app.user.model.Country;
import app.user.model.Role;
import app.user.model.User;
import app.user.model.UserVersion;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.wallet.service.WalletService;
import app.web.dto.TopCategories;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@Import(TestWebMvcConfig.class)
public class DashboardControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private WalletService walletService;

    @MockitoBean
    private TransactionService transactionService;
    @MockitoBean
    private UserRepository userRepository;

    @Test
    void getDashboardPage_shouldReturnDashboardView_withEmptyTopCategories() throws Exception {

        UUID userId = UUID.randomUUID();
        User mockUser = User.builder()
                .id(userId)
                .username("testUser")
                .password("testPassword")
                .email("test@example.com")
                .role(Role.USER)
                .userVersion(UserVersion.BASIC)
                .isActive(true)
                .build();

        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .name("Default")
                .income(new BigDecimal("1000.00"))
                .expense(new BigDecimal("200.00"))
                .balance(new BigDecimal("800.00"))
                .user(mockUser)
                .build();
        mockUser.setWallet(wallet);
        mockUser.setSubscriptions(Collections.emptyList());

        when(userService.getById(userId)).thenReturn(mockUser);
        when(transactionService.getTopCategories(wallet.getId())).thenReturn(Collections.emptyList());

        UserData userData = new UserData(
                userId, mockUser.getUsername(), mockUser.getPassword(), mockUser.getEmail(), mockUser.getRole(), mockUser.isActive());

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/dashboard")
                .with(user(userData));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("wallet"))
                .andExpect(model().attribute("topCategories", Collections.emptyList()))
                .andExpect(model().attribute("categoryNames", Collections.emptyList()))
                .andExpect(model().attribute("categoryPercents", Collections.emptyList()));
    }

    @Test
    void getDashboardPage_shouldReturnDashboardView_withEmptySubscriptions() throws Exception {

        UUID userId = UUID.randomUUID();
        User mockUser = User.builder()
                .id(userId)
                .username("testUser")
                .password("testPassword")
                .email("test@example.com")
                .role(Role.USER)
                .userVersion(UserVersion.BASIC)
                .isActive(true)
                .build();

        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .name("Default")
                .income(new BigDecimal("1000.00"))
                .expense(new BigDecimal("200.00"))
                .balance(new BigDecimal("800.00"))
                .user(mockUser)
                .build();
        mockUser.setWallet(wallet);
        mockUser.setSubscriptions(Collections.emptyList());

        List<TopCategories> topCategories = List.of(
                new TopCategories(Category.FOOD, new BigDecimal("200.00"), 50)
        );

        when(userService.getById(userId)).thenReturn(mockUser);
        when(transactionService.getTopCategories(wallet.getId())).thenReturn(topCategories);

        UserData userData = new UserData(
                userId, mockUser.getUsername(), mockUser.getPassword(), mockUser.getEmail(), mockUser.getRole(), mockUser.isActive());

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/dashboard")
                .with(user(userData));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("subscription"))
                .andExpect(model().attribute("subscription", Collections.emptyList()));
    }

    private User mockUser(UUID id) {

        return User.builder()
                .id(id)
                .username("testUser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .country(Country.BULGARIA)
                .role(Role.USER)
                .userVersion(UserVersion.BASIC)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    private Wallet mockWallet(UUID walletId, User user) {

        Wallet wallet = Wallet.builder()
                .id(walletId)
                .name("Default")
                .income(new BigDecimal("1000.00"))
                .expense(new BigDecimal("200.00"))
                .balance(new BigDecimal("800.00"))
                .transactions(Collections.emptyList())
                .build();
        wallet.setUser(user);
        return wallet;
    }

    private Subscription mockSubscription(String name, LocalDate paidDate, User user) {

        Subscription subscription = new Subscription();
        subscription.setId(UUID.randomUUID());
        subscription.setName(name);
        subscription.setPrice(new BigDecimal("15.99"));
        subscription.setPeriod(SubscriptionPeriod.MONTHLY);
        subscription.setType(SubscriptionType.DEFAULT);
        subscription.setExpiryOn(LocalDate.now().plusDays(10));
        subscription.setPaidDate(paidDate);
        subscription.setUser(user);
        return subscription;
    }
}
