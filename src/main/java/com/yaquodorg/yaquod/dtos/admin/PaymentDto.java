package com.yaquodorg.yaquod.dtos.admin;

import com.yaquodorg.yaquod.entity.Payment;
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
    private String paymobOrderId;
    private String paymobTransactionId;
    private Timestamp createdAt;
    private Timestamp paidAt;
    private Long userId;
    private Long tripId;

    public static PaymentDto fromEntity(Payment p) {
        return PaymentDto.builder()
                .id(p.getId())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .status(p.getStatus())
                .paymobOrderId(p.getPaymobOrderId())
                .paymobTransactionId(p.getPaymobTransactionId())
                .createdAt(p.getCreatedAt())
                .paidAt(p.getPaidAt())
                .userId(p.getUser() != null ? p.getUser().getId() : null)
                .tripId(p.getTrip() != null ? p.getTrip().getId() : null)
                .build();
    }
}
