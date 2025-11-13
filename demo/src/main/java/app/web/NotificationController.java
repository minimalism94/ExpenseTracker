package app.web;

import app.notification.service.NotificationService;
import app.security.UserData;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ModelAndView showNotificationPage(@AuthenticationPrincipal UserData userData) {

        ModelAndView modelAndView = new ModelAndView("notifications");
        modelAndView.addObject("userNotifications",  notificationService.getPreferenceByUserId(userData.getUserId()));
        modelAndView.addObject("lastNotifications", notificationService.getUserLastNotifications(userData.getUserId()));

        return modelAndView;
    }
}
