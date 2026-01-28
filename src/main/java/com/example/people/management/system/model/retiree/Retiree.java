package com.example.people.management.system.model.retiree;

import com.example.people.management.system.model.Person;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@DiscriminatorValue("RETIREE")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Retiree extends Person {

    private Double pensionAmount;
    private Integer yearsWorked;
}
