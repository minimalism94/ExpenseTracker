package app.web;

import app.exception.CustomException;
import app.payment.service.StripeService;
import app.security.UserData;
import app.user.model.User;
import app.user.model.UserVersion;
import app.user.repository.UserRepository;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.UUID;

@Controller
@RequestMapping("/upgrade")
@Slf4j
public class UpgradeController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final StripeService stripeService;
    private static final BigDecimal PRO_VERSION_PRICE = new BigDecimal("1.00");

    @Value("${stripe.public.key}")
    private String stripePublicKey;

    @Autowired
    public UpgradeController(UserService userService, UserRepository userRepository, StripeService stripeService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.stripeService = stripeService;
    }

    @GetMapping
    public ModelAndView showUpgradePage(@AuthenticationPrincipal UserData userData, 
                                       org.springframework.security.web.csrf.CsrfToken csrfToken) {
        User user = userService.getById(userData.getUserId());
        Wallet wallet = user.getWallet();
        
        ModelAndView modelAndView = new ModelAndView("upgrade");
        modelAndView.addObject("user", user);
        modelAndView.addObject("wallet", wallet);
        modelAndView.addObject("proPrice", PRO_VERSION_PRICE);
        modelAndView.addObject("isPro", user.getUserVersion() == UserVersion.PRO);
        modelAndView.addObject("stripePublicKey", stripePublicKey);
        
        if (csrfToken != null) {
            modelAndView.addObject("_csrf", csrfToken);
        }
        
        return modelAndView;
    }

    @PostMapping("/create-checkout-session")
    @ResponseBody
    public String createCheckoutSession(@AuthenticationPrincipal UserData userData) {
        try {
            User user = userService.getById(userData.getUserId());

            if (user.getUserVersion() == UserVersion.PRO) {
                return "{\"error\": \"User is already Pro\"}";
            }

            Session session = stripeService.createCheckoutSession(user, PRO_VERSION_PRICE);
            return "{\"sessionId\": \"" + session.getId() + "\"}";
        } catch (StripeException e) {
            log.error("Error creating Stripe checkout session", e);
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    @GetMapping("/success")
    public ModelAndView handleSuccess(@RequestParam("session_id") String sessionId, 
                                      @AuthenticationPrincipal UserData userData) {
        try {
            Session session = stripeService.retrieveSession(sessionId);
            
            if ("complete".equals(session.getStatus()) && "paid".equals(session.getPaymentStatus())) {
                String userIdStr = session.getMetadata().get("userId");
                UUID userId = UUID.fromString(userIdStr);
                
                if (!userId.equals(userData.getUserId())) {
                    log.warn("User ID mismatch: session userId={}, authenticated userId={}", userId, userData.getUserId());
                    return new ModelAndView("redirect:/upgrade?error=unauthorized");
                }
                
                User user = userService.getById(userId);
                if (user.getUserVersion() != UserVersion.PRO) {
                    user.setUserVersion(UserVersion.PRO);
                    userRepository.save(user);
                    log.info("Upgraded user {} to PRO version", userId);
                }
                
                return new ModelAndView("redirect:/report");
            } else {
                return new ModelAndView("redirect:/upgrade?error=payment_not_complete");
            }
        } catch (Exception e) {
            log.error("Error processing successful payment", e);
            return new ModelAndView("redirect:/upgrade?error=processing_failed");
        }
    }

    @GetMapping("/cancel")
    public ModelAndView handleCancel() {
        return new ModelAndView("redirect:/upgrade?error=payment_cancelled");
    }

    @PostMapping("/webhook")
    @ResponseBody
    public String handleWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            com.stripe.net.Webhook.constructEvent(payload, sigHeader, stripeService.getWebhookSecret());
            log.info("Webhook received and verified");
            return "{\"status\": \"success\"}";
        } catch (Exception e) {
            log.error("Webhook error", e);
            return "{\"status\": \"error\"}";
        }
    }
}

