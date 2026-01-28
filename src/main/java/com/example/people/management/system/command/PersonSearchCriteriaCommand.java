package com.example.people.management.system.command;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
public class PersonSearchCriteriaCommand {

    private Map<String, Object> filters = new HashMap<>();

    private Integer page = 0;

    private Integer size = 20;

}
