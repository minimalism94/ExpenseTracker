package app.web;

import app.security.UserData;
import app.transactions.model.Category;
import app.transactions.model.Transaction;
import app.transactions.model.Type;
import app.transactions.service.TransactionService;
import app.user.model.Country;
import app.user.model.Role;
import app.user.model.User;
import app.user.model.UserVersion;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.web.dto.TransactionDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@Import({GlobalExceptionHandler.class, TestWebMvcConfig.class})
public class TransactionControllerApiTest {

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getTransactionsPage_shouldReturnTransactionsView_withUserAndTransactions() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        User user = mockUser(userId);
        Wallet wallet = mockWallet(walletId, user);
        user.setWallet(wallet);

        Transaction transaction = mockTransaction(wallet);
        wallet.setTransactions(List.of(transaction));

        when(userService.getById(userId)).thenReturn(user);

        MockHttpServletRequestBuilder requestBuilder = get("/transactions")
                .with(user(mockAuth(userId)));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("transactions"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("transaction"))
                .andExpect(model().attributeExists("types"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("allTransactions"));

        verify(userService).getById(userId);
    }

    @Test
    void addTransaction_shouldRedirectToTransactions_whenValidData() throws Exception {

        UUID userId = UUID.randomUUID();
        User user = mockUser(userId);
        Wallet wallet = mockWallet(UUID.randomUUID(), user);
        user.setWallet(wallet);

        when(userService.getById(userId)).thenReturn(user);
        doNothing().when(transactionService).processTransaction(any(TransactionDto.class), eq(userId));

        MockHttpServletRequestBuilder requestBuilder = post("/transactions/add")
                .with(user(mockAuth(userId)))
                .with(csrf())
                .param("amount", "100.00")
                .param("date", LocalDateTime.now().toString())
                .param("type", Type.INCOME.name())
                .param("category", Category.TRANSPORT.name())
                .param("description", "Monthly Salary");

        mockMvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"));

        verify(transactionService).processTransaction(any(TransactionDto.class), eq(userId));
    }

    @Test
    void deleteTransaction_shouldRedirectToDashboard_whenValidId() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        doNothing().when(transactionService).deleteTransaction(eq(transactionId), eq(userId));

        MockHttpServletRequestBuilder requestBuilder = post("/transactions/delete/" + transactionId)
                .with(user(mockAuth(userId)))
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(transactionService).deleteTransaction(eq(transactionId), eq(userId));
    }

    @Test
    void deleteTransaction_shouldReturnView_whenIllegalArgumentException() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        User user = mockUser(userId);
        Wallet wallet = mockWallet(UUID.randomUUID(), user);
        user.setWallet(wallet);

        when(userService.getById(userId)).thenReturn(user);
        doThrow(new IllegalArgumentException("Transaction not found"))
                .when(transactionService).deleteTransaction(eq(transactionId), eq(userId));

        MockHttpServletRequestBuilder requestBuilder = post("/transactions/delete/" + transactionId)
                .with(user(mockAuth(userId)))
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("transactions"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Transaction not found"));
    }

    @Test
    void deleteTransaction_shouldReturnView_whenSecurityException() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        User user = mockUser(userId);
        Wallet wallet = mockWallet(UUID.randomUUID(), user);
        user.setWallet(wallet);

        when(userService.getById(userId)).thenReturn(user);
        doThrow(new SecurityException("Unauthorized"))
                .when(transactionService).deleteTransaction(eq(transactionId), eq(userId));

        MockHttpServletRequestBuilder requestBuilder = post("/transactions/delete/" + transactionId)
                .with(user(mockAuth(userId)))
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("transactions"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Unauthorized"));
    }

    @Test
    void addTransaction_shouldReturnView_whenIllegalArgumentException() throws Exception {

        UUID userId = UUID.randomUUID();
        User user = mockUser(userId);
        Wallet wallet = mockWallet(UUID.randomUUID(), user);
        user.setWallet(wallet);

        when(userService.getById(userId)).thenReturn(user);
        doThrow(new IllegalArgumentException("Insufficient balance"))
                .when(transactionService).processTransaction(any(TransactionDto.class), eq(userId));

        MockHttpServletRequestBuilder requestBuilder = post("/transactions/add")
                .with(user(mockAuth(userId)))
                .with(csrf())
                .param("amount", "10000.00")
                .param("date", LocalDateTime.now().toString())
                .param("type", Type.EXPENSE.name())
                .param("category", Category.FOOD.name())
                .param("description", "Too much food");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("transactions"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Insufficient balance"));
    }

    private UserData mockAuth(UUID id) {

        return new UserData(id
                , "testUser"
                , "password"
                , "test@example.com"
                , Role.USER
                , true);
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

    private Transaction mockTransaction(Wallet wallet) {

        return Transaction.builder()
                .id(UUID.randomUUID())
                .amount(new BigDecimal("50.00"))
                .date(LocalDateTime.now())
                .type(Type.EXPENSE)
                .category(Category.FOOD)
                .description("Test transaction")
                .wallet(wallet)
                .build();
    }
}
