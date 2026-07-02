package com.smartrental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main Spring Boot Application Class for Smart Rental Management System.
 * 
 * This application provides:
 * - Tenant & Landlord Management (Role-based access)
 * - Rent Payment Tracking with Razorpay Integration
 * - Maintenance Request System
 * - Automated Rent Reminders (Scheduled Tasks)
 * - Document Upload & Management (Cloudinary)
 * - AI Chatbot Assistant (OpenAI API)
 * 
 * @author Smart Rental Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.smartrental.repository")
@EntityScan(basePackages = "com.smartrental.model")
@ComponentScan(basePackages = "com.smartrental")
public class SmartRentalManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartRentalManagementApplication.class, args);
    }
}