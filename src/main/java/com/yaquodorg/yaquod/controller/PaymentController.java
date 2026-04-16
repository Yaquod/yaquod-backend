package com.yaquodorg.yaquod.controller;

import static com.yaquodorg.yaquod.response.ApiResponse.createSuccessResponse;

import com.yaquodorg.yaquod.dtos.payment.CreateCheckoutResponse;
import com.yaquodorg.yaquod.dtos.payment.SavedCardDto;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.response.ApiResponse;
import com.yaquodorg.yaquod.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment", description = "Payment APIs for Paymob integration")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/checkout-url")
    @Operation(
            summary = "Create checkout URL for adding card",
            description =
                    "Creates a Paymob intention and returns unified checkout URL for user to add"
                            + " their card")
    public ResponseEntity<ApiResponse<CreateCheckoutResponse>> createCheckout(
            @AuthenticationPrincipal User user) {
        log.info("Received create checkout request for user: {}", user.getId());

        CreateCheckoutResponse response = paymentService.createCheckoutUrl(user.getId());
        return ResponseEntity.ok(createSuccessResponse(response));
    }

    @PostMapping("/webhook")
    @Operation(
            summary = "Paymob webhook callback",
            description = "Endpoint for Paymob to send payment and card token callbacks")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload) {
        log.info("Received webhook from Paymob");

        paymentService.processPaymentCallback(payload);
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/saved-cards")
    @Operation(
            summary = "Get user's saved cards",
            description = "Returns list of saved cards for a user")
    public ResponseEntity<ApiResponse<List<SavedCardDto>>> getSavedCards(
            @AuthenticationPrincipal User user) {
        log.info("Getting saved cards for user: {}", user.getId());

        List<SavedCardDto> cards = paymentService.getUserSavedCards(user.getId());
        return ResponseEntity.ok(createSuccessResponse(cards));
    }

    @DeleteMapping("/saved-cards/{cardId}")
    @Operation(summary = "Delete a saved card", description = "Deletes a saved card for a user")
    public ResponseEntity<ApiResponse<Void>> deleteSavedCard(
            @PathVariable Long cardId, @AuthenticationPrincipal User user) {
        log.info("Deleting saved card: {} for user: {}", cardId, user.getId());

        paymentService.deleteSavedCard(cardId, user.getId());
        return ResponseEntity.ok(createSuccessResponse(null));
    }
}
