package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

/**
 * Upload immagini per il flusso richieste del Manager.
 *
 * Il payload di una richiesta (ProductRequest/MenuRequest) è JSON e non può
 * trasportare un file binario. Il Manager carica prima l'immagine qui, ottiene
 * un `imgPath` (filename servito da /uploads/images/**) e lo mette nel payload.
 * All'approvazione l'Admin crea/aggiorna l'entità con quell'imgPath.
 */
@RestController
@RequestMapping("/api/uploads")
@Slf4j
public class UploadController {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @PostMapping(value = "/image", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("Nessun file ricevuto");
        }
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Immagine caricata: {}", filename);
            return ResponseEntity.ok(Map.of("imgPath", filename));
        } catch (IOException e) {
            log.error("Upload immagine fallito: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Errore salvataggio immagine: " + e.getMessage());
        }
    }
}
