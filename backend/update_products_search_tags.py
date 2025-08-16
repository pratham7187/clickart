from pymongo import MongoClient

client = MongoClient("mongodb://localhost:27017/")
db = client["clickkart"]
products = db["products"]

# Update men's products with better search tags
men_updates = [
    # T-shirts
    {"filter": {"category": "men", "subcategory": "tshirt"}, 
     "update": {"$set": {"tags": ["men", "mens", "tshirt", "t-shirt", "tee", "shirt", "casual", "top"]}}},
    
    # Formal shirts
    {"filter": {"category": "men", "subcategory": "formal"}, 
     "update": {"$set": {"tags": ["men", "mens", "formal", "shirt", "office", "business", "dress shirt"]}}},
    
    # Jeans
    {"filter": {"category": "men", "subcategory": "jeans"}, 
     "update": {"$set": {"tags": ["men", "mens", "jeans", "denim", "pants", "trousers", "bottom", "casual"]}}},
    
    # Joggers
    {"filter": {"category": "men", "subcategory": "joggers"}, 
     "update": {"$set": {"tags": ["men", "mens", "joggers", "trackpants", "pants", "sports", "casual", "bottom"]}}},
    
    # Footwear
    {"filter": {"category": "men", "subcategory": "footwear"}, 
     "update": {"$set": {"tags": ["men", "mens", "footwear", "shoes", "sneakers", "sandals", "casual"]}}}
]

# Update women's products with better search tags
women_updates = [
    # Sarees
    {"filter": {"category": "women", "subcategory": "sarees"}, 
     "update": {"$set": {"tags": ["women", "womens", "ladies", "saree", "sarees", "indian", "ethnic", "traditional"]}}},
    
    # Lehenga
    {"filter": {"category": "women", "subcategory": "lehenga"}, 
     "update": {"$set": {"tags": ["women", "womens", "ladies", "lehenga", "indian", "ethnic", "traditional", "dress"]}}},
    
    # Jeans
    {"filter": {"category": "women", "subcategory": "jeans"}, 
     "update": {"$set": {"tags": ["women", "womens", "ladies", "jeans", "denim", "pants", "trousers", "bottom", "casual"]}}},
    
    # Kurtis
    {"filter": {"category": "women", "subcategory": "kurtis"}, 
     "update": {"$set": {"tags": ["women", "womens", "ladies", "kurti", "kurtis", "kurta", "indian", "ethnic", "top"]}}},
    
    # Footwear
    {"filter": {"category": "women", "subcategory": "footwear"}, 
     "update": {"$set": {"tags": ["women", "womens", "ladies", "footwear", "shoes", "heels", "sandals", "flats"]}}}
]

# Update kids' products with better search tags
kids_updates = [
    # Upperwear
    {"filter": {"category": "kids", "subcategory": "upperwear"}, 
     "update": {"$set": {"tags": ["kids", "children", "child", "upperwear", "top", "shirt", "tshirt", "jacket"]}}},
    
    # Bottomwear
    {"filter": {"category": "kids", "subcategory": "bottomwear"}, 
     "update": {"$set": {"tags": ["kids", "children", "child", "bottomwear", "bottom", "pants", "shorts", "jeans"]}}},
    
    # Footwear
    {"filter": {"category": "kids", "subcategory": "footwear"}, 
     "update": {"$set": {"tags": ["kids", "children", "child", "footwear", "shoes", "sneakers", "sandals"]}}}
]

# Apply all updates
all_updates = men_updates + women_updates + kids_updates

print("Starting to update products with search tags...")

for update in all_updates:
    result = products.update_many(update["filter"], update["update"])
    print(f"Updated {result.modified_count} products matching {update['filter']}")

print("✅ All products updated with search-friendly tags!")

# Also let's add some example products with better naming for testing
sample_products = [
    {
        "name": "Men's Casual Pants",
        "price": "₹899",
        "image": "jeans/j1.jpeg",
        "category": "men",
        "subcategory": "jeans",
        "tags": ["men", "mens", "pants", "casual", "jeans", "denim", "trousers"]
    },
    {
        "name": "Women's Dress Pants",
        "price": "₹1299",
        "image": "imgwomen/j1.jpeg",
        "category": "women",
        "subcategory": "jeans",
        "tags": ["women", "womens", "ladies", "pants", "dress pants", "formal", "trousers"]
    },
    {
        "name": "Kids Sports Pants",
        "price": "₹599",
        "image": "imgkids/s1.jpeg",
        "category": "kids",
        "subcategory": "bottomwear",
        "tags": ["kids", "children", "pants", "sports", "trackpants", "joggers"]
    }
]

# Insert sample products
try:
    products.insert_many(sample_products)
    print("✅ Added sample products with better naming!")
except Exception as e:
    print(f"Note: Sample products may already exist - {e}")

print("\n🎉 Search enhancement complete! Your search should now work much better.")
print("Try searching for: 'mens pants', 'women saree', 'kids shoes', etc.")