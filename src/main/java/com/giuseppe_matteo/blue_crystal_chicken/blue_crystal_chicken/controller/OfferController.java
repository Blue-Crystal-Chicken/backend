package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.controller;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.OfferMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.OfferRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.OfferProductResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.OfferResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;
    private final OfferMapper offerMapper;

    // GET /api/offers
    @GetMapping
    public ResponseEntity<List<OfferResponse>> getAll() {
        return ResponseEntity.ok(offerService.findAll());
    }

    // GET /api/offers/top-offers?limit=5
    @GetMapping("/v1/top")
    public ResponseEntity<List<OfferResponse>> getTop(@RequestParam(defaultValue = "5") int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return ResponseEntity.ok(offerService.findPage(pageable).getContent());
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OfferResponse> create(@ModelAttribute OfferRequest offer) {
        return ResponseEntity.status(HttpStatus.CREATED).body(offerService.create(offer));
    }

    // PUT /api/offers/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OfferResponse> update(@PathVariable Long id, @ModelAttribute OfferRequest offer) {
        return ResponseEntity.ok(offerService.update(id, offer));
    }

    // POST /api/offers/{offerId}/products/{productId}
    // Body: { "quantity": 1 }
    @PostMapping("/{offerId}/products/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeProduct(
            @PathVariable Long offerId,
            @PathVariable Long productId) {
        offerService.removeProductFromOffer(offerId, productId);
        return ResponseEntity.noContent().build();
    }

    // POST /api/offers/{offerId}/menus/{menuId}
    @PostMapping("/{offerId}/menus/{menuId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OfferResponse> addMenu(
            @PathVariable Long offerId,
            @PathVariable Long menuId) {
        return ResponseEntity.ok(offerService.addMenuToOffer(offerId, menuId));
    }

    // DELETE /api/offers/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        offerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
