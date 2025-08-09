from pymongo import MongoClient

client = MongoClient("mongodb://localhost:27017/")
db = client["clickkart"]
products = db["products"]

kids_products = [
    # 🧥 Upperwear
    {"name": "Cartoon Tee", "price": "₹299", "image": "imgkids/u1.jpeg", "category": "kids", "subcategory": "upperwear"},
    {"name": "Striped Shirt", "price": "₹349", "image": "imgkids/u2.jpeg", "category": "kids", "subcategory": "upperwear"},
    {"name": "Hooded Jacket", "price": "₹499", "image": "imgkids/u3.jpeg", "category": "kids", "subcategory": "upperwear"},
    {"name": "Printed Sweatshirt", "price": "₹399", "image": "imgkids/u4.jpeg", "category": "kids", "subcategory": "upperwear"},
    {"name": "Denim Shirt", "price": "₹449", "image": "imgkids/u5.jpeg", "category": "kids", "subcategory": "upperwear"},
    {"name": "Basic Tee Pack", "price": "₹599", "image": "imgkids/u6.jpeg", "category": "kids", "subcategory": "upperwear"},
    {"name": "Basic Frok", "price": "₹599", "image": "imgkids/u7.jpeg", "category": "kids", "subcategory": "upperwear"},

    # 👖 Bottom Wear
    {"name": "Denim Shorts", "price": "₹499", "image": "imgkids/s1.jpeg", "category": "kids", "subcategory": "bottomwear"},
    {"name": "Cargo Pants", "price": "₹399", "image": "imgkids/s2.jpeg", "category": "kids", "subcategory": "bottomwear"},
    {"name": "Joggers", "price": "₹449", "image": "imgkids/s3.jpeg", "category": "kids", "subcategory": "bottomwear"},
    {"name": "Printed Leggings", "price": "₹349", "image": "imgkids/s4.jpeg", "category": "kids", "subcategory": "bottomwear"},
    {"name": "Knee-Length Shorts", "price": "₹299", "image": "imgkids/s5.jpeg", "category": "kids", "subcategory": "bottomwear"},
    {"name": "Basic Pants", "price": "₹429", "image": "imgkids/s6.jpeg", "category": "kids", "subcategory": "bottomwear"},

    # 👟 Footwear
    {"name": "Light-Up Sneakers", "price": "₹799", "image": "imgkids/f1.jpeg", "category": "kids", "subcategory": "footwear"},
    {"name": "Kids Crocs", "price": "₹599", "image": "imgkids/f2.jpeg", "category": "kids", "subcategory": "footwear"},
    {"name": "Basic Sandals", "price": "₹649", "image": "imgkids/f3.jpeg", "category": "kids", "subcategory": "footwear"},
    {"name": "Sports Shoes", "price": "₹899", "image": "imgkids/f4.jpeg", "category": "kids", "subcategory": "footwear"},
    {"name": "Rain Boots", "price": "₹699", "image": "imgkids/f5.jpeg", "category": "kids", "subcategory": "footwear"},
    {"name": "Velcro Sandals", "price": "₹549", "image": "imgkids/f6.jpeg", "category": "kids", "subcategory": "footwear"},
    {"name": "Casual Sandals", "price": "₹549", "image": "imgkids/f7.jpeg", "category": "kids", "subcategory": "footwear"},
]

products.insert_many(kids_products)
print("✅ Inserted all kids' products into MongoDB!")
