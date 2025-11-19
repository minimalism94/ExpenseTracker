package app.web;

import app.security.UserData;
import app.subscription.model.Subscription;
import app.subscription.service.SubscriptionsService;
import app.transactions.model.Transaction;
import app.transactions.model.Type;
import app.transactions.service.TransactionService;
import app.user.model.User;
import app.user.model.UserVersion;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.web.dto.TopCategories;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/report")
public class ReportController {

    private final UserService userService;
    private final TransactionService transactionService;
    private final SubscriptionsService subscriptionsService;

    public ReportController(UserService userService, TransactionService transactionService, SubscriptionsService subscriptionsService) {
        this.userService = userService;
        this.transactionService = transactionService;
        this.subscriptionsService = subscriptionsService;
    }

    @GetMapping()
    public ModelAndView showReports(@AuthenticationPrincipal UserData userData) {
        User user = userService.getById(userData.getUserId());
        
        if (user.getUserVersion() != UserVersion.PRO) {
            return new ModelAndView("redirect:/upgrade");
        }
        
        Wallet wallet = user.getWallet();

        ModelAndView modelAndView = new ModelAndView("report");
        modelAndView.addObject("user", user);
        modelAndView.addObject("wallet", wallet);
        modelAndView.addObject("categoryNames", transactionService.getCategoryNamesForCurrentMonth(wallet.getId()));
        modelAndView.addObject("categoryPercents", transactionService.getCategoryPercentsForCurrentMonth(wallet.getId()));
        modelAndView.addObject("categoryAmounts", transactionService.getCategoryAmountsForCurrentMonth(wallet.getId()));
        modelAndView.addObject("allTransactions", transactionService.getCurrentMonthTransactions(wallet.getId()));
        modelAndView.addObject("biggestExpense", transactionService.getBiggestExpenseForCurrentMonth(wallet.getId()));
        modelAndView.addObject("biggestExpenseName", transactionService.getBiggestExpenseCategoryName(wallet.getId()));
        
        // Calculate total expenses including subscriptions
        BigDecimal transactionExpenses = transactionService.getTotalExpensesForCurrentMonth(wallet.getId());
        List<Subscription> paidSubscriptions = subscriptionsService.getPaidSubscriptionsForCurrentMonth(user.getId());
        BigDecimal subscriptionExpenses = paidSubscriptions.stream()
                .map(Subscription::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal currentMonthExpenses = transactionExpenses.add(subscriptionExpenses);
        
        modelAndView.addObject("currentMonthExpenses", currentMonthExpenses);
        modelAndView.addObject("currentMonthIncome", transactionService.getTotalIncomeForCurrentMonth(wallet.getId()));
        modelAndView.addObject("expenseHistory", transactionService.getExpenseHistoryByDay(wallet.getId()));
        modelAndView.addObject("paidSubscriptions", paidSubscriptions);

        return modelAndView;
    }
}
