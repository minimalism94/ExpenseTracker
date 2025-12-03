package app.web;

import app.security.UserData;
import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionType;
import app.subscription.service.SubscriptionsService;
import app.transactions.model.Category;
import app.transactions.model.Transaction;
import app.transactions.model.Type;
import app.transactions.service.TransactionService;
import app.user.model.Role;
import app.user.model.User;
import app.user.model.UserVersion;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.wallet.model.Wallet;
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
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@Import(TestWebMvcConfig.class)
public class ReportControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private SubscriptionsService subscriptionsService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getReportPage_shouldReturnReportView_withUserData() throws Exception {

        UUID userId = UUID.randomUUID();
        User mockUser = User.builder()
                .id(userId)
                .username("testUser")
                .password("testPassword")
                .role(Role.USER)
                .userVersion(UserVersion.PRO)
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

        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("50.00"))
                .date(LocalDateTime.now().minusDays(1))
                .type(Type.EXPENSE)
                .category(Category.FOOD)
                .description("Groceries")
                .wallet(wallet)
                .build();

        when(userService.getById(userId)).thenReturn(mockUser);
        when(transactionService.getCategoryNamesForCurrentMonth(wallet.getId())).thenReturn(List.of("Food", "Transport"));
        when(transactionService.getCategoryPercentsForCurrentMonth(wallet.getId())).thenReturn(List.of(50, 30));
        when(transactionService.getCategoryAmountsForCurrentMonth(wallet.getId())).thenReturn(List.of(new BigDecimal("100.00"), new BigDecimal("60.00")));
        when(transactionService.getCurrentMonthTransactions(wallet.getId())).thenReturn(List.of(transaction));
        when(transactionService.getBiggestExpenseForCurrentMonth(wallet.getId())).thenReturn(transaction);
        when(transactionService.getBiggestExpenseCategoryName(wallet.getId())).thenReturn("Food");
        when(transactionService.getTotalExpensesForCurrentMonth(wallet.getId())).thenReturn(new BigDecimal("200.00"));
        when(transactionService.getTotalIncomeForCurrentMonth(wallet.getId())).thenReturn(new BigDecimal("1000.00"));
        when(transactionService.getExpenseHistoryByDay(wallet.getId())).thenReturn(new HashMap<>());
        when(subscriptionsService.getPaidSubscriptionsForCurrentMonth(userId)).thenReturn(Collections.emptyList());

        UserData userData = new UserData(
                userId, mockUser.getUsername(), mockUser.getPassword(), mockUser.getEmail(), mockUser.getRole(), mockUser.isActive());

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/report")
                .with(user(userData));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("report"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("wallet"))
                .andExpect(model().attributeExists("categoryNames"))
                .andExpect(model().attributeExists("categoryPercents"))
                .andExpect(model().attributeExists("categoryAmounts"))
                .andExpect(model().attributeExists("allTransactions"))
                .andExpect(model().attributeExists("biggestExpense"))
                .andExpect(model().attributeExists("biggestExpenseName"))
                .andExpect(model().attributeExists("currentMonthExpenses"))
                .andExpect(model().attributeExists("currentMonthIncome"))
                .andExpect(model().attributeExists("expenseHistory"))
                .andExpect(model().attributeExists("paidSubscriptions"));
    }

    @Test
    void getReportPage_shouldRedirectToUpgrade_whenBasicUser() throws Exception {

        UUID userId = UUID.randomUUID();
        User mockUser = User.builder()
                .id(userId)
                .username("testUser")
                .password("testPassword")
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

        when(userService.getById(userId)).thenReturn(mockUser);

        UserData userData = new UserData(
                userId, mockUser.getUsername(), mockUser.getPassword(), mockUser.getEmail(), mockUser.getRole(), mockUser.isActive());

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/report")
                .with(user(userData));

        mockMvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/upgrade"));
    }

    @Test
    void getReportPage_shouldReturnReportView_withEmptyData() throws Exception {

        UUID userId = UUID.randomUUID();
        User mockUser = User.builder()
                .id(userId)
                .username("testUser")
                .password("testPassword")
                .role(Role.USER)
                .userVersion(UserVersion.PRO)
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

        when(userService.getById(userId)).thenReturn(mockUser);
        when(transactionService.getCategoryNamesForCurrentMonth(wallet.getId())).thenReturn(Collections.emptyList());
        when(transactionService.getCategoryPercentsForCurrentMonth(wallet.getId())).thenReturn(Collections.emptyList());
        when(transactionService.getCategoryAmountsForCurrentMonth(wallet.getId())).thenReturn(Collections.emptyList());
        when(transactionService.getCurrentMonthTransactions(wallet.getId())).thenReturn(Collections.emptyList());
        when(transactionService.getBiggestExpenseForCurrentMonth(wallet.getId())).thenReturn(null);
        when(transactionService.getBiggestExpenseCategoryName(wallet.getId())).thenReturn(null);
        when(transactionService.getTotalExpensesForCurrentMonth(wallet.getId())).thenReturn(BigDecimal.ZERO);
        when(transactionService.getTotalIncomeForCurrentMonth(wallet.getId())).thenReturn(BigDecimal.ZERO);
        when(subscriptionsService.getPaidSubscriptionsForCurrentMonth(userId)).thenReturn(Collections.emptyList());

        UserData userData = new UserData(
                userId, mockUser.getUsername(), mockUser.getPassword(), mockUser.getEmail(), mockUser.getRole(), mockUser.isActive());

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/report")
                .with(user(userData));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("report"))
                .andExpect(model().attribute("categoryNames", Collections.emptyList()))
                .andExpect(model().attribute("categoryPercents", Collections.emptyList()))
                .andExpect(model().attribute("categoryAmounts", Collections.emptyList()))
                .andExpect(model().attribute("allTransactions", Collections.emptyList()))
                .andExpect(model().attribute("currentMonthExpenses", BigDecimal.ZERO));
    }

    @Test
    void getReportPage_shouldReturnReportView_withCombinedExpenses() throws Exception {

        UUID userId = UUID.randomUUID();
        User mockUser = User.builder()
                .id(userId)
                .username("testUser")
                .password("testPassword")
                .role(Role.USER)
                .userVersion(UserVersion.PRO)
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

        Subscription subscription = Subscription.builder()
                .id(UUID.randomUUID())
                .name("Netflix")
                .price(new BigDecimal("19.99"))
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.DEFAULT)
                .expiryOn(LocalDate.now().plusDays(5))
                .paidDate(LocalDate.now())
                .user(mockUser)
                .build();
        List<Subscription> subscriptions = List.of(subscription);

        BigDecimal transactionExpenses = new BigDecimal("200.00");
        BigDecimal subscriptionExpenses = new BigDecimal("19.99");
        BigDecimal expectedTotal = transactionExpenses.add(subscriptionExpenses);

        when(userService.getById(userId)).thenReturn(mockUser);
        when(transactionService.getTotalExpensesForCurrentMonth(wallet.getId())).thenReturn(transactionExpenses);
        when(subscriptionsService.getPaidSubscriptionsForCurrentMonth(userId)).thenReturn(subscriptions);

        UserData userData = new UserData(
                userId, mockUser.getUsername(), mockUser.getPassword(), mockUser.getEmail(), mockUser.getRole(), mockUser.isActive());

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/report")
                .with(user(userData));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("report"))
                .andExpect(model().attribute("currentMonthExpenses", expectedTotal));
    }
}
