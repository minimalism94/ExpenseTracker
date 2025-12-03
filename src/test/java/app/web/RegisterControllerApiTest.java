package app.web;

import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegisterController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RegisterControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Captor
    private ArgumentCaptor<RegisterRequest> registerRequestCaptor;

    @Test
    void getRegisterPage_shouldReturnRegisterPage() throws Exception {

        MockHttpServletRequestBuilder request = get("/register");

        mockMvc.perform(request)
                .andExpect(view().name("register"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("registerRequest"))
                .andExpect(model().attribute("registerRequest", org.hamcrest.Matchers.instanceOf(RegisterRequest.class)));
    }

    @Test
    void postRegisterPage_shouldRedirectToLogin_whenValidData() throws Exception {

        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "testuser")
                .formField("email", "test@example.com")
                .formField("password", "password123")
                .formField("country", "BULGARIA")
                .with(csrf());

        doNothing().when(userService).register(any(RegisterRequest.class));

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService).register(registerRequestCaptor.capture());
        RegisterRequest values = registerRequestCaptor.getValue();

        assertEquals("testuser", values.getUsername());
        assertEquals("test@example.com", values.getEmail());
        assertEquals("password123", values.getPassword());
    }

    @Test
    void postRegisterPage_shouldReturnRegisterView_withInvalidData() throws Exception {

        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "")
                .formField("email", "")
                .formField("password", "")
                .formField("country", "")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"))
                .andExpect(model().attributeHasFieldErrors("registerRequest",
                        "username", "email", "password", "country"));

        verify(userService, never()).register(any());
    }
}
