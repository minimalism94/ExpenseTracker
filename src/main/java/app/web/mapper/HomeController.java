package app.web.mapper;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        // Това ще каже на Spring Boot да върне index.html от templates/
        return "index";
    }
}
