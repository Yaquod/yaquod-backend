package com.yaquodorg.yaquod.repository;

import com.yaquodorg.yaquod.entity.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymobOrderId(String orderId);
}
