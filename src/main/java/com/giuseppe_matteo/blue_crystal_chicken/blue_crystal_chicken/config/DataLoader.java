package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.CategoryRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.ProductRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.Category;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.CategoryName;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.exception.ProductAlreadyExistsException;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.CategoryRepository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.CategoryService;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.ProductService;

import lombok.extern.slf4j.Slf4j;

@Component
@Transactional
@Slf4j
public class DataLoader implements CommandLineRunner {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductService productService;

    @Override
    public void run(String... args) throws Exception {
        createCategory();
        createProducts();
    }

    // -----------------------------------------------------------------------
    // CATEGORIES
    // -----------------------------------------------------------------------

    private void createCategory() {
        for (CategoryName categoryName : CategoryName.values()) {
            categoryService.save(new CategoryRequest(categoryName.name()));
        }
    }

    // -----------------------------------------------------------------------
    // PRODUCTS
    // -----------------------------------------------------------------------

    /**
     * Seeds the full Blue Crystal Chicken menu.
     * Each call is wrapped in a try-catch so that duplicate-name exceptions
     * (thrown on repeated startup) are silently skipped.
     */
    private void createProducts() {

        // --- HAMBURGER ---------------------------------------------------
        Category hamburger = getCategory(CategoryName.HAMBURGER);

        saveProduct("Classic Blue Burger",
                hamburger,
                5.90,
                "Pollo fritto croccante, lattuga, pomodoro e maionese speciale.",
                "images/prodotti/classic_blue_burger.jpg"    
            );

        saveProduct("The Crystal Spicy",
                hamburger,
                6.50,
                "Petto di pollo piccante, jalapeños, cheddar fuso e cipolla caramellata.",
                "images/prodotti/the_crystal_spicy.jpg"    
            );

        saveProduct("Mountain Wrap",
                hamburger,
                6.20,
                "Pollo grigliato, bacon croccante e salsa BBQ in un bun brioche.",
                "images/prodotti/mountain_wrap.jpg"    
            );

        // --- FRIES -------------------------------------------------------
        Category fries = getCategory(CategoryName.FRIES);

        saveProduct("Diamond Fries",
                fries,
                2.50,
                "Patatine classiche dal taglio a diamante, extra croccanti.",
                "images/prodotti/diamond_fries.jpg"    
            );

        saveProduct("Cheesy Blue Fries",
                fries,
                3.20,
                "Patatine fritte ricoperte di salsa al formaggio e granella di bacon.",
                "images/prodotti/cheesy_blue_fries.jpg"    
            );

        // --- SNACK -------------------------------------------------------
        Category snack = getCategory(CategoryName.SNACK);

        saveProduct("Crystal Nuggets",
                snack,
                3.50,
                "Pepite di pollo con panatura ai cereali.",
                "images/prodotti/crystal_nuggets.jpg"    
            );

        saveProduct("Spicy Wings",
                snack,
                4.50,
                "Alette di pollo marinate in salsa piccante (6 o 12 pezzi).",
                "images/prodotti/spicy_wings.jpg"    
            );

        // --- DRINK -------------------------------------------------------
        Category drink = getCategory(CategoryName.DRINK);

        saveProduct("Blue Lagoon Soda",
                drink,
                2.20,
                "Soda rinfrescante ai frutti blu e limone.",
                "images/prodotti/blue_lagoon_soda.jpg"    
            );

        saveProduct("Iced Tea",
                drink,
                2.00,
                "Tè freddo alla pesca o al limone fatto in casa.",
                "images/prodotti/iced_tea.jpg"    
            );

        // --- SAUCE -------------------------------------------------------
        Category sauce = getCategory(CategoryName.SAUCE);

        saveProduct("Crystal Mayo",
                sauce,
                0.50,
                "Maionese all'aglio ed erbe fini.",
                "images/prodotti/crystal_mayo.jpg"    
            );

        saveProduct("Blue BBQ",
                sauce,
                0.50,
                "Salsa barbecue con un tocco segreto di affumicatura.",
                "images/prodotti/blue_bbq.jpg"    
            );

        // --- DESSERT -----------------------------------------------------
        Category dessert = getCategory(CategoryName.DESSERT);

        saveProduct("Frozen Crystal Brownie",
                dessert,
                3.80,
                "Brownie caldo al cioccolato servito con una pallina di gelato alla vaniglia.",
                "images/prodotti/frozen_crystal_brownie.jpg"    
            );
    }

    // -----------------------------------------------------------------------
    // HELPERS
    // -----------------------------------------------------------------------

    /**
     * Fetches a Category by name; throws IllegalStateException if not found
     * (should never happen since createCategory() runs first).
     */
    private Category getCategory(CategoryName name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException("Category not found: " + name));
    }

    /**
     * Builds and persists a ProductRequest. Silently skips on duplicates.
     */
    private void saveProduct(String name, Category category, Double price, String nutritionalInfo,String img_path) {
        try {
            ProductRequest request = new ProductRequest();
            request.setName(name);
            request.setCategory(category);
            request.setPrice(price);
            request.setNutritionalInfo(nutritionalInfo);
            request.setImagePath(img_path);
            productService.createProduct(request);
            log.info("Product created: {}", name);
        } catch (ProductAlreadyExistsException e) {
            log.debug("Product already exists, skipping: {}", name);
        }
    }
}

