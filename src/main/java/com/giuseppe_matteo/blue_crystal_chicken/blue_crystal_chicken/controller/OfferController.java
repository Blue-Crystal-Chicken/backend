package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.controller;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OfferEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OfferProduct;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;

    // GET /api/offers
    @GetMapping
    public ResponseEntity<List<OfferEntity>> getAll() {
        return ResponseEntity.ok(offerService.findAll());
    }

    // GET /api/offers/{id}
    @GetMapping("/{id}")
    public ResponseEntity<OfferEntity> getById(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.findById(id));
    }

    // GET /api/offers/search?name=promo
    @GetMapping("/search")
    public ResponseEntity<List<OfferEntity>> search(@RequestParam String name) {
        return ResponseEntity.ok(offerService.findByName(name));
    }

    // GET /api/offers/by-menu/{menuId}
    @GetMapping("/by-menu/{menuId}")
    public ResponseEntity<List<OfferEntity>> getByMenu(@PathVariable Long menuId) {
        return ResponseEntity.ok(offerService.findByMenuId(menuId));
    }

    // GET /api/offers/by-product/{productId}
    @GetMapping("/by-product/{productId}")
    public ResponseEntity<List<OfferEntity>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(offerService.findByProductId(productId));
    }

    // GET /api/offers/{id}/products
    @GetMapping("/{id}/products")
    public ResponseEntity<List<OfferProduct>> getProducts(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.findProductsByOfferId(id));
    }

    // POST /api/offers
    @PostMapping
    public ResponseEntity<OfferEntity> create(@RequestBody OfferEntity offer) {
        return ResponseEntity.status(HttpStatus.CREATED).body(offerService.create(offer));
    }

    // PUT /api/offers/{id}
    @PutMapping("/{id}")
    public ResponseEntity<OfferEntity> update(@PathVariable Long id, @RequestBody OfferEntity offer) {
        return ResponseEntity.ok(offerService.update(id, offer));
    }

    // POST /api/offers/{offerId}/products/{productId}
    // Body: { "quantity": 1 }
    @PostMapping("/{offerId}/products/{productId}")
    public ResponseEntity<OfferProduct> addProduct(
            @PathVariable Long offerId,
            @PathVariable Long productId,
            @RequestBody Map<String, Object> body) {
        Integer quantity = (Integer) body.get("quantity");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(offerService.addProductToOffer(offerId, productId, quantity));
    }

    // DELETE /api/offers/{offerId}/products/{productId}
    @DeleteMapping("/{offerId}/products/{productId}")
    public ResponseEntity<Void> removeProduct(
            @PathVariable Long offerId,
            @PathVariable Long productId) {
        offerService.removeProductFromOffer(offerId, productId);
        return ResponseEntity.noContent().build();
    }

    // POST /api/offers/{offerId}/menus/{menuId}
    @PostMapping("/{offerId}/menus/{menuId}")
    public ResponseEntity<OfferEntity> addMenu(
            @PathVariable Long offerId,
            @PathVariable Long menuId) {
        return ResponseEntity.ok(offerService.addMenuToOffer(offerId, menuId));
    }

    // DELETE /api/offers/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        offerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
