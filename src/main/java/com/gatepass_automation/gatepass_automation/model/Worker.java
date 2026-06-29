package com.gatepass_automation.gatepass_automation.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Worker {
    private String name;
    private String fatherName;
    private String dob;
    private String gender;
    private String mobile;
    private String address;
    private String aadhaar;
    private String vehicleNumber;
    private String driverName;
}