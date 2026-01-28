package com.example.people.management.system.model.employee;

import com.example.people.management.system.model.Person;
import com.example.people.management.system.model.position.Position;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Formula;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@DiscriminatorValue("EMPLOYEE")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Employee extends Person {

    private LocalDate employmentStartDate;
    private String currentPosition;
    @OneToMany(
            mappedBy = "employee",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private Set<Position> positions = new HashSet<>();
    private Double salary;

    public void addPosition(Position pos) {
        positions.add(pos);
        pos.setEmployee(this);
    }

    @Formula("(SELECT COUNT(p.id) FROM positions p WHERE p.employee_id = id)")
    private Long positionsCount;
}
