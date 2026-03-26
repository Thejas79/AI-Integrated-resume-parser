package com.tap.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.tap.model.Payment;
import com.tap.model.User;
import com.tap.repository.PaymentRepository;
import com.tap.repository.UserRepository;
import com.tap.util.JwtUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Map;

@CrossOrigin(origins = "https://benevolent-sunburst-76e62d.netlify.app")
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Autowired private UserRepository userRepo;
    @Autowired private PaymentRepository paymentRepo;
    @Autowired private JwtUtil jwtUtil;

    // Step 1 — Create Razorpay order
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestHeader("Authorization") String bearer) {
        try {
            Long userId = jwtUtil.getUserId(bearer);
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            JSONObject opts = new JSONObject();
            opts.put("amount", 5000);        // ₹50 in paise
            opts.put("currency", "INR");
            opts.put("receipt", "rcpt_" + userId + "_" + System.currentTimeMillis());

            Order order = client.orders.create(opts);
            String orderId = order.get("id");

            // Save pending payment record
            Payment p = new Payment();
            p.setUserId(userId);
            p.setRazorpayOrderId(orderId);
            p.setStatus("CREATED");
            paymentRepo.save(p);

            return ResponseEntity.ok(Map.of(
                    "orderId",  orderId,
                    "amount",   "5000",
                    "currency", "INR",
                    "name",     user.getName() != null ? user.getName() : "",
                    "email",    user.getEmail()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Step 2 — Verify signature and add credits
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestHeader("Authorization") String bearer,
            @RequestBody Map<String, String> body) {
        try {
            Long userId = jwtUtil.getUserId(bearer);

            String orderId    = body.get("razorpay_order_id");
            String paymentId  = body.get("razorpay_payment_id");
            String signature  = body.get("razorpay_signature");

            // Verify HMAC-SHA256 signature
            String data     = orderId + "|" + paymentId;
            Mac mac         = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keySecret.getBytes(), "HmacSHA256"));
            String expected = HexFormat.of().formatHex(mac.doFinal(data.getBytes()));

            if (!expected.equals(signature)) {
                return ResponseEntity.status(400).body(Map.of("error", "Invalid payment signature"));
            }

            // Update payment record
            Payment p = paymentRepo.findByRazorpayOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            p.setRazorpayPaymentId(paymentId);
            p.setRazorpaySignature(signature);
            p.setStatus("VERIFIED");
            p.setExpiresAt(LocalDateTime.now().plusWeeks(1));  // 1 week validity
            paymentRepo.save(p);

            // Add 5 credits to user
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setPaidChecks(user.getPaidChecks() + 5);
            userRepo.save(user);

            return ResponseEntity.ok(Map.of(
                    "success",    true,
                    "paidChecks", user.getPaidChecks(),
                    "freeChecks", user.getFreeChecks()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}