package com.example.people.management.system.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentDto extends PersonDto {

    private String universityName;
    private Integer studyYear;
    private String fieldOfStudy;
    private Double scholarshipAmount;

}
