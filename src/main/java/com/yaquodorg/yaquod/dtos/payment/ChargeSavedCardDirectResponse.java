package com.yaquodorg.yaquod.dtos.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargeSavedCardDirectResponse {

    private String orderId;
    private String transactionId;
    private boolean success;
    private String message;
}
