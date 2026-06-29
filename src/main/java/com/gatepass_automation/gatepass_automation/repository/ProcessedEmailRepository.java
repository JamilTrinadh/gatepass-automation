package com.gatepass_automation.gatepass_automation.repository;

import com.gatepass_automation.gatepass_automation.model.ProcessedEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedEmailRepository extends JpaRepository<ProcessedEmail, String> {

}