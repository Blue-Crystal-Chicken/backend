package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.LocationRepository;

@Service
public class LocationSchedulerService {

    @Autowired
    private LocationRepository locationRepository;

    // Ogni giorno alle 08:00 → apre i locali
    @Scheduled(cron = "0 0 8 * * *")
    public void apriLocali() {
        locationRepository.setAllIsOpen(true);
        System.out.println("Locali aperti alle 08:00");
    }

    // Ogni giorno alle 22:00 → chiude i locali
    @Scheduled(cron = "0 0 22 * * *")
    public void chiudiLocali() {
        locationRepository.setAllIsOpen(false);
        System.out.println("Locali chiusi alle 22:00");
    }
}
