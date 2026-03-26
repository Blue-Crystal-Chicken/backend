package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.ProductMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.ProductRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.ProductResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.IngredientEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.ProductEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.IngredientRepository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.ProductRepository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.exception.ProductNotFoundException;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.exception.ProductAlreadyExistsException;

import java.util.Collections;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductMapper productMapper;

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

        ProductEntity saved = productRepository.save(entity);
        log.info("Product created with id: {}", saved.getId());
        return productMapper.toResponse(saved);

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