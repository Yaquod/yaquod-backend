package com.yaquodorg.yaquod.service.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.yaquodorg.yaquod.repository.UserRepository;
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
    private final UserRepository userRepository;

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

    @Value("${payment.paymob.notification-url}")
    private String notificationUrl;

    @Value("${payment.paymob.redirection-url}")
    private String redirectionUrl;

    private static final String UNIFIED_CHECKOUT_URL = "https://accept.paymob.com/unifiedcheckout/";

    @Override
    @Transactional
    public CreateCheckoutResponse createCheckoutUrl(Long userId) {
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
    public PayWithSavedCardResponse payWithSavedCard(User user, double amountInEgp, Long savedCardId) {
        log.info("Pay with saved card for user: {}, amount: {} EGP", user.getId(), amountInEgp);

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

        Map<String, Object> intentionRequest = buildCitIntentionRequest(user, savedCard, amountInCents);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Token " + secretKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(intentionRequest, headers);

        String url = paymobBaseUrl + "/v1/intention/";
        log.info("Calling Paymob CIT Intention API: {}", url);

        String response = restTemplate.postForObject(url, request, String.class);

        return parseCitIntentionResponse(response);
    }

    private Map<String, Object> buildCitIntentionRequest(User user, SavedCard savedCard, int amountInCents) {
        Map<String, Object> billingData = new HashMap<>();
        billingData.put("first_name", user.getFirstName() != null ? user.getFirstName() : "User");
        billingData.put("last_name", user.getLastName() != null ? user.getLastName() : "Name");
        billingData.put("email", user.getEmail());
        billingData.put("phone_number", user.getPhoneNumber() != null ? user.getPhoneNumber() : "+20000000000");

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

            String checkoutUrl = UNIFIED_CHECKOUT_URL + "?publicKey=" + publicKey + "&clientSecret=" + clientSecret;

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
            // TODO: The orderId is extracted assuming it's a transaction callback not a token
            // callback.
            String orderId = root.path("obj").path("order").path("id").asText();
            String transactionId = root.path("obj").path("id").asText();
            int amountCents = root.path("obj").path("amount_cents").asInt();

            if ("TOKEN".equals(type)) {
                processCardTokenCallback(root.path("obj"), orderId);
            } else if ("TRANSACTION".equals(type)) {
                processTransactionCallback(root.path("obj"), orderId, transactionId, amountCents);
            }

        } catch (Exception e) {
            log.error("Failed to process payment callback", e);
            throw new RuntimeException("Failed to process callback", e);
        }
    }

    private void processCardTokenCallback(JsonNode obj, String orderId) {
        log.info("Processing card token callback, orderId: {}", orderId);

        String token = obj.path("token").asText();
        String maskedPan = obj.path("masked_pan").asText();
        String cardSubtype = obj.path("card_subtype").asText();
        String email = obj.path("email").asText();

        if (token == null || token.isEmpty()) {
            log.warn("No token found in callback");
            return;
        }

        User user = userService.getUserByEmail(email);

        if (savedCardRepository.existsByUserIdAndToken(user.getId(), token)) {
            log.info("Card token already exists for user: {}", user.getId());
            return;
        }

        SavedCard savedCard =
                SavedCard.builder()
                        .token(token)
                        .maskedPan(maskedPan)
                        .cardSubtype(cardSubtype)
                        .paymobOrderId(orderId)
                        .user(user)
                        .build();

        savedCardRepository.save(savedCard);
        log.info("Saved card token for user: {}, orderId: {}", user.getId(), orderId);
    }

    private void processTransactionCallback(
            JsonNode obj, String orderId, String transactionId, int amountCents) {
        log.info(
                "Processing transaction callback, orderId: {}, transactionId: {}",
                orderId,
                transactionId);

        String email = obj.path("payment_key_claims").path("billing_data").path("email").asText();

        User user = userService.getUserByEmail(email);

        Payment payment = paymentRepository.findByPaymobOrderId(orderId).orElse(null);

        if (payment == null) {
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
