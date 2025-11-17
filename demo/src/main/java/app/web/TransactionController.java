
package app.web;

import app.security.UserData;
import app.user.model.User;
import app.transactions.model.Category;
import app.transactions.model.Transaction;
import app.transactions.model.Type;
import app.transactions.service.TransactionService;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.web.dto.TransactionDto;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class TransactionController {
    @Autowired
    private final TransactionService transactionService;
    private final UserService userService;

    public TransactionController(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    @GetMapping("/transactions")
        public ModelAndView showTransaction(@AuthenticationPrincipal UserData userData) {
        User currentUser=  userService.getById(userData.getUserId());
        List<Transaction>allTransaction = currentUser.getWallet().getTransactions();
        ModelAndView modelAndView = new ModelAndView("transactions");
        modelAndView.addObject("user", currentUser);
        modelAndView.addObject("transaction", new TransactionDto());
        modelAndView.addObject("types", Type.values());
        modelAndView.addObject("categories", Category.values());
        modelAndView.addObject("allTransactions", allTransaction);

        return modelAndView;
    }
    @PostMapping("/transactions/add")
    public ModelAndView createTransaction(@Valid TransactionDto dto, BindingResult bindingResult, @AuthenticationPrincipal UserData userData) {

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("transactions");
            User currentUser = userService.getById(userData.getUserId());
            List<Transaction> allTransaction = currentUser.getWallet().getTransactions();
            modelAndView.addObject("types", Type.values());
            modelAndView.addObject("categories", Category.values());
            modelAndView.addObject("error", "Please correct the form errors.");
            modelAndView.addObject("transaction", dto);
            modelAndView.addObject("allTransactions", allTransaction);
            return modelAndView;
        }

        try {
            transactionService.processTransaction(dto, userData.getUserId());
            return new ModelAndView("redirect:/transactions");
        } catch (IllegalArgumentException e) {
            ModelAndView modelAndView = new ModelAndView("transactions");
            User currentUser = userService.getById(userData.getUserId());
            List<Transaction> allTransaction = currentUser.getWallet().getTransactions();
            modelAndView.addObject("types", Type.values());
            modelAndView.addObject("categories", Category.values());
            modelAndView.addObject("error", e.getMessage());
            modelAndView.addObject("transaction", dto);
            modelAndView.addObject("allTransactions", allTransaction);
            return modelAndView;
        }
    }
    @PostMapping("/transactions/delete/{id}")
    public ModelAndView deleteTransaction(@PathVariable UUID id, @AuthenticationPrincipal UserData userData) {
        transactionService.deleteTransaction(id, userData.getUserId());
        return new ModelAndView("redirect:/dashboard");
    }




}
