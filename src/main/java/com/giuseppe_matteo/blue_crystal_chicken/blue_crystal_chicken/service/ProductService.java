package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.ProductMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.ProductRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.ProductResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.CategoryName;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.IngredientEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.ProductEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.IngredientRepository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.ProductRepository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.exception.ProductNotFoundException;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.exception.ProductAlreadyExistsException;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductMapper productMapper;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public ProductService(ProductRepository productRepository,
            IngredientRepository ingredientRepository,
            ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.ingredientRepository = ingredientRepository;
        this.productMapper = productMapper;
    }

    // -------------------------------------------------------------------------
    // GET ALL
    // -------------------------------------------------------------------------

    public List<ProductResponse> getAllProducts() {
        log.info("Fetching all products");
        List<ProductEntity> products = productRepository.findAll();
        log.info("Found {} products", products.size());
        return products.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // GET BY ID
    // -------------------------------------------------------------------------

    public ProductResponse getProductById(Long id) {
        log.info("Fetching product with id: {}", id);
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found with id: {}", id);
                    return new ProductNotFoundException("Prodotto non trovato con id: " + id);
                });
        return productMapper.toResponse(product);
    }

    // Add this method to ProductService.java
    public ProductEntity findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Prodotto non trovato con id: " + id));
    }

    // -------------------------------------------------------------------------
    // GET BY CATEGORY ID
    // -------------------------------------------------------------------------

    public List<ProductResponse> getProductsByCategoryId(Long categoryId) {
        log.info("Fetching products with category id: {}", categoryId);
        if (categoryId == 0) {
            return getAllProducts();
        }
        List<ProductEntity> products = productRepository.findByCategoryId(categoryId);
        log.info("Found {} products", products.size());
        return products.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // GET BY CATEGORY NAME
    // -------------------------------------------------------------------------

    public List<ProductResponse> getProductsByCategoryName(String name){
        log.info("Fetching products with category name: {}",name);
        if(name.equals("ALL")){
            log.info("Fetching all products");
            return getAllProducts();
        }
        log.info("Converting category name to enum");
        CategoryName categoryName = CategoryName.valueOf(name);
        log.info("Fetching products with category name: {}",categoryName);
        List<ProductEntity> products = productRepository.findByCategoryName(categoryName);
        log.info("Found {} products", products.size());
        return products.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }


    // -------------------------------------------------------------------------
    // CREATE
    // -------------------------------------------------------------------------

    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating new product with name: {}", request.getName());

        if (productRepository.existsByName(request.getName())) {
            log.warn("Product already exists with name: {}", request.getName());
            throw new ProductAlreadyExistsException(
                    "Prodotto già esistente con nome: " + request.getName());
        }

        ProductEntity entity = productMapper.toEntity(request);

        if (request.getIngredientIds() != null && !request.getIngredientIds().isEmpty()) {
            List<IngredientEntity> ingredients = ingredientRepository.findAllById(request.getIngredientIds());
            entity.setIngredients(ingredients);
        }

        // Gestione immagine: MultipartFile (API admin) oppure path diretto (DataLoader)
        if (request.getImage() != null) {
            try {
                entity.setImg_path(saveImage(request.getImage()));
            } catch (IOException e) {
                log.error("Failed to save image for product: {}", request.getName(), e);
                throw new RuntimeException("Errore nel salvataggio dell'immagine: " + e.getMessage(), e);
            }
        } else if (request.getImagePath() != null) {
            entity.setImg_path(request.getImagePath());
        } else {
            log.warn("No image provided for product: {}", request.getName());
            entity.setImg_path(null);
        }

        ProductEntity saved = productRepository.save(entity);
        log.info("Product created with id: {}", saved.getId());
        return productMapper.toResponse(saved);
    }

    private String saveImage(MultipartFile file) throws IOException {
    // Crea la cartella se non esiste
    Path uploadPath = Paths.get(uploadDir);
    if (!Files.exists(uploadPath)) {
        Files.createDirectories(uploadPath);
    }

    // Nome univoco per evitare conflitti
    String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
    Path filePath = uploadPath.resolve(filename);
    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

    return filename;
}

    // -------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        log.info("Updating product with id: {}", id);

        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found with id: {}", id);
                    return new ProductNotFoundException("Prodotto non trovato con id: " + id);
                });
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setSize(request.getSize());
        product.setQuantity(request.getQuantity());
        product.setAdditions(request.getAdditions());
        product.setPrice(request.getPrice());
        product.setNutritionalInfo(request.getNutritionalInfo());

        if (request.getIngredientIds() != null) {
            List<IngredientEntity> ingredients = ingredientRepository.findAllById(request.getIngredientIds());
            product.setIngredients(ingredients);
        } else {
            product.setIngredients(Collections.emptyList());
        }

        ProductEntity updated = productRepository.save(product);
        log.info("Product updated with id: {}", updated.getId());
        return productMapper.toResponse(updated);

    }

    // -------------------------------------------------------------------------
    // DELETE
    // -------------------------------------------------------------------------

    public ProductResponse deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);

        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found with id: {}", id);
                    return new ProductNotFoundException("Prodotto non trovato con id: " + id);
                });

        productRepository.delete(product);
        log.info("Product deleted with id: {}", id);
        return productMapper.toResponse(product);
    }
}