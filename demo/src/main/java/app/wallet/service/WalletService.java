package app.wallet.service;

import app.transactions.service.TransactionService;
import app.user.model.User;
import app.wallet.model.Wallet;
import app.wallet.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final TransactionService transactionService;

    @Autowired
    public WalletService(WalletRepository walletRepository, TransactionService transactionService) {
        this.walletRepository = walletRepository;
        this.transactionService = transactionService;
    }

    public void createDefaultWallet(User user) {
        Wallet wallet = Wallet.builder()
                .user(user)
                .name("Default")
                .income(new BigDecimal("0"))
                .expense(new BigDecimal("0"))
                .balance(new BigDecimal("100"))
                .currency(Currency.getInstance("BGN"))
                .build();
        walletRepository.save(wallet);
    }

    private Wallet getById(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet by id [%s] was not found".formatted(walletId)));
    }
}
