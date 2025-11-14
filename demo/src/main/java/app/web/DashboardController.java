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
import app.web.dto.TopCategories;
import org.springframework.boot.Banner;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        List<TopCategories> topCategories = transactionService.getTopCategories(wallet.getId());
        List<String> categoryNames = topCategories.stream()
                .map(c -> c.getCategory().getName())
                .toList();

        List<Integer> categoryPercents = topCategories.stream()
                .map(TopCategories::getPercent)
                .toList();
        
        List<Subscription> subscription = user.getSubscriptions().stream()
                .sorted((s1, s2) -> s2.getExpiryOn().compareTo(s1.getExpiryOn()))
                .limit(3)
                .collect(Collectors.toList());
        
        List <Transaction>  allTransaction= wallet.getTransactions();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("dashboard");
        modelAndView.addObject("user", user);
        modelAndView.addObject("wallet", wallet);
        modelAndView.addObject("subscription", subscription);
        modelAndView.addObject("transactions", allTransaction);
        modelAndView.addObject("topCategories",  transactionService.getTopCategories(wallet.getId()));
        modelAndView.addObject("categoryNames", categoryNames);
        modelAndView.addObject("categoryPercents", categoryPercents);

        return modelAndView;
    }
}
