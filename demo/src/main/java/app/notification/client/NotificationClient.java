package app.notification.client;

import app.notification.client.dto.NotificationRequest;
import app.notification.client.dto.NotificationsResponse;
import app.notification.client.dto.PreferenceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import app.notification.client.dto.UpsertPreferenceRequest;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient (name = "notification-svc", url = "localhost:9091/api/v1")
public interface NotificationClient {

    @PostMapping("/preferences")
    ResponseEntity <Void> upsertPreference(@RequestBody UpsertPreferenceRequest requestBody);

    @GetMapping("/preferences")
    ResponseEntity<PreferenceResponse> getPreferences(@RequestParam ("userId")UUID userId);

    @GetMapping("/notifications")
    ResponseEntity<List<NotificationsResponse>> getNotifications(@RequestParam ("userId")UUID userId);


    @PostMapping("notifications")
    ResponseEntity<Void> sendNotification(@RequestBody NotificationRequest requestBody);
}


