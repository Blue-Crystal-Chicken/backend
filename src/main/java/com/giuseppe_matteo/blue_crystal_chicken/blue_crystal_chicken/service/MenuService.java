package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.MenuMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.MenuProductRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.MenuRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.MenuResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.MenuEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.MenuProduct;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.ProductEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.key.MenuProductKey;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.MenuProductRepository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.MenuRepository;

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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuProductRepository menuProductRepository;
    private final ProductService productService;
    private final MenuMapper menuMapper;

    @Value("${app.upload.dir}")
    private String uploadDir;

    // ── READ ────────────────────────────────────────────────────────────────

    public List<MenuResponse> findAll() {
        return menuMapper.toResponseList(menuRepository.findAll());
    }

    public MenuResponse findMenuResponseById(Long id) {
        return menuMapper.toResponse(findById(id));
    }

    public MenuEntity findById(Long id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu non trovato con id: " + id));
    }

    public List<MenuResponse> findByName(String name) {
        return menuMapper.toResponseList(menuRepository.findByNameContainingIgnoreCase(name));
    }

    public List<MenuResponse> findByPriceRange(Double min, Double max) {
        return menuMapper.toResponseList(menuRepository.findByPriceBetween(min, max));
    }

    public List<MenuResponse> findByOfferId(Long offerId) {
        return menuMapper.toResponseList(menuRepository.findByOfferId(offerId));
    }

    public List<MenuProduct> findProductsByMenuId(Long menuId) {
        return menuProductRepository.findById_MenuId(menuId);
    }

    public List<MenuProduct> findObligatoryProductsByMenuId(Long menuId) {
        return menuProductRepository.findById_MenuIdAndObligatoryTrue(menuId);
    }

    public List<MenuProduct> findMenuByProductId(Long productId){
        return menuProductRepository.findById_ProductId(productId);
    }

    // ── WRITE ───────────────────────────────────────────────────────────────

    @Transactional
    public MenuResponse create(MenuEntity menu) {
        if (menuRepository.existsByName(menu.getName())) {
            throw new RuntimeException("Menu con nome '" + menu.getName() + "' già esistente");
        }
        return menuMapper.toResponse(menuRepository.save(menu));
    }

    @Transactional
    public MenuResponse createMenu(MenuRequest request) {
        if (menuRepository.existsByName(request.getName())) {
            throw new RuntimeException("Menu con nome '" + request.getName() + "' già esistente");
        }

        MenuEntity menu = new MenuEntity();
        menu.setName(request.getName());
        menu.setPrice(request.getPrice());
        menu.setDescription(request.getDescription());
        
        // Handle Image Upload
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                menu.setImgPath(saveImage(request.getImage()));
            } catch (IOException e) {
                throw new RuntimeException("Errore nel salvataggio dell'immagine: " + e.getMessage());
            }
        } else {
            menu.setImgPath(request.getImgPath());
        }
        
        menu.setMenuProducts(new ArrayList<>());
        
        MenuEntity savedMenu = menuRepository.save(menu);

        if (request.getProducts() != null) {
            for (MenuProductRequest pr : request.getProducts()) {
                addProductToMenu(savedMenu.getId(), pr.getProductId(), pr.getQuantity(), pr.getObligatory());
            }
        }

        return findMenuResponseById(savedMenu.getId());
    }

    @Transactional
    public MenuResponse update(Long id, MenuRequest request) {
        MenuEntity existing = findById(id);
        existing.setName(request.getName());
        existing.setPrice(request.getPrice());
        existing.setDescription(request.getDescription());

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                existing.setImgPath(saveImage(request.getImage()));
            } catch (IOException e) {
                throw new RuntimeException("Errore nel salvataggio dell'immagine: " + e.getMessage());
            }
        }

        return menuMapper.toResponse(menuRepository.save(existing));
    }

    @Transactional
    public MenuProduct addProductToMenu(Long menuId, Long productId, Integer quantity, Boolean obligatory) {
        if (menuProductRepository.existsById_MenuIdAndId_ProductId(menuId, productId)) {
            throw new RuntimeException("Prodotto già presente nel menu");
        }
        MenuEntity menu = findById(menuId);
        ProductEntity product = productService.findById(productId);

        MenuProduct menuProduct = new MenuProduct();
        menuProduct.setId(new MenuProductKey(menuId, productId));
        menuProduct.setMenu(menu);
        menuProduct.setProduct(product);
        menuProduct.setQuantity(quantity);
        menuProduct.setObligatory(obligatory);
        return menuProductRepository.save(menuProduct);
    }

    @Transactional
    public void removeProductFromMenu(Long menuId, Long productId) {
        if (!menuProductRepository.existsById_MenuIdAndId_ProductId(menuId, productId)) {
            throw new RuntimeException("Prodotto non trovato nel menu");
        }
        menuProductRepository.deleteById_MenuIdAndId_ProductId(menuId, productId);
    }

    @Transactional
    public void delete(Long id) {
        if (!menuRepository.existsById(id)) {
            throw new RuntimeException("Menu non trovato con id: " + id);
        }
        menuRepository.deleteById(id);
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
