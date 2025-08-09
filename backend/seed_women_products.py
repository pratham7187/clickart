from pymongo import MongoClient

client = MongoClient("mongodb://localhost:27017/")
db = client["clickkart"]
products = db["products"]

# Optional: clear old women's products first
products.delete_many({"category": "women"})

women_products = [
    # Sarees
    {"name": "Silk Saree", "price": "₹1499", "image": "imgwomen/s1.jpeg", "subcategory": "sarees", "category": "women"},
    {"name": "Cotton Saree", "price": "₹1199", "image": "imgwomen/s2.jpeg", "subcategory": "sarees", "category": "women"},
    {"name": "Partywear Saree", "price": "₹1899", "image": "imgwomen/s3.jpeg", "subcategory": "sarees", "category": "women"},
    {"name": "Printed Saree", "price": "₹999", "image": "imgwomen/s4.jpeg", "subcategory": "sarees", "category": "women"},
    {"name": "Banarasi Saree", "price": "₹2099", "image": "imgwomen/s5.jpeg", "subcategory": "sarees", "category": "women"},
    {"name": "Designer Saree", "price": "₹2499", "image": "imgwomen/s6.jpeg", "subcategory": "sarees", "category": "women"},

    # Lehenga
    {"name": "Bridal Lehenga", "price": "₹4999", "image": "imgwomen/l1.jpeg", "subcategory": "lehenga", "category": "women"},
    {"name": "Festive Lehenga", "price": "₹2999", "image": "imgwomen/l2.jpeg", "subcategory": "lehenga", "category": "women"},
    {"name": "Anarkali Lehenga", "price": "₹3499", "image": "imgwomen/l3.jpeg", "subcategory": "lehenga", "category": "women"},
    {"name": "Net Lehenga", "price": "₹2899", "image": "imgwomen/l4.jpeg", "subcategory": "lehenga", "category": "women"},
    {"name": "Floral Lehenga", "price": "₹3199", "image": "imgwomen/l5.jpeg", "subcategory": "lehenga", "category": "women"},
    {"name": "Designer Lehenga", "price": "₹3799", "image": "imgwomen/l6.jpeg", "subcategory": "lehenga", "category": "women"},

    # Jeans
    {"name": "High Waist Jeans", "price": "₹999", "image": "imgwomen/j1.jpeg", "subcategory": "jeans", "category": "women"},
    {"name": "Slim Fit Jeans", "price": "₹1199", "image": "imgwomen/j2.jpeg", "subcategory": "jeans", "category": "women"},
    {"name": "Ripped Jeans", "price": "₹1199", "image": "imgwomen/j3.jpeg", "subcategory": "jeans", "category": "women"},
    {"name": "Bootcut Jeans", "price": "₹1249", "image": "imgwomen/j4.jpeg", "subcategory": "jeans", "category": "women"},
    {"name": "Casual Fit", "price": "₹999", "image": "imgwomen/j5.jpeg", "subcategory": "jeans", "category": "women"},
    {"name": "Straight Jeans", "price": "₹1299", "image": "imgwomen/j6.jpeg", "subcategory": "jeans", "category": "women"},

    # Kurtis
    {"name": "Printed Kurti", "price": "₹899", "image": "imgwomen/k1.jpeg", "subcategory": "kurtis", "category": "women"},
    {"name": "Anarkali Kurti", "price": "₹1199", "image": "imgwomen/k2.jpeg", "subcategory": "kurtis", "category": "women"},
    {"name": "Casual Kurti", "price": "₹799", "image": "imgwomen/k3.jpeg", "subcategory": "kurtis", "category": "women"},
    {"name": "Designer Kurti", "price": "₹1499", "image": "imgwomen/k4.jpeg", "subcategory": "kurtis", "category": "women"},
    {"name": "Floral Kurti", "price": "₹999", "image": "imgwomen/k5.jpeg", "subcategory": "kurtis", "category": "women"},
    {"name": "Festive Kurti", "price": "₹1349", "image": "imgwomen/k6.jpeg", "subcategory": "kurtis", "category": "women"},

    # Footwear
    {"name": "Heels Combo", "price": "₹1499", "image": "imgwomen/f1.jpeg", "subcategory": "footwear", "category": "women"},
    {"name": "Flats Fashion", "price": "₹799", "image": "imgwomen/f2.jpeg", "subcategory": "footwear", "category": "women"},
    {"name": "Kolhapuri Sandals", "price": "₹999", "image": "imgwomen/f3.jpeg", "subcategory": "footwear", "category": "women"},
    {"name": "Slip-on Sneakers", "price": "₹1099", "image": "imgwomen/f4.jpeg", "subcategory": "footwear", "category": "women"},
    {"name": "Ballet Flats", "price": "₹899", "image": "imgwomen/f5.jpeg", "subcategory": "footwear", "category": "women"},
    {"name": "Block Heels", "price": "₹1299", "image": "imgwomen/f6.jpeg", "subcategory": "footwear", "category": "women"},
]

products.insert_many(women_products)
print("✅ Inserted all women's products into MongoDB!")
