package app.event.listener;

import app.event.UserUpgradedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UpgradeEventListener {

    @EventListener
    public void handleUserUpgraded(UserUpgradedEvent event) {
        log.info("User {} upgraded from {} to PRO",
                event.getUser().getUsername(),
                event.getPreviousVersion());
    }
}

