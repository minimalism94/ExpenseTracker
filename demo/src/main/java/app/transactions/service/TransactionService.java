package app.transactions.service;

import app.exception.CustomException;
import app.transactions.model.Category;
import app.transactions.model.Transaction;
import app.transactions.model.Type;
import app.transactions.repository.TransactionRepository;
import app.user.model.User;
import app.user.service.UserService;
import app.wallet.model.Wallet;
import app.wallet.repository.WalletRepository;
import app.web.dto.TransactionDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.LinkedHashMap;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service

public class TransactionService {


    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;


    @Autowired
    public TransactionService(TransactionRepository transactionRepository, WalletRepository walletRepository) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
    }

    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public List<Transaction> findAll() {
        return transactionRepository.findAll(Sort.by(Sort.Direction.DESC, "date"));
    }


    public void processTransaction(TransactionDto dto, UUID userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException("Wallet not found"));

        BigDecimal amount = dto.getAmount();

        if (dto.getType() == Type.EXPENSE) {
            if (wallet.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException("Insufficient balance for this expense.");
            }
            wallet.setExpense(wallet.getExpense().add(amount));
            wallet.setBalance(wallet.getBalance().subtract(amount));
        } else if (dto.getType() == Type.INCOME) {
            wallet.setIncome(wallet.getIncome().add(amount));
            wallet.setBalance(wallet.getBalance().add(amount));
        }

        Transaction transaction = Transaction.builder()
                .amount(amount)
                .date(dto.getDate())
                .type(dto.getType())
                .category(dto.getCategory())
                .description(dto.getDescription())
                .wallet(wallet)
                .build();

        transactionRepository.save(transaction);
        walletRepository.save(wallet);
    }

    public Map<Category, BigDecimal> getTopCategories(UUID walletId) {
        List<Object[]> results = transactionRepository.findTopCategoriesByWallet(walletId);

        return results.stream()
                .limit(3)
                .collect(Collectors.toMap(
                        row -> (Category) row[0],
                        row -> (BigDecimal) row[1],
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

    }

    public Map<String, Integer> calculateCategoryPercents(Map<Category, BigDecimal> categoryTotals) {
        BigDecimal total = categoryTotals.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return categoryTotals.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name().toLowerCase(),
                        e -> e.getValue()
                                .multiply(BigDecimal.valueOf(100))
                                .divide(total, 0, RoundingMode.HALF_UP)
                                .intValue(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }



}

