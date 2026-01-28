package com.example.people.management.system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto extends PersonDto {
    private LocalDate employmentStartDate;
    private String position;
    private Double salary;
    private Long positionsCount;
}
