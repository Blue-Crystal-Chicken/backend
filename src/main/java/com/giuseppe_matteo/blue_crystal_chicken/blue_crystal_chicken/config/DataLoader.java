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
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.MenuProductRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.MenuRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.LocationEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.MenuService;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.OfferRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.OfferService;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.OfferRepository;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.IngredientService;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.repository.IngredientRepository;

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
        private MenuService menuService;

        @Autowired
        private OfferService offerService;

        @Autowired
        private ProductRepository productRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private OfferRepository offerRepository;

        @Autowired
        private IngredientService ingredientService;

        @Autowired
        private IngredientRepository ingredientRepository;

        @Override
        public void run(String... args) throws Exception {
                createCategory();
                createIngredients();
                createProducts();
                createLocations();
                createUsers();
                createMenus();
                createOffers();
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
        // INGREDIENTS
        // -----------------------------------------------------------------------

        private void createIngredients() {
                String[][] ingredientsData = {
                        {"Cheddar", "Formaggio cheddar fuso", "0.80", "100.0"},
                        {"Bacon", "Bacon croccante affumicato", "1.20", "50.0"},
                        {"Cipolla Caramellata", "Cipolla dolce caramellata", "0.50", "80.0"},
                        {"Jalapeños", "Peperoncini piccanti a fette", "0.60", "40.0"},
                        {"Uovo", "Uovo all'occhio di bue", "1.00", "30.0"},
                        {"Doppio Pollo", "Un pezzo extra di pollo fritto", "2.50", "20.0"}
                };

                for (String[] data : ingredientsData) {
                        try {
                                com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.IngredientRequest req = 
                                        new com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.IngredientRequest();
                                req.setName(data[0]);
                                req.setDescription(data[1]);
                                req.setPrice(Double.parseDouble(data[2]));
                                req.setQuantity(Double.parseDouble(data[3]));
                                ingredientService.create(req);
                                log.info("Ingredient created: {}", data[0]);
                        } catch (Exception e) {
                                log.debug("Ingredient already exists or error: {}", e.getMessage());
                        }
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
                // Build ingredient name → ID map
                Map<String, Long> iMap = new HashMap<>();
                ingredientRepository.findAll().forEach(ing -> iMap.put(ing.getName(), ing.getId()));

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
                burger.setImgPath("images/prodotti/classic_blue_burger.jpg");
                burger.setIngredientIds(List.of(iMap.get("Cheddar"), iMap.get("Bacon")));
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
                spicyBurger.setImgPath("images/prodotti/the_crystal_spicy.jpg");
                spicyBurger.setIngredientIds(List.of(iMap.get("Jalapeños"), iMap.get("Cheddar")));
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
                wrapProd.setImgPath("images/prodotti/mountain_wrap.jpg");
                wrapProd.setIngredientIds(List.of(iMap.get("Bacon"), iMap.get("Cheddar")));
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
                friesProd.setImgPath("images/prodotti/diamond_fries.jpg");
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
                cheesyFries.setImgPath("images/prodotti/cheesy_blue_fries.jpg");
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
                nuggets.setImgPath("images/prodotti/crystal_nuggets.jpg");
                saveProduct(nuggets);

                ProductRequest wings = new ProductRequest();
                wings.setName("Spicy Wings");
                wings.setCategoryName("SNACK");
                wings.setPrice(4.50);
                wings.setDescription("Alette di pollo piccanti.");
                wings.setCalories(500);
                wings.setQuantity(6);
                wings.setIsSpicy(true);
                wings.setImgPath("images/prodotti/spicy_wings.jpg");
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
                drink1.setTemperature("Cold");
                drink1.setSize("Small");
                drink1.setImgPath("images/prodotti/blue_lagoon_soda.jpg");
                saveProduct(drink1);

                ProductRequest tea = new ProductRequest();
                tea.setName("Iced Tea");
                tea.setCategoryName("DRINK");
                tea.setPrice(2.00);
                tea.setDescription("Tè freddo pesca/limone.");
                tea.setCalories(90);
                tea.setLiters(0.33);
                tea.setIsCarbonated(false);
                tea.setTemperature("Cold");
                tea.setSize("Small");
                tea.setImgPath("images/prodotti/iced_tea.jpg");
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
                mayo.setImgPath("images/prodotti/crystal_mayo.jpg");
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
                bbq.setImgPath("images/prodotti/blue_bbq.jpg");
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
                brownie.setTemperature("Hot/Cold");
                brownie.setImgPath("images/prodotti/frozen_crystal_brownie.jpg");
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
        // MENUS
        // -----------------------------------------------------------------------

        private void createMenus() {
                if (!menuService.findAll().isEmpty()) {
                        log.debug("Menus already exist, skipping menu creation");
                        return;
                }

                // Build product name → ID map
                Map<String, Long> p = new HashMap<>();
                productRepository.findAll().forEach(prod -> p.put(prod.getName(), prod.getId()));

                MenuRequest classicMenu = new MenuRequest();
                classicMenu.setName("Classic Blue Menu");
                classicMenu.setPrice(8.50);
                classicMenu.setDescription("Il nostro menu classico: Classic Blue Burger, Diamond Fries e Blue Lagoon Soda.");
                classicMenu.setImgPath("images/menu/classic_blue_burger.jpg");
                classicMenu.setProducts(List.of(
                        new MenuProductRequest(p.get("Classic Blue Burger"), 1, true),
                        new MenuProductRequest(p.get("Blue Lagoon Soda"), 1, true),
                        new MenuProductRequest(p.get("Diamond Fries"), 1, true)
                ));

                try {
                        menuService.createMenu(classicMenu);
                        log.info("Menu created: {}", classicMenu.getName());
                } catch (Exception e) {
                        log.debug("Menu already exists or error, skipping: {}", e.getMessage());
                }
        }

        // -----------------------------------------------------------------------
        // OFFERS
        // -----------------------------------------------------------------------

        private void createOffers() {
                if (!offerService.findAll().isEmpty()) {
                        log.debug("Offers already exist, skipping offer creation");
                        return;
                }

                OfferRequest familyOffer = new OfferRequest();
                familyOffer.setName("Family Crystal Pack");
                familyOffer.setPrice(19.90);
                familyOffer.setDescription("4 Classic Blue Burger + 2 Diamond Fries + 1.5L Soda.");
                familyOffer.setImgPath("images/offers/family_pack.jpg");

                try {
                        offerService.create(familyOffer);
                        log.info("Offer created: {}", familyOffer.getName());
                } catch (Exception e) {
                        log.debug("Offer already exists or error, skipping: {}", e.getMessage());
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

                // Build ingredient name → ID map
                Map<String, Long> iMap = new HashMap<>();
                ingredientRepository.findAll().forEach(ing -> iMap.put(ing.getName(), ing.getId()));

                // Common location details for test orders
                String locName = "Blue Crystal Torino";
                String locAddr = "Via Roma 10";
                String locCity = "Torino";

                // ── ORDER 1: Dine-in, Card, Table T-05 ──────────────────────────
                // Classic Blue Burger + Diamond Fries + Blue Lagoon Soda
                OrderRequest o1 = new OrderRequest();
                o1.setUserId(user.getId());
                o1.setServiceType("DINE_IN");
                o1.setOrderType("STANDARD");
                o1.setTableNumber("T-05");
                o1.setPaymentType("CARD");
                o1.setLocationName(locName);
                o1.setLocationAddress(locAddr);
                o1.setLocationCity(locCity);
                o1.setItems(List.of(
                        new OrderItemRequest(p.get("Classic Blue Burger"), null, 1, "Extra Bacon", List.of(iMap.get("Bacon"))),
                        new OrderItemRequest(p.get("Diamond Fries"), null, 1, null, null),
                        new OrderItemRequest(p.get("Blue Lagoon Soda"), null, 1, null, null)));
                saveOrder(o1);

                // ── ORDER 2: Takeaway, Cash ──────────────────────────────────────
                // The Crystal Spicy + Cheesy Blue Fries + Iced Tea
                OrderRequest o2 = new OrderRequest();
                o2.setUserId(user.getId());
                o2.setServiceType("TAKEAWAY");
                o2.setOrderType("STANDARD");
                o2.setPaymentType("CASH");
                o2.setLocationName(locName);
                o2.setLocationAddress(locAddr);
                o2.setLocationCity(locCity);
                o2.setItems(List.of(
                        new OrderItemRequest(p.get("The Crystal Spicy"), null, 1, "Extra piccante", List.of(iMap.get("Jalapeños"), iMap.get("Uovo"))),
                        new OrderItemRequest(p.get("Cheesy Blue Fries"), null, 1, null, null),
                        new OrderItemRequest(p.get("Iced Tea"), null, 1, null, null)));
                saveOrder(o2);

                // ── ORDER 3: Delivery, Card ──────────────────────────────────────
                // Crystal Nuggets x2 + Spicy Wings + Blue BBQ
                OrderRequest o3 = new OrderRequest();
                o3.setUserId(user.getId());
                o3.setServiceType("DELIVERY");
                o3.setOrderType("STANDARD");
                o3.setPaymentType("CARD");
                o3.setLocationName(locName);
                o3.setLocationAddress(locAddr);
                o3.setLocationCity(locCity);
                o3.setItems(List.of(
                        new OrderItemRequest(p.get("Crystal Nuggets"), null, 2, null, null),
                        new OrderItemRequest(p.get("Spicy Wings"), null, 1, "Extra piccante per favore", null),
                        new OrderItemRequest(p.get("Blue BBQ"), null, 1, null, null)));
                saveOrder(o3);

                // ── ORDER 4: Dine-in, Cash, Table T-12 ──────────────────────────
                // Mountain Wrap + Classic Blue Burger + Crystal Mayo
                OrderRequest o4 = new OrderRequest();
                o4.setUserId(user.getId());
                o4.setServiceType("DINE_IN");
                o4.setOrderType("STANDARD");
                o4.setTableNumber("T-12");
                o4.setPaymentType("CASH");
                o4.setLocationName(locName);
                o4.setLocationAddress(locAddr);
                o4.setLocationCity(locCity);
                o4.setItems(List.of(
                        new OrderItemRequest(p.get("Mountain Wrap"), null, 1, null, null),
                        new OrderItemRequest(p.get("Classic Blue Burger"), null, 1, "Senza pomodoro", null),
                        new OrderItemRequest(p.get("Crystal Mayo"), null, 2, null, null)));
                saveOrder(o4);

                // ── ORDER 5: Dine-in, Card, Table T-03 ──────────────────────────
                // Frozen Crystal Brownie + Blue Lagoon Soda x2
                OrderRequest o5 = new OrderRequest();
                o5.setUserId(user.getId());
                o5.setServiceType("DINE_IN");
                o5.setOrderType("STANDARD");
                o5.setTableNumber("T-03");
                o5.setPaymentType("CARD");
                o5.setLocationName(locName);
                o5.setLocationAddress(locAddr);
                o5.setLocationCity(locCity);
                o5.setItems(List.of(
                        new OrderItemRequest(p.get("Frozen Crystal Brownie"), null, 1, null, null),
                        new OrderItemRequest(p.get("Blue Lagoon Soda"), null, 2, null, null)));
                saveOrder(o5);

                // ── ORDER 6: Takeaway, Card, WITH OFFER ─────────────────────────
                // Family Crystal Pack
                Map<String, Long> off = new HashMap<>();
                offerRepository.findAll().forEach(o -> off.put(o.getName(), o.getId()));

                OrderRequest o6 = new OrderRequest();
                o6.setUserId(user.getId());
                o6.setServiceType("TAKEAWAY");
                o6.setOrderType("STANDARD");
                o6.setPaymentType("CARD");
                o6.setLocationName(locName);
                o6.setLocationAddress(locAddr);
                o6.setLocationCity(locCity);
                o6.setItems(List.of(
                        new OrderItemRequest(null, off.get("Family Crystal Pack"), 1, "Consegna veloce", null)));
                saveOrder(o6);
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
