package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.CategoryRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.OrderItemRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.OrderRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.ProductRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.Register;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.CategoryName;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.ProductEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.Role;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.UserEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.exception.ProductAlreadyExistsException;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.ProductRepository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.UserRepository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.CategoryService;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.OrderService;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.LocationService;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.ProductService;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.UserService;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.LocationEntity;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        @Autowired
        private OrderService orderService;

        @Autowired
        private LocationService locationService;

        @Autowired
        private ProductRepository productRepository;

        @Autowired
        private UserRepository userRepository;

        @Override
        public void run(String... args) throws Exception {
                createCategory();
                createProducts();
                createLocations();
                createUsers();
                createOrders();
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
        }

        // -----------------------------------------------------------------------
        // USERS
        // -----------------------------------------------------------------------

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

        // -----------------------------------------------------------------------
        // LOCATIONS
        // -----------------------------------------------------------------------

        private void createLocations() {
                if (!locationService.findAll().isEmpty()) {
                        log.debug("Locations already exist, skipping location creation");
                        return;
                }

                String[][] locationsData = {
                        {"Blue Crystal Torino", "Via Roma 10", "Torino", "011", "1234567"},
                        {"Blue Crystal Roma", "Via del Corso 50", "Roma", "06", "1234567"},
                        {"Blue Crystal Milano", "Piazza Duomo 1", "Milano", "02", "1234567"},
                        {"Blue Crystal Bologna", "Via dell'Indipendenza 15", "Bologna", "051", "1234567"},
                        {"Blue Crystal Napoli", "Via Toledo 100", "Napoli", "081", "1234567"},
                        {"Blue Crystal Bari", "Corso Vittorio Emanuele 20", "Bari", "080", "1234567"}
                };

                for (String[] data : locationsData) {
                        LocationEntity loc = new LocationEntity();
                        loc.setName(data[0]);
                        loc.setAddress(data[1]);
                        loc.setCity(data[2]);
                        loc.setPhoneCode(data[3]);
                        loc.setPhoneNumber(data[4]);
                        loc.setIsOpen(true);
                        loc.setManuallyClosed(false);
                        loc.setStatus("ACTIVE");
                        locationService.create(loc);
                        log.info("Location created: {}", loc.getName());
                }
        }

        // -----------------------------------------------------------------------
        // ORDERS
        // -----------------------------------------------------------------------

        /**
         * Seeds 5 test orders with varied products, service types, and payment methods.
         * Skips order creation if orders already exist in the database.
         */
        private void createOrders() {
                if (!orderService.findAll().isEmpty()) {
                        log.debug("Orders already exist, skipping order creation");
                        return;
                }

                UserEntity user = userRepository.findByEmail("gspptesse@gmail.com").orElse(null);
                if (user == null) {
                        log.warn("Test user not found, skipping order creation");
                        return;
                }

                // Build product name → ID map
                Map<String, Long> p = new HashMap<>();
                productRepository.findAll().forEach(prod -> p.put(prod.getName(), prod.getId()));

                // ── ORDER 1: Dine-in, Card, Table T-05 ──────────────────────────
                // Classic Blue Burger + Diamond Fries + Blue Lagoon Soda
                OrderRequest o1 = new OrderRequest();
                o1.setUserId(user.getId());
                o1.setServiceType("DINE_IN");
                o1.setOrderType("STANDARD");
                o1.setTableNumber("T-05");
                o1.setPaymentType("CARD");
                o1.setItems(List.of(
                        new OrderItemRequest(p.get("Classic Blue Burger"), 1, null),
                        new OrderItemRequest(p.get("Diamond Fries"), 1, null),
                        new OrderItemRequest(p.get("Blue Lagoon Soda"), 1, null)));
                saveOrder(o1);

                // ── ORDER 2: Takeaway, Cash ──────────────────────────────────────
                // The Crystal Spicy + Cheesy Blue Fries + Iced Tea
                OrderRequest o2 = new OrderRequest();
                o2.setUserId(user.getId());
                o2.setServiceType("TAKEAWAY");
                o2.setOrderType("STANDARD");
                o2.setPaymentType("CASH");
                o2.setItems(List.of(
                        new OrderItemRequest(p.get("The Crystal Spicy"), 1, null),
                        new OrderItemRequest(p.get("Cheesy Blue Fries"), 1, null),
                        new OrderItemRequest(p.get("Iced Tea"), 1, null)));
                saveOrder(o2);

                // ── ORDER 3: Delivery, Card ──────────────────────────────────────
                // Crystal Nuggets x2 + Spicy Wings + Blue BBQ
                OrderRequest o3 = new OrderRequest();
                o3.setUserId(user.getId());
                o3.setServiceType("DELIVERY");
                o3.setOrderType("STANDARD");
                o3.setPaymentType("CARD");
                o3.setItems(List.of(
                        new OrderItemRequest(p.get("Crystal Nuggets"), 2, null),
                        new OrderItemRequest(p.get("Spicy Wings"), 1, "Extra piccante per favore"),
                        new OrderItemRequest(p.get("Blue BBQ"), 1, null)));
                saveOrder(o3);

                // ── ORDER 4: Dine-in, Cash, Table T-12 ──────────────────────────
                // Mountain Wrap + Classic Blue Burger + Crystal Mayo
                OrderRequest o4 = new OrderRequest();
                o4.setUserId(user.getId());
                o4.setServiceType("DINE_IN");
                o4.setOrderType("STANDARD");
                o4.setTableNumber("T-12");
                o4.setPaymentType("CASH");
                o4.setItems(List.of(
                        new OrderItemRequest(p.get("Mountain Wrap"), 1, null),
                        new OrderItemRequest(p.get("Classic Blue Burger"), 1, "Senza pomodoro"),
                        new OrderItemRequest(p.get("Crystal Mayo"), 2, null)));
                saveOrder(o4);

                // ── ORDER 5: Dine-in, Card, Table T-03 ──────────────────────────
                // Frozen Crystal Brownie + Blue Lagoon Soda x2
                OrderRequest o5 = new OrderRequest();
                o5.setUserId(user.getId());
                o5.setServiceType("DINE_IN");
                o5.setOrderType("STANDARD");
                o5.setTableNumber("T-03");
                o5.setPaymentType("CARD");
                o5.setItems(List.of(
                        new OrderItemRequest(p.get("Frozen Crystal Brownie"), 1, null),
                        new OrderItemRequest(p.get("Blue Lagoon Soda"), 2, null)));
                saveOrder(o5);
        }

        // -----------------------------------------------------------------------
        // HELPERS
        // -----------------------------------------------------------------------

        private void saveProduct(ProductRequest request) {
                try {
                        productService.createProduct(request);
                        log.info("Product created: {}", request.getName());
                } catch (ProductAlreadyExistsException e) {
                        log.debug("Product already exists, skipping: {}", request.getName());
                }
        }

        private void saveOrder(OrderRequest request) {
                try {
                        orderService.create(request);
                        log.info("Order created: {} items, service={}", 
                                request.getItems().size(), request.getServiceType());
                } catch (Exception e) {
                        log.debug("Order already exists or error, skipping: {}", e.getMessage());
                }
        }
}
