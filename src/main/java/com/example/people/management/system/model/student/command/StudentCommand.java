package com.example.people.management.system.model.student.command;

import jakarta.persistence.Column;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class StudentCommand {

    @NotBlank(message = "first name is required")
    @Size(min = 2,max = 50, message = "first name must be between 2 and 50 characters long.")
    private String firstName;

    @NotBlank(message = "last name is required")
    @Size(min = 2,max = 50, message = "last name must be between 2 and 50 characters long.")
    private String lastName;

    @NotNull(message = "pesel is required")
    @Pattern(regexp = "\\d{11}", message = "the pesel number must contain 11 digits")
    @Column(unique = true, nullable = false, length = 11)
    private String pesel;

    @NotNull(message = "height is required")
    @Min(value = 50, message = "height must be at least 50cm")
    @Max(value = 300, message = "the height cannot exceed 300cm")
    private Integer height;

    @NotNull(message = "weight is required")
    @DecimalMin(value = "10.0", message = "weight must be at least 10.0kg")
    @DecimalMax(value = "500.0", message = "the weight cannot exceed 500 kg")
    private Double weight;

    @NotBlank(message = "email is required")
    @Email(message = "invalid email address format")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "name of the university is required")
    @Size(max = 200)
    private String universityName;

    @NotNull(message = "year of study is required")
    @Min(value = 1, message = "the year of study must be at least 1")
    private Integer studyYear;

    @NotBlank(message = "field of study is required")
    @Size(max = 100)
    private String fieldOfStudy;

    @NotNull(message = "scholarship is required")
    @PositiveOrZero(message = "the scholarship cannot be negative")
    private Double scholarshipAmount;
}
