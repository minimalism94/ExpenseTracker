
package app.web;

import app.security.UserData;
import app.user.model.User;
import app.transactions.model.Category;
import app.transactions.model.Transaction;
import app.transactions.model.Type;
import app.transactions.service.TransactionService;
import app.user.service.UserService;
import app.web.dto.TransactionDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

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

        ModelAndView modelAndView = new ModelAndView("transactions");
        modelAndView.addObject("transaction", new TransactionDto());
        modelAndView.addObject("types", Type.values());
        modelAndView.addObject("categories", Category.values());

        return modelAndView;
    }
    @PostMapping("/transactions/add")
    public ModelAndView createTransaction(@Valid @ModelAttribute("transaction") TransactionDto dto, BindingResult bindingResult, @AuthenticationPrincipal UserData userData) {

        ModelAndView modelAndView = new ModelAndView("transactions");
        modelAndView.addObject("types", Type.values());
        modelAndView.addObject("categories", Category.values());

        if (bindingResult.hasErrors()) {
            modelAndView.addObject("error", "Please correct the form errors.");
            modelAndView.addObject("transaction", dto); // Връща попъллнената форма
            return modelAndView;
        }

        try {
            transactionService.processTransaction(dto, userData.getUserId());
            modelAndView.addObject("success", "Transaction saved successfully!");
            modelAndView.addObject("transaction", new TransactionDto()); // Ако мине връща празната форма
        } catch (IllegalArgumentException e) {
            modelAndView.addObject("error", e.getMessage());
            modelAndView.addObject("transaction", dto); //При грешка връща пулната форма
        }

        return modelAndView;
    }




}
