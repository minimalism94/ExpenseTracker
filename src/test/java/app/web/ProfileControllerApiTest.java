package app.web;

import app.security.UserData;
import app.user.model.Role;
import app.user.model.User;
import app.user.model.UserVersion;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.UserEditRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@Import({GlobalExceptionHandler.class, TestWebMvcConfig.class})
public class ProfileControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void editProfile_shouldReturnProfileEditView_withUserDetails() throws Exception {

        UUID userId = UUID.randomUUID();
        User user = mockUser(userId);
        user.setFirstName("test");
        user.setLastName("test");
        user.setEmail("test@test.test");

        when(userService.getById(userId)).thenReturn(user);

        MockHttpServletRequestBuilder requestBuilder = get("/profile")
                .with(user(mockAuth(userId)));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("userEditRequest", org.hamcrest.Matchers.instanceOf(UserEditRequest.class)));

        verify(userService).getById(userId);
    }

    @Test
    void updateProfile_shouldRedirectToDashboard_whenValidData() throws Exception {

        UUID userId = UUID.randomUUID();
        User user = mockUser(userId);

        when(userService.getById(userId)).thenReturn(user);

        MockHttpServletRequestBuilder requestBuilder = post("/profile")
                .with(user(mockAuth(userId)))
                .with(csrf())
                .param("username", "test")
                .param("firstName", "test")
                .param("lastName", "test")
                .param("email", "test@test.test")
                .param("country", "BULGARIA");

        mockMvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        verify(userService).editUserDetails(eq(userId), any(UserEditRequest.class));
    }


    @Test
    void updateProfile_shouldReturnView_whenEmailInvalid() throws Exception {

        UUID userId = UUID.randomUUID();
        User user = mockUser(userId);

        when(userService.getById(userId)).thenReturn(user);

        MockHttpServletRequestBuilder requestBuilder = post("/profile")
                .with(user(mockAuth(userId)))
                .with(csrf())
                .param("username", "test")
                .param("firstName", "test")
                .param("lastName", "test")
                .param("email", "notEmail")
                .param("country", "BULGARIA");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeHasFieldErrors("userEditRequest", "email"));
    }

    @Test
    void updateProfile_shouldReturnView_whenUsernameTooShort() throws Exception {

        UUID userId = UUID.randomUUID();
        User user = mockUser(userId);

        when(userService.getById(userId)).thenReturn(user);

        MockHttpServletRequestBuilder requestBuilder = post("/profile")
                .with(user(mockAuth(userId)))
                .with(csrf())
                .param("username", "t")
                .param("firstName", "test")
                .param("lastName", "test")
                .param("email", "test@test.test")
                .param("country", "BULGARIA");

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeHasFieldErrors("userEditRequest", "username"));
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
                .role(Role.USER)
                .userVersion(UserVersion.BASIC)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .subscriptions(Collections.emptyList())
                .build();
    }
}
