package com.revamp.booking.service;

import com.revamp.booking.dto.AppointmentRequest;
import com.revamp.booking.model.Booking;
import com.revamp.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final TimeslotClient timeslotClient;

    public Booking createAppointment(String customerId, String customerName, AppointmentRequest req) {
        Booking booking = new Booking();
        booking.setCustomerId(customerId);
        booking.setCustomerName(customerName);
        booking.setServiceType(req.getServiceType());
        booking.setDate(LocalDate.parse(req.getDate()));
        booking.setInstructions(req.getInstructions());
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());
        booking.setStatus("pending");

        if (req.getVehicleId() != null) {
            booking.setVehicleId(req.getVehicleId());
        }
        if (req.getVehicleDetails() != null) {
            Booking.VehicleDetails vd = new Booking.VehicleDetails();
            vd.setMake(req.getVehicleDetails().getMake());
            vd.setModel(req.getVehicleDetails().getModel());
            vd.setYear(req.getVehicleDetails().getYear());
            vd.setRegistrationNumber(req.getVehicleDetails().getRegistrationNumber());
            booking.setVehicleDetails(vd);
        }

        if ("Service".equalsIgnoreCase(req.getServiceType())) {
            if (req.getTimeSlotId() == null || req.getTimeSlotId().isBlank()) {
                throw new IllegalArgumentException("timeSlotId is required for service bookings");
            }
            boolean booked = timeslotClient.bookSlot(req.getTimeSlotId());
            if (!booked) {
                throw new IllegalStateException("Time slot is already booked");
            }
            booking.setTimeSlotId(req.getTimeSlotId());
            // Optionally fetch slot for start/end times via availability list (not mandatory for MVP)
        } else {
            booking.setNeededModifications(req.getNeededModifications());
            booking.setEstimatedTimeHours(req.getEstimatedTimeHours());
            booking.setEstimatedCost(req.getEstimatedCost());
        }

        return bookingRepository.save(booking);
    }
}
