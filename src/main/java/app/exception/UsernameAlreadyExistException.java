package app.exception;

public class UsernameAlreadyExistException extends RuntimeException {

    public UsernameAlreadyExistException(String username) {
        super("User with username '%s' already exists".formatted(username));
    }
}

