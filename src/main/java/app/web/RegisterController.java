package app.web;

import app.user.model.Country;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class RegisterController {
    UserService userService;
    @Autowired
    public RegisterController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/register")
    public ModelAndView showRegisterPage(ModelAndView modelAndView) {
        ModelAndView modelAndView1=  new ModelAndView();
        modelAndView1.setViewName("register");
        modelAndView1.addObject("registerRequest", new RegisterRequest());

        return modelAndView1;
    }
    @PostMapping("/register")
    public ModelAndView registerNewUser(@Valid RegisterRequest registerRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("register");
            modelAndView.addObject("user", registerRequest);
            return modelAndView;
        }

        userService.register(registerRequest);
        return new ModelAndView("redirect:/login");
    }
}
