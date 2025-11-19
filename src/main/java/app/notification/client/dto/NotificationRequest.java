package app.notification.client.dto;


import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class NotificationRequest {

    private UUID userId;

    private String type;
    private String subject;

    private String body;
    
    private String attachmentBase64;
    private String attachmentFileName;
    private String attachmentContentType;
}
