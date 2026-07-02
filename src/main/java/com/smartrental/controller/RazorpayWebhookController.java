package com.smartrental.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartrental.model.dto.MarkPaidRequestDTO;
import com.smartrental.repository.PaymentRepository;
import com.smartrental.service.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhook", description = "External payment webhook handlers")
public class RazorpayWebhookController {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @Value("${razorpay.webhook-secret:}")
    private String webhookSecret;

    @PostMapping("/razorpay")
    public ResponseEntity<Map<String, String>> handleRazorpayWebhook(
            @RequestBody String body,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        log.info("Received Razorpay webhook");

        if (!verifySignature(body, signature)) {
            log.warn("Invalid Razorpay webhook signature");
            return ResponseEntity.status(401).body(Map.of("error", "Invalid signature"));
        }

        try {
            JsonNode event = objectMapper.readTree(body);
            String eventType = event.get("event").asText();
            log.info("Razorpay event: {}", eventType);

            if ("payment.captured".equals(eventType) || "order.paid".equals(eventType)) {
                JsonNode paymentNode = event.get("payload").get("payment").get("entity");
                String razorpayOrderId = paymentNode.get("order_id").asText();
                String razorpayPaymentId = paymentNode.get("id").asText();

                var paymentOpt = paymentRepository.findByRazorpayOrderId(razorpayOrderId);
                if (paymentOpt.isPresent()) {
                    var payment = paymentOpt.get();
                    MarkPaidRequestDTO request = MarkPaidRequestDTO.builder()
                            .razorpayPaymentId(razorpayPaymentId)
                            .paymentMode("RAZORPAY")
                            .paymentDate(LocalDate.now())
                            .build();
                    paymentService.markAsPaid(payment.getId(), request);
                    log.info("Payment {} marked as paid via Razorpay webhook", payment.getId());
                } else {
                    log.warn("No payment found for Razorpay order: {}", razorpayOrderId);
                }
            }

            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception e) {
            log.error("Error processing Razorpay webhook: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    private boolean verifySignature(String body, String signature) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("Razorpay webhook secret not configured — skipping signature verification");
            return true;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secret);
            byte[] hash = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return MessageDigest.isEqual(hexString.toString().getBytes(), signature.getBytes());
        } catch (Exception e) {
            log.error("Signature verification failed: {}", e.getMessage());
            return false;
        }
    }
}
