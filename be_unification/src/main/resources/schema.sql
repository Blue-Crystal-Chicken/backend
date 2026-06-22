-- ============================================================
-- SCHEMA DDL - blue_crystal_chicken
-- ============================================================

-- ------------------------------------------------------------
-- USERS
-- ------------------------------------------------------------
CREATE TABLE USERS (
    id          BIGSERIAL       NOT NULL,
    Name        VARCHAR(255)    NOT NULL,
    Surname     VARCHAR(255)    NOT NULL,
    Birthday    DATE,
    Email       VARCHAR(255)    NOT NULL UNIQUE,
    Password    VARCHAR(255)    NOT NULL,
    Gender      VARCHAR(50),
    Phone       VARCHAR(50),
    Role        VARCHAR(50)     NOT NULL DEFAULT 'USER',
    Created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Updated_at  TIMESTAMP,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

-- ------------------------------------------------------------
-- LOCATIONS
-- ------------------------------------------------------------
CREATE TABLE LOCATIONS (
    id              BIGSERIAL       NOT NULL,
    Name            VARCHAR(255)    NOT NULL,
    Address         VARCHAR(255),
    City            VARCHAR(100),
    Phone_code      VARCHAR(20),
    Phone_number    VARCHAR(50),
    Opening_hours   VARCHAR(255),
    Status          VARCHAR(50),
    Created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Updated_at      TIMESTAMP,
    CONSTRAINT pk_locations PRIMARY KEY (id)
);

-- ------------------------------------------------------------
-- OFFER
-- ------------------------------------------------------------
CREATE TABLE OFFER (
    id          BIGSERIAL       NOT NULL,
    Name        VARCHAR(255)    NOT NULL,
    Description TEXT,
    Created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Updated_at  TIMESTAMP,
    CONSTRAINT pk_offer PRIMARY KEY (id)
);

-- ------------------------------------------------------------
-- MENU
-- ------------------------------------------------------------
CREATE TABLE MENU (
    id          BIGSERIAL       NOT NULL,
    Name        VARCHAR(255)    NOT NULL,
    Price       FLOAT           NOT NULL,
    Description VARCHAR(255),
    Created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Updated_at  TIMESTAMP,
    CONSTRAINT pk_menu PRIMARY KEY (id)
);

-- ------------------------------------------------------------
-- PRODUCTS
-- ------------------------------------------------------------
CREATE TABLE PRODUCTS (
    id               BIGSERIAL       NOT NULL,
    Name             VARCHAR(255)    NOT NULL,
    Category         VARCHAR(100),
    Size             VARCHAR(50),
    Quantity         INT,
    Additions        FLOAT,
    Price            FLOAT           NOT NULL,
    Nutritional_info TEXT,
    Created_at       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Updated_at       TIMESTAMP,
    CONSTRAINT pk_products PRIMARY KEY (id)
);

-- ------------------------------------------------------------
-- INGREDIENTS
-- ------------------------------------------------------------
CREATE TABLE INGREDIENTS (
    id          BIGSERIAL       NOT NULL,
    Name        VARCHAR(255)    NOT NULL,
    Description VARCHAR(255),
    Price       FLOAT,
    Quantity    FLOAT,
    Created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Updated_at  TIMESTAMP,
    CONSTRAINT pk_ingredients PRIMARY KEY (id)
);

-- ------------------------------------------------------------
-- ORDERS
-- ------------------------------------------------------------
CREATE TABLE ORDERS (
    id              BIGSERIAL       NOT NULL,
    Order_id        VARCHAR(100),
    Code            VARCHAR(100),
    Service_type    VARCHAR(50),
    Order_type      VARCHAR(50),
    Table_number    VARCHAR(50),
    Payment_type    VARCHAR(50),
    Payment_amount  DECIMAL(10,2),
    Total_at        DECIMAL(10,2),
    Created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Updated_at      TIMESTAMP,
    User_id         BIGINT          NOT NULL,
    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT fk_orders_users FOREIGN KEY (User_id)
        REFERENCES USERS (id)
);

-- ------------------------------------------------------------
-- Offer_Menu  (junction: OFFER ↔ MENU)
-- ------------------------------------------------------------
CREATE TABLE Offer_Menu (
    Offer_id    BIGINT      NOT NULL,
    Menu_id     BIGINT      NOT NULL,
    Created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Updated_at  TIMESTAMP,
    CONSTRAINT pk_offer_menu PRIMARY KEY (Offer_id, Menu_id),
    CONSTRAINT fk_offermenu_offer FOREIGN KEY (Offer_id)
        REFERENCES OFFER (id),
    CONSTRAINT fk_offermenu_menu FOREIGN KEY (Menu_id)
        REFERENCES MENU (id)
);

-- ------------------------------------------------------------
-- Menu_Products  (junction: MENU ↔ PRODUCTS)
-- ------------------------------------------------------------
CREATE TABLE Menu_Products (
    Menu_id     BIGINT      NOT NULL,
    Product_id  BIGINT      NOT NULL,
    Quantity    INT,
    Obligatory  BOOLEAN     DEFAULT FALSE,
    Created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Updated_at  TIMESTAMP,
    CONSTRAINT pk_menu_products PRIMARY KEY (Menu_id, Product_id),
    CONSTRAINT fk_menuproducts_menu FOREIGN KEY (Menu_id)
        REFERENCES MENU (id),
    CONSTRAINT fk_menuproducts_product FOREIGN KEY (Product_id)
        REFERENCES PRODUCTS (id)
);

-- ------------------------------------------------------------
-- Offer_Products  (junction: OFFER ↔ PRODUCTS)
-- ------------------------------------------------------------
CREATE TABLE Offer_Products (
    Offer_id    BIGINT      NOT NULL,
    Product_id  BIGINT      NOT NULL,
    FK          BIGINT,
    Quantity    INT,
    Created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Updated_at  TIMESTAMP,
    CONSTRAINT pk_offer_products PRIMARY KEY (Offer_id, Product_id),
    CONSTRAINT fk_offerproducts_offer FOREIGN KEY (Offer_id)
        REFERENCES OFFER (id),
    CONSTRAINT fk_offerproducts_product FOREIGN KEY (Product_id)
        REFERENCES PRODUCTS (id)
);

-- ------------------------------------------------------------
-- Product_Ingredients  (junction: PRODUCTS ↔ INGREDIENTS)
-- ------------------------------------------------------------
CREATE TABLE Product_Ingredients (
    Product_id      BIGINT      NOT NULL,
    Ingredient_id   BIGINT      NOT NULL,
    Created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Updated_at      TIMESTAMP,
    CONSTRAINT pk_product_ingredients PRIMARY KEY (Product_id, Ingredient_id),
    CONSTRAINT fk_prodingr_product FOREIGN KEY (Product_id)
        REFERENCES PRODUCTS (id),
    CONSTRAINT fk_prodingr_ingredient FOREIGN KEY (Ingredient_id)
        REFERENCES INGREDIENTS (id)
);

-- ------------------------------------------------------------
-- Location_Ingredients  (junction: LOCATIONS ↔ INGREDIENTS)
-- ------------------------------------------------------------
CREATE TABLE Location_Ingredients (
    Location_id     BIGINT      NOT NULL,
    Ingredient_id   BIGINT      NOT NULL,
    Quantity        FLOAT,
    Created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Updated_at      TIMESTAMP,
    CONSTRAINT pk_location_ingredients PRIMARY KEY (Location_id, Ingredient_id),
    CONSTRAINT fk_locingr_location FOREIGN KEY (Location_id)
        REFERENCES LOCATIONS (id),
    CONSTRAINT fk_locingr_ingredient FOREIGN KEY (Ingredient_id)
        REFERENCES INGREDIENTS (id)
);

-- ------------------------------------------------------------
-- Order_Products  (junction: ORDERS ↔ PRODUCTS)
-- ------------------------------------------------------------
CREATE TABLE Order_Products (
    Order_id        BIGINT      NOT NULL,
    Product_id      BIGINT      NOT NULL,
    Additions       FLOAT,
    Quantity        INT,
    Price           FLOAT,
    Special_note    VARCHAR(255),
    Created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    Updated_at      TIMESTAMP,
    CONSTRAINT pk_order_products PRIMARY KEY (Order_id, Product_id),
    CONSTRAINT fk_ordprod_order FOREIGN KEY (Order_id)
        REFERENCES ORDERS (id),
    CONSTRAINT fk_ordprod_product FOREIGN KEY (Product_id)
        REFERENCES PRODUCTS (id)
);