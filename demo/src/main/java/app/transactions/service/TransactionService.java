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
import app.web.dto.TopCategories;
import app.web.dto.TransactionDto;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.*;

import java.math.BigDecimal;
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


    public List<TopCategories> getTopCategories(UUID walletId) {
        List<TopCategories> rawTop = transactionRepository.topCategories(walletId);


        List<TopCategories> top3 = rawTop.stream()
                .limit(3)
                .collect(Collectors.toList());

        BigDecimal total = calculateTotalAmount(top3);

        calculatePercents(top3, total);

        return top3;
    }

    public List<TopCategories> getAllExpenseCategories(UUID walletId) {
        return transactionRepository.topCategories(walletId);
    }

    private BigDecimal calculateTotalAmount(List<TopCategories> categories) {
        return categories.stream()
                .map(TopCategories::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void calculatePercents(List<TopCategories> categories, BigDecimal total) {
        for (TopCategories dto : categories) {
            int percent = total.compareTo(BigDecimal.ZERO) > 0
                    ? dto.getTotalAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(total, 0, RoundingMode.HALF_UP)
                    .intValue()
                    : 0;
            dto.setPercent(percent);
        }

    }

    @Transactional
    public void deleteTransaction(UUID transactionId, UUID userId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        Wallet wallet = transaction.getWallet();
        if (wallet == null || wallet.getUser() == null || !wallet.getUser().getId().equals(userId)) {
            throw new SecurityException("You are not authorized to delete this transaction");
        }

        wallet.getTransactions().remove(transaction);
        walletRepository.save(wallet);
    }





}

