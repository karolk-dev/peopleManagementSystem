package com.example.people.management.system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RetireeDto extends PersonDto {

    private Double pensionAmount;
    private Integer yearsWorked;

}
