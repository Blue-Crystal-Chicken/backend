package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.MenuEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OfferEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OfferProduct;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.ProductEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.key.OfferProductKey;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.OfferProductRepository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OfferService {

    private final OfferRepository offerRepository;
    private final OfferProductRepository offerProductRepository;
    private final ProductService productService;
    private final MenuService menuService;

    // ── READ ────────────────────────────────────────────────────────────────

    public List<OfferEntity> findAll() {
        return offerRepository.findAll();
    }

    public OfferEntity findById(Long id) {
        return offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offerta non trovata con id: " + id));
    }

    public List<OfferEntity> findByName(String name) {
        return offerRepository.findByNameContainingIgnoreCase(name);
    }

    public List<OfferEntity> findByMenuId(Long menuId) {
        return offerRepository.findByMenuId(menuId);
    }

    public List<OfferEntity> findByProductId(Long productId) {
        return offerRepository.findByProductId(productId);
    }

    public List<OfferProduct> findProductsByOfferId(Long offerId) {
        return offerProductRepository.findById_OfferId(offerId);
    }

    // ── WRITE ───────────────────────────────────────────────────────────────

    @Transactional
    public OfferEntity create(OfferEntity offer) {
        if (offerRepository.existsByName(offer.getName())) {
            throw new RuntimeException("Offerta con nome '" + offer.getName() + "' già esistente");
        }
        return offerRepository.save(offer);
    }

    @Transactional
    public OfferEntity update(Long id, OfferEntity updated) {
        OfferEntity existing = findById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        return offerRepository.save(existing);
    }

    @Transactional
    public OfferProduct addProductToOffer(Long offerId, Long productId, Integer quantity) {
        if (offerProductRepository.existsById_OfferIdAndId_ProductId(offerId, productId)) {
            throw new RuntimeException("Prodotto già presente nell'offerta");
        }
        OfferEntity offer = findById(offerId);
        ProductEntity product = productService.findById(productId);

        OfferProduct offerProduct = new OfferProduct();
        offerProduct.setId(new OfferProductKey(offerId, productId));
        offerProduct.setOffer(offer);
        offerProduct.setProduct(product);
        offerProduct.setQuantity(quantity);
        return offerProductRepository.save(offerProduct);
    }

    @Transactional
    public void removeProductFromOffer(Long offerId, Long productId) {
        if (!offerProductRepository.existsById_OfferIdAndId_ProductId(offerId, productId)) {
            throw new RuntimeException("Prodotto non trovato nell'offerta");
        }
        offerProductRepository.deleteById_OfferIdAndId_ProductId(offerId, productId);
    }

    @Transactional
    public OfferEntity addMenuToOffer(Long offerId, Long menuId) {
        OfferEntity offer = findById(offerId);
        MenuEntity menu = menuService.findById(menuId);
        if (offer.getMenus().stream().anyMatch(m -> m.getId().equals(menuId))) {
            throw new RuntimeException("Menu già presente nell'offerta");
        }
        offer.getMenus().add(menu);
        return offerRepository.save(offer);
    }

    @Transactional
    public void delete(Long id) {
        if (!offerRepository.existsById(id)) {
            throw new RuntimeException("Offerta non trovata con id: " + id);
        }
        offerRepository.deleteById(id);
    }
}
