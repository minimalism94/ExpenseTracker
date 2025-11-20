package app.event;

import app.user.model.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserUpgradedEvent extends ApplicationEvent {
    private final User user;
    private final String previousVersion;

    public UserUpgradedEvent(Object source, User user, String previousVersion) {
        super(source);
        this.user = user;
        this.previousVersion = previousVersion;
    }
}

