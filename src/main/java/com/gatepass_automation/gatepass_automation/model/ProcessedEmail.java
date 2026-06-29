package com.gatepass_automation.gatepass_automation.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "processed_emails")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedEmail {

    @Id
    private String emailId; // Storing Microsoft's unique Immutable Graph ID

    private LocalDateTime processedAt;
}