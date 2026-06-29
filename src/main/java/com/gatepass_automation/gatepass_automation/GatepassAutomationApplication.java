package com.gatepass_automation.gatepass_automation;

import com.gatepass_automation.gatepass_automation.config.MicrosoftConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(MicrosoftConfig.class)
public class GatepassAutomationApplication {

	public static void main(String[] args) {

		SpringApplication.run(GatepassAutomationApplication.class, args);
		System.out.println("Application Started Successfully 🚀🚀🚀🚀🚀🚀🚀🚀");
	}

}
