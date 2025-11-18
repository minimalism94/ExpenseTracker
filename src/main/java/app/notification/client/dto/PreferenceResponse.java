package app.notification.client.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PreferenceResponse {

    private String type;

    private boolean notificationEnabled;

    private String contactInfo;
}
