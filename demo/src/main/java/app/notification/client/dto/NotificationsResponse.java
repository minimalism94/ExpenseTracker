package app.notification.client.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationsResponse {

    private String subject;

    private LocalDateTime createdOn;

    private String status;

    private String type;
}
