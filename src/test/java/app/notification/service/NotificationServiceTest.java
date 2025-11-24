package app.notification.service;

import app.exception.NotificationRetryFailedException;
import app.notification.client.NotificationClient;
import app.notification.client.dto.NotificationRequest;
import app.notification.client.dto.NotificationsResponse;
import app.notification.client.dto.PreferenceResponse;
import app.notification.client.dto.UpsertPreferenceRequest;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private NotificationService notificationService;

    private UUID userId;
    private PreferenceResponse preferenceResponse;
    private NotificationsResponse notificationResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        preferenceResponse = PreferenceResponse.builder()
                .type("EMAIL")
                .notificationEnabled(true)
                .contactInfo("test@example.com")
                .build();

        notificationResponse = NotificationsResponse.builder()
                .subject("Test Notification")
                .createdOn(LocalDateTime.now())
                .status("SENT")
                .type("SMS")
                .build();
    }

    @Test
    void should_UpsertPreference_When_ValidRequestProvided() {
        when(notificationClient.upsertPreference(any(UpsertPreferenceRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> notificationService.upsertPreference(userId, true, "test@example.com"));

        ArgumentCaptor<UpsertPreferenceRequest> captor = ArgumentCaptor.forClass(UpsertPreferenceRequest.class);
        verify(notificationClient).upsertPreference(captor.capture());

        UpsertPreferenceRequest request = captor.getValue();
        assertEquals(userId, request.getUserId());
        assertTrue(request.isNotificationEnabled());
        assertEquals("test@example.com", request.getContactInfo());
    }

    @Test
    void should_ThrowNotificationRetryFailedException_When_FeignExceptionOccurs() {
        FeignException feignException = mock(FeignException.class);
        when(notificationClient.upsertPreference(any(UpsertPreferenceRequest.class)))
                .thenThrow(feignException);

        assertThrows(NotificationRetryFailedException.class,
                () -> notificationService.upsertPreference(userId, true, "test@example.com"));

        verify(notificationClient).upsertPreference(any(UpsertPreferenceRequest.class));
    }

    @Test
    void should_ReturnPreferenceResponse_When_UserIdExists() {
        ResponseEntity<PreferenceResponse> responseEntity = ResponseEntity.ok(preferenceResponse);
        when(notificationClient.getPreferences(userId)).thenReturn(responseEntity);

        PreferenceResponse result = notificationService.getPreferenceByUserId(userId);

        assertNotNull(result);
        assertEquals("EMAIL", result.getType());
        assertTrue(result.isNotificationEnabled());
        assertEquals("test@example.com", result.getContactInfo());
        verify(notificationClient).getPreferences(userId);
    }

    @Test
    void should_ReturnNull_When_PreferenceResponseBodyIsNull() {
        ResponseEntity<PreferenceResponse> responseEntity = ResponseEntity.ok(null);
        when(notificationClient.getPreferences(userId)).thenReturn(responseEntity);

        PreferenceResponse result = notificationService.getPreferenceByUserId(userId);

        assertNull(result);
        verify(notificationClient).getPreferences(userId);
    }

    @Test
    void should_ReturnLastFiveNotifications_When_MoreThanFiveExist() {
        List<NotificationsResponse> notifications = List.of(
                NotificationsResponse.builder().subject("Notification 1").createdOn(LocalDateTime.now()).build(),
                NotificationsResponse.builder().subject("Notification 2").createdOn(LocalDateTime.now()).build(),
                NotificationsResponse.builder().subject("Notification 3").createdOn(LocalDateTime.now()).build(),
                NotificationsResponse.builder().subject("Notification 4").createdOn(LocalDateTime.now()).build(),
                NotificationsResponse.builder().subject("Notification 5").createdOn(LocalDateTime.now()).build(),
                NotificationsResponse.builder().subject("Notification 6").createdOn(LocalDateTime.now()).build(),
                NotificationsResponse.builder().subject("Notification 7").createdOn(LocalDateTime.now()).build()
        );

        ResponseEntity<List<NotificationsResponse>> responseEntity = ResponseEntity.ok(notifications);
        when(notificationClient.getNotifications(userId)).thenReturn(responseEntity);

        List<NotificationsResponse> result = notificationService.getUserLastNotifications(userId);

        assertNotNull(result);
        assertEquals(5, result.size());
        verify(notificationClient).getNotifications(userId);
    }

    @Test
    void should_ReturnAllNotifications_When_LessThanFiveExist() {
        List<NotificationsResponse> notifications = List.of(
                NotificationsResponse.builder().subject("Notification 1").createdOn(LocalDateTime.now()).build(),
                NotificationsResponse.builder().subject("Notification 2").createdOn(LocalDateTime.now()).build()
        );

        ResponseEntity<List<NotificationsResponse>> responseEntity = ResponseEntity.ok(notifications);
        when(notificationClient.getNotifications(userId)).thenReturn(responseEntity);

        List<NotificationsResponse> result = notificationService.getUserLastNotifications(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(notificationClient).getNotifications(userId);
    }

    @Test
    void should_ReturnEmptyList_When_ResponseBodyIsNull() {
        ResponseEntity<List<NotificationsResponse>> responseEntity = ResponseEntity.ok(null);
        when(notificationClient.getNotifications(userId)).thenReturn(responseEntity);

        List<NotificationsResponse> result = notificationService.getUserLastNotifications(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(notificationClient).getNotifications(userId);
    }

    @Test
    void should_SendNotification_When_ValidRequestProvided() {
        String subject = "Test Subject";
        String body = "Test Body";
        when(notificationClient.sendNotification(any(NotificationRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> notificationService.send(userId, subject, body));

        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notificationClient).sendNotification(captor.capture());

        NotificationRequest request = captor.getValue();
        assertEquals(userId, request.getUserId());
        assertEquals("SMS", request.getType());
        assertEquals(subject, request.getSubject());
        assertEquals(body, request.getBody());
    }

    @Test
    void should_ThrowNotificationRetryFailedException_When_FeignExceptionOccursOnSend() {
        String subject = "Test Subject";
        String body = "Test Body";
        FeignException feignException = mock(FeignException.class);
        when(notificationClient.sendNotification(any(NotificationRequest.class)))
                .thenThrow(feignException);

        assertThrows(NotificationRetryFailedException.class,
                () -> notificationService.send(userId, subject, body));

        verify(notificationClient).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void should_UpsertPreferenceWithDisabledNotifications_When_NotificationEnabledIsFalse() {
        when(notificationClient.upsertPreference(any(UpsertPreferenceRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> notificationService.upsertPreference(userId, false, null));

        ArgumentCaptor<UpsertPreferenceRequest> captor = ArgumentCaptor.forClass(UpsertPreferenceRequest.class);
        verify(notificationClient).upsertPreference(captor.capture());

        UpsertPreferenceRequest request = captor.getValue();
        assertEquals(userId, request.getUserId());
        assertFalse(request.isNotificationEnabled());
        assertNull(request.getContactInfo());
    }
}

