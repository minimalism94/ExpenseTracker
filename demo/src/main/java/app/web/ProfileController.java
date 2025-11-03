package app.web;


import app.security.UserData;
import app.user.model.Country;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.UserEditRequest;
import app.web.dto.mapper.DtoMapper;
import ch.qos.logback.core.model.Model;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    ModelAndView profilePage(@AuthenticationPrincipal UserData userData) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profile");
        User user = userService.getById(userData.getUserId());
        modelAndView.addObject("user", user);
        modelAndView.addObject("countries", Country.values());
        modelAndView.addObject("userEditRequest", DtoMapper.mapUserToUserEditRequest(user));

        return modelAndView;
    }
    @PostMapping("/profile")
    public ModelAndView updateProfile(@AuthenticationPrincipal UserData userData,
                                      @Valid @ModelAttribute("userEditRequest") UserEditRequest dto,
                                      BindingResult bindingResult) {

        ModelAndView modelAndView = new ModelAndView();

        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("profile");
            modelAndView.addObject("user", userService.getById(userData.getUserId()));
            modelAndView.addObject("userEditRequest", dto);
            return modelAndView;
        }

        userService.editUserDetails(userData.getUserId(), dto);

        return new ModelAndView("redirect:/profile");
    }
}

