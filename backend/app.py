from flask import Flask, request, jsonify
from flask_cors import CORS
from pymongo import MongoClient
from bson import ObjectId
import datetime

app = Flask(__name__)
CORS(app)

# MongoDB Connection
client = MongoClient("mongodb://localhost:27017/")
db = client["clickkart"]
users = db["users"]
products = db["products"]
wishlist = db["wishlist"]
orders = db["orders"]

# -------------------- USER AUTH --------------------
@app.route("/api/auth/register", methods=["POST"])
def register():
    data = request.get_json()
    if users.find_one({"email": data["email"]}):
        return jsonify({"error": "Email already registered"}), 400
    users.insert_one({
        "name": data["name"],
        "email": data["email"],
        "password": data["password"]
    })
    return jsonify({"message": "Registration successful"})

@app.route("/api/auth/login", methods=["POST"])
def login():
    data = request.get_json()
    user = users.find_one({"email": data["email"], "password": data["password"]})
    if not user:
        return jsonify({"error": "Invalid credentials"}), 401
    return jsonify({"message": "Login successful", "email": user["email"]})

# Fixed login/register endpoints to match frontend calls
@app.route("/api/login", methods=["POST"])
def login_alt():
    data = request.get_json()
    user = users.find_one({"email": data["email"], "password": data["password"]})
    if not user:
        return jsonify({"message": "Invalid credentials"}), 401
    return jsonify({"message": "Login successful", "email": user["email"]})

@app.route("/api/register", methods=["POST"])
def register_alt():
    data = request.get_json()
    if users.find_one({"email": data["email"]}):
        return jsonify({"message": "Email already registered"}), 400
    users.insert_one({
        "name": data["name"],
        "email": data["email"],
        "password": data["password"]
    })
    return jsonify({"message": "Registration successful"})

# -------------------- USER PROFILE --------------------
@app.route("/api/user/<email>", methods=["GET"])
def get_user(email):
    user = users.find_one({"email": email})
    if not user:
        return jsonify({"error": "User not found"}), 404
    return jsonify({"name": user.get("name", "ClickKart User"), "email": user["email"]})

# -------------------- PRODUCTS --------------------
@app.route("/api/products", methods=["GET"])
def get_products():
    category = request.args.get("category")
    filter_query = {"category": category} if category else {}
    products_list = list(products.find(filter_query))
    for p in products_list:
        p["_id"] = str(p["_id"])
    return jsonify(products_list)

# Category-specific product routes
@app.route("/api/products/<category>", methods=["GET"])
def get_products_by_category(category):
    products_list = list(products.find({"category": category}))
    for p in products_list:
        p["_id"] = str(p["_id"])
    return jsonify(products_list)

# -------------------- WISHLIST --------------------
@app.route("/api/wishlist", methods=["GET"])
def get_wishlist():
    email = request.args.get("email")
    items = list(wishlist.find({"email": email}))
    for item in items:
        item["_id"] = str(item["_id"])
    return jsonify(items)

@app.route("/api/wishlist/<email>", methods=["GET"])
def get_wishlist_by_email(email):
    items = list(wishlist.find({"email": email}))
    for item in items:
        item["_id"] = str(item["_id"])
    return jsonify(items)

@app.route("/api/wishlist", methods=["POST"])
def add_to_wishlist():
    data = request.get_json()
    # Check if item already exists in wishlist
    existing = wishlist.find_one({"email": data["email"], "name": data["name"]})
    if existing:
        return jsonify({"message": "Item already in wishlist"})
    wishlist.insert_one(data)
    return jsonify({"message": "Item added to wishlist"})

@app.route("/api/wishlist/<item_id>", methods=["DELETE"])
def remove_from_wishlist(item_id):
    wishlist.delete_one({"_id": ObjectId(item_id)})
    return jsonify({"message": "Item removed from wishlist"})

@app.route("/api/wishlist/item/<item_id>", methods=["DELETE"])
def remove_wishlist_item(item_id):
    result = wishlist.delete_one({"_id": ObjectId(item_id)})
    if result.deleted_count > 0:
        return jsonify({"message": "Item removed from wishlist"})
    return jsonify({"message": "Item not found"}), 404

# -------------------- ORDERS --------------------
@app.route("/api/orders", methods=["GET"])
def get_orders():
    email = request.args.get("email")
    user_orders = list(orders.find({"email": email}))
    for order in user_orders:
        order["_id"] = str(order["_id"])
    return jsonify(user_orders)

@app.route("/api/orders/<email>", methods=["GET"])
def get_orders_by_email(email):
    user_orders = list(orders.find({"email": email}))
    for order in user_orders:
        order["_id"] = str(order["_id"])
    return jsonify(user_orders)

@app.route("/api/orders", methods=["POST"])
def place_order():
    data = request.get_json()
    data["ordered_on"] = datetime.datetime.now().strftime("%d/%m/%Y, %I:%M:%S %p")
    orders.insert_one(data)
    return jsonify({"message": "Order placed successfully"})

# Buy endpoint for product.js
@app.route("/api/buy", methods=["POST"])
def buy_product():
    data = request.get_json()
    data["ordered_on"] = datetime.datetime.now().strftime("%d/%m/%Y, %I:%M:%S %p")
    orders.insert_one(data)
    return jsonify({"message": "Order placed successfully"})

# -------------------- SEARCH --------------------
@app.route("/api/search", methods=["GET"])
def search_products():
    query = request.args.get("query", "").strip()
    if not query:
        return jsonify([])

    products_list = list(products.find({
        "$or": [
            {"name": {"$regex": query, "$options": "i"}},       # Search in product name
            {"category": {"$regex": query, "$options": "i"}},   # Search in category
            {"subcategory": {"$regex": query, "$options": "i"}} # Search in subcategory
        ]
    }))

    for p in products_list:
        p["_id"] = str(p["_id"])
    return jsonify(products_list)


# -------------------- RUN APP --------------------
if __name__ == "__main__":
    app.run(debug=True)