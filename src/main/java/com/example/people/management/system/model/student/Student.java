package com.example.people.management.system.model.student;

import com.example.people.management.system.model.Person;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@DiscriminatorValue("STUDENT")
@NoArgsConstructor
@AllArgsConstructor
public class Student extends Person {

    private String universityName;
    private Integer studyYear;
    private String fieldOfStudy;
    private Double scholarshipAmount;

}
