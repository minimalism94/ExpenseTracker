package app.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard() {
        // Това ще каже на Spring Boot да върне dashboard.html от templates/
        return "dashboard";
    }
}
