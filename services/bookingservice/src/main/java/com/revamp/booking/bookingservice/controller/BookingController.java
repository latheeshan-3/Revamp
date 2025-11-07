package com.revamp.booking.bookingservice.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.revamp.booking.bookingservice.model.Appointment;
import com.revamp.booking.bookingservice.service.BookingService;

@RestController
@RequestMapping("/api/bookings/appointments")
@CrossOrigin(origins = "*")
public class BookingController {

	@Autowired
	private BookingService bookingService;

	/**
	 * Create a new appointment
	 */
	@PostMapping
	public ResponseEntity<?> createAppointment(@RequestBody Appointment appointment) {
		try {
			Appointment created = bookingService.createAppointment(appointment);
			return ResponseEntity.ok(created);
		} catch (RuntimeException e) {
			// Return error message for validation errors
			Map<String, Object> errorResponse = new java.util.HashMap<>();
			errorResponse.put("message", e.getMessage());
			errorResponse.put("error", e.getClass().getSimpleName());
			return ResponseEntity.badRequest().body(errorResponse);
		} catch (Exception e) {
			Map<String, Object> errorResponse = new java.util.HashMap<>();
			errorResponse.put("message", "Failed to create appointment: " + e.getMessage());
			errorResponse.put("error", "InternalServerError");
			return ResponseEntity.status(500).body(errorResponse);
		}
	}

	/**
	 * Get appointment by ID
	 */
	@GetMapping("/{id}")
	public ResponseEntity<Appointment> getAppointmentById(@PathVariable String id) {
		try {
			return bookingService.getAppointmentById(id)
					.map(ResponseEntity::ok)
					.orElse(ResponseEntity.notFound().build());
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	/**
	 * Get all appointments
	 */
	@GetMapping
	public ResponseEntity<List<Appointment>> getAllAppointments() {
		try {
			List<Appointment> appointments = bookingService.getAllAppointments();
			System.out.println("DEBUG: Controller returning " + appointments.size() + " appointment(s)");
			return ResponseEntity.ok(appointments);
		} catch (Exception e) {
			System.err.println("ERROR: Failed to get appointments: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.badRequest().build();
		}
	}

	/**
	 * Get appointments by customer ID
	 */
	@GetMapping("/customer/{customerId}")
	public ResponseEntity<List<Appointment>> getAppointmentsByCustomerId(@PathVariable String customerId) {
		try {
			List<Appointment> appointments = bookingService.getAppointmentsByCustomerId(customerId);
			return ResponseEntity.ok(appointments);
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	/**
	 * Update appointment status
	 */
	@PutMapping("/{id}/status")
	public ResponseEntity<Appointment> updateAppointmentStatus(
			@PathVariable String id,
			@RequestBody Map<String, String> request) {
		try {
			String status = request.get("status");
			Appointment appointment = bookingService.updateAppointmentStatus(id, status);
			return ResponseEntity.ok(appointment);
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	/**
	 * Assign employees to appointment
	 */
	@PutMapping("/{id}/assign-employees")
	public ResponseEntity<Appointment> assignEmployees(
			@PathVariable String id,
			@RequestBody Map<String, Object> request) {
		try {
			@SuppressWarnings("unchecked")
			List<String> employeeIds = (List<String>) request.get("employeeIds");
			@SuppressWarnings("unchecked")
			List<String> employeeNames = (List<String>) request.get("employeeNames");
			
			Appointment appointment = bookingService.assignEmployees(id, employeeIds, employeeNames);
			return ResponseEntity.ok(appointment);
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	/**
	 * Cancel appointment
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<?> cancelAppointment(@PathVariable String id) {
		try {
			bookingService.cancelAppointment(id);
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	/**
	 * Get appointments by date range
	 */
	@GetMapping("/range")
	public ResponseEntity<List<Appointment>> getAppointmentsByDateRange(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		try {
			List<Appointment> appointments = bookingService.getAppointmentsByDateRange(startDate, endDate);
			return ResponseEntity.ok(appointments);
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
	}

	/**
	 * Validate booking before creation
	 * This endpoint can be called by other services to validate bookings
	 */
	@PostMapping("/validate")
	public ResponseEntity<Map<String, Object>> validateBooking(@RequestBody Map<String, Object> request) {
		try {
			String serviceType = (String) request.get("serviceType");
			String dateStr = (String) request.get("date");
			String timeSlotId = (String) request.get("timeSlotId");
			
			if (dateStr == null) {
				return ResponseEntity.badRequest()
					.body(Map.of("isValid", false, "message", "Date is required"));
			}
			
			LocalDate date = LocalDate.parse(dateStr);
			Map<String, Object> result = new java.util.HashMap<>();
			
			// Check if date is unavailable
			boolean isUnavailable = bookingService.isDateUnavailable(date);
			boolean isSunday = date.getDayOfWeek().getValue() == 7;
			
			if (isUnavailable || isSunday) {
				result.put("isValid", false);
				if (isUnavailable) {
					result.put("message", "Selected date is unavailable (holiday/maintenance)");
				} else {
					result.put("message", "Shop is closed on Sundays");
				}
				return ResponseEntity.ok(result);
			}
			
			// For Service type, validate time slot
			if ("Service".equals(serviceType)) {
				if (timeSlotId == null || timeSlotId.isEmpty()) {
					result.put("isValid", false);
					result.put("message", "Time slot ID is required for Service bookings");
					return ResponseEntity.ok(result);
				}
				
				// Check if time slot exists and is available
				Optional<com.revamp.booking.bookingservice.model.TimeSlot> slotOpt = 
					bookingService.getTimeSlotById(timeSlotId);
				
				if (!slotOpt.isPresent()) {
					result.put("isValid", false);
					result.put("message", "Time slot not found");
					return ResponseEntity.ok(result);
				}
				
				com.revamp.booking.bookingservice.model.TimeSlot slot = slotOpt.get();
				
				if (!slot.isAvailable()) {
					result.put("isValid", false);
					result.put("message", "Time slot is already booked");
					return ResponseEntity.ok(result);
				}
				
				if (!slot.getDate().equals(date)) {
					result.put("isValid", false);
					result.put("message", "Time slot date does not match selected date");
					return ResponseEntity.ok(result);
				}
			}
			
			// Validation passed
			result.put("isValid", true);
			result.put("message", "Booking is valid");
			return ResponseEntity.ok(result);
			
		} catch (Exception e) {
			return ResponseEntity.badRequest()
				.body(Map.of("isValid", false, "message", "Error validating booking: " + e.getMessage()));
		}
	}
}

