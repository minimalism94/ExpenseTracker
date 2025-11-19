package app.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {


    public UserNotFoundException(String username) {
        super("User with username '%s' not found".formatted(username));
    }

    public UserNotFoundException(UUID id) {
        super("User with id '%s' not found".formatted(id));
    }
}

