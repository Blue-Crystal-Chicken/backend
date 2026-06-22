package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request;

import lombok.Data;

/**
 * Richiesta di creazione di una sede dall'Admin.
 * Oltre ai dati della sede può contenere, opzionalmente, le credenziali del
 * MANAGER da creare e assegnare a quella sede: se {@code managerEmail} e
 * {@code managerPassword} sono valorizzati, viene creato l'account manager
 * (ruolo MANAGER) collegato alla nuova sede. Gli altri campi manager hanno
 * default sensati lato server.
 */
@Data
public class LocationCreateRequest {

    // ── Dati sede ──
    private String name;
    private String address;
    private String city;
    private String phoneNumber;
    private Boolean isOpen;

    // ── Account manager della sede (opzionale) ──
    private String managerEmail;
    private String managerPassword;
    private String managerName;
    private String managerSurname;
    private String managerPhone;
}
