package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot 4 usa Jackson 3 (tools.jackson) come default: il bean ObjectMapper
 * auto-configurato è quello di Jackson 3. Diversi servizi qui (es. ChangeRequestService)
 * usano però il classico Jackson 2 (com.fasterxml.jackson.databind.ObjectMapper),
 * per cui non esiste un bean. Questo @Bean lo registra esplicitamente.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
