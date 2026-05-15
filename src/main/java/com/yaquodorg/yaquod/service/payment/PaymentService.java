package com.yaquodorg.yaquod.service.payment;

import com.yaquodorg.yaquod.dtos.payment.ChargeSavedCardDirectResponse;
import com.yaquodorg.yaquod.dtos.payment.CreateCheckoutResponse;
import com.yaquodorg.yaquod.dtos.payment.PayWithSavedCardResponse;
import com.yaquodorg.yaquod.dtos.payment.SavedCardDto;
import com.yaquodorg.yaquod.entity.User;
import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    CreateCheckoutResponse createCardTokenizationCheckout(User user);

    CreateCheckoutResponse createOneTimePayment(User user, BigDecimal amountInEgp);

    PayWithSavedCardResponse payWithSavedCard(User user, BigDecimal amountInEgp, Long savedCardId);

    ChargeSavedCardDirectResponse chargeSavedCardDirectly(
            User user, BigDecimal amountInEgp, Long savedCardId, Long requestId);

    void processPaymentCallback(String payload);

    List<SavedCardDto> getUserSavedCards(Long userId);

    void deleteSavedCard(Long cardId, Long userId);
}
