package com.yaquodorg.yaquod.dtos.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargeSavedCardDirectRequest {

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @Positive(message = "Saved Card ID must be positive")
    private Long savedCardId;

    @NotNull(message = "Request ID cannot be null")
    @Positive(message = "Request ID must be positive")
    private Long requestId;
}
