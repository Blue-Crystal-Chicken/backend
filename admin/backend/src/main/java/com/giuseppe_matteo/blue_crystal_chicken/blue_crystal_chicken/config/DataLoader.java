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
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.security.servicies.UserDetailsImpl;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.security.jwt.JwtUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.IngredientEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OrderEntity;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.OrderStatus;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.IngredientService;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.service.LocationIngredientService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        private IngredientService ingredientService;

        @Autowired
        private LocationIngredientService locationIngredientService;

        @PersistenceContext
        private EntityManager em;

        @Autowired
        private ProductRepository productRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private JwtUtils jwtUtils;

        @Autowired
        private OfferRepository offerRepository;

        @Override
        public void run(String... args) throws Exception {
                // Guardia globale: con ddl-auto=update i dati persistono tra i riavvii.
                // Se il seed è già stato eseguito (presenza dell'admin), NON riseminare
                // nulla, così non si creano duplicati a ogni avvio.
                if (userRepository.findByEmail("admin@bluecrystal.it").isPresent()) {
                        log.info("DataLoader: dati già presenti (admin seed trovato) → seeding saltato.");
                        logAdminToken("admin@bluecrystal.it");
                        return;
                }
                log.info("DataLoader: primo avvio, eseguo il seeding iniziale.");
                createCategory();
                createProducts();
                createLocations();
                createIngredients();
                createLocationStock();
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
                burger.setImgPath("images/prodotti/classic_blue_burger.jpg");
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

                // Utente ADMIN dedicato all'integrazione col servizio notifiche (8085).
                Register adminReq = new Register();
                adminReq.setName("Admin");
                adminReq.setSurname("Notifiche");
                adminReq.setEmail("admin@bluecrystal.it");
                adminReq.setPassword("admin123");
                adminReq.setPhone("3290000000");
                adminReq.setGender("Other");
                adminReq.setBirthday("2000-01-01");
                adminReq.setRole(Role.ADMIN);
                userService.registerUser(adminReq);

                // Utente MANAGER assegnato alla prima sede (per l'app manager :5185)
                Register managerReq = new Register();
                managerReq.setName("Mario");
                managerReq.setSurname("Manager");
                managerReq.setEmail("manager@bluecrystal.it");
                managerReq.setPassword("manager123");
                managerReq.setPhone("3290000001");
                managerReq.setGender("Other");
                managerReq.setBirthday("1990-01-01");
                managerReq.setRole(Role.MANAGER);
                userService.registerUser(managerReq);

                java.util.List<LocationEntity> locs = locationService.findAll();
                if (!locs.isEmpty()) {
                        UserEntity manager = userRepository.findByEmail("manager@bluecrystal.it").orElse(null);
                        if (manager != null) {
                                manager.setLocation(locs.get(0));
                                userRepository.save(manager);
                                log.info("MANAGER manager@bluecrystal.it / manager123 -> sede '{}' (id {})",
                                                locs.get(0).getName(), locs.get(0).getId());
                        }
                }

                logAdminToken("admin@bluecrystal.it");
        }

        /**
         * Genera e stampa nel log un JWT valido per l'admin indicato.
         * Il token è firmato con `be.app.jwtSecret` (HS512) e include il claim `roles`,
         * quindi è utilizzabile anche dal servizio notifiche (8085) se condivide lo stesso secret.
         * Nota: scade dopo `be.app.jwtExpirationMs` (default 24h) → a ogni riavvio ne viene stampato uno nuovo.
         */
        private void logAdminToken(String email) {
                UserEntity admin = userRepository.findByEmail(email).orElse(null);
                if (admin == null) {
                        log.warn("Admin {} non trovato: impossibile generare il token.", email);
                        return;
                }
                UserDetailsImpl ud = UserDetailsImpl.build(admin);
                Authentication auth = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
                String token = jwtUtils.generateJwtToken(auth);
                log.info("======================= TOKEN ADMIN (per 8085 / Giuseppe) =======================");
                log.info("Login:    {} / admin123", email);
                log.info("Ruolo:    ROLE_ADMIN  |  Scadenza: 24h dall'avvio");
                log.info("Bearer    {}", token);
                log.info("=================================================================================");
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
                        {"Blue Crystal Roma", "Via del Corso 50", "Roma", "06", "1234567"},
                        {"Blue Crystal Milano", "Piazza Duomo 1", "Milano", "02", "1234567"},
                        {"Blue Crystal Pescara", "Corso Umberto I 100", "Pescara", "085", "1234567"}
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
        // INGREDIENTS (catalogo globale)
        // -----------------------------------------------------------------------

        /**
         * Semina il catalogo ingredienti. Le quantità globali sono calibrate per
         * accendere TUTTI i KPI della pagina Magazzino:
         *  - alcune referenze sotto la soglia di 10 (Bacon, Jalapeños)
         *  - alcune esaurite a 0 (Salsa BBQ, Ghiaccio)
         */
        private void createIngredients() {
                if (!ingredientService.findAll().isEmpty()) {
                        log.debug("Ingredients already exist, skipping ingredient creation");
                        return;
                }

                // {nome, descrizione, prezzo, quantità}
                Object[][] data = {
                        {"Petto di Pollo", "Carne di pollo fresca da allevamento a terra", 4.50, 120.0},
                        {"Pane Brioche", "Panini morbidi tostati al momento", 0.40, 200.0},
                        {"Lattuga", "Insalata iceberg croccante", 1.20, 60.0},
                        {"Pomodoro", "Pomodori maturi a fette", 1.50, 45.0},
                        {"Cheddar", "Formaggio cheddar fuso", 2.80, 35.0},
                        {"Bacon", "Bacon affumicato croccante", 3.50, 8.0},
                        {"Patate", "Patate selezionate da frittura", 1.00, 150.0},
                        {"Olio di Semi", "Olio di semi per friggitrice", 2.00, 24.0},
                        {"Jalapeños", "Peperoncini jalapeño piccanti", 2.50, 5.0},
                        {"Salsa BBQ", "Salsa barbecue affumicata", 1.80, 0.0},
                        {"Maionese", "Maionese cremosa Crystal", 1.60, 18.0},
                        {"Ghiaccio", "Cubetti di ghiaccio per bevande", 0.20, 0.0}
                };

                for (Object[] row : data) {
                        try {
                                IngredientEntity ing = new IngredientEntity();
                                ing.setName((String) row[0]);
                                ing.setDescription((String) row[1]);
                                ing.setPrice((Double) row[2]);
                                ing.setQuantity((Double) row[3]);
                                ingredientService.create(ing);
                                log.info("Ingredient created: {}", ing.getName());
                        } catch (Exception e) {
                                log.debug("Ingredient skipped: {}", e.getMessage());
                        }
                }
        }

        // -----------------------------------------------------------------------
        // LOCATION STOCK (scorte per sede)
        // -----------------------------------------------------------------------

        /**
         * Distribuisce tutti gli ingredienti su tutte le sedi con quantità varie,
         * includendo volutamente alcune scorte sotto soglia / a zero così da
         * popolare il dettaglio sede e l'endpoint /stock/low.
         */
        private void createLocationStock() {
                List<LocationEntity> locations = locationService.findAll();
                List<IngredientEntity> ingredients = ingredientService.findAll();
                if (locations.isEmpty() || ingredients.isEmpty()) {
                        log.debug("No locations or ingredients, skipping stock seeding");
                        return;
                }

                // Evita di ri-seminare se una sede ha già scorte
                if (!locationIngredientService.findByLocationId(locations.get(0).getId()).isEmpty()) {
                        log.debug("Location stock already exists, skipping");
                        return;
                }

                int i = 0;
                for (LocationEntity loc : locations) {
                        for (IngredientEntity ing : ingredients) {
                                // Una scorta su cinque è critica (0, 3 o 6 unità), le altre abbondanti
                                double qty = ((i + ing.getId()) % 5 == 0)
                                        ? (i % 3) * 3.0
                                        : 20.0 + ((i * 7 + ing.getId()) % 80);
                                try {
                                        locationIngredientService.addIngredientToLocation(loc.getId(), ing.getId(), qty);
                                } catch (Exception e) {
                                        log.debug("Stock skipped for loc {} ing {}: {}", loc.getId(), ing.getId(), e.getMessage());
                                }
                                i++;
                        }
                        log.info("Stock seeded for location: {}", loc.getName());
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

                java.time.LocalDateTime now = java.time.LocalDateTime.now();

                OfferRequest familyOffer = new OfferRequest();
                familyOffer.setName("Family Crystal Pack");
                familyOffer.setPrice(19.90);
                familyOffer.setDescription("4 Classic Blue Burger + 2 Diamond Fries + 1.5L Soda.");
                familyOffer.setImgPath("images/offers/family_pack.jpg");
                familyOffer.setDiscountPercentage(20.0);
                familyOffer.setStartDate(now.minusDays(3).toString());
                familyOffer.setEndDate(now.plusDays(27).toString());
                familyOffer.setActive(true);

                OfferRequest lunchOffer = new OfferRequest();
                lunchOffer.setName("Crystal Lunch Deal");
                lunchOffer.setPrice(9.90);
                lunchOffer.setDescription("1 Burger a scelta + Diamond Fries + bibita media.");
                lunchOffer.setImgPath("images/offers/lunch_deal.jpg");
                lunchOffer.setDiscountPercentage(15.0);
                lunchOffer.setStartDate(now.minusDays(1).toString());
                lunchOffer.setEndDate(now.plusDays(13).toString());
                lunchOffer.setActive(true);

                OfferRequest summerOffer = new OfferRequest();
                summerOffer.setName("Summer Frost Combo");
                summerOffer.setPrice(12.50);
                summerOffer.setDescription("Crystal Spicy + Cheesy Blue Fries + Iced Tea.");
                summerOffer.setImgPath("images/offers/summer_combo.jpg");
                summerOffer.setDiscountPercentage(10.0);
                // Offerta scaduta, per popolare lo stato "non attiva"
                summerOffer.setStartDate(now.minusDays(40).toString());
                summerOffer.setEndDate(now.minusDays(10).toString());
                summerOffer.setActive(false);

                for (OfferRequest req : java.util.List.of(familyOffer, lunchOffer, summerOffer)) {
                        try {
                                offerService.create(req);
                                log.info("Offer created: {}", req.getName());
                        } catch (Exception e) {
                                log.debug("Offer already exists or error, skipping: {}", e.getMessage());
                        }
                }
        }

        // -----------------------------------------------------------------------
        // ORDERS
        // -----------------------------------------------------------------------

        /**
         * Semina ~40 ordini distribuiti sugli ultimi 30 giorni, ciascuno con sede,
         * tipo servizio, pagamento, basket e stato variati. Gli ordini vengono poi
         * RETRODATATI via query nativa: createdAt è updatable=false e impostato in
         * @PrePersist, quindi non è modificabile tramite JPA. Questo popola in modo
         * realistico Dashboard, Finanze, Ordini e Report (trend, funnel, per-sede).
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

                List<LocationEntity> locations = locationService.findAll();
                if (locations.isEmpty()) {
                        log.warn("No locations, skipping order creation");
                        return;
                }

                // Mappa nome prodotto → ID e nome offerta → ID
                Map<String, Long> p = new HashMap<>();
                productRepository.findAll().forEach(prod -> p.put(prod.getName(), prod.getId()));
                Map<String, Long> off = new HashMap<>();
                offerRepository.findAll().forEach(o -> off.put(o.getName(), o.getId()));

                // Panieri ricorrenti (l'ultimo usa un'offerta, per popolare i bundle)
                List<List<OrderItemRequest>> baskets = List.of(
                        List.of(new OrderItemRequest(p.get("Classic Blue Burger"), null, 1, null),
                                new OrderItemRequest(p.get("Diamond Fries"), null, 1, null),
                                new OrderItemRequest(p.get("Blue Lagoon Soda"), null, 1, null)),
                        List.of(new OrderItemRequest(p.get("The Crystal Spicy"), null, 1, null),
                                new OrderItemRequest(p.get("Cheesy Blue Fries"), null, 1, null),
                                new OrderItemRequest(p.get("Iced Tea"), null, 1, null)),
                        List.of(new OrderItemRequest(p.get("Crystal Nuggets"), null, 2, null),
                                new OrderItemRequest(p.get("Spicy Wings"), null, 1, "Extra piccante"),
                                new OrderItemRequest(p.get("Blue BBQ"), null, 1, null)),
                        List.of(new OrderItemRequest(p.get("Mountain Wrap"), null, 1, null),
                                new OrderItemRequest(p.get("Classic Blue Burger"), null, 1, "Senza pomodoro"),
                                new OrderItemRequest(p.get("Crystal Mayo"), null, 2, null)),
                        List.of(new OrderItemRequest(p.get("Frozen Crystal Brownie"), null, 1, null),
                                new OrderItemRequest(p.get("Blue Lagoon Soda"), null, 2, null)),
                        List.of(new OrderItemRequest(p.get("Classic Blue Burger"), null, 2, null),
                                new OrderItemRequest(p.get("Cheesy Blue Fries"), null, 1, null),
                                new OrderItemRequest(p.get("Iced Tea"), null, 2, null)),
                        List.of(new OrderItemRequest(null, off.get("Family Crystal Pack"), 1, "Consegna veloce"))
                );

                String[] services = { "DINE_IN", "TAKEAWAY", "DELIVERY" };
                String[] payments = { "CARD", "CASH" };
                LocalDateTime now = LocalDateTime.now();

                int idx = 0;
                for (int d = 29; d >= 0; d--) {
                        int ordersToday = (d % 3 == 0) ? 2 : 1; // volume variabile
                        for (int k = 0; k < ordersToday; k++) {
                                LocationEntity loc = locations.get(idx % locations.size());
                                String service = services[idx % services.length];
                                String payment = payments[idx % payments.length];
                                List<OrderItemRequest> basket = baskets.get(idx % baskets.size());
                                String table = "DINE_IN".equals(service) ? ("T-" + (1 + (idx % 20))) : null;

                                // Stato: gli ordini odierni sono in lavorazione/attesa, i più
                                // vecchi consegnati, con qualche cancellazione sparsa.
                                OrderStatus status;
                                if (d == 0) status = (k == 0) ? OrderStatus.PREPARING : OrderStatus.PENDING;
                                else if (d == 1) status = OrderStatus.READY;
                                else if (idx % 11 == 0) status = OrderStatus.CANCELLED;
                                else status = OrderStatus.DELIVERED;

                                // Timestamp realistico: orari di servizio 11–20, oggi entro l'ultima ora
                                LocalDateTime when = (d == 0)
                                        ? now.minusMinutes(20L * (k + 1))
                                        : now.minusDays(d)
                                                .withHour(11 + (idx % 10))
                                                .withMinute((idx * 7) % 60)
                                                .withSecond(0).withNano(0);

                                OrderRequest req = new OrderRequest();
                                req.setUserId(user.getId());
                                req.setLocationId(loc.getId());
                                req.setServiceType(service);
                                req.setOrderType("STANDARD");
                                req.setTableNumber(table);
                                req.setPaymentType(payment);
                                req.setItems(basket);

                                saveBackdatedOrder(req, status, when);
                                idx++;
                        }
                }
                log.info("Seeded {} backdated orders across 30 days", idx);
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

        /**
         * Crea un ordine e ne forza stato e data via query nativa.
         * createdAt è updatable=false → l'unico modo per retrodatarlo è SQL diretto.
         * status/created_at/updated_at vengono scritti in un solo UPDATE, bypassando
         * Hibernate (che, grazie a updatable=false, non sovrascriverà più createdAt).
         */
        private void saveBackdatedOrder(OrderRequest request, OrderStatus status, LocalDateTime when) {
                try {
                        OrderEntity saved = orderService.create(request);
                        em.flush(); // garantisce l'INSERT prima dell'UPDATE nativo
                        em.createNativeQuery(
                                "UPDATE orders SET status = :st, created_at = :ts, updated_at = :ts WHERE id = :id")
                                .setParameter("st", status.name())
                                .setParameter("ts", when)
                                .setParameter("id", saved.getId())
                                .executeUpdate();
                } catch (Exception e) {
                        log.debug("Order skipped: {}", e.getMessage());
                }
        }
}
