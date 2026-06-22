package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.address;

public enum AddressType {
    VIA("Via", "Via"),
    VIALE("Viale", "Vle"),
    CORSO("Corso", "C.so"),
    PIAZZA("Piazza", "P.zza"),
    PIAZZALE("Piazzale", "P.le"),
    LARGO("Largo", "L.go"),
    VICOLO("Vicolo", "Vic."),
    STRADA("Strada", "Str."),
    CONTRADA("Contrada", "C.da"),
    LUNGOMARE("Lungomare", "L.mare"),
    ALTRO("Altro", ""); // Fallback per toponimi rari (es. Calle, Creuza)

    private final String nomeEsteso;
    private final String abbreviazione;

    // Costruttore privato dell'enum
    AddressType(String nomeEsteso, String abbreviazione) {
        this.nomeEsteso = nomeEsteso;
        this.abbreviazione = abbreviazione;
    }

    public String getNomeEsteso() {
        return nomeEsteso;
    }

    public String getAbbreviazione() {
        return abbreviazione;
    }

    /**
     * Metodo di utilità per fare il parsing sicuro da stringa (es. da input utente).
     * Evita eccezioni di tipo IllegalArgumentException se l'input non è perfetto.
     */
    public static AddressType parsingSicuro(String input) {
        if (input == null || input.trim().isEmpty()) {
            return ALTRO;
        }
        
        String inputNormalizzato = input.trim().toUpperCase();
        for (AddressType tipo : AddressType.values()) {
            if (tipo.name().equals(inputNormalizzato) || 
                tipo.getNomeEsteso().toUpperCase().equals(inputNormalizzato)) {
                return tipo;
            }
        }
        return ALTRO;
    }
}
