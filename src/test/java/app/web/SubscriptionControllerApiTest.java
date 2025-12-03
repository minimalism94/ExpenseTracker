package app.web;

import app.exception.CustomException;
import app.security.UserData;
import app.subscription.model.Subscription;
import app.subscription.model.SubscriptionPeriod;
import app.subscription.model.SubscriptionType;
import app.subscription.service.SubscriptionsService;
import app.user.model.Country;
import app.user.model.Role;
import app.user.model.User;
import app.user.model.UserVersion;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.SubscriptionDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
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

@WebMvcTest(SubscriptionController.class)
@Import({GlobalExceptionHandler.class, TestWebMvcConfig.class})
public class SubscriptionControllerApiTest {

    @MockitoBean
    private SubscriptionsService subscriptionService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getSubscriptionsPage_shouldReturnSubscriptionsView_withUserAndSubscriptions() throws Exception {

        UUID userId = UUID.randomUUID();
        User user = mockUser(userId);
        Subscription subscription = mockSubscription(user);
        List<Subscription> subscriptions = List.of(subscription);

        when(userService.getById(userId)).thenReturn(user);
        when(subscriptionService.getByUsername("testuser")).thenReturn(subscriptions);

        MockHttpServletRequestBuilder requestBuilder = get("/payments")
                .with(user(mockAuth(userId)));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("subscriptions"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("subscriptions"))
                .andExpect(model().attributeExists("subscription"))
                .andExpect(model().attributeExists("periods"))
                .andExpect(model().attributeExists("types"));
    }

    @Test
    void addSubscription_shouldRedirectToPayments_whenValidData() throws Exception {

        UUID userId = UUID.randomUUID();
        User user = mockUser(userId);

        when(userService.getById(userId)).thenReturn(user);
        doNothing().when(subscriptionService).saveSubscription(any(SubscriptionDto.class), eq("testuser"));

        MockHttpServletRequestBuilder requestBuilder = post("/payments/add")
                .with(user(mockAuth(userId)))
                .with(csrf())
                .param("name", "Spotify")
                .param("price", "9.99")
                .param("period", SubscriptionPeriod.MONTHLY.name())
                .param("type", SubscriptionType.DEFAULT.name())
                .param("expiryOn", LocalDate.now().plusDays(30).toString());

        mockMvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payments"));

        verify(subscriptionService).saveSubscription(any(SubscriptionDto.class), eq("testuser"));
    }

    @Test
    void addSubscription_shouldReturnView_withInvalidData() throws Exception {

        UUID userId = UUID.randomUUID();
        User user = mockUser(userId);
        List<Subscription> subscriptions = Collections.singletonList(mockSubscription(user));

        when(userService.getById(userId)).thenReturn(user);
        when(subscriptionService.getByUsername("testuser")).thenReturn(subscriptions);

        MockHttpServletRequestBuilder requestBuilder = post("/payments/add")
                .with(user(mockAuth(userId)))
                .with(csrf())
                .param("name", "")
                .param("price", "")
                .param("period", "")
                .param("type", "")
                .param("expiryOn", "");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("subscriptions"))
                .andExpect(model().attributeExists("subscription"))
                .andExpect(model().attributeHasFieldErrors("subscription",
                        "name", "price", "period", "type", "expiryOn"));

        verify(subscriptionService, never()).saveSubscription(any(), any());
    }

    @Test
    void addSubscription_shouldReturnView_whenPriceIsZero() throws Exception {

        UUID userId = UUID.randomUUID();
        User user = mockUser(userId);
        List<Subscription> subscriptions = Collections.singletonList(mockSubscription(user));

        when(userService.getById(userId)).thenReturn(user);
        when(subscriptionService.getByUsername("testuser")).thenReturn(subscriptions);

        MockHttpServletRequestBuilder requestBuilder = post("/payments/add")
                .with(user(mockAuth(userId)))
                .with(csrf())
                .param("name", "Spotify")
                .param("price", "0.00")
                .param("period", SubscriptionPeriod.MONTHLY.name())
                .param("type", SubscriptionType.DEFAULT.name())
                .param("expiryOn", LocalDate.now().plusDays(30).toString());

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("subscriptions"))
                .andExpect(model().attributeHasFieldErrors("subscription", "price"));

        verify(subscriptionService, never()).saveSubscription(any(), any());
    }

    @Test
    void deleteSubscription_shouldRedirectToPayments_whenValidId() throws Exception {

        UUID subscriptionId = UUID.randomUUID();

        doNothing().when(subscriptionService).deleteById(eq(subscriptionId));

        MockHttpServletRequestBuilder requestBuilder = post("/payments/delete/" + subscriptionId)
                .with(user(mockAuth(UUID.randomUUID())))
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payments"));

        verify(subscriptionService).deleteById(eq(subscriptionId));
    }

    @Test
    void paySubscription_shouldRedirectToDashboard_whenValidId() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID subscriptionId = UUID.randomUUID();

        doNothing().when(subscriptionService).paySubscription(eq(subscriptionId), eq(userId));

        MockHttpServletRequestBuilder requestBuilder = post("/payments/pay/" + subscriptionId)
                .with(user(mockAuth(userId)))
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(subscriptionService).paySubscription(eq(subscriptionId), eq(userId));
    }

    @Test
    void addSubscription_shouldReturnView_whenCustomException() throws Exception {

        UUID userId = UUID.randomUUID();
        User user = mockUser(userId);
        List<Subscription> subscriptions = Collections.singletonList(mockSubscription(user));

        when(userService.getById(userId)).thenReturn(user);
        when(subscriptionService.getByUsername("testuser")).thenReturn(subscriptions);
        doThrow(new CustomException("Subscription already exists"))
                .when(subscriptionService).saveSubscription(any(SubscriptionDto.class), eq("testuser"));

        MockHttpServletRequestBuilder requestBuilder = post("/payments/add")
                .with(user(mockAuth(userId)))
                .with(csrf())
                .param("name", "Netflix")
                .param("price", "19.99")
                .param("period", SubscriptionPeriod.MONTHLY.name())
                .param("type", SubscriptionType.DEFAULT.name())
                .param("expiryOn", LocalDate.now().plusDays(30).toString());

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("subscriptions"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Subscription already exists"));

        verify(subscriptionService).saveSubscription(any(SubscriptionDto.class), eq("testuser"));
    }

    @Test
    void addSubscription_shouldReturnView_whenIllegalArgumentException() throws Exception {

        UUID userId = UUID.randomUUID();
        User user = mockUser(userId);
        List<Subscription> subscriptions = Collections.singletonList(mockSubscription(user));

        when(userService.getById(userId)).thenReturn(user);
        when(subscriptionService.getByUsername("testuser")).thenReturn(subscriptions);
        doThrow(new IllegalArgumentException("Invalid subscription data"))
                .when(subscriptionService).saveSubscription(any(SubscriptionDto.class), eq("testuser"));

        MockHttpServletRequestBuilder requestBuilder = post("/payments/add")
                .with(user(mockAuth(userId)))
                .with(csrf())
                .param("name", "Spotify")
                .param("price", "9.99")
                .param("period", SubscriptionPeriod.MONTHLY.name())
                .param("type", SubscriptionType.DEFAULT.name())
                .param("expiryOn", LocalDate.now().plusDays(30).toString());

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("subscriptions"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Invalid subscription data"));

        verify(subscriptionService).saveSubscription(any(SubscriptionDto.class), eq("testuser"));
    }

    private UserData mockAuth(UUID id) {

        return new UserData(id
                , "testuser"
                , "password"
                , "test@example.com"
                , Role.USER
                , true);
    }

    private User mockUser(UUID id) {

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

    private Subscription mockSubscription(User user) {

        return Subscription.builder()
                .id(UUID.randomUUID())
                .name("Netflix")
                .price(new BigDecimal("19.99"))
                .period(SubscriptionPeriod.MONTHLY)
                .type(SubscriptionType.DEFAULT)
                .expiryOn(LocalDate.now().plusDays(5))
                .user(user)
                .build();
    }
}
