package com.yaquodorg.yaquod.service.payment;

import com.yaquodorg.yaquod.dtos.payment.CreateCheckoutResponse;
import com.yaquodorg.yaquod.dtos.payment.SavedCardDto;
import java.util.List;

public interface PaymentService {
    CreateCheckoutResponse createCheckoutUrl(Long userId);

    void processPaymentCallback(String payload);

    List<SavedCardDto> getUserSavedCards(Long userId);

    void deleteSavedCard(Long cardId, Long userId);
}
