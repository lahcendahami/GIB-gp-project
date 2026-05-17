package com.example.payment.service;

import com.example.payment.dto.PaymentRequest;
import com.example.payment.model.Payment;
import com.example.payment.model.PaymentStatus;
import com.example.payment.repository.PaymentRepository;
import com.example.shared.exception.BusinessException;
import com.example.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void create_ShouldSavePayment() {
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(1L);
        request.setAmount(BigDecimal.valueOf(99.99));
        request.setPaymentMethod("CREDIT_CARD");

        Payment savedPayment = Payment.builder()
                .id(1L)
                .orderId(1L)
                .amount(BigDecimal.valueOf(99.99))
                .paymentMethod("CREDIT_CARD")
                .status(PaymentStatus.PENDING)
                .build();
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        Payment result = paymentService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(PaymentStatus.PENDING, result.getStatus());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void processPayment_WhenPending_ShouldComplete() {
        Payment payment = Payment.builder()
                .id(1L)
                .status(PaymentStatus.PENDING)
                .build();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        Payment result = paymentService.processPayment(1L);

        assertEquals(PaymentStatus.COMPLETED, result.getStatus());
    }

    @Test
    void processPayment_WhenNotPending_ShouldThrowException() {
        Payment payment = Payment.builder()
                .id(1L)
                .status(PaymentStatus.COMPLETED)
                .build();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThrows(BusinessException.class, () -> paymentService.processPayment(1L));
    }

    @Test
    void refundPayment_WhenNotCompleted_ShouldThrowException() {
        Payment payment = Payment.builder()
                .id(1L)
                .status(PaymentStatus.PENDING)
                .build();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThrows(BusinessException.class, () -> paymentService.refundPayment(1L));
    }

    @Test
    void findById_WhenNotFound_ShouldThrowException() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.findById(1L));
    }
}
