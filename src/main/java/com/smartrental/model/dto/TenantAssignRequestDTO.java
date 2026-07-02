package com.smartrental.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantAssignRequestDTO {

    @NotBlank(message = "Unit number is required")
    private String unitNumber;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private String email;

    @NotBlank(message = "Phone is required")
    private String phoneNumber;

    @NotNull(message = "Rent amount is required")
    @Positive(message = "Rent must be positive")
    private BigDecimal rentAmount;

    private BigDecimal deposit;

    @NotNull(message = "Lease start date is required")
    private LocalDate leaseStart;

    private LocalDate leaseEnd;

    @NotBlank(message = "Password is required for tenant login")
    private String password;
}
