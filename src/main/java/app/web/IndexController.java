package app.web;

import app.user.model.User;
import app.user.service.userService;
import app.web.dto.LoginRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.naming.Binding;

@Controller
public class IndexController {

    private final userService userService;

    public IndexController(userService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String showIndexPage() {

        return "index";
    }

    @GetMapping("/login")
    public ModelAndView showLoginPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        modelAndView.addObject("loginRequest", new LoginRequest());

        return modelAndView; // login.html в templates
    }

    @PostMapping("/login")
    public ModelAndView processLogin(@Valid LoginRequest loginRequest, BindingResult bindingResult, HttpSession session) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("login");
        }

        User user  = userService.login(loginRequest);
        session.setAttribute("userId", user.getId());
        return new ModelAndView("redirect:/dashboard");
    }

}

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

