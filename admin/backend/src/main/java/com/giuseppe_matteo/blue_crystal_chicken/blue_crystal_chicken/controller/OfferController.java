package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.controller;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.OfferMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.OfferRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.OfferProductResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.OfferResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.OfferService;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.messaging.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;
    private final OfferMapper offerMapper;
    private final NotificationPublisher notificationPublisher;

    // GET /api/offers
    @GetMapping
    public ResponseEntity<List<OfferResponse>> getAll() {
        return ResponseEntity.ok(offerService.findAll());
    }

    // GET /api/offers/{id}
    @GetMapping("/{id}")
    public ResponseEntity<OfferResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.findOfferResponseById(id));
    }

    // GET /api/offers/search?name=promo
    @GetMapping("/search")
    public ResponseEntity<List<OfferResponse>> search(@RequestParam String name) {
        return ResponseEntity.ok(offerService.findByName(name));
    }

    // GET /api/offers/by-menu/{menuId}
    @GetMapping("/by-menu/{menuId}")
    public ResponseEntity<List<OfferResponse>> getByMenu(@PathVariable Long menuId) {
        return ResponseEntity.ok(offerService.findByMenuId(menuId));
    }

    // GET /api/offers/by-product/{productId}
    @GetMapping("/by-product/{productId}")
    public ResponseEntity<List<OfferResponse>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(offerService.findByProductId(productId));
    }

    // GET /api/offers/{id}/products
    @GetMapping("/{id}/products")
    public ResponseEntity<List<OfferProductResponse>> getProducts(@PathVariable Long id) {
        return ResponseEntity.ok(offerMapper.toOfferProductResponseList(offerService.findProductsByOfferId(id)));
    }

    // POST /api/offers
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<OfferResponse> create(@ModelAttribute OfferRequest offer) {
        OfferResponse created = offerService.create(offer);
        notificationPublisher.publish("event.offer.created", "OFFER_CREATED", "SUCCESS", "Marketing",
                "Nuova offerta creata", "L'offerta \"" + created.getName() + "\" è stata creata.");
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/offers/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<OfferResponse> update(@PathVariable Long id, @ModelAttribute OfferRequest offer) {
        OfferResponse updated = offerService.update(id, offer);
        notificationPublisher.publish("event.offer.updated", "OFFER_UPDATED", "INFO", "Marketing",
                "Offerta modificata", "L'offerta \"" + updated.getName() + "\" è stata aggiornata.");
        return ResponseEntity.ok(updated);
    }

    // POST /api/offers/{offerId}/products/{productId}
    // Body: { "quantity": 1 }
    @PostMapping("/{offerId}/products/{productId}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<OfferProductResponse> addProduct(
            @PathVariable Long offerId,
            @PathVariable Long productId,
            @RequestBody Map<String, Object> body) {
        Integer quantity = (Integer) body.get("quantity");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(offerMapper.toOfferProductResponse(offerService.addProductToOffer(offerId, productId, quantity)));
    }

    // DELETE /api/offers/{offerId}/products/{productId}
    @DeleteMapping("/{offerId}/products/{productId}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<Void> removeProduct(
            @PathVariable Long offerId,
            @PathVariable Long productId) {
        offerService.removeProductFromOffer(offerId, productId);
        return ResponseEntity.noContent().build();
    }

    // POST /api/offers/{offerId}/menus/{menuId}
    @PostMapping("/{offerId}/menus/{menuId}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<OfferResponse> addMenu(
            @PathVariable Long offerId,
            @PathVariable Long menuId) {
        return ResponseEntity.ok(offerService.addMenuToOffer(offerId, menuId));
    }

    // DELETE /api/offers/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        offerService.delete(id);
        notificationPublisher.publish("event.offer.deleted", "OFFER_DELETED", "WARNING", "Marketing",
                "Offerta eliminata", "L'offerta #" + id + " è stata eliminata.");
        return ResponseEntity.noContent().build();
    }
}
