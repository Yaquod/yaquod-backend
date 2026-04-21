package com.yaquodorg.yaquod.dtos.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCheckoutResponse {
    private String checkoutUrl;
    private String clientSecret;
    private String publicKey;
    private String orderId;
}
