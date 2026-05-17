package com.example.payment.controller;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.model.Payment;
import com.example.payment.service.PaymentService;
import com.example.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Payment>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Payment>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.findById(id)));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<Payment>>> getByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.findByOrderId(orderId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Payment>> create(@Valid @RequestBody PaymentRequest request) {
        Payment created = paymentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Payment created successfully", created));
    }

    @PutMapping("/{id}/process")
    public ResponseEntity<ApiResponse<Payment>> process(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Payment processed", paymentService.processPayment(id)));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<Payment>> refund(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Payment refunded", paymentService.refundPayment(id)));
    }
}
