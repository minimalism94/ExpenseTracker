package app.scheduler;

import app.notification.client.NotificationClient;
import app.notification.client.dto.NotificationRequest;
import app.notification.client.dto.PreferenceResponse;
import app.notification.client.dto.UpsertPreferenceRequest;
import app.report.service.PdfReportService;
import app.scheduler.config.CronExpressions;
import app.user.model.User;
import app.user.model.UserVersion;
import app.user.repository.UserRepository;
import app.wallet.model.Wallet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.Base64;
import java.util.List;

@Service
@Slf4j
public class ReportScheduler {

    private final UserRepository userRepository;
    private final PdfReportService pdfReportService;
    private final NotificationClient notificationClient;

    @Autowired
    public ReportScheduler(UserRepository userRepository, PdfReportService pdfReportService, NotificationClient notificationClient) {
        this.userRepository = userRepository;
        this.pdfReportService = pdfReportService;
        this.notificationClient = notificationClient;
    }

    @Scheduled(cron = CronExpressions.MONTHLY_FIRST_DAY_9AM)
    @Transactional
    public void sendMonthlyReports() {
        log.info("Starting monthly report generation and email sending...");

        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        List<User> proUsers = userRepository.findAll().stream()
                .filter(user -> user.getUserVersion() == UserVersion.PRO)
                .filter(User::isMonthlyReportEmailEnabled)
                .filter(user -> user.getEmail() != null && !user.getEmail().isBlank())
                .toList();

        log.info("Found {} PRO users with monthly report email enabled", proUsers.size());

        for (User user : proUsers) {
            try {
                Wallet wallet = user.getWallet();
                if (wallet == null) {
                    log.warn("User {} has no wallet, skipping report", user.getId());
                    continue;
                }

                try {
                    PreferenceResponse preference = notificationClient.getPreferences(user.getId()).getBody();
                    if (preference == null || !preference.isNotificationEnabled()) {
                        log.warn("User {} has notifications disabled or no preference, creating/updating preference", user.getId());

                        UpsertPreferenceRequest prefRequest =
                                UpsertPreferenceRequest.builder()
                                        .userId(user.getId())
                                        .notificationEnabled(true)
                                        .contactInfo(user.getEmail())
                                        .build();
                        notificationClient.upsertPreference(prefRequest);
                    }
                } catch (Exception e) {
                    log.warn("Failed to check/update notification preference for user {}, trying to create one", user.getId());
                    try {
                        UpsertPreferenceRequest prefRequest =
                                UpsertPreferenceRequest.builder()
                                        .userId(user.getId())
                                        .notificationEnabled(true)
                                        .contactInfo(user.getEmail())
                                        .build();
                        notificationClient.upsertPreference(prefRequest);
                    } catch (Exception ex) {
                        log.error("Failed to create notification preference for user {}, skipping report", user.getId(), ex);
                        continue;
                    }
                }

                byte[] pdfBytes = pdfReportService.generateMonthlyReportPdf(user, wallet, previousMonth);

                String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);

                String monthName = previousMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy"));
                String fileName = String.format("Monthly_Report_%s.pdf", previousMonth.format(java.time.format.DateTimeFormatter.ofPattern("yyyy_MM")));
                String subject = String.format("Monthly Financial Report - %s", monthName);
                String body = String.format(
                        "Dear %s,\n\n" +
                                "Please find your monthly financial report for %s attached to this email.\n\n" +
                                "This report includes:\n" +
                                "- Current balance and summary\n" +
                                "- Expense history\n" +
                                "- Paid subscriptions\n" +
                                "- All transactions\n" +
                                "- Expenses by category\n\n" +
                                "The PDF report is attached to this email.\n\n" +
                                "Best regards,\nSmartExpense Team",
                        user.getUsername(),
                        monthName
                );

                NotificationRequest notificationRequest = NotificationRequest.builder()
                        .userId(user.getId())
                        .type("EMAIL")
                        .subject(subject)
                        .body(body)
                        .attachmentBase64(pdfBase64)
                        .attachmentFileName(fileName)
                        .attachmentContentType("application/pdf")
                        .build();

                notificationClient.sendNotification(notificationRequest);

                log.info("Successfully sent monthly report to user {}", user.getId());
            } catch (Exception e) {
                log.error("Failed to send monthly report to user {}", user.getId(), e);
            }
        }

        log.info("Completed monthly report generation and email sending");
    }
}

