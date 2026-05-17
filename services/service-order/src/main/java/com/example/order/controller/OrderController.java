package com.example.order.controller;

import com.example.shared.dto.ApiResponse;
import com.example.order.dto.OrderRequest;
import com.example.order.dto.OrderStatusRequest;
import com.example.order.model.Order;
import com.example.order.model.OrderStatus;
import com.example.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(orderService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.findById(id)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Order>>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.findByUserId(userId)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Order>>> getByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.findByStatus(status)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Order>> create(@Valid @RequestBody OrderRequest request) {
        Order created = orderService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Order placed successfully", created));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Order>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated", orderService.updateStatus(id, request)));
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return ResponseEntity.ok(ApiResponse.ok("Order cancelled btw ", null));
    }
}
