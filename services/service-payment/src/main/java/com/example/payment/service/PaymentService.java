package com.example.payment.service;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.model.Payment;
import com.example.payment.model.PaymentStatus;
import com.example.payment.repository.PaymentRepository;
import com.example.shared.exception.BusinessException;
import com.example.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    public Payment findById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
    }

    public List<Payment> findByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Transactional
    public Payment create(PaymentRequest request) {
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .transactionRef(UUID.randomUUID().toString())
                .build();
        Payment saved = paymentRepository.save(payment);
        log.info("Created payment id={} orderId={} amount={}", saved.getId(), saved.getOrderId(), saved.getAmount());
        return saved;
    }

    @Transactional
    public Payment processPayment(Long id) {
        Payment payment = findById(id);
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("Payment is not in PENDING status, current: " + payment.getStatus());
        }
        payment.setStatus(PaymentStatus.COMPLETED);
        log.info("Processed payment id={}", id);
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment refundPayment(Long id) {
        Payment payment = findById(id);
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BusinessException("Only COMPLETED payments can be refunded, current: " + payment.getStatus());
        }
        payment.setStatus(PaymentStatus.REFUNDED);
        log.info("Refunded payment id={}", id);
        return paymentRepository.save(payment);
    }
}
