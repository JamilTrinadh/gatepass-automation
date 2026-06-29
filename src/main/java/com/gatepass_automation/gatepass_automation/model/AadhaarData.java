package com.gatepass_automation.gatepass_automation.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AadhaarData {
    private String name;
    private String dob;
    private String gender;
    private String aadhaarNumber;
    private String mobile;
    private String address;
}