package app.transactions.repository;

import app.transactions.model.Transaction;
import app.web.dto.TopCategories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("""
    SELECT new app.web.dto.TopCategories(t.category, SUM(t.amount), 0)
    FROM Transaction t
    WHERE t.wallet.id = :walletId AND t.type = app.transactions.model.Type.EXPENSE
    GROUP BY t.category
    ORDER BY SUM(t.amount) DESC
""")
    List<TopCategories> topCategories(@Param("walletId") UUID walletId);



    List<Transaction> findByWalletId(UUID walletId);


}

