package app.transactions.repository;

import app.transactions.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("""
    SELECT t.category, SUM(t.amount)
    FROM Transaction t
    WHERE t.wallet.id = :walletId
    GROUP BY t.category
    ORDER BY SUM(t.amount) DESC
""")
    List<Object[]> findTopCategoriesByWallet(@Param("walletId") UUID walletId);



    List<Transaction> findByWalletId(UUID walletId);
}
