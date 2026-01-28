package com.example.people.management.system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonDto {

    private long id;
    private Long version;
    private String firstName;
    private String lastName;
    private String pesel;
    private Integer height;
    private Double weight;
    private String email;
    private String personType;
}
