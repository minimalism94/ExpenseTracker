package app.transactions.service;

import app.exception.CustomException;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {


    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

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



}

