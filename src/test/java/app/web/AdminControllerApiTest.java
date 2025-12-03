package app.web;

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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(TestWebMvcConfig.class)
public class AdminControllerApiTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserRepository userRepository;

    @Test
    void getAdminPanel_shouldReturnAdminPanelView_withStats() throws Exception {

        UUID adminId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User admin = mockAdmin(adminId);
        User regularUser = mockUser(userId);
        List<User> allUsers = List.of(admin, regularUser);

        when(userService.getById(adminId)).thenReturn(admin);
        when(userService.getAllUsers()).thenReturn(allUsers);

        MockHttpServletRequestBuilder requestBuilder = get("/admin")
                .with(user(mockAuth(adminId)));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("adminPanel"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("allUser"))
                .andExpect(model().attribute("allUser", 2));
    }

    @Test
    void deleteUser_shouldRedirect_whenValid() throws Exception {

        UUID userId = UUID.randomUUID();

        doNothing().when(userService).delete(eq(userId));

        MockHttpServletRequestBuilder requestBuilder = post("/admin/" + userId + "/delete")
                .with(user(mockAuth(UUID.randomUUID())))
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(userService).delete(eq(userId));
    }

    @Test
    void toggleUserActiveStatus_shouldRedirect_whenValid() throws Exception {

        UUID userId = UUID.randomUUID();

        doNothing().when(userService).setActive(eq(userId));

        MockHttpServletRequestBuilder requestBuilder = post("/admin/" + userId + "/active")
                .with(user(mockAuth(UUID.randomUUID())))
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(userService).setActive(eq(userId));
    }

    @Test
    void changeUserRole_shouldRedirect_whenValid() throws Exception {

        UUID userId = UUID.randomUUID();

        doNothing().when(userService).setRole(eq(userId));

        MockHttpServletRequestBuilder requestBuilder = post("/admin/" + userId + "/role")
                .with(user(mockAuth(UUID.randomUUID())))
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));

        verify(userService).setRole(eq(userId));
    }

    @Test
    void getAdminPanel_shouldReturnAdminPanelView_withEmptyUsers() throws Exception {

        UUID adminId = UUID.randomUUID();
        User admin = mockAdmin(adminId);

        when(userService.getById(adminId)).thenReturn(admin);
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        MockHttpServletRequestBuilder requestBuilder = get("/admin")
                .with(user(mockAuth(adminId)));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("adminPanel"))
                .andExpect(model().attribute("users", Collections.emptyList()))
                .andExpect(model().attribute("allUser", 0));
    }

    private UserData mockAuth(UUID id) {

        return new UserData(id
                , "admin"
                , "password"
                , "admin@example.com"
                , Role.ADMIN
                , true);
    }

    private User mockAdmin(UUID id) {

        return User.builder()
                .id(id)
                .username("admin")
                .email("admin@example.com")
                .firstName("Admin")
                .lastName("User")
                .country(Country.BULGARIA)
                .role(Role.ADMIN)
                .userVersion(UserVersion.PRO)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
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
}
