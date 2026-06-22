package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.OfferMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.OfferProductRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.OfferRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.OfferResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.menu.MenuEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.offer.OfferEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.join.OfferProduct;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.product.ProductEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.join.key.OfferProductKey;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.OfferProductRepository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
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
        log.info("Fetching all offers");
        List<OfferResponse> offers = offerMapper.toResponseList(offerRepository.findAll());
        log.info("Found {} offers", offers.size());
        return offers;
    }

    public Page<OfferResponse> findPage(Pageable pageable) {
        return offerRepository.findAll(pageable).map(offerMapper::toResponse);
    }

    public OfferResponse findOfferResponseById(Long id) {
        return offerMapper.toResponse(findById(id));
    }

    public OfferEntity findById(Long id) {
        log.info("Finding offer by id: {}", id);
        return offerRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Offer not found with id: {}", id);
                    return new RuntimeException("Offerta non trovata con id: " + id);
                });
    }

    public OfferResponse findByIdWithProducts(Long id) {
        OfferEntity offerEntity = offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offerta non trovata con id: " + id));
        ArrayList<OfferProduct> products = new ArrayList<>(offerProductRepository.findById_OfferId(id));
        offerEntity.setOfferProducts(products);
        return offerMapper.toResponse(offerEntity);
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
        log.info("Creating new offer: {}", request.getName());
        if (offerRepository.existsByName(request.getName())) {
            log.error("Offer with name '{}' already exists", request.getName());
            throw new RuntimeException("Offerta con nome '" + request.getName() + "' già esistente");
        }
        OfferEntity offer = new OfferEntity();
        offer.setName(request.getName());
        offer.setDescription(request.getDescription());
        offer.setPrice(request.getPrice());
        
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

        if (request.getProducts() != null) {
            List<OfferProduct> offerProducts = new ArrayList<>();
            for (OfferProductRequest prodReq : request.getProducts()) {
                ProductEntity product = productService.findById(prodReq.getProductId());
                OfferProduct offerProduct = new OfferProduct();
                offerProduct.setId(new OfferProductKey(saved.getId(), product.getId()));
                offerProduct.setOffer(saved); 
                offerProduct.setProduct(product);
                offerProduct.setQuantity(prodReq.getQuantity());
                offerProducts.add(offerProduct);
            }
            saved.setOfferProducts(offerProducts);
            saved = offerRepository.save(saved); 
        }
    return offerMapper.toResponse(saved);
    }

    @Transactional
    public OfferResponse update(Long id, OfferRequest request) {
        OfferEntity existing = findById(id);
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());

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
        log.info("Deleting offer with id: {}", id);
        if (!offerRepository.existsById(id)) {
            log.error("Attempted to delete non-existent offer with id: {}", id);
            throw new RuntimeException("Offerta non trovata con id: " + id);
        }
        offerRepository.deleteById(id);
        log.info("Offer deleted successfully: {}", id);
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
