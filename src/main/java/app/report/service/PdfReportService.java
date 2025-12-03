package app.report.service;

import app.subscription.model.Subscription;
import app.subscription.service.SubscriptionsService;
import app.transactions.model.Transaction;
import app.transactions.service.TransactionService;
import app.user.model.User;
import app.wallet.model.Wallet;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class PdfReportService {

    private final TransactionService transactionService;
    private final SubscriptionsService subscriptionsService;
    private final TemplateEngine templateEngine;

    public PdfReportService(TransactionService transactionService,
                            SubscriptionsService subscriptionsService,
                            TemplateEngine templateEngine) {
        this.transactionService = transactionService;
        this.subscriptionsService = subscriptionsService;
        this.templateEngine = templateEngine;
    }

    public byte[] generateMonthlyReportPdf(User user, Wallet wallet, YearMonth month) {
        try {
            List<Transaction> allTransactions = transactionService.getCurrentMonthTransactions(wallet.getId());
            List<String> categoryNames = transactionService.getCategoryNamesForCurrentMonth(wallet.getId());
            List<Integer> categoryPercents = transactionService.getCategoryPercentsForCurrentMonth(wallet.getId());
            List<BigDecimal> categoryAmounts = transactionService.getCategoryAmountsForCurrentMonth(wallet.getId());
            BigDecimal transactionExpenses = transactionService.getTotalExpensesForCurrentMonth(wallet.getId());
            List<Subscription> paidSubscriptions = subscriptionsService.getPaidSubscriptionsForCurrentMonth(user.getId());

            BigDecimal subscriptionExpenses = paidSubscriptions.stream()
                    .map(Subscription::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal currentMonthExpenses = transactionExpenses.add(subscriptionExpenses);

            BigDecimal currentMonthIncome = transactionService.getTotalIncomeForCurrentMonth(wallet.getId());
            Transaction biggestExpense = transactionService.getBiggestExpenseForCurrentMonth(wallet.getId());
            String biggestExpenseName = transactionService.getBiggestExpenseCategoryName(wallet.getId());
            Map<String, BigDecimal> expenseHistory = transactionService.getExpenseHistoryByDay(wallet.getId());

            Context context = new Context(Locale.getDefault());
            context.setVariable("user", user);
            context.setVariable("wallet", wallet);
            context.setVariable("allTransactions", allTransactions);
            context.setVariable("categoryNames", categoryNames);
            context.setVariable("categoryPercents", categoryPercents);
            context.setVariable("categoryAmounts", categoryAmounts);
            context.setVariable("currentMonthExpenses", currentMonthExpenses);
            context.setVariable("currentMonthIncome", currentMonthIncome);
            context.setVariable("biggestExpense", biggestExpense);
            context.setVariable("biggestExpenseName", biggestExpenseName);
            context.setVariable("expenseHistory", expenseHistory);
            context.setVariable("paidSubscriptions", paidSubscriptions);
            context.setVariable("month", month.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
            context.setVariable("currentDate", java.time.LocalDate.now());

            String html = templateEngine.process("report-pdf", context);

            html = html.replaceAll("<meta([^>]*[^/])>", "<meta$1 />");
            html = html.replaceAll("<link([^>]*[^/])>", "<link$1 />");

            // OpenHTMLToPDF - безплатна алтернатива на iText7 html2pdf
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();
        } catch (Exception e) {
            String errorMessage = "Failed to generate PDF report: " + e.getMessage();
            if (e.getCause() != null) {
                errorMessage += " - Cause: " + e.getCause().getMessage();
            }
            throw new RuntimeException(errorMessage, e);
        }
    }
}

