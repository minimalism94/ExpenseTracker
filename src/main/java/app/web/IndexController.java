package app.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class IndexController {

    @GetMapping("/")
    public String showIndexPage() {

        return "index";
    }

//    @PostMapping("/loginn")
//    public String processLogin(@RequestParam String username,
//                               @RequestParam String password) {
//        // Тук можеш да добавиш логика за проверка
//        if ("admin".equals(username) && "1234".equals(password)) {
//            return "redirect:/dashboard";
//        }
//        return "redirect:/loginn?error";
//    }
//
//    @GetMapping("/register")
//    public String showRegisterPage() {
//        return "register"; // register.html
//    }
//
//    @GetMapping("/forgot-password")
//    public String showForgotPasswordPage() {
//        return "forgot-password"; // forgot-password.html
//    }
}
