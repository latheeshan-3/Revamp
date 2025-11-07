package com.revamp.booking.bookingservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
public class MongoConfig {

	@Value("${spring.data.mongodb.uri}")
	private String mongoUri;

	@Value("${BOOKING_MONGO_DATABASE:Time-slot}")
	private String databaseName;

	@Bean
	public MongoTemplate mongoTemplate() {
		if (mongoUri == null || mongoUri.trim().isEmpty()) {
			throw new IllegalStateException(
				"ERROR: MongoDB URI is not configured in application.properties!"
			);
		}
		
		String dbName = databaseName;
		if (mongoUri.contains("/")) {
			String[] parts = mongoUri.split("/");
			if (parts.length > 1) {
				String dbPart = parts[parts.length - 1];
				if (dbPart.contains("?")) {
					dbName = dbPart.split("\\?")[0];
				} else if (!dbPart.trim().isEmpty()) {
					dbName = dbPart;
				}
			}
		}
		
		System.out.println("Creating mongoTemplate for BookingService");
		System.out.println("Database Name: " + dbName);
		
		MongoClient mongoClient = MongoClients.create(mongoUri);
		MongoTemplate template = new MongoTemplate(mongoClient, dbName);
		System.out.println("✓ mongoTemplate created successfully");
		return template;
	}
}

