from pymongo import MongoClient

client = MongoClient("mongodb://localhost:27017/")
db = client["clickkart"]
products = db["products"]

sample_products = [
    {
        "title": "Men's Casual T-Shirt",
        "price": 499,
        "category": "T-Shirts",
        "image": "img/tshirt1.jpg",
        "rating": 4.2,
        "quantity": 50
    },
    {
        "title": "Women's Kurti",
        "price": 699,
        "category": "Kurtis",
        "image": "img/kurti1.jpg",
        "rating": 4.5,
        "quantity": 30
    },
    {
        "title": "Men's Formal Shirt",
        "price": 899,
        "category": "Formal Shirts",
        "image": "img/formal1.jpg",
        "rating": 4.1,
        "quantity": 40
    }
]

products.insert_many(sample_products)
print("Sample products inserted.")
