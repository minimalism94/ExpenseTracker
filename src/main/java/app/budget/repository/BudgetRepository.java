package app.budget.repository;

import app.budget.model.Budget;
import app.transactions.model.Category;
import app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    List<Budget> findByUserAndYearAndMonth(User user, int year, int month);

    Optional<Budget> findByUserAndCategoryAndYearAndMonth(User user, Category category, int year, int month);

}

