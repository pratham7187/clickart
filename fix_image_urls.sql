-- ============================================================
-- ClickKart — Fix image_url for all products
-- Maps every product's subcategory to actual files in
-- FontEndClick/images/<subfolder>/<file>.jpeg
--
-- Naming convention used:
--   Men T-Shirts   → images/imgmentshirt/t1.jpeg … t6.jpeg
--   Men Formals    → images/imgformals/f1a.jpeg … f6a.jpeg (main view)
--   Men Jeans      → images/jeans/j1.jpeg … j6.jpeg
--   Men Joggers    → images/joggers/jo1.jpeg … jo6.jpeg
--   Men Footwear   → images/footwear/fo1.jpeg … fo6.jpeg
--   Women Sarees   → images/imgwomen/s1.jpeg … s6.jpeg
--   Women Lehenga  → images/imgwomen/l1.jpeg … l6.jpeg
--   Women Jeans    → images/imgwomen/j1.jpeg … j6.jpeg
--   Women Kurtis   → images/imgwomen/k1.jpeg … k6.jpeg
--   Women Footwear → images/imgwomen/f1.jpeg … f6.jpeg
--   Kids Upperwear → images/imgkids/u1.jpeg … u6.jpeg
--   Kids Footwear  → images/imgkids/f1.jpeg … f6.jpeg
--   Kids Bottomwear→ images/imgkids/s1.jpeg … s6.jpeg
-- ============================================================

USE clickkart_db;

-- ─── MEN (category_id = 1) ────────────────────────────────

-- T-Shirts (6 products — t1 to t6)
SET @row := 0;
UPDATE products
SET image_url = CONCAT('images/imgmentshirt/t', (@row := @row + 1), '.jpeg')
WHERE category_id = 1 AND subcategory = 'tshirt'
ORDER BY id;

-- Formal Shirts (6 products — f1a to f6a)
SET @row := 0;
UPDATE products
SET image_url = CONCAT('images/imgformals/f', (@row := @row + 1), 'a.jpeg')
WHERE category_id = 1 AND subcategory = 'formal'
ORDER BY id;

-- Jeans (6 products — j1 to j6)
SET @row := 0;
UPDATE products
SET image_url = CONCAT('images/jeans/j', (@row := @row + 1), '.jpeg')
WHERE category_id = 1 AND subcategory = 'jeans'
ORDER BY id;

-- Joggers (6 products — jo1 to jo6)
SET @row := 0;
UPDATE products
SET image_url = CONCAT('images/joggers/jo', (@row := @row + 1), '.jpeg')
WHERE category_id = 1 AND subcategory = 'joggers'
ORDER BY id;

-- Footwear (6 products — fo1 to fo6)
SET @row := 0;
UPDATE products
SET image_url = CONCAT('images/footwear/fo', (@row := @row + 1), '.jpeg')
WHERE category_id = 1 AND subcategory = 'footwear'
ORDER BY id;

-- ─── WOMEN (category_id = 2) ──────────────────────────────

-- Sarees (6 products — s1 to s6)
SET @row := 0;
UPDATE products
SET image_url = CONCAT('images/imgwomen/s', (@row := @row + 1), '.jpeg')
WHERE category_id = 2 AND subcategory = 'sarees'
ORDER BY id;

-- Lehenga (6 products — l1 to l6)
SET @row := 0;
UPDATE products
SET image_url = CONCAT('images/imgwomen/l', (@row := @row + 1), '.jpeg')
WHERE category_id = 2 AND subcategory = 'lehenga'
ORDER BY id;

-- Jeans (6 products — j1 to j6)
SET @row := 0;
UPDATE products
SET image_url = CONCAT('images/imgwomen/j', (@row := @row + 1), '.jpeg')
WHERE category_id = 2 AND subcategory = 'jeans'
ORDER BY id;

-- Kurtis (6 products — k1 to k6)
SET @row := 0;
UPDATE products
SET image_url = CONCAT('images/imgwomen/k', (@row := @row + 1), '.jpeg')
WHERE category_id = 2 AND subcategory = 'kurtis'
ORDER BY id;

-- Footwear (6 products — f1 to f6)
SET @row := 0;
UPDATE products
SET image_url = CONCAT('images/imgwomen/f', (@row := @row + 1), '.jpeg')
WHERE category_id = 2 AND subcategory = 'footwear'
ORDER BY id;

-- ─── KIDS (category_id = 3) ───────────────────────────────

-- Upperwear (7 products — u1 to u7)
SET @row := 0;
UPDATE products
SET image_url = CONCAT('images/imgkids/u', (@row := @row + 1), '.jpeg')
WHERE category_id = 3 AND subcategory = 'upperwear'
ORDER BY id;

-- Footwear (7 products — f1 to f7)
SET @row := 0;
UPDATE products
SET image_url = CONCAT('images/imgkids/f', (@row := @row + 1), '.jpeg')
WHERE category_id = 3 AND subcategory = 'footwear'
ORDER BY id;

-- Bottomwear (6 products — s1 to s6)
SET @row := 0;
UPDATE products
SET image_url = CONCAT('images/imgkids/s', (@row := @row + 1), '.jpeg')
WHERE category_id = 3 AND subcategory = 'bottomwear'
ORDER BY id;

-- ─── Verify ───────────────────────────────────────────────
SELECT id, name, subcategory, image_url
FROM products
ORDER BY category_id, subcategory, id;
