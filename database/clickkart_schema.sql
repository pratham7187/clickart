-- =============================================================================
--  ClickKart - Complete MySQL 8.0 Schema
--  File    : clickkart_schema.sql
--  DB      : clickkart_db
--  Engine  : InnoDB
--  Charset : utf8mb4 / utf8mb4_unicode_ci
-- =============================================================================
--
--  CREATION ORDER (dependency safe - no FK errors)
--  1. users
--  2. categories
--  3. products       (depends on: categories)
--  4. cart           (depends on: users)
--  5. cart_items     (depends on: cart, products)
--  6. wishlist_items (depends on: users, products)
--  7. orders         (depends on: users)
--  8. order_items    (depends on: orders, products)
--
--  HOW TO RUN
--  MySQL Workbench : File > Open SQL Script > Execute All (lightning bolt)
--  MySQL CLI       : mysql -u root -p < clickkart_schema.sql
--
--  SEED DATA PASSWORDS (BCrypt strength=10)
--  admin@clickkart.com  ->  Admin@1234
--  priya@example.com    ->  User@1234
--  arjun@example.com    ->  User@1234
--
--  If login fails after Spring Boot setup, regenerate hashes with:
--      new BCryptPasswordEncoder(10).encode("Admin@1234")
--  then UPDATE the users table with the fresh hash.
-- =============================================================================


-- ---------------------------------------------------------------------------
-- Session settings
-- ---------------------------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = 'STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';
SET NAMES utf8mb4;


-- ---------------------------------------------------------------------------
-- Database
-- ---------------------------------------------------------------------------
DROP DATABASE IF EXISTS clickkart_db;
CREATE DATABASE clickkart_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE clickkart_db;


-- =============================================================================
--  TABLE 1: users
--  One row per registered account (customer or admin).
--  Passwords are BCrypt hashes. is_active=0 is soft-delete.
-- =============================================================================
CREATE TABLE users (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Surrogate primary key',
    name        VARCHAR(100) NOT NULL                COMMENT 'Full display name',
    email       VARCHAR(150) NOT NULL                COMMENT 'Login email - globally unique',
    password    VARCHAR(255) NOT NULL                COMMENT 'BCrypt hash - never plain text',
    role        ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER' COMMENT 'RBAC role',
    is_active   TINYINT(1)   NOT NULL DEFAULT 1      COMMENT '1=active 0=soft-deleted',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Registration timestamp',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update timestamp',

    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'All registered ClickKart accounts';


-- =============================================================================
--  TABLE 2: categories
--  Lookup table for product categories.
--  Replaces a hardcoded ENUM - new categories can be added at runtime.
-- =============================================================================
CREATE TABLE categories (
    id           INT          NOT NULL AUTO_INCREMENT COMMENT 'Surrogate primary key',
    name         VARCHAR(100) NOT NULL                COMMENT 'API slug: men | women | kids',
    display_name VARCHAR(150) NOT NULL                COMMENT 'UI label: Mens Fashion etc',
    description  TEXT                                 COMMENT 'Optional category description',
    image_url    VARCHAR(500)                         COMMENT 'Hero image path',
    sort_order   INT          NOT NULL DEFAULT 0      COMMENT 'Display sequence on home page',
    is_active    TINYINT(1)   NOT NULL DEFAULT 1      COMMENT '0 hides category from storefront',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Row creation timestamp',

    PRIMARY KEY (id),
    UNIQUE KEY uq_categories_name (name),
    INDEX idx_categories_sort (sort_order)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Product categories - extensible lookup table';


-- =============================================================================
--  TABLE 3: products
--  The complete product catalog.
--  price must be > 0. stock cannot go below 0 (enforced by CHECK constraint).
--  FULLTEXT index on (name, subcategory) powers keyword search.
-- =============================================================================
CREATE TABLE products (
    id          BIGINT         NOT NULL AUTO_INCREMENT COMMENT 'Surrogate primary key',
    name        VARCHAR(200)   NOT NULL                COMMENT 'Product display name',
    price       DECIMAL(10,2)  NOT NULL                COMMENT 'Unit price in INR - numeric only',
    image_url   VARCHAR(500)   NOT NULL                COMMENT 'Relative asset path or URL',
    category_id INT            NOT NULL                COMMENT 'FK to categories.id',
    subcategory VARCHAR(100)   NOT NULL                COMMENT 'tshirt|formal|jeans|sarees|kurtis etc',
    description TEXT                                   COMMENT 'Long-form product description',
    stock       INT            NOT NULL DEFAULT 0      COMMENT 'Available inventory count',
    is_active   TINYINT(1)     NOT NULL DEFAULT 1      COMMENT '0 = delisted, stays on order history',
    created_at  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Listing timestamp',
    updated_at  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last admin update',

    PRIMARY KEY (id),

    CONSTRAINT chk_products_price CHECK (price > 0),
    CONSTRAINT chk_products_stock CHECK (stock >= 0),

    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    INDEX idx_products_category_id (category_id),
    INDEX idx_products_subcategory  (subcategory),
    INDEX idx_products_price        (price),
    INDEX idx_products_is_active    (is_active),
    INDEX idx_products_cat_sub      (category_id, subcategory),
    FULLTEXT INDEX idx_products_fulltext (name, subcategory)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'ClickKart product catalog';


-- =============================================================================
--  TABLE 4: cart
--  One cart header per user (enforced by UNIQUE on user_id).
--  Created lazily on first add-to-cart. Cleared after checkout.
-- =============================================================================
CREATE TABLE cart (
    id         BIGINT   NOT NULL AUTO_INCREMENT COMMENT 'Surrogate primary key',
    user_id    BIGINT   NOT NULL                COMMENT 'FK to users.id - one cart per user',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Cart creation timestamp',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last modification timestamp',

    PRIMARY KEY (id),
    UNIQUE KEY uq_cart_user_id (user_id),

    CONSTRAINT fk_cart_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Cart header - one row per user';


-- =============================================================================
--  TABLE 5: cart_items
--  Individual product lines inside a cart.
--  Price is NOT stored here - price snapshot happens at checkout in order_items.
--  UNIQUE(cart_id, product_id) prevents the same product appearing twice.
-- =============================================================================
CREATE TABLE cart_items (
    id         BIGINT   NOT NULL AUTO_INCREMENT COMMENT 'Surrogate primary key',
    cart_id    BIGINT   NOT NULL                COMMENT 'FK to cart.id',
    product_id BIGINT   NOT NULL                COMMENT 'FK to products.id',
    quantity   INT      NOT NULL DEFAULT 1      COMMENT 'Units in cart - minimum 1',
    added_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When item was added',

    PRIMARY KEY (id),

    CONSTRAINT chk_cart_items_qty CHECK (quantity >= 1),

    UNIQUE KEY uq_cart_items_cart_product (cart_id, product_id),

    CONSTRAINT fk_cart_items_cart
        FOREIGN KEY (cart_id) REFERENCES cart (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_cart_items_product
        FOREIGN KEY (product_id) REFERENCES products (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Cart line items - one row per product per cart';


-- =============================================================================
--  TABLE 6: wishlist_items
--  Products a user has bookmarked. No quantity - wishlist is a save-for-later list.
--  UNIQUE(user_id, product_id) prevents duplicate wishlist entries.
-- =============================================================================
CREATE TABLE wishlist_items (
    id         BIGINT   NOT NULL AUTO_INCREMENT COMMENT 'Surrogate primary key',
    user_id    BIGINT   NOT NULL                COMMENT 'FK to users.id',
    product_id BIGINT   NOT NULL                COMMENT 'FK to products.id',
    added_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When product was wishlisted',

    PRIMARY KEY (id),

    UNIQUE KEY uq_wishlist_user_product (user_id, product_id),

    CONSTRAINT fk_wishlist_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_wishlist_product
        FOREIGN KEY (product_id) REFERENCES products (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    INDEX idx_wishlist_user_id (user_id)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Wishlist - products saved by users';


-- =============================================================================
--  TABLE 7: orders
--  One row per checkout event. total_amount is a snapshot - never recalculate.
--  ON DELETE RESTRICT on user_id: orders are legal records, use soft-delete.
--  Status lifecycle: PLACED > CONFIRMED > SHIPPED > DELIVERED | CANCELLED
-- =============================================================================
CREATE TABLE orders (
    id           BIGINT         NOT NULL AUTO_INCREMENT COMMENT 'Surrogate primary key',
    user_id      BIGINT         NOT NULL                COMMENT 'FK to users.id',
    total_amount DECIMAL(10,2)  NOT NULL                COMMENT 'Checkout total snapshot - never changes after insert',
    pincode      VARCHAR(10)    NOT NULL                COMMENT 'Delivery pincode',
    address      TEXT           NOT NULL                COMMENT 'Full delivery address snapshot',
    status       ENUM('PLACED','CONFIRMED','SHIPPED','DELIVERED','CANCELLED') NOT NULL DEFAULT 'PLACED' COMMENT 'Order lifecycle status',
    ordered_at   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Checkout timestamp',
    updated_at   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last status change timestamp',

    PRIMARY KEY (id),

    CONSTRAINT chk_orders_total CHECK (total_amount > 0),

    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    INDEX idx_orders_user_id   (user_id),
    INDEX idx_orders_status    (status),
    INDEX idx_orders_ordered_at (ordered_at)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Order headers - one row per checkout';


-- =============================================================================
--  TABLE 8: order_items
--  Individual product lines within an order (supports multi-product orders).
--  price_at_purchase: IMMUTABLE price snapshot at checkout time.
--  ON DELETE RESTRICT on product_id: cannot delete a product on a real order.
-- =============================================================================
CREATE TABLE order_items (
    id                BIGINT        NOT NULL AUTO_INCREMENT COMMENT 'Surrogate primary key',
    order_id          BIGINT        NOT NULL               COMMENT 'FK to orders.id',
    product_id        BIGINT        NOT NULL               COMMENT 'FK to products.id',
    quantity          INT           NOT NULL               COMMENT 'Units purchased',
    price_at_purchase DECIMAL(10,2) NOT NULL               COMMENT 'Unit price snapshot at checkout - never updated after insert',

    PRIMARY KEY (id),

    CONSTRAINT chk_order_items_qty   CHECK (quantity >= 1),
    CONSTRAINT chk_order_items_price CHECK (price_at_purchase > 0),

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id) REFERENCES products (id)
        ON DELETE RESTRICT,

    INDEX idx_order_items_order_id   (order_id),
    INDEX idx_order_items_product_id (product_id)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Order line items - price snapshot per product per order';


-- ---------------------------------------------------------------------------
-- Re-enable FK checks
-- ---------------------------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 1;


-- =============================================================================
--  VERIFY: confirm all 8 tables exist
-- =============================================================================
SELECT
    TABLE_NAME    AS `Table`,
    TABLE_ROWS    AS `Rows`,
    ENGINE        AS `Engine`
FROM
    INFORMATION_SCHEMA.TABLES
WHERE
    TABLE_SCHEMA = 'clickkart_db'
ORDER BY
    TABLE_NAME;


-- =============================================================================
-- =============================================================================
--
--   SEED DATA
--
-- =============================================================================
-- =============================================================================


-- ---------------------------------------------------------------------------
-- SEED 1: categories
-- ---------------------------------------------------------------------------
INSERT INTO categories (name, display_name, description, image_url, sort_order, is_active) VALUES
('men',   'Mens Fashion',   'T-shirts, formal shirts, jeans, joggers and footwear for men.',    'assets/image/men.jpg',   1, 1),
('women', 'Womens Fashion', 'Sarees, lehengas, kurtis, jeans and footwear for women.',          'assets/image/women.jpg', 2, 1),
('kids',  'Kids Fashion',   'Clothing and footwear for boys and girls.',                        'assets/image/kid.jpg',   3, 1);


-- ---------------------------------------------------------------------------
-- SEED 2: users
-- ---------------------------------------------------------------------------
-- Passwords are BCrypt hashes at strength 10.
--
-- +-------------------------+-------------+-------+
-- | Email                   | Password    | Role  |
-- +-------------------------+-------------+-------+
-- | admin@clickkart.com     | Admin@1234  | ADMIN |
-- | priya@example.com       | User@1234   | USER  |
-- | arjun@example.com       | User@1234   | USER  |
-- +-------------------------+-------------+-------+
--
-- If login fails after Spring Boot is running, regenerate hashes:
--   String hash = new BCryptPasswordEncoder(10).encode("Admin@1234");
-- then UPDATE users SET password = '<hash>' WHERE email = 'admin@clickkart.com';
-- ---------------------------------------------------------------------------
INSERT INTO users (name, email, password, role, is_active) VALUES
(
    'ClickKart Admin',
    'admin@clickkart.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'ADMIN',
    1
),
(
    'Priya Sharma',
    'priya@example.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'USER',
    1
),
(
    'Arjun Mehta',
    'arjun@example.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'USER',
    1
);

-- NOTE: The hash above ($2a$10$92IXUNpk...) is the well-known BCrypt test vector
-- for the password "password". It is intentionally used as a placeholder so the
-- INSERT succeeds. Replace with real hashes using BCryptPasswordEncoder before
-- any real testing or deployment. Use the UPDATE statement pattern shown above.


-- ---------------------------------------------------------------------------
-- SEED 3: products (30 products across 3 categories)
-- ---------------------------------------------------------------------------
-- category_id: 1=men | 2=women | 3=kids
-- ---------------------------------------------------------------------------
INSERT INTO products (name, price, image_url, category_id, subcategory, description, stock, is_active) VALUES

-- --------------------------------------------------------
-- MEN - T-Shirts
-- --------------------------------------------------------
(
    'Classic Round Neck T-Shirt',
    499.00,
    'assets/imgmentshirt/t1.jpeg',
    1, 'tshirt',
    'Clean round-neck classic T-shirt in breathable cotton. Perfect for everyday casual wear.',
    150, 1
),
(
    'Printed Graphic Tee',
    599.00,
    'assets/imgmentshirt/t2.jpeg',
    1, 'tshirt',
    'Vibrant all-over graphic printed tee. Regular fit with ribbed collar.',
    120, 1
),
(
    'U-Neck Casual Tee',
    549.00,
    'assets/imgmentshirt/t3.jpeg',
    1, 'tshirt',
    'Comfortable U-neck tee in soft jersey fabric. Ideal for layering.',
    100, 1
),
(
    'Dropped Shoulder T-Shirt',
    479.00,
    'assets/imgmentshirt/t4.jpeg',
    1, 'tshirt',
    'Easy-fit casual T-shirt with a dropped shoulder. Great for relaxed weekends.',
    130, 1
),

-- --------------------------------------------------------
-- MEN - Formal Shirts
-- --------------------------------------------------------
(
    'Green Formal Shirt',
    999.00,
    'assets/imgformals/f1a.jpeg',
    1, 'formal',
    'Sharp full-sleeve formal shirt in muted sage green. Wrinkle-resistant cotton blend.',
    60, 1
),
(
    'Sky Blue Formal Shirt',
    899.00,
    'assets/imgformals/f2a.jpeg',
    1, 'formal',
    'Slim-fit sky blue formal shirt with spread collar. Pairs well with trousers and chinos.',
    75, 1
),
(
    'Wine Formal Shirt',
    949.00,
    'assets/imgformals/f3a.jpeg',
    1, 'formal',
    'Deep wine-tone formal shirt with concealed button placket. Elegant for evening events.',
    50, 1
),
(
    'Classic White Formal Shirt',
    1049.00,
    'assets/imgformals/f4b.jpeg',
    1, 'formal',
    'Crisp white formal shirt - a wardrobe essential for every professional.',
    90, 1
),

-- --------------------------------------------------------
-- MEN - Jeans
-- --------------------------------------------------------
(
    'Slim Fit Dark Wash Jeans',
    1299.00,
    'assets/jeans/j1.jpeg',
    1, 'jeans',
    'Slim fit dark-wash denim with comfortable stretch fabric. Casual and semi-formal.',
    80, 1
),
(
    'Classic Blue Denim Jeans',
    1399.00,
    'assets/jeans/j2.jpeg',
    1, 'jeans',
    'Mid-rise classic blue denim. Straight cut with five-pocket design.',
    65, 1
),

-- --------------------------------------------------------
-- MEN - Joggers
-- --------------------------------------------------------
(
    'Track Joggers',
    899.00,
    'assets/joggers/jo1.jpeg',
    1, 'joggers',
    'Lightweight polyester track joggers with elastic waistband and side pockets.',
    110, 1
),
(
    'Grey Marl Fleece Joggers',
    949.00,
    'assets/joggers/jo2.jpeg',
    1, 'joggers',
    'Comfortable grey marl fleece joggers with tapered fit and ribbed cuffs.',
    95, 1
),

-- --------------------------------------------------------
-- MEN - Footwear
-- --------------------------------------------------------
(
    'Adidas Style Sneakers',
    1499.00,
    'assets/footwear/fo1.jpeg',
    1, 'footwear',
    'Classic low-top sneakers with cushioned EVA sole. Lightweight for daily use.',
    45, 1
),
(
    'Casual Canvas Sneakers',
    1399.00,
    'assets/footwear/fo2.jpeg',
    1, 'footwear',
    'Lace-up canvas sneakers in a clean minimal design. Versatile for any casual outfit.',
    55, 1
),

-- --------------------------------------------------------
-- WOMEN - Sarees
-- --------------------------------------------------------
(
    'Silk Saree',
    1499.00,
    'assets/imgwomen/s1.jpeg',
    2, 'sarees',
    'Elegant pure silk saree with zari border. Timeless for festivals and weddings.',
    40, 1
),
(
    'Handloom Cotton Saree',
    1199.00,
    'assets/imgwomen/s2.jpeg',
    2, 'sarees',
    'Soft handloom cotton saree with woven border. Comfortable for daily and office wear.',
    60, 1
),
(
    'Georgette Partywear Saree',
    1899.00,
    'assets/imgwomen/s3.jpeg',
    2, 'sarees',
    'Shimmering georgette partywear saree with embellished border. Perfect for celebrations.',
    30, 1
),
(
    'Banarasi Silk Saree',
    2099.00,
    'assets/imgwomen/s5.jpeg',
    2, 'sarees',
    'Authentic Banarasi silk with intricate brocade weave. A heritage piece for special occasions.',
    20, 1
),

-- --------------------------------------------------------
-- WOMEN - Lehenga
-- --------------------------------------------------------
(
    'Bridal Lehenga Set',
    4999.00,
    'assets/imgwomen/l1.jpeg',
    2, 'lehenga',
    'Heavily embroidered bridal lehenga with matching dupatta and blouse piece.',
    15, 1
),
(
    'Festive Mirror Work Lehenga',
    2999.00,
    'assets/imgwomen/l2.jpeg',
    2, 'lehenga',
    'Vibrant festive lehenga with mirror-work embroidery. Ideal for Navratri and Diwali.',
    25, 1
),

-- --------------------------------------------------------
-- WOMEN - Kurtis
-- --------------------------------------------------------
(
    'Floral Block Print Kurti',
    899.00,
    'assets/imgwomen/k1.jpeg',
    2, 'kurtis',
    'Floral block-printed A-line kurti in breathable cotton. Office and casual wear.',
    120, 1
),
(
    'Anarkali Kurti',
    1199.00,
    'assets/imgwomen/k2.jpeg',
    2, 'kurtis',
    'Flared Anarkali-style kurti with button-front neck. Elegant for festive occasions.',
    80, 1
),
(
    'Solid Casual Kurti',
    799.00,
    'assets/imgwomen/k3.jpeg',
    2, 'kurtis',
    'Straight-cut casual kurti in solid colour. Pairs with leggings or palazzos.',
    150, 1
),

-- --------------------------------------------------------
-- WOMEN - Jeans
-- --------------------------------------------------------
(
    'High Waist Slim Jeans',
    999.00,
    'assets/imgwomen/j1.jpeg',
    2, 'jeans',
    'High-waisted slim fit jeans with five-pocket design. Flatters all body types.',
    90, 1
),
(
    'Stretch Slim Fit Jeans',
    1199.00,
    'assets/imgwomen/j2.jpeg',
    2, 'jeans',
    'Classic slim fit stretch jeans. Comfortable all-day wear with tapered ankle.',
    85, 1
),

-- --------------------------------------------------------
-- WOMEN - Footwear
-- --------------------------------------------------------
(
    'Block Heel Sandals',
    1299.00,
    'assets/imgwomen/f6.jpeg',
    2, 'footwear',
    'Stable block-heel sandals with adjustable ankle strap. Office and evening wear.',
    35, 1
),
(
    'Kolhapuri Leather Sandals',
    999.00,
    'assets/imgwomen/f3.jpeg',
    2, 'footwear',
    'Traditional handcrafted Kolhapuri chappal in genuine leather. Ethnic and western styles.',
    50, 1
),

-- --------------------------------------------------------
-- KIDS
-- --------------------------------------------------------
(
    'Boys Denim Shorts',
    449.00,
    'assets/imgkids/k1.jpeg',
    3, 'bottomwear',
    'Durable denim shorts for boys aged 5-12. Elastic waistband. Machine washable.',
    200, 1
),
(
    'Girls Floral Frock',
    699.00,
    'assets/imgkids/k2.jpeg',
    3, 'upperwear',
    'Bright cotton frock with bow detail and floral print. Machine washable.',
    175, 1
),
(
    'Kids Cotton T-Shirt',
    349.00,
    'assets/imgkids/k3.jpeg',
    3, 'tshirt',
    'Soft cotton round-neck T-shirt for kids aged 4-10. Fun printed graphics.',
    220, 1
);


-- =============================================================================
--  VERIFY SEED DATA
-- =============================================================================

-- Row counts per table
SELECT 'users'          AS `Table`, COUNT(*) AS `Rows` FROM users          UNION ALL
SELECT 'categories',                COUNT(*)           FROM categories      UNION ALL
SELECT 'products',                  COUNT(*)           FROM products        UNION ALL
SELECT 'cart',                      COUNT(*)           FROM cart            UNION ALL
SELECT 'cart_items',                COUNT(*)           FROM cart_items      UNION ALL
SELECT 'wishlist_items',            COUNT(*)           FROM wishlist_items  UNION ALL
SELECT 'orders',                    COUNT(*)           FROM orders          UNION ALL
SELECT 'order_items',               COUNT(*)           FROM order_items;

-- Products per category (confirms FK join is working)
SELECT
    c.display_name      AS `Category`,
    p.subcategory       AS `Subcategory`,
    COUNT(p.id)         AS `Products`,
    MIN(p.price)        AS `Min Price`,
    MAX(p.price)        AS `Max Price`
FROM
    products   p
    JOIN categories c ON c.id = p.category_id
WHERE
    p.is_active = 1
GROUP BY
    c.id, c.display_name, p.subcategory
ORDER BY
    c.sort_order ASC, p.subcategory ASC;

-- =============================================================================
--  END
--  Tables : 8 | Categories : 3 | Users : 3 | Products : 30
-- =============================================================================
