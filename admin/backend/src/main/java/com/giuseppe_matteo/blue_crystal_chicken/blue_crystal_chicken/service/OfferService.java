package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.OfferMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.OfferRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.OfferResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.MenuEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OfferEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OfferProduct;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.ProductEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.key.OfferProductKey;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.OfferProductRepository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OfferService {

    private final OfferRepository offerRepository;
    private final OfferProductRepository offerProductRepository;
    private final ProductService productService;
    private final MenuService menuService;
    private final OfferMapper offerMapper;

    @Value("${app.upload.dir}")
    private String uploadDir;

    // ── READ ────────────────────────────────────────────────────────────────

    public List<OfferResponse> findAll() {
        return offerMapper.toResponseList(offerRepository.findAll());
    }

    public OfferResponse findOfferResponseById(Long id) {
        return offerMapper.toResponse(findById(id));
    }

    public OfferEntity findById(Long id) {
        return offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offerta non trovata con id: " + id));
    }

    public List<OfferResponse> findByName(String name) {
        return offerMapper.toResponseList(offerRepository.findByNameContainingIgnoreCase(name));
    }

    public List<OfferResponse> findByMenuId(Long menuId) {
        return offerMapper.toResponseList(offerRepository.findByMenuId(menuId));
    }

    public List<OfferResponse> findByProductId(Long productId) {
        return offerMapper.toResponseList(offerRepository.findByProductId(productId));
    }

    public List<OfferProduct> findProductsByOfferId(Long offerId) {
        return offerProductRepository.findById_OfferId(offerId);
    }

    // ── WRITE ───────────────────────────────────────────────────────────────

    @Transactional
    public OfferResponse create(OfferRequest request) {
        if (offerRepository.existsByName(request.getName())) {
            throw new RuntimeException("Offerta con nome '" + request.getName() + "' già esistente");
        }
        OfferEntity offer = new OfferEntity();
        offer.setName(request.getName());
        offer.setDescription(request.getDescription());
        offer.setPrice(request.getPrice());
        offer.setDiscountPercentage(request.getDiscountPercentage());
        offer.setStartDate(parseDate(request.getStartDate()));
        offer.setEndDate(parseDate(request.getEndDate()));
        offer.setActive(request.getActive() != null ? request.getActive() : Boolean.TRUE);

        // Handle Image Upload
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                offer.setImgPath(saveImage(request.getImage()));
            } catch (IOException e) {
                throw new RuntimeException("Errore nel salvataggio dell'immagine: " + e.getMessage());
            }
        } else {
            offer.setImgPath(request.getImgPath());
        }

        OfferEntity saved = offerRepository.save(offer);

        if (request.getMenuIds() != null) {
            for (Long menuId : request.getMenuIds()) {
                addMenuToOffer(saved.getId(), menuId);
            }
        }

        return offerMapper.toResponse(saved);
    }

    @Transactional
    public OfferResponse update(Long id, OfferRequest request) {
        OfferEntity existing = findById(id);
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());
        existing.setDiscountPercentage(request.getDiscountPercentage());
        existing.setStartDate(parseDate(request.getStartDate()));
        existing.setEndDate(parseDate(request.getEndDate()));
        if (request.getActive() != null) {
            existing.setActive(request.getActive());
        }

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                existing.setImgPath(saveImage(request.getImage()));
            } catch (IOException e) {
                throw new RuntimeException("Errore nel salvataggio dell'immagine: " + e.getMessage());
            }
        }

        return offerMapper.toResponse(offerRepository.save(existing));
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
    public OfferResponse addMenuToOffer(Long offerId, Long menuId) {
        OfferEntity offer = findById(offerId);
        MenuEntity menu = menuService.findById(menuId);
        if (offer.getMenus().stream().anyMatch(m -> m.getId().equals(menuId))) {
            throw new RuntimeException("Menu già presente nell'offerta");
        }
        offer.getMenus().add(menu);
        return offerMapper.toResponse(offerRepository.save(offer));
    }

    @Transactional
    public void delete(Long id) {
        if (!offerRepository.existsById(id)) {
            throw new RuntimeException("Offerta non trovata con id: " + id);
        }
        offerRepository.deleteById(id);
    }

    // Parsa una data ISO ("2026-06-19T14:30") in modo difensivo; null se vuota/non valida
    private LocalDateTime parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value.trim());
        } catch (Exception e) {
            throw new RuntimeException("Formato data non valido (atteso ISO yyyy-MM-ddTHH:mm): " + value);
        }
    }

    private String saveImage(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }
}
