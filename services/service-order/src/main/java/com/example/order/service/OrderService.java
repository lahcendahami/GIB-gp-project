package com.example.order.service;

import com.example.shared.exception.BusinessException;
import com.example.shared.exception.ResourceNotFoundException;
import com.example.order.dto.OrderRequest;
import com.example.order.dto.OrderStatusRequest;
import com.example.order.model.Order;
import com.example.order.model.OrderStatus;
import com.example.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Transactional
    public Order create(OrderRequest request) {
        Order order = Order.builder()
                .userId(request.getUserId())
                .productName(request.getProductName())
                .quantity(request.getQuantity())
                .totalPrice(request.getTotalPrice())
                .status(OrderStatus.PENDING)
                .build();
        Order saved = orderRepository.save(order);
        log.info("Created order id={} userId={}", saved.getId(), saved.getUserId());
        return saved;
    }

    @Transactional
    public Order updateStatus(Long id, OrderStatusRequest request) {
        Order order = findById(id);

        // Guard: cannot revert a cancelled or delivered order
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Cannot update a cancelled order.");
        }
        if (order.getStatus() == OrderStatus.DELIVERED
                && request.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException("Cannot revert a delivered order.");
        }

        order.setStatus(request.getStatus());
        order.setUpdatedAt(LocalDateTime.now());
        log.info("Order id={} status changed to {}", id, request.getStatus());
        return orderRepository.save(order);
    }

    @Transactional
    public void cancel(Long id) {
        Order order = findById(id);
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException("Cannot cancel a delivered order.");
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        log.info("Cancelled order id={}", id);
    }
}
