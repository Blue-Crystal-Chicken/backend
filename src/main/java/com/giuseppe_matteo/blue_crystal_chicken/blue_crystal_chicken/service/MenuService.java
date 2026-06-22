package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.MenuMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.MenuProductRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.MenuRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.MenuResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.menu.MenuEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.join.MenuProduct;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.product.ProductEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.user.UserEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.join.UserFavoriteMenu;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.join.key.MenuProductKey;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.join.key.UserMenuKey;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.MenuProductRepository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.MenuRepository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.UserFavoriteMenuRepository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.UserRepository;
import org.springframework.http.ResponseEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
import org.springframework.data.domain.Pageable;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuProductRepository menuProductRepository;
    private final ProductService productService;
    private final MenuMapper menuMapper;
    private final UserFavoriteMenuRepository userFavoriteMenuRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    // ── READ ────────────────────────────────────────────────────────────────

    public List<MenuResponse> findAll() {
        log.info("Fetching all menus");
        List<MenuResponse> menus = menuMapper.toResponseList(menuRepository.findAll());
        log.info("Found {} menus", menus.size());
        return menus;
    }

    public List<MenuResponse> findAll(Pageable pageable) {
        return menuMapper.toResponseList(menuRepository.findAll(pageable).getContent());
    }

    public MenuResponse findMenuResponseById(Long id) {
        return menuMapper.toResponse(findById(id));
    }

    public MenuResponse findMenuResponseById(Long id, Long userId) {
        MenuResponse response = menuMapper.toResponse(findById(id));
        response.setIsFavorite(isFavorite(userId, id));
        return response;
    }

    public MenuEntity findById(Long id) {
        log.info("Finding menu by id: {}", id);
        return menuRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Menu not found with id: {}", id);
                    return new RuntimeException("Menu non trovato con id: " + id);
                });
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

    public List<MenuResponse> findMenuByProductId(Long productId){
        ArrayList<MenuEntity> menuProducts = new ArrayList<>();
        for(MenuProduct mp : menuProductRepository.findByProductId(productId)){
            menuProducts.add(mp.getMenu());
        }
        log.debug("MenuProducts trovati per productId {}: {}", productId, menuProducts);
        return menuMapper.toResponseList(menuProducts);
    }

    // ── WRITE ───────────────────────────────────────────────────────────────

    @Transactional
    public MenuResponse create(MenuEntity menu) {
        log.info("Creating new menu: {}", menu.getName());
        if (menuRepository.existsByName(menu.getName())) {
            log.error("Menu with name '{}' already exists", menu.getName());
            throw new RuntimeException("Menu con nome '" + menu.getName() + "' già esistente");
        }
        MenuEntity saved = menuRepository.save(menu);
        log.info("Menu created successfully: {}", saved.getId());
        return menuMapper.toResponse(saved);
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
        log.info("Deleting menu with id: {}", id);
        if (!menuRepository.existsById(id)) {
            log.error("Attempted to delete non-existent menu with id: {}", id);
            throw new RuntimeException("Menu non trovato con id: " + id);
        }
        menuRepository.deleteById(id);
        log.info("Menu deleted successfully: {}", id);
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

    //-------- FAVORITE --------

    @Transactional
    public ResponseEntity<?> addUserFavoriteMenu(Long userId, Long menuId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con id: " + userId));
        MenuEntity menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("Menu non trovato con id: " + menuId));

        UserFavoriteMenu userFavoriteMenu = new UserFavoriteMenu();
        userFavoriteMenu.setId(new UserMenuKey(userId, menuId));
        userFavoriteMenu.setUser(user);
        userFavoriteMenu.setMenu(menu);
        userFavoriteMenuRepository.save(userFavoriteMenu);
        return ResponseEntity.ok("User favorite menu added successfully");
    }

    @Transactional(readOnly = true)
    public ResponseEntity<List<MenuResponse>> getUserFavoriteMenus(Long userId) {
        List<UserFavoriteMenu> userFavoriteMenus = userFavoriteMenuRepository.findByIdUserId(userId);
        List<MenuResponse> menuResponses = new ArrayList<>();
        for (UserFavoriteMenu userFavoriteMenu : userFavoriteMenus) {
            MenuResponse response = menuMapper.toResponse(userFavoriteMenu.getMenu());
            response.setIsFavorite(true);
            menuResponses.add(response);
        }
        return ResponseEntity.ok(menuResponses);
    }

    @Transactional
    public ResponseEntity<?> deleteUserFavoriteMenu(Long userId, Long menuId) {
        UserFavoriteMenu userFavoriteMenu = userFavoriteMenuRepository.findById(new UserMenuKey(userId, menuId))
                .orElseThrow(() -> {
                    log.warn("Menu not found with menu id {} - user id {}", menuId, userId);
                    return new RuntimeException("Menu favorito non trovato con id: " + menuId);
                });
        userFavoriteMenuRepository.delete(userFavoriteMenu);
        log.info("Favorite menu deleted with id {} - user id {}", menuId, userId);
        return ResponseEntity.ok("User favorite menu deleted successfully");
    }

    public boolean isFavorite(Long userId, Long menuId) {
        if (userId == null) return false;
        return userFavoriteMenuRepository.findById(new UserMenuKey(userId, menuId)).isPresent();
    }
}
