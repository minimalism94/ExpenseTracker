package app.notification.service;

import app.notification.client.NotificationClient;
import app.notification.client.dto.NotificationRequest;
import app.notification.client.dto.NotificationsResponse;
import app.notification.client.dto.PreferenceResponse;
import app.notification.client.dto.UpsertPreferenceRequest;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class NotificationService {

    private final NotificationClient client;
        @Autowired
    public NotificationService(NotificationClient client) {
        this.client = client;
    }

    public void upsertPreference(UUID userId, boolean notificationEnabled, String contactInfo){

        UpsertPreferenceRequest dto =  UpsertPreferenceRequest.builder()
                .userId(userId)
                .notificationEnabled(notificationEnabled)
                .contactInfo(contactInfo)
                .build();
        try {
            client.upsertPreference(dto);
        }catch (FeignException e){

                log.error("[S2S Call] Failed due to [%s]".formatted(e.getMessage()));
        }

    }

    public PreferenceResponse getPreferenceByUserId (UUID userId){
           return client.getPreferences(userId).getBody();
    }

    public List<NotificationsResponse> getUserLastNotifications(UUID userId) {

        ResponseEntity<List<NotificationsResponse>> response = client.getNotifications(userId);
        return response.getBody() != null ? response.getBody().stream().limit(5).toList()
                : Collections.emptyList();
    }

    public void send(UUID userId, String subject, String body) {

        NotificationRequest dto = NotificationRequest.builder()
                .userId(userId)
                .type("SMS")
                .subject(subject)
                .body(body)
                .build();
        client.sendNotification(dto);



    }
}
