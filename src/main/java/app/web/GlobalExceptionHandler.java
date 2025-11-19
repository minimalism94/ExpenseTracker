package app.web;

import app.exception.CustomException;
import app.exception.UserNotFoundException;
import app.exception.UsernameAlreadyExistException;
import app.web.dto.RegisterRequest;
import com.stripe.exception.StripeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleUserNotFound(UserNotFoundException ex) {
        log.error("User not found: {}", ex.getMessage());
        return createErrorModel("User Not Found", ex.getMessage(), "The requested user could not be found.", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UsernameAlreadyExistException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ModelAndView handleUsernameAlreadyExists(UsernameAlreadyExistException ex) {
        log.error("Username already exists: {}", ex.getMessage());
        ModelAndView modelAndView = new ModelAndView("register");
        modelAndView.addObject("registerRequest", new app.web.dto.RegisterRequest());
        modelAndView.addObject("error", ex.getMessage());
        return modelAndView;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Illegal argument: {}", ex.getMessage());
        return createErrorModel("Invalid Request", ex.getMessage(), "The request contains invalid data. Please check your input and try again.", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({SecurityException.class,
            AccessDeniedException.class
    })
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleSecurityException(SecurityException ex) {
        log.error("Security exception: {}", ex.getMessage());
        return createErrorModel("Access Denied", ex.getMessage(), "You don't have permission to perform this action.", HttpStatus.FORBIDDEN);
    }






    @ExceptionHandler(CustomException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleCustomException(CustomException ex) {
        log.error("Custom exception: {}", ex.getMessage());
        return createErrorModel("Error", ex.getMessage(), "An error occurred while processing your request.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(StripeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleStripeException(StripeException ex) {
        log.error("Stripe error: {}", ex.getMessage());
        ModelAndView modelAndView = new ModelAndView("redirect:/upgrade");
        modelAndView.addObject("error", "Payment processing failed: " + ex.getMessage());
        return modelAndView;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleLeftoverException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return createErrorModel("Internal Server Error", "An unexpected error occurred", "We're sorry, but something went wrong. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ModelAndView createErrorModel(String title, String message, String description, HttpStatus status) {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("errorTitle", title);
        modelAndView.addObject("errorMessage", message);
        modelAndView.addObject("errorDescription", description);
        modelAndView.addObject("statusCode", status.value());
        modelAndView.addObject("statusName", status.getReasonPhrase());
        return modelAndView;
    }
}

