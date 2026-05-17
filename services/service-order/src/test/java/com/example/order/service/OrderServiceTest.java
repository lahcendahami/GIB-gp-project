package com.example.order.service;

import com.example.order.dto.OrderRequest;
import com.example.order.dto.OrderStatusRequest;
import com.example.order.model.Order;
import com.example.order.model.OrderStatus;
import com.example.order.repository.OrderRepository;
import com.example.shared.exception.BusinessException;
import com.example.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void create_ShouldSaveAndReturnOrder() {
        OrderRequest request = new OrderRequest();
        request.setUserId(1L);
        request.setProductName("Test Product");
        request.setQuantity(2);
        request.setTotalPrice(java.math.BigDecimal.valueOf(100.0));

        Order savedOrder = Order.builder().id(100L).userId(1L).status(OrderStatus.PENDING).build();
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        Order result = orderService.create(request);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void updateStatus_WhenCancelled_ShouldThrowException() {
        Order order = Order.builder().id(1L).status(OrderStatus.CANCELLED).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderStatusRequest request = new OrderStatusRequest();
        request.setStatus(OrderStatus.DELIVERED);

        assertThrows(BusinessException.class, () -> orderService.updateStatus(1L, request));
    }

    @Test
    void findById_WhenNotFound_ShouldThrowException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.findById(1L));
    }
}
