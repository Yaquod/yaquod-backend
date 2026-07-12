package com.yaquodorg.yaquod.controller;

import static com.yaquodorg.yaquod.response.ApiResponse.createSuccessResponse;

import com.yaquodorg.yaquod.dtos.payment.ChargeSavedCardDirectRequest;
import com.yaquodorg.yaquod.dtos.payment.ChargeSavedCardDirectResponse;
import com.yaquodorg.yaquod.dtos.payment.CreateCheckoutResponse;
import com.yaquodorg.yaquod.dtos.payment.OneTimePaymentRequest;
import com.yaquodorg.yaquod.dtos.payment.PayWithSavedCardRequest;
import com.yaquodorg.yaquod.dtos.payment.PayWithSavedCardResponse;
import com.yaquodorg.yaquod.dtos.payment.PaymentDto;
import com.yaquodorg.yaquod.dtos.payment.SavedCardDto;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.response.ApiResponse;
import com.yaquodorg.yaquod.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment", description = "Payment APIs for Paymob integration")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/cards")
    @Operation(
            summary = "Add new card",
            description =
                    "Creates a Paymob intention and returns unified checkout URL for user to add"
                            + " their card")
    public ResponseEntity<ApiResponse<CreateCheckoutResponse>> addCard(
            @AuthenticationPrincipal User user) {
        log.info("Received add card request for user: {}", user.getId());

        CreateCheckoutResponse response = paymentService.createCardTokenizationCheckout(user);
        return ResponseEntity.ok(createSuccessResponse(response));
    }

    @PostMapping("/one-time")
    @Operation(
            summary = "One-time payment",
            description = "Creates a payment intention with variable amount for one-time payment")
    public ResponseEntity<ApiResponse<CreateCheckoutResponse>> createOneTimePayment(
            @AuthenticationPrincipal User user, @Valid @RequestBody OneTimePaymentRequest request) {
        log.info(
                "One-time payment for user: {}, amount: {} EGP", user.getId(), request.getAmount());

        CreateCheckoutResponse response =
                paymentService.createOneTimePayment(user, request.getAmount());
        return ResponseEntity.ok(createSuccessResponse(response));
    }

    @PostMapping("/cit")
    @Operation(
            summary = "Pay with saved card (CIT)",
            description = "Customer Initiated Transaction - requires 3DS verification")
    public ResponseEntity<ApiResponse<PayWithSavedCardResponse>> payWithSavedCard(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PayWithSavedCardRequest request) {
        log.info("CIT payment for user: {}, amount: {} EGP", user.getId(), request.getAmount());

        PayWithSavedCardResponse response =
                paymentService.payWithSavedCard(
                        user, request.getAmount(), request.getSavedCardId());
        return ResponseEntity.ok(createSuccessResponse(response));
    }

    @PostMapping("/mit")
    @Operation(
            summary = "Direct charge (MIT)",
            description = "Merchant Initiated Transaction - no 3DS required")
    public ResponseEntity<ApiResponse<ChargeSavedCardDirectResponse>> chargeSavedCardDirectly(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChargeSavedCardDirectRequest request) {
        log.info(
                "MIT direct charge for user: {}, amount: {} EGP",
                user.getId(),
                request.getAmount());

        ChargeSavedCardDirectResponse response =
                paymentService.chargeSavedCardDirectly(
                        user,
                        request.getAmount(),
                        request.getSavedCardId(),
                        request.getRequestId());
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

    @GetMapping
    @Operation(
            summary = "Get paginated payments for current user",
            description =
                    "Retrieves paginated payments for the currently authenticated user, sorted by"
                            + " creation date descending")
    public ResponseEntity<ApiResponse<Page<PaymentDto>>> getUserPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user) {
        log.info("Getting paginated payments for user: {}", user.getId());

        Page<PaymentDto> payments =
                paymentService.getUserPaymentsPaginated(
                        PageRequest.of(page, size, Sort.by("createdAt").descending()),
                        user.getId());
        return ResponseEntity.ok(createSuccessResponse(payments));
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
