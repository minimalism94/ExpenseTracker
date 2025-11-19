package app.exception;

public class NotificationRetryFailedException extends RuntimeException {

    public NotificationRetryFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}

