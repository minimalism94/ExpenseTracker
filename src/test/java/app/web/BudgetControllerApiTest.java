package app.web;

import app.budget.model.Budget;
import app.budget.service.BudgetService;
import app.security.UserData;
import app.transactions.model.Category;
import app.user.model.Country;
import app.user.model.Role;
import app.user.model.User;
import app.user.model.UserVersion;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.web.dto.BudgetDto;
import app.web.dto.BudgetPageData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BudgetController.class)
@Import(TestWebMvcConfig.class)
public class BudgetControllerApiTest {

    @MockitoBean
    private BudgetService budgetService;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getBudgetPage_shouldReturnBudgetView_whenProUser() throws Exception {

        UUID userId = UUID.randomUUID();
        User proUser = mockProUser(userId);
        BudgetPageData pageData = mockBudgetPageData(proUser);

        when(userService.getById(userId)).thenReturn(proUser);
        when(budgetService.getBudgetPageData(eq(userId), any(), any())).thenReturn(pageData);

        MockHttpServletRequestBuilder requestBuilder = get("/budget")
                .with(user(mockAuth(userId)));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("budget"))
                .andExpect(model().attributeExists("pageData"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("budgets"))
                .andExpect(model().attributeExists("budgetInfo"))
                .andExpect(model().attributeExists("allCategories"))
                .andExpect(model().attributeExists("categoriesWithBudgets"))
                .andExpect(model().attributeExists("totalBudget"))
                .andExpect(model().attributeExists("totalSpent"))
                .andExpect(model().attributeExists("totalRemaining"))
                .andExpect(model().attributeExists("currentMonth"))
                .andExpect(model().attributeExists("currentMonthName"))
                .andExpect(model().attributeExists("budgetDto"))
                .andExpect(model().attributeExists("previousMonth"))
                .andExpect(model().attributeExists("nextMonth"));

        verify(userService).getById(userId);
        verify(budgetService).getBudgetPageData(eq(userId), any(), any());
    }

    @Test
    void getBudgetPage_shouldRedirectToUpgrade_whenBasicUser() throws Exception {

        UUID userId = UUID.randomUUID();
        User basicUser = mockBasicUser(userId);

        when(userService.getById(userId)).thenReturn(basicUser);

        MockHttpServletRequestBuilder requestBuilder = get("/budget")
                .with(user(mockAuth(userId)));

        mockMvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/upgrade"));

        verify(userService).getById(userId);
        verify(budgetService, never()).getBudgetPageData(any(), any(), any());
    }

    @Test
    void getBudgetPage_shouldReturnBudgetView_whenMonthAndYearProvided() throws Exception {

        UUID userId = UUID.randomUUID();
        User proUser = mockProUser(userId);
        BudgetPageData pageData = mockBudgetPageData(proUser);

        when(userService.getById(userId)).thenReturn(proUser);
        when(budgetService.getBudgetPageData(eq(userId), eq(10), eq(2024))).thenReturn(pageData);

        MockHttpServletRequestBuilder requestBuilder = get("/budget")
                .param("month", "10")
                .param("year", "2024")
                .with(user(mockAuth(userId)));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(view().name("budget"))
                .andExpect(model().attributeExists("pageData"));

        verify(userService).getById(userId);
        verify(budgetService).getBudgetPageData(eq(userId), eq(10), eq(2024));
    }

    @Test
    void addBudget_shouldRedirectToBudget_whenValidData() throws Exception {

        UUID userId = UUID.randomUUID();
        User proUser = mockProUser(userId);
        Budget savedBudget = Budget.builder()
                .id(UUID.randomUUID())
                .category(Category.FOOD)
                .amount(new BigDecimal("500.00"))
                .month(11)
                .year(2024)
                .user(proUser)
                .build();

        when(userService.getById(userId)).thenReturn(proUser);
        when(budgetService.createOrUpdateBudget(eq(userId), any(BudgetDto.class))).thenReturn(savedBudget);

        MockHttpServletRequestBuilder requestBuilder = post("/budget/add")
                .with(user(mockAuth(userId)))
                .with(csrf())
                .param("category", Category.FOOD.name())
                .param("amount", "500.00")
                .param("month", "11")
                .param("year", "2024");

        mockMvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/budget?month=11&year=2024"));

        verify(budgetService).createOrUpdateBudget(eq(userId), any(BudgetDto.class));
    }

    @Test
    void addBudget_shouldRedirectToBudgetWithError_whenValidationFails() throws Exception {

        UUID userId = UUID.randomUUID();
        User proUser = mockProUser(userId);

        when(userService.getById(userId)).thenReturn(proUser);

        MockHttpServletRequestBuilder requestBuilder = post("/budget/add")
                .with(user(mockAuth(userId)))
                .with(csrf())
                .param("amount", "0.00")
                .param("month", "11")
                .param("year", "2024");

        mockMvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/budget?error=validation_failed"));

        verify(budgetService, never()).createOrUpdateBudget(any(), any());
    }

    @Test
    void deleteBudget_shouldRedirectToBudget_whenValidId() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        User proUser = mockProUser(userId);

        when(userService.getById(userId)).thenReturn(proUser);
        doNothing().when(budgetService).deleteBudget(eq(budgetId), eq(userId));

        MockHttpServletRequestBuilder requestBuilder = post("/budget/delete/" + budgetId)
                .with(user(mockAuth(userId)))
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/budget"));

        verify(budgetService).deleteBudget(eq(budgetId), eq(userId));
    }

    @Test
    void deleteBudget_shouldRedirectToBudgetWithParams_whenMonthAndYearProvided() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID budgetId = UUID.randomUUID();
        User proUser = mockProUser(userId);

        when(userService.getById(userId)).thenReturn(proUser);
        doNothing().when(budgetService).deleteBudget(eq(budgetId), eq(userId));

        MockHttpServletRequestBuilder requestBuilder = post("/budget/delete/" + budgetId)
                .param("month", "11")
                .param("year", "2024")
                .with(user(mockAuth(userId)))
                .with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/budget?month=11&year=2024"));

        verify(budgetService).deleteBudget(eq(budgetId), eq(userId));
    }

    private UserData mockAuth(UUID id) {

        return new UserData(id
                , "testUser"
                , "password"
                , "test@example.com"
                , Role.USER
                , true);
    }

    private User mockProUser(UUID id) {

        return User.builder()
                .id(id)
                .username("prouser")
                .email("pro@example.com")
                .firstName("Pro")
                .lastName("User")
                .country(Country.BULGARIA)
                .role(Role.USER)
                .userVersion(UserVersion.PRO)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    private User mockBasicUser(UUID id) {

        return User.builder()
                .id(id)
                .username("basicuser")
                .email("basic@example.com")
                .firstName("Basic")
                .lastName("User")
                .country(Country.BULGARIA)
                .role(Role.USER)
                .userVersion(UserVersion.BASIC)
                .isActive(true)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    private BudgetPageData mockBudgetPageData(User user) {

        Budget testBudget = Budget.builder()
                .id(UUID.randomUUID())
                .category(Category.FOOD)
                .amount(new BigDecimal("500.00"))
                .month(11)
                .year(2024)
                .user(user)
                .build();

        YearMonth currentYearMonth = YearMonth.now();
        return BudgetPageData.builder()
                .user(user)
                .budgets(List.of(testBudget))
                .budgetInfo(new HashMap<>())
                .allCategories(List.of(Category.values()))
                .categoriesWithBudgets(Set.of(Category.FOOD))
                .totalBudget(new BigDecimal("500.00"))
                .totalSpent(new BigDecimal("200.00"))
                .totalRemaining(new BigDecimal("300.00"))
                .currentMonth(currentYearMonth)
                .currentMonthName(currentYearMonth.getMonth().name())
                .previousMonth(currentYearMonth.minusMonths(1))
                .nextMonth(currentYearMonth.plusMonths(1))
                .build();
    }
}
