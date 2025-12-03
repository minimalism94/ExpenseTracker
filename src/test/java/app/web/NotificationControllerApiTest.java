package app.web;

import app.notification.client.dto.NotificationsResponse;
import app.notification.client.dto.PreferenceResponse;
import app.notification.service.NotificationService;
import app.security.UserData;
import app.user.model.Country;
import app.user.model.Role;
import app.user.model.User;
import app.user.model.UserVersion;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import(TestWebMvcConfig.class)
public class NotificationControllerApiTest {

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getNotificationsPage_shouldReturnNotificationsView_withUserAndPreferences() throws Exception {

        UUID userId = UUID.randomUUID();
        User user = mockUser(userId, true);
        PreferenceResponse preference = PreferenceResponse.builder()
                .type("EMAIL")
                .notificationEnabled(true)
                .contactInfo("test@example.com")
                .build();
        List<NotificationsResponse> notifications = List.of(
                NotificationsResponse.builder()
                        .subject("Alert 1")
                        .createdOn(LocalDateTime.now())
                        .status("SENT")
                        .type("EMAIL")
                        .build()
        );

        when(userService.getById(userId)).thenReturn(user);
        when(notificationService.getPreferenceByUserId(userId)).thenReturn(preference);
        when(notificationService.getUserLastNotifications(userId)).thenReturn(notifications);

        MockHttpServletRequestBuilder requestBuilder = get("/notifications")
                .with(user(mockAuth(userId)));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("notifications"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("userNotifications"))
                .andExpect(model().attributeExists("lastNotifications"));
    }

    @Test
    void toggleNotifications_shouldRedirectToNotifications_whenToggledOn() throws Exception {

        UUID userId = UUID.randomUUID();
        User user = mockUser(userId, true);

        when(userService.getById(userId)).thenReturn(user);
        when(notificationService.getPreferenceByUserId(userId)).thenReturn(null);
        doNothing().when(notificationService).upsertPreference(eq(userId), eq(true), eq(null));

        MockHttpServletRequestBuilder requestBuilder = post("/notifications/toggle")
                .with(user(mockAuth(userId)))
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));

        verify(notificationService).upsertPreference(eq(userId), eq(true), eq(null));
    }

    @Test
    void toggleNotifications_shouldRedirectToNotifications_whenToggledOff() throws Exception {

        UUID userId = UUID.randomUUID();
        User user = mockUser(userId, true);
        PreferenceResponse existingPreference = PreferenceResponse.builder()
                .type("EMAIL")
                .notificationEnabled(true)
                .contactInfo("test@example.com")
                .build();

        when(userService.getById(userId)).thenReturn(user);
        when(notificationService.getPreferenceByUserId(userId)).thenReturn(existingPreference);
        doNothing().when(notificationService).upsertPreference(eq(userId), eq(false), eq("test@example.com"));

        MockHttpServletRequestBuilder requestBuilder = post("/notifications/toggle")
                .with(user(mockAuth(userId)))
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));

        verify(notificationService).upsertPreference(eq(userId), eq(false), eq("test@example.com"));
    }

    @Test
    void toggleMonthlyReport_shouldRedirectToNotifications_whenToggled() throws Exception {

        UUID userId = UUID.randomUUID();
        User user = mockUser(userId, true);
        User updatedUser = User.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .country(user.getCountry())
                .role(user.getRole())
                .userVersion(user.getUserVersion())
                .isActive(user.isActive())
                .createdOn(user.getCreatedOn())
                .updatedOn(user.getUpdatedOn())
                .monthlyReportEmailEnabled(false)
                .build();

        when(userService.getById(userId)).thenReturn(user);
        when(userService.save(any(User.class))).thenReturn(updatedUser);

        MockHttpServletRequestBuilder requestBuilder = post("/notifications/toggle-monthly-report")
                .with(user(mockAuth(userId)))
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));

        verify(userService).save(any(User.class));
    }

    private UserData mockAuth(UUID id) {

        return new UserData(id
                , "testUser"
                , "password"
                , "test@example.com"
                , Role.USER
                , true);
    }

    private User mockUser(UUID id, boolean monthlyReportEmailEnabled) {

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
                .monthlyReportEmailEnabled(monthlyReportEmailEnabled)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }
}
