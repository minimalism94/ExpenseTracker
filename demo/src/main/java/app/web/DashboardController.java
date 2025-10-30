package app.web;

import app.security.UserData;
import app.subscription.model.Subscription;
import app.transactions.model.Category;
import app.transactions.model.Transaction;
import app.transactions.service.TransactionService;
import app.user.model.User;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.wallet.service.WalletService;
import org.springframework.boot.Banner;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final UserService userService;
    private final WalletService walletService;
    private final TransactionService transactionService;

    public DashboardController(UserService userService, WalletService walletService, TransactionService transactionService) {
        this.userService = userService;
        this.walletService = walletService;
        this.transactionService = transactionService;
    }

    @GetMapping("/dashboard")
    public ModelAndView getHomePage (@AuthenticationPrincipal UserData userData) {
        User user = userService.getById(userData.getUserId());
        Wallet wallet = user.getWallet();
        Map<Category, BigDecimal> topCategories = transactionService.getTopCategories(wallet.getId());
        Map<String, Integer> percents = transactionService.calculateCategoryPercents(topCategories);
        //TODO Make them order by Asc by date
        List<Subscription> subscription = user.getSubscriptions();
        List <Transaction>  allTransaction= wallet.getTransactions();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("dashboard");
        modelAndView.addObject("user", user);
        modelAndView.addObject("wallet", wallet);
        modelAndView.addObject("subscription", subscription);
        modelAndView.addObject("transactions", allTransaction);
        modelAndView.addObject("topCategories", topCategories);
        modelAndView.addObject("percents", percents);

        return modelAndView;
    }
}
