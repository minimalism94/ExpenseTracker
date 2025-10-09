//package app.web;
//
//import app.transactions.model.Category;
//import app.transactions.model.Transaction;
//import app.transactions.service.TransactionService;
//import ch.qos.logback.core.model.Model;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.ModelAttribute;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.servlet.ModelAndView;
//
//@Controller
//public class TransactionController {
//
//    private final TransactionService transactionService;
//
//    public TransactionController(TransactionService transactionService) {
//        this.transactionService = transactionService;
//    }
//
////    @GetMapping("/transactions")
////    public String showRegisterPage() {
////
////        return "transactions";
////    }
//
//    // 🔹 Показва формата + таблицата с всички транзакции
//    @GetMapping("/transactions")
//    public ModelAndView showTransactionForm() {
//        ModelAndView mav = new ModelAndView("transactions");
//        mav.addObject("transaction", new Transaction());
//        mav.addObject("categories", Category.values());
//        mav.addObject("transactions", transactionService.findAll()); // ✅ добавяме всички транзакции
//        mav.addObject("page", "transactions");
//        return mav;
//    }
//    // Приема POST от формата и запазва транзакцията
//    @PostMapping("/transactions/save")
//    public String saveTransaction(@ModelAttribute("transaction") Transaction transaction) {
//        transactionService.saveTransaction(transaction);
//        return "redirect:/transactions";
//    }
//
////    // Страница след успешно запазване
////    @GetMapping("/transactions/success")
////    public String showSuccessPage() {
////        return "transaction_success";
////    }
//}
package app.web;

import app.user.model.User;
import app.transactions.model.Category;
import app.transactions.model.Transaction;
import app.transactions.model.Type;
import app.transactions.service.TransactionService;
import app.web.dto.TransactionDto;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/transactions")
    public ModelAndView showTransactionForm() {
        ModelAndView modelAndView = new ModelAndView("transactions");
        modelAndView.addObject("transaction", new TransactionDto());

        return modelAndView;
    }

    }
