package com.example.people.management.system.config;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Set;

@Configuration
@EnableAsync
public class ApplicationConfig {

    @Bean
    public ModelMapper modelMapper(Set<Converter> converter) {
        ModelMapper modelMapper = new ModelMapper();
        converter.forEach(modelMapper::addConverter);
        return modelMapper;
    }
}
