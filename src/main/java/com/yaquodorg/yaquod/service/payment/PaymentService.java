package com.yaquodorg.yaquod.service.payment;

import com.yaquodorg.yaquod.dtos.payment.CreateCheckoutResponse;
import com.yaquodorg.yaquod.dtos.payment.PayWithSavedCardResponse;
import com.yaquodorg.yaquod.dtos.payment.SavedCardDto;
import com.yaquodorg.yaquod.entity.User;
import java.util.List;

public interface PaymentService {
    CreateCheckoutResponse createCheckoutUrl(Long userId);

    PayWithSavedCardResponse payWithSavedCard(User user, double amountInEgp, Long savedCardId);

    void processPaymentCallback(String payload);

    List<SavedCardDto> getUserSavedCards(Long userId);

    void deleteSavedCard(Long cardId, Long userId);
}
