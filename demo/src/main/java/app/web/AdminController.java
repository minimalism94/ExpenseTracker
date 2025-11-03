package app.web;

import app.user.model.User;
import app.user.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
public class AdminController {


    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin")
    public ModelAndView showAdminPage() {

        List<User> allUsers = userService.getAllUsers();
        ModelAndView modelAndView = new ModelAndView("adminPanel");
        modelAndView.addObject("users", allUsers);
        modelAndView.addObject("allUser", allUsers.size());

        return modelAndView;
    }
    @PostMapping("admin/{id}/delete")
    public String delete(@PathVariable UUID id) {
        userService.delete(id);
        return "redirect:/admin";
    }

    @PostMapping("admin/{id}/active")
    public String changeActive(@PathVariable UUID id) {
        userService.setActive(id);
        return "redirect:/admin";
    }
    @PostMapping("admin/{id}/role")
    public String chagneRole(@PathVariable UUID id) {
        userService.setRole(id);
        return "redirect:/admin";
    }

}
