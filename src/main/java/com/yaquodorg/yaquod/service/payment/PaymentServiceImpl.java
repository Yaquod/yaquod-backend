package com.yaquodorg.yaquod.service.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaquodorg.yaquod.dtos.payment.ChargeSavedCardDirectResponse;
import com.yaquodorg.yaquod.dtos.payment.CreateCheckoutResponse;
import com.yaquodorg.yaquod.dtos.payment.PayWithSavedCardResponse;
import com.yaquodorg.yaquod.dtos.payment.SavedCardDto;
import com.yaquodorg.yaquod.entity.Payment;
import com.yaquodorg.yaquod.entity.PaymentStatus;
import com.yaquodorg.yaquod.entity.SavedCard;
import com.yaquodorg.yaquod.entity.User;
import com.yaquodorg.yaquod.exception.ResourceNotFoundException;
import com.yaquodorg.yaquod.repository.PaymentRepository;
import com.yaquodorg.yaquod.repository.SavedCardRepository;
import com.yaquodorg.yaquod.service.user.UserService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final UserService userService;

    private final SavedCardRepository savedCardRepository;
    private final PaymentRepository paymentRepository;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${payment.paymob.base-url}")
    private String paymobBaseUrl;

    @Value("${payment.paymob.api-key}")
    private String apiKey;

    @Value("${payment.paymob.secret-key}")
    private String secretKey;

    @Value("${payment.paymob.public-key}")
    private String publicKey;

    @Value("${payment.paymob.integration-id}")
    private String integrationId;

    @Value("${payment.paymob.moto-integration-id}")
    private String motoIntegrationId;

    @Value("${payment.paymob.notification-url}")
    private String notificationUrl;

    @Value("${payment.paymob.redirection-url}")
    private String redirectionUrl;

    private static final String UNIFIED_CHECKOUT_URL = "https://accept.paymob.com/unifiedcheckout/";

    @Override
    @Transactional
    public CreateCheckoutResponse createCardTokenizationCheckout(Long userId) {
        log.info("Creating checkout URL for user: {}", userId);

        User user = userService.getUserById(userId);

        Map<String, Object> intentionRequest = buildIntentionRequest(user);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Token " + secretKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(intentionRequest, headers);

        String url = paymobBaseUrl + "/v1/intention/";
        log.info("Calling Paymob Create Intention API: {}", url);

        String response = restTemplate.postForObject(url, request, String.class);

        return parseIntentionResponse(response);
    }

    @Override
    @Transactional
    public CreateCheckoutResponse createOneTimePayment(User user, double amountInEgp) {
        log.info(
                "Creating one-time payment for user: {}, amount: {} EGP",
                user.getId(),
                amountInEgp);

        int amountInCents = (int) (amountInEgp * 100);

        Map<String, Object> intentionRequest = buildOneTimeIntentionRequest(user, amountInCents);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Token " + secretKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(intentionRequest, headers);

        String url = paymobBaseUrl + "/v1/intention/";
        log.info("Calling Paymob Create Intention API: {}", url);

        String response = restTemplate.postForObject(url, request, String.class);

        return parseIntentionResponse(response);
    }

    private Map<String, Object> buildOneTimeIntentionRequest(User user, int amountInCents) {
        Map<String, Object> billingData = new HashMap<>();
        billingData.put("first_name", user.getFirstName() != null ? user.getFirstName() : "User");
        billingData.put("last_name", user.getLastName() != null ? user.getLastName() : "Name");
        billingData.put("email", user.getEmail());
        billingData.put(
                "phone_number",
                user.getPhoneNumber() != null ? user.getPhoneNumber() : "+20000000000");

        Map<String, Object> item = new HashMap<>();
        item.put("name", "One-time Payment");
        item.put("amount", amountInCents);
        item.put("description", "Payment for order");

        Map<String, Object> request = new HashMap<>();
        request.put("amount", amountInCents);
        request.put("currency", "EGP");
        request.put("payment_methods", List.of(Integer.parseInt(integrationId)));
        request.put("items", List.of(item));
        request.put("billing_data", billingData);
        request.put("notification_url", notificationUrl);
        request.put("redirection_url", redirectionUrl);

        return request;
    }

    @Override
    @Transactional
    public PayWithSavedCardResponse payWithSavedCard(
            User user, double amountInEgp, Long savedCardId) {
        log.info("CIT payment for user: {}, amount: {} EGP", user.getId(), amountInEgp);

        SavedCard savedCard;
        if (savedCardId != null) {
            savedCard = savedCardRepository.findById(savedCardId).orElse(null);
            if (savedCard == null || !savedCard.getUser().getId().equals(user.getId())) {
                throw new ResourceNotFoundException("Saved card not found");
            }
        } else {
            if (user.getSavedCards().isEmpty()) {
                throw new ResourceNotFoundException("No saved cards found for user");
            }
            savedCard = user.getSavedCards().get(0);
        }

        int amountInCents = (int) (amountInEgp * 100);

        Map<String, Object> intentionRequest =
                buildCitIntentionRequest(user, savedCard, amountInCents);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Token " + secretKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(intentionRequest, headers);

        String url = paymobBaseUrl + "/v1/intention/";
        log.info("Calling Paymob CIT Intention API: {}", url);

        String response = restTemplate.postForObject(url, request, String.class);

        return parseCitIntentionResponse(response);
    }

    @Override
    @Transactional
    public ChargeSavedCardDirectResponse chargeSavedCardDirectly(
            User user, double amountInEgp, Long savedCardId) {
        log.info("MIT direct charge for user: {}, amount: {} EGP", user.getId(), amountInEgp);

        SavedCard savedCard;
        if (savedCardId != null) {
            savedCard = savedCardRepository.findById(savedCardId).orElse(null);
            if (savedCard == null || !savedCard.getUser().getId().equals(user.getId())) {
                throw new ResourceNotFoundException("Saved card not found");
            }
        } else {
            if (user.getSavedCards().isEmpty()) {
                throw new ResourceNotFoundException("No saved cards found for user");
            }
            savedCard = user.getSavedCards().get(0);
        }

        int amountInCents = (int) (amountInEgp * 100);

        Map<String, Object> intentionRequest =
                buildMitIntentionRequest(user, savedCard, amountInCents);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Token " + secretKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(intentionRequest, headers);

        String intentionUrl = paymobBaseUrl + "/v1/intention/";
        log.info("Calling Paymob MIT Intention API: {}", intentionUrl);

        String intentionResponse = restTemplate.postForObject(intentionUrl, request, String.class);

        String paymentToken = extractPaymentToken(intentionResponse);
        String orderId = extractOrderId(intentionResponse);

        Map<String, Object> payRequest = new HashMap<>();
        Map<String, Object> source = new HashMap<>();
        source.put("identifier", savedCard.getToken());
        source.put("subtype", "TOKEN");
        payRequest.put("source", source);
        payRequest.put("payment_token", paymentToken);

        HttpEntity<Map<String, Object>> payHttpRequest = new HttpEntity<>(payRequest, headers);

        String payUrl = paymobBaseUrl + "/api/acceptance/payments/pay";
        log.info("Calling Paymob Pay API: {}", payUrl);

        String payResponse = restTemplate.postForObject(payUrl, payHttpRequest, String.class);

        return parseMitPayResponse(payResponse, orderId, user, savedCard, amountInCents);
    }

    private Map<String, Object> buildMitIntentionRequest(
            User user, SavedCard savedCard, int amountInCents) {
        Map<String, Object> billingData = new HashMap<>();
        billingData.put("first_name", user.getFirstName() != null ? user.getFirstName() : "User");
        billingData.put("last_name", user.getLastName() != null ? user.getLastName() : "Name");
        billingData.put("email", user.getEmail());
        billingData.put(
                "phone_number",
                user.getPhoneNumber() != null ? user.getPhoneNumber() : "+20000000000");

        Map<String, Object> item = new HashMap<>();
        item.put("name", "Trip Payment");
        item.put("amount", amountInCents);
        item.put("description", "Payment for trip");

        Map<String, Object> request = new HashMap<>();
        request.put("amount", amountInCents);
        request.put("currency", "EGP");
        request.put("payment_methods", List.of(Integer.parseInt(motoIntegrationId)));
        request.put("items", List.of(item));
        request.put("billing_data", billingData);
        request.put("card_tokens", List.of(savedCard.getToken()));

        return request;
    }

    private String extractPaymentToken(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("payment_keys").get(0).path("key").asText();
        } catch (Exception e) {
            log.error("Failed to extract payment token", e);
            throw new RuntimeException("Failed to extract payment token", e);
        }
    }

    private String extractOrderId(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("intention_order_id").asText();
        } catch (Exception e) {
            log.error("Failed to extract order ID", e);
            throw new RuntimeException("Failed to extract order ID", e);
        }
    }

    private ChargeSavedCardDirectResponse parseMitPayResponse(
            String responseBody,
            String orderId,
            User user,
            SavedCard savedCard,
            int amountInCents) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            String transactionId = root.path("id").asText();
            boolean success = root.path("success").asBoolean();
            String message = root.path("data").path("message").asText();

            if (success) {
                Payment payment =
                        Payment.builder()
                                .amount(
                                        new java.math.BigDecimal(amountInCents)
                                                .divide(new java.math.BigDecimal(100)))
                                .currency("EGP")
                                .status(PaymentStatus.PAID)
                                .paymobOrderId(orderId)
                                .paymobTransactionId(transactionId)
                                .user(user)
                                .savedCard(savedCard)
                                .paidAt(new java.sql.Timestamp(System.currentTimeMillis()))
                                .build();

                paymentRepository.save(payment);
                log.info(
                        "MIT payment successful, orderId: {}, transactionId: {}",
                        orderId,
                        transactionId);
            } else {
                log.warn("MIT payment failed: {}", message);
            }

            return ChargeSavedCardDirectResponse.builder()
                    .orderId(orderId)
                    .transactionId(transactionId)
                    .success(success)
                    .message(message)
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse MIT pay response", e);
            throw new RuntimeException("Failed to parse MIT pay response", e);
        }
    }

    private Map<String, Object> buildCitIntentionRequest(
            User user, SavedCard savedCard, int amountInCents) {
        Map<String, Object> billingData = new HashMap<>();
        billingData.put("first_name", user.getFirstName() != null ? user.getFirstName() : "User");
        billingData.put("last_name", user.getLastName() != null ? user.getLastName() : "Name");
        billingData.put("email", user.getEmail());
        billingData.put(
                "phone_number",
                user.getPhoneNumber() != null ? user.getPhoneNumber() : "+20000000000");

        Map<String, Object> item = new HashMap<>();
        item.put("name", "Trip Payment");
        item.put("amount", amountInCents);
        item.put("description", "Payment for trip");

        Map<String, Object> request = new HashMap<>();
        request.put("amount", amountInCents);
        request.put("currency", "EGP");
        request.put("payment_methods", List.of(Integer.parseInt(integrationId)));
        request.put("items", List.of(item));
        request.put("billing_data", billingData);
        request.put("card_tokens", List.of(savedCard.getToken()));
        request.put("notification_url", notificationUrl);
        request.put("redirection_url", redirectionUrl);

        return request;
    }

    private PayWithSavedCardResponse parseCitIntentionResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            String clientSecret = root.path("client_secret").asText();
            String orderId = root.path("intention_order_id").asText();

            String checkoutUrl =
                    UNIFIED_CHECKOUT_URL
                            + "?publicKey="
                            + publicKey
                            + "&clientSecret="
                            + clientSecret;

            return PayWithSavedCardResponse.builder()
                    .checkoutUrl(checkoutUrl)
                    .clientSecret(clientSecret)
                    .orderId(orderId)
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse Paymob CIT intention response", e);
            throw new RuntimeException("Failed to create checkout URL", e);
        }
    }

    private Map<String, Object> buildIntentionRequest(User user) {
        Map<String, Object> billingData = new HashMap<>();
        billingData.put("first_name", user.getFirstName() != null ? user.getFirstName() : "User");
        billingData.put("last_name", user.getLastName() != null ? user.getLastName() : "Name");
        billingData.put("email", user.getEmail());
        billingData.put(
                "phone_number",
                user.getPhoneNumber() != null ? user.getPhoneNumber() : "+20000000000");

        Map<String, Object> item = new HashMap<>();
        item.put("name", "Card Registration");
        item.put("amount", 100);
        item.put("description", "Card tokenization for future payments");

        Map<String, Object> request = new HashMap<>();
        request.put("amount", 100);
        request.put("currency", "EGP");
        request.put("payment_methods", List.of(Integer.parseInt(integrationId)));
        request.put("items", List.of(item));
        request.put("billing_data", billingData);
        request.put("notification_url", notificationUrl);
        request.put("redirection_url", redirectionUrl);

        return request;
    }

    private CreateCheckoutResponse parseIntentionResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            String clientSecret = root.path("client_secret").asText();
            String orderId = root.path("intention_order_id").asText();

            String checkoutUrl =
                    UNIFIED_CHECKOUT_URL
                            + "?publicKey="
                            + publicKey
                            + "&clientSecret="
                            + clientSecret;

            return CreateCheckoutResponse.builder()
                    .checkoutUrl(checkoutUrl)
                    .clientSecret(clientSecret)
                    .publicKey(publicKey)
                    .orderId(orderId)
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse Paymob intention response", e);
            throw new RuntimeException("Failed to create checkout URL", e);
        }
    }

    @Override
    @Transactional
    public void processPaymentCallback(String payload) {
        log.info("Processing payment callback from Paymob");

        try {
            JsonNode root = objectMapper.readTree(payload);
            String type = root.path("type").asText();

            if ("TOKEN".equals(type)) {
                processCardTokenCallback(root.path("obj"));
            } else if ("TRANSACTION".equals(type)) {
                processTransactionCallback(root.path("obj"));
            }

        } catch (Exception e) {
            log.error("Failed to process payment callback", e);
            throw new RuntimeException("Failed to process callback", e);
        }
    }

    private void processCardTokenCallback(JsonNode obj) {
        String orderId = obj.path("order_id").asText();
        String token = obj.path("token").asText();
        String maskedPan = obj.path("masked_pan").asText();
        String cardSubtype = obj.path("card_subtype").asText();
        String email = obj.path("email").asText();

        log.info("Processing card token callback, orderId: {}", orderId);

        if (token == null || token.isEmpty()) {
            log.warn("No token found in callback");
            return;
        }

        // TODO: The card holder name is not real but extracted from the user
        // email itself
        User user = userService.getUserByEmail(email);
        String firstName = user.getFirstName().toUpperCase();
        String lastName = user.getLastName().toUpperCase();
        String fullName =
                new StringBuilder().append(firstName).append(" ").append(lastName).toString();

        if (savedCardRepository.existsByUserIdAndToken(user.getId(), token)) {
            log.info("Card token already exists for user: {}", user.getId());
            return;
        }

        SavedCard savedCard =
                SavedCard.builder()
                        .token(token)
                        .maskedPan(maskedPan)
                        .cardholderName(fullName)
                        .cardSubtype(cardSubtype)
                        .paymobOrderId(orderId)
                        .user(user)
                        .build();

        savedCardRepository.save(savedCard);
        log.info("Saved card token for user: {}, orderId: {}", user.getId(), orderId);
    }

    private void processTransactionCallback(JsonNode obj) {
        String email = obj.path("payment_key_claims").path("billing_data").path("email").asText();
        String orderId = obj.path("order").path("id").asText();
        String transactionId = obj.path("id").asText();
        int amountCents = obj.path("amount_cents").asInt();

        log.info(
                "Processing transaction callback, orderId: {}, transactionId: {}",
                orderId,
                transactionId);

        User user = userService.getUserByEmail(email);

        Payment payment = paymentRepository.findByPaymobOrderId(orderId).orElse(null);

        if (payment == null) {
            // TODO: This logic always assume that the transaction was completed
            // using the user's main card and not the actual card the user used
            SavedCard savedCard = null;
            if (!user.getSavedCards().isEmpty()) {
                savedCard = user.getSavedCards().get(0);
            }

            payment =
                    Payment.builder()
                            .amount(
                                    new java.math.BigDecimal(amountCents)
                                            .divide(new java.math.BigDecimal(100)))
                            .currency("EGP")
                            .status(PaymentStatus.PAID)
                            .paymobOrderId(orderId)
                            .paymobTransactionId(transactionId)
                            .user(user)
                            .savedCard(savedCard)
                            .paidAt(new java.sql.Timestamp(System.currentTimeMillis()))
                            .build();

            paymentRepository.save(payment);
            log.info("Created new payment record for orderId: {}", orderId);
        } else {
            payment.setPaymobOrderId(orderId);
            payment.setPaymobTransactionId(transactionId);
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(new java.sql.Timestamp(System.currentTimeMillis()));
            paymentRepository.save(payment);
            log.info("Updated payment status to PAID for orderId: {}", orderId);
        }
    }

    @Override
    public List<SavedCardDto> getUserSavedCards(Long userId) {
        log.info("Getting saved cards for user: {}", userId);

        List<SavedCard> cards = savedCardRepository.findByUserId(userId);

        return cards.stream()
                .map(
                        card ->
                                SavedCardDto.builder()
                                        .id(card.getId())
                                        .maskedPan(card.getMaskedPan())
                                        .cardSubtype(card.getCardSubtype())
                                        .cardholderName(card.getCardholderName())
                                        .createdAt(card.getCreatedAt())
                                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteSavedCard(Long cardId, Long userId) {
        log.info("Deleting saved card: {} for user: {}", cardId, userId);

        SavedCard card =
                savedCardRepository
                        .findById(cardId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Saved card not found with id: " + cardId));

        if (!card.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Card does not belong to user");
        }

        savedCardRepository.delete(card);
        log.info("Deleted saved card: {}", cardId);
    }
}
