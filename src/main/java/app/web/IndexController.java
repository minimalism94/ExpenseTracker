package app.web;

import app.user.service.UserService;
import app.web.dto.LoginRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {

    private final UserService userService;

    public IndexController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String showIndexPage() {

        return "index";
    }

    @GetMapping("/login")
    public ModelAndView showLoginPage(@RequestParam(value = "error", required = false) String error) {
        ModelAndView modelAndView = new ModelAndView();
        if (error != null) {
            modelAndView.addObject("errorMessage", true);
        }
        modelAndView.setViewName("login");
        modelAndView.addObject("loginRequest", new LoginRequest());

        return modelAndView;
    }

    @GetMapping("/error")
    public String showErrorPage() {
        // This endpoint is used by AccessDeniedHandler to forward to error.html
        // The error attributes are set in the request by AccessDeniedHandler
        return "error";
    }

}


