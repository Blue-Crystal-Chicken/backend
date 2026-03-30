package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.CategoryRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.ProductRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.Register;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.CategoryName;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.Role;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.exception.ProductAlreadyExistsException;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.CategoryService;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.ProductService;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Component
@Transactional
@Slf4j
public class DataLoader implements CommandLineRunner {

        @Autowired
        private CategoryService categoryService;

        @Autowired
        private ProductService productService;

        @Autowired
        private UserService userService;

        @Override
        public void run(String... args) throws Exception {
                createCategory();
                createProducts();
                createUsers();
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

                // --------------------------------------------------
                // 🍔 HAMBURGER
                // --------------------------------------------------

                ProductRequest burger = new ProductRequest();
                burger.setName("Classic Blue Burger");
                burger.setCategoryName("HAMBURGER");
                burger.setPrice(5.90);
                burger.setDescription("Pollo fritto croccante, lattuga, pomodoro e maionese speciale.");
                burger.setCalories(650);
                burger.setWeight(250.0);
                burger.setIsSpicy(false);
                burger.setIsVegetarian(false);
                burger.setIsVegan(false);
                burger.setIsGlutenFree(false);
                burger.setImagePath("images/prodotti/classic_blue_burger.jpg");
                saveProduct(burger);

                ProductRequest spicyBurger = new ProductRequest();
                spicyBurger.setName("The Crystal Spicy");
                spicyBurger.setCategoryName("HAMBURGER");
                spicyBurger.setPrice(6.50);
                spicyBurger.setDescription("Pollo piccante, jalapeños e cheddar.");
                spicyBurger.setCalories(720);
                spicyBurger.setWeight(270.0);
                spicyBurger.setIsSpicy(true);
                spicyBurger.setIsVegetarian(false);
                spicyBurger.setIsVegan(false);
                spicyBurger.setIsGlutenFree(false);
                spicyBurger.setImagePath("images/prodotti/the_crystal_spicy.jpg");
                saveProduct(spicyBurger);

                // --------------------------------------------------
                // 🌯 WRAP
                // --------------------------------------------------

                ProductRequest wrapProd = new ProductRequest();
                wrapProd.setName("Mountain Wrap");
                wrapProd.setCategoryName("WRAP");
                wrapProd.setPrice(6.20);
                wrapProd.setDescription("Pollo grigliato, bacon e salsa BBQ.");
                wrapProd.setCalories(550);
                wrapProd.setWeight(220.0);
                wrapProd.setIsSpicy(false);
                wrapProd.setImagePath("images/prodotti/mountain_wrap.jpg");
                saveProduct(wrapProd);

                // --------------------------------------------------
                // 🍟 FRIES
                // --------------------------------------------------

                ProductRequest friesProd = new ProductRequest();
                friesProd.setName("Diamond Fries");
                friesProd.setCategoryName("FRIES");
                friesProd.setPrice(2.50);
                friesProd.setDescription("Patatine croccanti.");
                friesProd.setCalories(300);
                friesProd.setWeight(120.0);
                friesProd.setIsVegetarian(true);
                friesProd.setIsVegan(true);
                friesProd.setIsGlutenFree(true);
                friesProd.setImagePath("images/prodotti/diamond_fries.jpg");
                saveProduct(friesProd);

                ProductRequest cheesyFries = new ProductRequest();
                cheesyFries.setName("Cheesy Blue Fries");
                cheesyFries.setCategoryName("FRIES");
                cheesyFries.setPrice(3.20);
                cheesyFries.setDescription("Patatine con formaggio e bacon.");
                cheesyFries.setCalories(450);
                cheesyFries.setWeight(150.0);
                cheesyFries.setIsVegetarian(false);
                cheesyFries.setIsVegan(false);
                cheesyFries.setImagePath("images/prodotti/cheesy_blue_fries.jpg");
                saveProduct(cheesyFries);

                // --------------------------------------------------
                // 🍗 SNACK
                // --------------------------------------------------

                ProductRequest nuggets = new ProductRequest();
                nuggets.setName("Crystal Nuggets");
                nuggets.setCategoryName("SNACK");
                nuggets.setPrice(3.50);
                nuggets.setDescription("Pepite di pollo croccanti.");
                nuggets.setCalories(400);
                nuggets.setQuantity(6);
                nuggets.setIsSpicy(false);
                nuggets.setImagePath("images/prodotti/crystal_nuggets.jpg");
                saveProduct(nuggets);

                ProductRequest wings = new ProductRequest();
                wings.setName("Spicy Wings");
                wings.setCategoryName("SNACK");
                wings.setPrice(4.50);
                wings.setDescription("Alette di pollo piccanti.");
                wings.setCalories(500);
                wings.setQuantity(6);
                wings.setIsSpicy(true);
                wings.setImagePath("images/prodotti/spicy_wings.jpg");
                saveProduct(wings);

                // --------------------------------------------------
                // 🥤 DRINK
                // --------------------------------------------------

                ProductRequest drink1 = new ProductRequest();
                drink1.setName("Blue Lagoon Soda");
                drink1.setCategoryName("DRINK");
                drink1.setPrice(2.20);
                drink1.setDescription("Soda ai frutti blu.");
                drink1.setCalories(120);
                drink1.setLiters(0.33);
                drink1.setIsCarbonated(true);
                drink1.setTemperature("cold");
                drink1.setImagePath("images/prodotti/blue_lagoon_soda.jpg");
                saveProduct(drink1);

                ProductRequest tea = new ProductRequest();
                tea.setName("Iced Tea");
                tea.setCategoryName("DRINK");
                tea.setPrice(2.00);
                tea.setDescription("Tè freddo pesca/limone.");
                tea.setCalories(90);
                tea.setLiters(0.33);
                tea.setIsCarbonated(false);
                tea.setTemperature("cold");
                tea.setImagePath("images/prodotti/iced_tea.jpg");
                saveProduct(tea);

                // --------------------------------------------------
                // 🧂 SAUCE
                // --------------------------------------------------

                ProductRequest mayo = new ProductRequest();
                mayo.setName("Crystal Mayo");
                mayo.setCategoryName("SAUCE");
                mayo.setPrice(0.50);
                mayo.setDescription("Maionese speciale.");
                mayo.setCalories(150);
                mayo.setQuantity(1);
                mayo.setIsVegetarian(true);
                mayo.setImagePath("images/prodotti/crystal_mayo.jpg");
                saveProduct(mayo);

                ProductRequest bbq = new ProductRequest();
                bbq.setName("Blue BBQ");
                bbq.setCategoryName("SAUCE");
                bbq.setPrice(0.50);
                bbq.setDescription("Salsa BBQ affumicata.");
                bbq.setCalories(130);
                bbq.setQuantity(1);
                bbq.setIsVegetarian(true);
                bbq.setIsVegan(true);
                bbq.setImagePath("images/prodotti/blue_bbq.jpg");
                saveProduct(bbq);

                // --------------------------------------------------
                // 🍰 DESSERT
                // --------------------------------------------------

                ProductRequest brownie = new ProductRequest();
                brownie.setName("Frozen Crystal Brownie");
                brownie.setCategoryName("DESSERT");
                brownie.setPrice(3.80);
                brownie.setDescription("Brownie con gelato.");
                brownie.setCalories(600);
                brownie.setWeight(180.0);
                brownie.setTemperature("hot/cold");
                brownie.setImagePath("images/prodotti/frozen_crystal_brownie.jpg");
                saveProduct(brownie);
                }   // -----------------------------------------------------------------------
        // HELPERS
        // -----------------------------------------------------------------------

                /**
                 * Builds and persists a ProductRequest. Silently skips on duplicates.
                 */
                private void saveProduct(ProductRequest request) {
        try {
                productService.createProduct(request);
                log.info("Product created: {}", request.getName());
        } catch (ProductAlreadyExistsException e) {
                log.debug("Product already exists, skipping: {}", request.getName());
        }
        }

        private void createUsers() {

                Register userRequest = new Register();
                userRequest.setName("Giuseppe");
                userRequest.setSurname("Tesse");
                userRequest.setEmail("gspptesse@gmail.com");
                userRequest.setPassword("123456");
                userRequest.setPhone("3333333333");
                userRequest.setGender("Male");
                userRequest.setBirthday("2004-12-11");
                userRequest.setRole(Role.ADMIN);
                userService.registerUser(userRequest);
        }
}
