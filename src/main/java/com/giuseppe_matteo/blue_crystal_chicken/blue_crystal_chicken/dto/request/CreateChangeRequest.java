package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request;

import lombok.Data;

/**
 * Corpo inviato dal Manager per creare una richiesta di modifica.
 * `payload` come {@code Object} (non JsonNode): Spring Boot 4 deserializza con
 * Jackson 3; come Object diventa una Map che poi serializziamo a stringa.
 */
@Data
public class CreateChangeRequest {
    private String type;      // ChangeRequestType (CREATE_/UPDATE_/DELETE_ PRODUCT|MENU)
    private Long targetId;    // id dell'entità per UPDATE/DELETE (null nelle create)
    private String summary;   // descrizione breve (opzionale, generata se assente)
    private Object payload;   // corpo della create/update (JSON libero)
}
