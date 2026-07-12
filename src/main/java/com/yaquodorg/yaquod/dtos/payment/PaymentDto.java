package com.yaquodorg.yaquod.dtos.payment;

import com.yaquodorg.yaquod.entity.PaymentStatus;
import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private Long id;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String paymobTransactionId;
    private Timestamp createdAt;
    private Timestamp paidAt;
    private Long tripId;
}
