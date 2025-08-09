from pymongo import MongoClient

client = MongoClient("mongodb://localhost:27017/")
db = client["clickkart"]
products = db["products"]

# Optional: clear old men's products first
products.delete_many({"category": "men"})

# 🧢 All men's products
men_products = [
    # T-Shirts
    {"name": "T-Shirt Classic", "price": "₹499", "image": "imgmentshirt/t1.jpeg", "subcategory": "tshirt", "category": "men"},
    {"name": "Printed Tee", "price": "₹599", "image": "imgmentshirt/t2.jpeg", "subcategory": "tshirt", "category": "men"},
    {"name": "U-Neck Tee", "price": "₹549", "image": "imgmentshirt/t3.jpeg", "subcategory": "tshirt", "category": "men"},
    {"name": "Casual T-Shirt", "price": "₹479", "image": "imgmentshirt/t4.jpeg", "subcategory": "tshirt", "category": "men"},
    {"name": "Striped Tee", "price": "₹629", "image": "imgmentshirt/t5.jpeg", "subcategory": "tshirt", "category": "men"},
    {"name": "Urban Tee", "price": "₹529", "image": "imgmentshirt/t6.jpeg", "subcategory": "tshirt", "category": "men"},

    # Formal Shirts
    {"name": "Green Formal", "price": "₹999", "image": "imgformals/f1a.jpeg", "subcategory": "formal", "category": "men"},
    {"name": "Sky Blue Shirt", "price": "₹899", "image": "imgformals/f2a.jpeg", "subcategory": "formal", "category": "men"},
    {"name": "Wine Formal", "price": "₹949", "image": "imgformals/f3a.jpeg", "subcategory": "formal", "category": "men"},
    {"name": "White Formal", "price": "₹1,049", "image": "imgformals/f4b.jpeg", "subcategory": "formal", "category": "men"},
    {"name": "Checkered Shirt", "price": "₹1,099", "image": "imgformals/f5a.jpeg", "subcategory": "formal", "category": "men"},
    {"name": "Navy Blue Shirt", "price": "₹1,149", "image": "imgformals/f6a.jpeg", "subcategory": "formal", "category": "men"},

    # Jeans
    {"name": "Slim Fit Jeans", "price": "₹1,299", "image": "jeans/j1.jpeg", "subcategory": "jeans", "category": "men"},
    {"name": "Blue Denim", "price": "₹1,399", "image": "jeans/j2.jpeg", "subcategory": "jeans", "category": "men"},
    {"name": "Ripped Jeans", "price": "₹1,499", "image": "jeans/j3.jpeg", "subcategory": "jeans", "category": "men"},
    {"name": "Regular Fit Jeans", "price": "₹1,349", "image": "jeans/j4.jpeg", "subcategory": "jeans", "category": "men"},
    {"name": "Washed Denim", "price": "₹1,299", "image": "jeans/j5.jpeg", "subcategory": "jeans", "category": "men"},
    {"name": "Light Blue Jeans", "price": "₹1,399", "image": "jeans/j6.jpeg", "subcategory": "jeans", "category": "men"},

    # Joggers
    {"name": "Track Joggers", "price": "₹899", "image": "joggers/jo1.jpeg", "subcategory": "joggers", "category": "men"},
    {"name": "Grey Joggers", "price": "₹949", "image": "joggers/jo2.jpeg", "subcategory": "joggers", "category": "men"},
    {"name": "Sports Joggers", "price": "₹999", "image": "joggers/jo3.jpeg", "subcategory": "joggers", "category": "men"},
    {"name": "Wine Joggers", "price": "₹899", "image": "joggers/jo4.jpeg", "subcategory": "joggers", "category": "men"},
    {"name": "Relax Fit Joggers", "price": "₹1,049", "image": "joggers/jo5.jpeg", "subcategory": "joggers", "category": "men"},
    {"name": "Training Joggers", "price": "₹999", "image": "joggers/jo6.jpeg", "subcategory": "joggers", "category": "men"},

    # Footwear
    {"name": "Addidas Sneakers", "price": "₹1,499", "image": "footwear/fo1.jpeg", "subcategory": "footwear", "category": "men"},
    {"name": "Casual Sneakers", "price": "₹1,399", "image": "footwear/fo2.jpeg", "subcategory": "footwear", "category": "men"},
    {"name": "Nike Sneakers", "price": "₹1,699", "image": "footwear/fo3.jpeg", "subcategory": "footwear", "category": "men"},
    {"name": "Black Loafers", "price": "₹1,299", "image": "footwear/fo4.jpeg", "subcategory": "footwear", "category": "men"},
    {"name": "Brown Sandals Men", "price": "₹499", "image": "footwear/fo5.jpeg", "subcategory": "footwear", "category": "men"},
    {"name": "Arizona Men", "price": "₹1,199", "image": "footwear/fo6.jpeg", "subcategory": "footwear", "category": "men"},
]

products.insert_many(men_products)
print("✅ Inserted all men's products into MongoDB!")
