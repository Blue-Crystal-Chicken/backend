package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Heartbeat {

    @Scheduled(fixedRate = 60000) // Esegue ogni 60 secondi
    public void sendHeartbeat() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024; // Converti in MB
        log.info("Heartbeat: Il server è attivo e funzionante. Memoria utilizzata: {} MB", usedMemory);
    }
    
}
