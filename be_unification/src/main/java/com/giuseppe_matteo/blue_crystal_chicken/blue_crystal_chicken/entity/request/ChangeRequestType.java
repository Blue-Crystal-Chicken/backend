package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.request;

/**
 * Tipi di richiesta di modifica che un Manager può inoltrare all'Admin.
 * Il payload (JSON) della richiesta corrisponde al DTO della rispettiva create.
 */
public enum ChangeRequestType {
    // Prodotti — tutte le mutazioni passano dall'approvazione admin
    CREATE_PRODUCT, // payload = ProductRequest (JSON, imgPath opzionale)
    UPDATE_PRODUCT, // payload = ProductRequest, targetId = id prodotto
    DELETE_PRODUCT, // targetId = id prodotto

    // Menu — idem
    CREATE_MENU,    // payload = MenuRequest
    UPDATE_MENU,    // payload = MenuRequest, targetId = id menu
    DELETE_MENU,    // targetId = id menu

    // Offerte: gestite direttamente dal Manager (NON passano da qui).
    CREATE_OFFER    // (legacy / opzionale) payload = OfferRequest
}
