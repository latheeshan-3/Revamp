package com.revamp.booking.bookingservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Document(collection = "timeslots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlot {
	@Id
	private String id;
	
	private LocalDate date;
	private LocalTime startTime; // 08:00, 11:00, 14:00
	private LocalTime endTime;   // 11:00, 14:00, 17:00
	private boolean isAvailable;
	private String appointmentId; // null if available, appointment ID if booked
	
	public TimeSlot(LocalDate date, LocalTime startTime, LocalTime endTime) {
		this.date = date;
		this.startTime = startTime;
		this.endTime = endTime;
		this.isAvailable = true;
		this.appointmentId = null;
	}
}

