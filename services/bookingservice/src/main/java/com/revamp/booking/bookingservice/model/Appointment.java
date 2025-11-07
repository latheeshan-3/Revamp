package com.revamp.booking.bookingservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Document(collection = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
	@Id
	private String id;
	
	private String customerId;
	private String customerName;
	private String customerEmail;
	private String vehicle;
	private String serviceType; // "Service" or "Modification"
	private LocalDate date;
	private LocalTime time;
	private String status; // "Pending", "Approved", "In Progress", "Completed", "Delivered"
	private List<String> assignedEmployeeIds;
	private List<String> assignedEmployeeNames;
	private List<String> modifications; // For modification service
	private Double estimatedCost;
	private String timeSlotId; // For service bookings
	private LocalTime endTime; // Calculated end time
}

