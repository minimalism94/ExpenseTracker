package app.web;

import app.budget.service.BudgetService;
import app.security.UserData;
import app.user.model.User;
import app.user.model.UserVersion;
import app.user.service.UserService;
import app.web.dto.BudgetDto;
import app.web.dto.BudgetPageData;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/budget")
@Slf4j
public class BudgetController {

    private final BudgetService budgetService;
    private final UserService userService;

    public BudgetController(BudgetService budgetService, UserService userService) {
        this.budgetService = budgetService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView showBudgetPage(@AuthenticationPrincipal UserData userData,
                                       @RequestParam(value = "month", required = false) Integer month,
                                       @RequestParam(value = "year", required = false) Integer year) {
        User user = userService.getById(userData.getUserId());

        if (user.getUserVersion() != UserVersion.PRO) {
            return new ModelAndView("redirect:/upgrade");
        }

        BudgetPageData pageData = budgetService.getBudgetPageData(user.getId(), month, year);

        ModelAndView modelAndView = new ModelAndView("budget");
        modelAndView.addObject("pageData", pageData);
        modelAndView.addObject("user", pageData.getUser());
        modelAndView.addObject("budgets", pageData.getBudgets());
        modelAndView.addObject("budgetInfo", pageData.getBudgetInfo());
        modelAndView.addObject("allCategories", pageData.getAllCategories());
        modelAndView.addObject("categoriesWithBudgets", pageData.getCategoriesWithBudgets());
        modelAndView.addObject("totalBudget", pageData.getTotalBudget());
        modelAndView.addObject("totalSpent", pageData.getTotalSpent());
        modelAndView.addObject("totalRemaining", pageData.getTotalRemaining());
        modelAndView.addObject("currentMonth", pageData.getCurrentMonth());
        modelAndView.addObject("currentMonthName", pageData.getCurrentMonthName());
        modelAndView.addObject("budgetDto", new BudgetDto());
        modelAndView.addObject("previousMonth", pageData.getPreviousMonth());
        modelAndView.addObject("nextMonth", pageData.getNextMonth());

        return modelAndView;
    }

    @PostMapping("/add")
    public ModelAndView addBudget(@Valid @ModelAttribute BudgetDto budgetDto,
                                  BindingResult bindingResult,
                                  @AuthenticationPrincipal UserData userData) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("redirect:/budget?error=validation_failed");
        }

        budgetService.createOrUpdateBudget(userData.getUserId(), budgetDto);

        return new ModelAndView("redirect:/budget?month=" + budgetDto.getMonth() + "&year=" + budgetDto.getYear());
    }

    @PostMapping("/delete/{id}")
    public ModelAndView deleteBudget(@PathVariable UUID id,
                                     @AuthenticationPrincipal UserData userData,
                                     @RequestParam(value = "month", required = false) Integer month,
                                     @RequestParam(value = "year", required = false) Integer year) {
        budgetService.deleteBudget(id, userData.getUserId());

        String redirect = "/budget";
        if (month != null && year != null) {
            redirect += "?month=" + month + "&year=" + year;
        }

        return new ModelAndView("redirect:" + redirect);
    }
}

