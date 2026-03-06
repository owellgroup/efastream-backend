package com.efastream.repositories;

import com.efastream.models.entity.Subscription;
import com.efastream.models.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUserIdOrderByEndDateDesc(Long userId);

    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.status = :status ORDER BY s.endDate DESC")
    Optional<Subscription> findActiveByUserId(@Param("userId") Long userId, @Param("status") SubscriptionStatus status);

    boolean existsByUserIdAndStatus(Long userId, SubscriptionStatus status);
}
