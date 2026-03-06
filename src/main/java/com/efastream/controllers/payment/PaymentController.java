package com.efastream.controllers.payment;

import com.efastream.config.UnifiedUserDetails;
import com.efastream.models.dto.ApiResponse;
import com.efastream.models.dto.PaymentRequest;
import com.efastream.models.dto.PaymentResponse;
import com.efastream.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @AuthenticationPrincipal UnifiedUserDetails user,
            @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.createPayment(user.id(), request)));
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(@RequestParam String token) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.verifyAndActivateSubscription(token)));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getHistory(@AuthenticationPrincipal UnifiedUserDetails user) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentHistory(user.id())));
    }
}
