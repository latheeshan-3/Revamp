package com.revamp.booking.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "modification_items")
public class ModificationItem {
    @Id
    private String id;
    private String name;
    private Integer estimatedHours;
    private Integer unitPrice; // LKR
    private String description;
}
