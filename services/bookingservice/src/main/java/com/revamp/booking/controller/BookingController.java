package com.revamp.booking.controller;

import com.revamp.booking.dto.AppointmentRequest;
import com.revamp.booking.dto.AppointmentResponse;
import com.revamp.booking.model.Booking;
import com.revamp.booking.model.ModificationItem;
import com.revamp.booking.repository.BookingRepository;
import com.revamp.booking.repository.ModificationItemRepository;
import com.revamp.booking.service.BookingService;
import com.revamp.booking.service.StripeService;
import com.revamp.booking.util.JwtUtil;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final ModificationItemRepository modificationItemRepository;
    private final BookingRepository bookingRepository;
    private final StripeService stripeService;
    private final JwtUtil jwtUtil;

    @GetMapping("/modifications")
    public List<ModificationItem> listModifications() {
        return modificationItemRepository.findAll();
    }

    @PostMapping("/bookings/appointments")
    public ResponseEntity<AppointmentResponse> createAppointment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody AppointmentRequest request
    ) {
        String customerId;
        String customerName;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                Claims claims = jwtUtil.parseToken(authHeader);
                customerId = jwtUtil.getCustomerId(claims);
                customerName = jwtUtil.getCustomerName(claims);
            } catch (Exception e) {
                return ResponseEntity.status(401).build();
            }
        } else {
            return ResponseEntity.status(401).build();
        }

        Booking saved = bookingService.createAppointment(customerId, customerName, request);
        return ResponseEntity.ok(new AppointmentResponse(saved.getId(), saved.getStatus()));
    }

    @PostMapping("/bookings/{bookingId}/payment-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(
            @PathVariable String bookingId,
            @RequestBody PaymentIntentRequest req
    ) throws StripeException {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        Long amount = booking.getEstimatedCost() != null ? booking.getEstimatedCost().longValue() : req.getAmount();
        PaymentIntent intent = stripeService.createPaymentIntent(amount, "lkr", bookingId);

        Map<String, String> response = new HashMap<>();
        response.put("clientSecret", intent.getClientSecret());
        response.put("paymentIntentId", intent.getId());
        return ResponseEntity.ok(response);
    }

    @Data
    public static class PaymentIntentRequest {
        private Long amount;
    }
}
