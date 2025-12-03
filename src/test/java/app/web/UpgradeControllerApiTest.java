package app.web;

import app.payment.service.StripeService;
import app.security.UserData;
import app.user.model.Country;
import app.user.model.Role;
import app.user.model.User;
import app.user.model.UserVersion;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UpgradeController.class)
@Import({GlobalExceptionHandler.class, TestWebMvcConfig.class})
public class UpgradeControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private StripeService stripeService;

    @MockitoBean
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getUpgradePage_shouldReturnUpgradeView_withUserAndWallet() throws Exception {

        UUID userId = UUID.randomUUID();
        User basicUser = mockBasicUser(userId);
        Wallet wallet = mockWallet(userId);
        basicUser.setWallet(wallet);

        when(userService.getById(userId)).thenReturn(basicUser);

        MockHttpServletRequestBuilder requestBuilder = get("/upgrade")
                .with(user(mockAuth(userId)));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("upgrade"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("wallet"))
                .andExpect(model().attributeExists("proPrice"))
                .andExpect(model().attributeExists("isPro"))
                .andExpect(model().attributeExists("stripePublicKey"));
    }

    @Test
    void getUpgradePage_shouldShowError_whenErrorParamPresent() throws Exception {

        UUID userId = UUID.randomUUID();
        User basicUser = mockBasicUser(userId);
        Wallet wallet = mockWallet(userId);
        basicUser.setWallet(wallet);

        when(userService.getById(userId)).thenReturn(basicUser);

        MockHttpServletRequestBuilder requestBuilder = get("/upgrade")
                .param("error", "payment_not_complete")
                .with(user(mockAuth(userId)));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("upgrade"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Payment was not completed. Please try again."));
    }

    @Test
    void getUpgradePage_shouldShowIsProTrue_whenUserIsPro() throws Exception {

        UUID userId = UUID.randomUUID();
        User proUser = mockProUser(userId);
        Wallet wallet = mockWallet(userId);
        proUser.setWallet(wallet);

        when(userService.getById(userId)).thenReturn(proUser);

        MockHttpServletRequestBuilder requestBuilder = get("/upgrade")
                .with(user(mockAuth(userId)));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("upgrade"))
                .andExpect(model().attribute("isPro", true));
    }

    @Test
    void createCheckoutSession_shouldReturnSessionId_whenValidUser() throws Exception {

        UUID userId = UUID.randomUUID();
        User basicUser = mockBasicUser(userId);
        Wallet wallet = mockWallet(userId);
        basicUser.setWallet(wallet);

        Session mockSession = mock(Session.class);
        when(mockSession.getId()).thenReturn("test_session_id");
        when(userService.getById(userId)).thenReturn(basicUser);
        when(stripeService.createCheckoutSession(eq(basicUser), any(BigDecimal.class))).thenReturn(mockSession);

        MockHttpServletRequestBuilder requestBuilder = post("/upgrade/create-checkout-session")
                .with(user(mockAuth(userId)))
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().string("{\"sessionId\": \"test_session_id\"}"));

        verify(stripeService).createCheckoutSession(eq(basicUser), any(BigDecimal.class));
    }

    @Test
    void createCheckoutSession_shouldReturnError_whenUserIsAlreadyPro() throws Exception {

        UUID userId = UUID.randomUUID();
        User proUser = mockProUser(userId);
        Wallet wallet = mockWallet(userId);
        proUser.setWallet(wallet);

        when(userService.getById(userId)).thenReturn(proUser);

        MockHttpServletRequestBuilder requestBuilder = post("/upgrade/create-checkout-session")
                .with(user(mockAuth(userId)))
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().string("{\"error\": \"User is already Pro\"}"));

        verify(stripeService, never()).createCheckoutSession(any(User.class), any(BigDecimal.class));
    }

    @Test
    void createCheckoutSession_shouldReturnError_whenStripeException() throws Exception {

        UUID userId = UUID.randomUUID();
        User basicUser = mockBasicUser(userId);
        Wallet wallet = mockWallet(userId);
        basicUser.setWallet(wallet);

        StripeException stripeException = mock(StripeException.class);
        when(stripeException.getMessage()).thenReturn("Stripe error");
        when(userService.getById(userId)).thenReturn(basicUser);
        when(stripeService.createCheckoutSession(eq(basicUser), any(BigDecimal.class)))
                .thenThrow(stripeException);

        MockHttpServletRequestBuilder requestBuilder = post("/upgrade/create-checkout-session")
                .with(user(mockAuth(userId)))
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"error\"")));
    }

    @Test
    void handleSuccess_shouldRedirectToUpgradeWithError_whenPaymentNotComplete() throws Exception {

        UUID userId = UUID.randomUUID();
        Session mockSession = mock(Session.class);
        when(mockSession.getStatus()).thenReturn("open");
        when(mockSession.getPaymentStatus()).thenReturn("unpaid");
        when(stripeService.retrieveSession("test_session_id")).thenReturn(mockSession);

        MockHttpServletRequestBuilder requestBuilder = get("/upgrade/success")
                .param("session_id", "test_session_id")
                .with(user(mockAuth(userId)))
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/upgrade?error=payment_not_complete"));
    }

    @Test
    void handleSuccess_shouldRedirectToUpgradeWithError_whenUserIdMismatch() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID differentUserId = UUID.randomUUID();
        Session mockSession = mock(Session.class);
        when(mockSession.getStatus()).thenReturn("complete");
        when(mockSession.getPaymentStatus()).thenReturn("paid");
        Map<String, String> metadata = new HashMap<>();
        metadata.put("userId", differentUserId.toString());
        when(mockSession.getMetadata()).thenReturn(metadata);
        when(stripeService.retrieveSession("test_session_id")).thenReturn(mockSession);

        MockHttpServletRequestBuilder requestBuilder = get("/upgrade/success")
                .param("session_id", "test_session_id")
                .with(user(mockAuth(userId)))
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/upgrade?error=unauthorized"));
    }

    @Test
    void handleSuccess_shouldRedirectToUpgradeWithError_whenExceptionOccurs() throws Exception {

        UUID userId = UUID.randomUUID();
        when(stripeService.retrieveSession("test_session_id")).thenThrow(new RuntimeException("Error"));

        MockHttpServletRequestBuilder requestBuilder = get("/upgrade/success")
                .param("session_id", "test_session_id")
                .with(user(mockAuth(userId)))
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/upgrade?error=processing_failed"));
    }

    private UserData mockAuth(UUID id) {

        return new UserData(id
                , "testUser"
                , "password"
                , "test@example.com"
                , Role.USER
                , true);
    }

    private User mockBasicUser(UUID id) {

        return User.builder()
                .id(id)
                .username("testuser")
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

    private User mockProUser(UUID id) {

        return User.builder()
                .id(id)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .country(Country.BULGARIA)
                .role(Role.USER)
                .userVersion(UserVersion.PRO)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    private Wallet mockWallet(UUID userId) {

        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .name("Default")
                .income(new BigDecimal("1000.00"))
                .expense(new BigDecimal("200.00"))
                .balance(new BigDecimal("800.00"))
                .build();
        return wallet;
    }
}
