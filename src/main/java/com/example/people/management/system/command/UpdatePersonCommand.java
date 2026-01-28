package com.example.people.management.system.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class UpdatePersonCommand {

    private Long version;

    private String type;

    private Map<String, Object> data;
}
