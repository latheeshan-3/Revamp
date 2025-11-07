package com.revamp.booking.bookingservice.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.revamp.booking.bookingservice.model.Appointment;
import com.revamp.booking.bookingservice.model.TimeSlot;

@Service
public class BookingService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private TimeSlotService timeSlotService;

	@Autowired
	private UnavailableDateService unavailableDateService;

	/**
	 * Create a new appointment
	 */
	public Appointment createAppointment(Appointment appointment) {
		// Check if date is unavailable (for both Service and Modification)
		if (unavailableDateService.isDateUnavailable(appointment.getDate())) {
			throw new RuntimeException("Selected date is unavailable (holiday/maintenance)");
		}
		
		// Check if it's Sunday (for both Service and Modification)
		if (appointment.getDate().getDayOfWeek().getValue() == 7) {
			throw new RuntimeException("Shop is closed on Sundays");
		}
		
		// For Service type, book the time slot
		if ("Service".equals(appointment.getServiceType())) {
			if (appointment.getTimeSlotId() == null || appointment.getTimeSlotId().isEmpty()) {
				throw new RuntimeException("Time slot ID is required for Service appointments");
			}
			
			// Book the time slot
			TimeSlot slot = timeSlotService.bookSlot(appointment.getTimeSlotId(), null);
			appointment.setTime(slot.getStartTime());
			appointment.setEndTime(slot.getEndTime());
			
			// Save appointment first to get ID
			appointment.setStatus("Pending");
			Appointment saved = mongoTemplate.save(appointment);
			
			// Update slot with appointment ID
			slot.setAppointmentId(saved.getId());
			mongoTemplate.save(slot);
			
			return saved;
		} else {
			// For Modification, can be booked any time during shop hours (8am-5pm)
			if (appointment.getTime() == null) {
				appointment.setTime(LocalTime.of(8, 0)); // Default to 8am
			}
			appointment.setEndTime(LocalTime.of(17, 0)); // End at 5pm
		}
		
		appointment.setStatus("Pending");
		return mongoTemplate.save(appointment);
	}

	/**
	 * Get appointment by ID
	 */
	public Optional<Appointment> getAppointmentById(String id) {
		Query query = new Query(Criteria.where("id").is(id));
		Appointment appointment = mongoTemplate.findOne(query, Appointment.class);
		return Optional.ofNullable(appointment);
	}

	/**
	 * Get all appointments
	 */
	public List<Appointment> getAllAppointments() {
		List<Appointment> appointments = mongoTemplate.findAll(Appointment.class);
		System.out.println("DEBUG: Found " + appointments.size() + " appointment(s) in database");
		for (Appointment apt : appointments) {
			System.out.println("DEBUG: Appointment ID: " + apt.getId() + ", Customer: " + apt.getCustomerName() + ", Date: " + apt.getDate());
		}
		return appointments;
	}

	/**
	 * Get appointments by customer ID
	 */
	public List<Appointment> getAppointmentsByCustomerId(String customerId) {
		Query query = new Query(Criteria.where("customerId").is(customerId));
		return mongoTemplate.find(query, Appointment.class);
	}

	/**
	 * Update appointment status
	 */
	public Appointment updateAppointmentStatus(String id, String status) {
		Query query = new Query(Criteria.where("id").is(id));
		Appointment appointment = mongoTemplate.findOne(query, Appointment.class);
		
		if (appointment == null) {
			throw new RuntimeException("Appointment not found");
		}
		
		appointment.setStatus(status);
		return mongoTemplate.save(appointment);
	}

	/**
	 * Assign employees to appointment
	 */
	public Appointment assignEmployees(String appointmentId, List<String> employeeIds, List<String> employeeNames) {
		Query query = new Query(Criteria.where("id").is(appointmentId));
		Appointment appointment = mongoTemplate.findOne(query, Appointment.class);
		
		if (appointment == null) {
			throw new RuntimeException("Appointment not found");
		}
		
		appointment.setAssignedEmployeeIds(employeeIds);
		appointment.setAssignedEmployeeNames(employeeNames);
		appointment.setStatus("Approved");
		
		return mongoTemplate.save(appointment);
	}

	/**
	 * Cancel appointment and release time slot if applicable
	 */
	public void cancelAppointment(String appointmentId) {
		Query query = new Query(Criteria.where("id").is(appointmentId));
		Appointment appointment = mongoTemplate.findOne(query, Appointment.class);
		
		if (appointment != null) {
			// Release time slot if it's a Service appointment
			if ("Service".equals(appointment.getServiceType()) && appointment.getTimeSlotId() != null) {
				timeSlotService.releaseSlot(appointment.getTimeSlotId());
			}
			
			mongoTemplate.remove(appointment);
		}
	}

	/**
	 * Get appointments by date range
	 */
	public List<Appointment> getAppointmentsByDateRange(LocalDate startDate, LocalDate endDate) {
		Query query = new Query(Criteria.where("date").gte(startDate).lte(endDate));
		return mongoTemplate.find(query, Appointment.class);
	}

	/**
	 * Check if a date is unavailable
	 */
	public boolean isDateUnavailable(LocalDate date) {
		return unavailableDateService.isDateUnavailable(date);
	}

	/**
	 * Get time slot by ID
	 */
	public Optional<TimeSlot> getTimeSlotById(String slotId) {
		return timeSlotService.getSlotById(slotId);
	}

	/**
	 * Book a time slot (for use by other services)
	 * This can be called when creating a booking in another service
	 */
	public TimeSlot bookTimeSlot(String slotId, String bookingId) {
		return timeSlotService.bookSlot(slotId, bookingId);
	}

	/**
	 * Release a time slot (for use by other services)
	 * This can be called when cancelling a booking in another service
	 */
	public void releaseTimeSlot(String slotId) {
		timeSlotService.releaseSlot(slotId);
	}
}

