package com.example.people.management.system.command;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class CreatePersonCommand {

    private String type;

    private Map<String, Object> data;

}
