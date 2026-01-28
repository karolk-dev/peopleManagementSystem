package com.example.people.management.system.model.employee.command;

import jakarta.persistence.Column;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class EditEmployeeCommand {

    @Size(min = 2, max = 50, message = "first name must be between 2 and 50 characters long.")
    private String firstName;

    @Size(min = 2, max = 50, message = "last name must be between 2 and 50 characters long.")
    private String lastName;

    @Pattern(regexp = "\\d{11}", message = "the pesel number must contain 11 digits")
    @Column(unique = true, nullable = false, length = 11)
    private String pesel;

    @Min(value = 50, message = "height must be at least 50cm")
    @Max(value = 300, message = "the height cannot exceed 300cm")
    private Integer height;

    @DecimalMin(value = "10.0", message = "weight must be at least 10.0kg")
    @DecimalMax(value = "500.0", message = "the weight cannot exceed 500 kg")
    private Double weight;

    @Email(message = "invalid email address format")
    @Size(max = 100)
    private String email;

    @PastOrPresent(message = "start date connot be in the future")
    private LocalDate employmentStartDate;

    @PositiveOrZero(message = "salary cannot be negative")
    private Double salary;
}
