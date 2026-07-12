package com.yaquodorg.yaquod.repository;

import com.yaquodorg.yaquod.entity.Payment;
import com.yaquodorg.yaquod.entity.PaymentStatus;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findByUserId(Long userId, Pageable pageable);

    Optional<Payment> findByPaymobOrderId(String orderId);

    Optional<Payment> findByTripId(Long tripId);

    long countByStatus(PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status")
    BigDecimal sumAmountByStatus(PaymentStatus status);
}
