package app.subscription.repository;

import app.subscription.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionsRepository extends JpaRepository<Subscription, UUID> {
    List<Subscription> findAllByUser_UsernameOrderByExpiryOnAsc(String username);


    Optional<Subscription> findByIdAndUser_Username(UUID id, String username);

    List<Subscription> findByExpiryOn(LocalDate localDate);

    List<Subscription> findByUser_IdOrderByExpiryOn(UUID userId);

    List<Subscription> findByUser_IdAndExpiryOnBeforeOrderByExpiryOn(UUID userId, LocalDate limit);
}
