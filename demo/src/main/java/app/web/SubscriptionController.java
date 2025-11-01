package app.web;


import app.security.UserData;
import app.subscription.model.Subscription;
import app.subscription.service.SubscriptionsService;
import app.web.dto.EditSubscriptionDto;
import app.web.dto.SubscriptionDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindingResult;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/payments")
public class SubscriptionController {

    private final SubscriptionsService subscriptionService;

    @Autowired
    public SubscriptionController(SubscriptionsService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }


    @GetMapping
    public ModelAndView listPayments(Principal principal) {
        List<Subscription> subscriptions = subscriptionService.getByUsername(principal.getName());
        ModelAndView modelAndView = new ModelAndView("listPayments");
        modelAndView.addObject("subscriptions", subscriptions);
        return modelAndView;
    }

    @GetMapping("/add")
    public ModelAndView showAddForm() {
        ModelAndView modelAndView = new ModelAndView("addPayments");
        modelAndView.addObject("subscription", new SubscriptionDto());
        return modelAndView;
    }


    @PostMapping("/add")
    public ModelAndView addPayment(@Valid @ModelAttribute("subscription") SubscriptionDto dto, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView("addPayments");
        }

        subscriptionService.saveSubscription(dto, principal.getName());
        return new ModelAndView("redirect:/payments");
    }


    @PostMapping("/delete/{id}")
    public ModelAndView deletePayment(@PathVariable UUID id, Principal principal) {
        subscriptionService.deleteById(id);
        return new ModelAndView("redirect:/payments");
    }

    @PostMapping("/pay/{id}")
    public ModelAndView paySubscription(@PathVariable UUID id, @AuthenticationPrincipal UserData userData) {
        subscriptionService.paySubscription(id, userData.getUserId());
        return new ModelAndView("redirect:/dashboard");
    }
}

