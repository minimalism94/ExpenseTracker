package app.transactions.service;

import app.exception.CustomException;
import app.transactions.model.Category;
import app.transactions.model.Transaction;
import app.transactions.model.Type;
import app.transactions.repository.TransactionRepository;
import app.user.model.Country;
import app.user.model.Role;
import app.user.model.User;
import app.user.model.UserVersion;
import app.user.repository.UserRepository;
import app.wallet.model.Wallet;
import app.wallet.repository.WalletRepository;
import app.web.dto.TransactionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TransactionServiceITest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("password123")
                .isActive(true)
                .role(Role.USER)
                .country(Country.BULGARIA)
                .userVersion(UserVersion.BASIC)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .monthlyReportEmailEnabled(false)
                .build();
        testUser = userRepository.save(testUser);

        testWallet = Wallet.builder()
                .user(testUser)
                .name("Default")
                .income(new BigDecimal("0"))
                .expense(new BigDecimal("0"))
                .balance(new BigDecimal("1000"))
                .build();
        testWallet = walletRepository.save(testWallet);
        testUser.setWallet(testWallet);
        userRepository.save(testUser);
    }

    @Test
    void should_SaveTransaction_When_ValidTransactionProvided() {

        Transaction transaction = Transaction.builder()
                .wallet(testWallet)
                .amount(new BigDecimal("200"))
                .type(Type.INCOME)
                .category(Category.OTHER)
                .description("Test transaction")
                .date(LocalDateTime.now())
                .build();

        Transaction saved = transactionService.saveTransaction(transaction);

        // Then
        assertNotNull(saved.getId());
        assertEquals(new BigDecimal("200"), saved.getAmount());
        assertEquals(testWallet.getId(), saved.getWallet().getId());
    }
}




