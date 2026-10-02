-- ═══════════════════════════════════════════════════════════════════
-- Add ALL missing products so every image in the folders is a product
-- ═══════════════════════════════════════════════════════════════════

-- ────────────────────────────────────────────────────────────────────
-- MEN — T-Shirts (have t1-t4, need t5,t6)
-- ────────────────────────────────────────────────────────────────────
INSERT INTO products (name, price, image_url, category_id, subcategory, description, stock, is_active) VALUES
('Striped Polo T-Shirt',         549.00, 'images/MenImage/imgmentshirt/t5.jpeg', 1, 'tshirt',  'Stylish striped polo for a smart casual look.', 100, 1),
('Henley Neck T-Shirt',          629.00, 'images/MenImage/imgmentshirt/t6.jpeg', 1, 'tshirt',  'Button-placket henley with a relaxed fit.', 90, 1);

-- ────────────────────────────────────────────────────────────────────
-- MEN — Formals (have f1a-f4b, need f5a,f6a)
-- ────────────────────────────────────────────────────────────────────
INSERT INTO products (name, price, image_url, category_id, subcategory, description, stock, is_active) VALUES
('Navy Blue Formal Shirt',       1099.00, 'images/MenImage/imgformals/f5a.jpeg', 1, 'formal',  'Premium navy formal shirt with a slim fit.', 55, 1),
('Charcoal Grey Formal Shirt',   1149.00, 'images/MenImage/imgformals/f6a.jpeg', 1, 'formal',  'Elegant charcoal grey shirt for office wear.', 50, 1);

-- ────────────────────────────────────────────────────────────────────
-- MEN — Jeans (have j1-j2, need j3-j6)
-- ────────────────────────────────────────────────────────────────────
INSERT INTO products (name, price, image_url, category_id, subcategory, description, stock, is_active) VALUES
('Tapered Fit Grey Jeans',       1349.00, 'images/MenImage/imgmenjeans/j3.jpeg', 1, 'jeans',   'Modern tapered fit in a cool grey wash.', 70, 1),
('Ripped Skinny Jeans',          1499.00, 'images/MenImage/imgmenjeans/j4.jpeg', 1, 'jeans',   'Trendy ripped skinny jeans for a street look.', 60, 1),
('Straight Fit Black Jeans',     1449.00, 'images/MenImage/imgmenjeans/j5.jpeg', 1, 'jeans',   'Classic straight fit in jet black denim.', 75, 1),
('Relaxed Fit Cargo Jeans',      1599.00, 'images/MenImage/imgmenjeans/j6.jpeg', 1, 'jeans',   'Relaxed cargo jeans with utility pockets.', 50, 1);

-- ────────────────────────────────────────────────────────────────────
-- MEN — Joggers (have jo1-jo2, need jo3-jo6)
-- ────────────────────────────────────────────────────────────────────
INSERT INTO products (name, price, image_url, category_id, subcategory, description, stock, is_active) VALUES
('Black Slim Joggers',           849.00, 'images/MenImage/imgmenjoggers/jo3.jpeg', 1, 'joggers', 'Slim-fit black joggers with ankle cuffs.', 100, 1),
('Olive Cargo Joggers',          999.00, 'images/MenImage/imgmenjoggers/jo4.jpeg', 1, 'joggers', 'Utility cargo joggers in olive green.', 80, 1),
('Navy Trackpants',              799.00, 'images/MenImage/imgmenjoggers/jo5.jpeg', 1, 'joggers', 'Comfortable navy trackpants for workouts.', 120, 1),
('Striped Side Panel Joggers',   949.00, 'images/MenImage/imgmenjoggers/jo6.jpeg', 1, 'joggers', 'Athletic joggers with contrast side stripes.', 85, 1);

-- ────────────────────────────────────────────────────────────────────
-- MEN — Footwear (have fo1-fo2, need fo3-fo6)
-- ────────────────────────────────────────────────────────────────────
INSERT INTO products (name, price, image_url, category_id, subcategory, description, stock, is_active) VALUES
('White Running Shoes',          1599.00, 'images/MenImage/footwear/fo3.jpeg', 1, 'footwear', 'Lightweight white running shoes for daily wear.', 50, 1),
('Brown Leather Loafers',        1899.00, 'images/MenImage/footwear/fo4.jpeg', 1, 'footwear', 'Premium brown leather loafers.', 40, 1),
('Black Sports Sneakers',        1699.00, 'images/MenImage/footwear/fo5.jpeg', 1, 'footwear', 'High-performance black sports sneakers.', 55, 1),
('Slip-On Casual Shoes',         1299.00, 'images/MenImage/footwear/fo6.jpeg', 1, 'footwear', 'Easy slip-on shoes for everyday comfort.', 65, 1);

-- ────────────────────────────────────────────────────────────────────
-- WOMEN — Sarees (have s1-s4, need s5,s6)
-- ────────────────────────────────────────────────────────────────────
INSERT INTO products (name, price, image_url, category_id, subcategory, description, stock, is_active) VALUES
('Kanjivaram Silk Saree',        2499.00, 'images/imgwomen/imgwomensaree/s5.jpeg', 2, 'sarees',  'Traditional Kanjivaram silk with gold zari.', 25, 1),
('Chiffon Printed Saree',        1099.00, 'images/imgwomen/imgwomensaree/s6.jpeg', 2, 'sarees',  'Lightweight chiffon saree with floral prints.', 50, 1);

-- ────────────────────────────────────────────────────────────────────
-- WOMEN — Lehenga (have l1-l2, need l3-l6)
-- ────────────────────────────────────────────────────────────────────
INSERT INTO products (name, price, image_url, category_id, subcategory, description, stock, is_active) VALUES
('Embroidered Lehenga Choli',    3499.00, 'images/imgwomen/imgwomenlehanga/l3.jpeg', 2, 'lehenga', 'Beautifully embroidered lehenga with choli set.', 20, 1),
('Designer Sequin Lehenga',      4499.00, 'images/imgwomen/imgwomenlehanga/l4.jpeg', 2, 'lehenga', 'Glamorous sequin lehenga for special occasions.', 15, 1),
('Pastel Net Lehenga',           3999.00, 'images/imgwomen/imgwomenlehanga/l5.jpeg', 2, 'lehenga', 'Soft pastel net lehenga with dupatta.', 18, 1),
('Velvet Bridal Lehenga',        5999.00, 'images/imgwomen/imgwomenlehanga/l6.jpeg', 2, 'lehenga', 'Royal velvet bridal lehenga with heavy work.', 10, 1);

-- ────────────────────────────────────────────────────────────────────
-- WOMEN — Kurtis (have k1-k3, need k4-k6)
-- ────────────────────────────────────────────────────────────────────
INSERT INTO products (name, price, image_url, category_id, subcategory, description, stock, is_active) VALUES
('Embroidered Cotton Kurti',     749.00, 'images/imgwomen/imgwomenlehanga/k4.jpeg', 2, 'kurtis',  'Comfortable cotton kurti with thread embroidery.', 130, 1),
('Printed A-Line Kurti',         849.00, 'images/imgwomen/imgwomenlehanga/k5.jpeg', 2, 'kurtis',  'Flowy A-line kurti with vibrant prints.', 110, 1),
('Straight Fit Rayon Kurti',     699.00, 'images/imgwomen/imgwomenlehanga/k6.jpeg', 2, 'kurtis',  'Elegant straight-fit rayon kurti for daily wear.', 140, 1);

-- ────────────────────────────────────────────────────────────────────
-- WOMEN — Jeans (have j1-j2, need j3-j6)
-- ────────────────────────────────────────────────────────────────────
INSERT INTO products (name, price, image_url, category_id, subcategory, description, stock, is_active) VALUES
('Bootcut Flared Jeans',         1099.00, 'images/imgwomen/imgwomenjean/j3.jpeg', 2, 'jeans',    'Stylish bootcut flared jeans for a retro vibe.', 70, 1),
('Ripped Boyfriend Jeans',       1299.00, 'images/imgwomen/imgwomenjean/j4.jpeg', 2, 'jeans',    'Relaxed boyfriend jeans with distressed details.', 65, 1),
('Skinny Fit Ankle Jeans',       1149.00, 'images/imgwomen/imgwomenjean/j5.jpeg', 2, 'jeans',    'Figure-hugging skinny jeans with ankle length.', 80, 1),
('Wide Leg Palazzo Jeans',       1199.00, 'images/imgwomen/imgwomenjean/j6.jpeg', 2, 'jeans',    'Trendy wide-leg palazzo denim jeans.', 60, 1);

-- ────────────────────────────────────────────────────────────────────
-- WOMEN — Footwear (have f3,f6 — need f1,f2,f4,f5)
-- ────────────────────────────────────────────────────────────────────
INSERT INTO products (name, price, image_url, category_id, subcategory, description, stock, is_active) VALUES
('Strappy Wedge Heels',          1399.00, 'images/imgwomen/imgwomenfoot/f1.jpeg', 2, 'footwear', 'Elegant strappy wedge heels for parties.', 40, 1),
('Flat Ethnic Juttis',           799.00,  'images/imgwomen/imgwomenfoot/f2.jpeg', 2, 'footwear', 'Handcrafted ethnic juttis with mirror work.', 60, 1),
('Pointed Toe Stilettos',        1799.00, 'images/imgwomen/imgwomenfoot/f4.jpeg', 2, 'footwear', 'Classy pointed toe stilettos for formal events.', 30, 1),
('Casual Slide Sandals',         599.00,  'images/imgwomen/imgwomenfoot/f5.jpeg', 2, 'footwear', 'Comfortable casual slide sandals.', 80, 1);

-- ────────────────────────────────────────────────────────────────────
-- KIDS — Upperwear (have u1,u2 — need u3-u7)
-- ────────────────────────────────────────────────────────────────────
INSERT INTO products (name, price, image_url, category_id, subcategory, description, stock, is_active) VALUES
('Kids Printed Hoodie',          599.00, 'images/imgkids/imgkidsupper/u3.jpeg', 3, 'upperwear', 'Warm printed hoodie for kids.', 150, 1),
('Boys Polo T-Shirt',            399.00, 'images/imgkids/imgkidsupper/u4.jpeg', 3, 'upperwear', 'Smart polo t-shirt for boys.', 180, 1),
('Girls Ruffle Top',             449.00, 'images/imgkids/imgkidsupper/u5.jpeg', 3, 'upperwear', 'Cute ruffle top for girls.', 160, 1),
('Kids Denim Jacket',            799.00, 'images/imgkids/imgkidsupper/u6.jpeg', 3, 'upperwear', 'Trendy denim jacket for kids.', 100, 1),
('Striped Full Sleeve Tee',      349.00, 'images/imgkids/imgkidsupper/u7.jpeg', 3, 'upperwear', 'Colourful striped full-sleeve tee.', 200, 1);

-- ────────────────────────────────────────────────────────────────────
-- KIDS — Bottomwear (have s1, need s2-s6)
-- ────────────────────────────────────────────────────────────────────
INSERT INTO products (name, price, image_url, category_id, subcategory, description, stock, is_active) VALUES
('Girls Cotton Leggings',        299.00, 'images/imgkids/imgkidsbottom/s2.jpeg', 3, 'bottomwear', 'Soft cotton leggings in bright colours.', 250, 1),
('Boys Cargo Shorts',            449.00, 'images/imgkids/imgkidsbottom/s3.jpeg', 3, 'bottomwear', 'Durable cargo shorts with pockets.', 180, 1),
('Kids Track Pants',             399.00, 'images/imgkids/imgkidsbottom/s4.jpeg', 3, 'bottomwear', 'Comfortable track pants for active kids.', 200, 1),
('Girls Pleated Skirt',          499.00, 'images/imgkids/imgkidsbottom/s5.jpeg', 3, 'bottomwear', 'Adorable pleated skirt for girls.', 140, 1),
('Boys Chino Pants',             549.00, 'images/imgkids/imgkidsbottom/s6.jpeg', 3, 'bottomwear', 'Smart chino pants for boys.', 120, 1);

-- ────────────────────────────────────────────────────────────────────
-- KIDS — Footwear (have none, need f1-f7)
-- ────────────────────────────────────────────────────────────────────
INSERT INTO products (name, price, image_url, category_id, subcategory, description, stock, is_active) VALUES
('Kids Velcro Sneakers',         599.00, 'images/imgkids/imgkidsfoot/f1.jpeg', 3, 'footwear', 'Easy-wear velcro sneakers for kids.', 150, 1),
('Toddler Sandals',              349.00, 'images/imgkids/imgkidsfoot/f2.jpeg', 3, 'footwear', 'Soft sole sandals for toddlers.', 200, 1),
('Boys Sports Shoes',            699.00, 'images/imgkids/imgkidsfoot/f3.jpeg', 3, 'footwear', 'Lightweight sports shoes for active boys.', 130, 1),
('Girls Glitter Shoes',          549.00, 'images/imgkids/imgkidsfoot/f4.jpeg', 3, 'footwear', 'Sparkly glitter shoes for girls.', 120, 1),
('Kids Slip-On Canvas',          449.00, 'images/imgkids/imgkidsfoot/f5.jpeg', 3, 'footwear', 'Comfortable slip-on canvas shoes.', 160, 1),
('Kids Rain Boots',              399.00, 'images/imgkids/imgkidsfoot/f6.jpeg', 3, 'footwear', 'Colourful rain boots for monsoon fun.', 100, 1),
('Cartoon Print Crocs',          499.00, 'images/imgkids/imgkidsfoot/f7.jpeg', 3, 'footwear', 'Fun cartoon print crocs for everyday wear.', 180, 1);
