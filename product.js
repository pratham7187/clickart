// product.js
// Loads product details from localStorage and sends wishlist/order requests to backend.

const API_BASE = "http://localhost:5000";

window.onload = function () {
  const productName = localStorage.getItem("productName");
  const productPrice = localStorage.getItem("productPrice");
  const productImage = localStorage.getItem("productImage");

  if (productName && productPrice && productImage) {
    document.getElementById("product-name").innerText = productName;
    document.getElementById("product-price").innerText = productPrice;
    document.getElementById("product-img").src = productImage;
  } else {
    alert("Product details are not available.");
  }

  function getUserEmail() {
    return localStorage.getItem("clickkart-user"); // login.js must store this
  }

  // BUY NOW -> backend
  document.getElementById("buy-now-btn").addEventListener("click", async function () {
    const pincode = document.getElementById("pincode-input")?.value;
    const address = document.getElementById("address-input")?.value;
    const quantity = parseInt(document.getElementById("quantity")?.value || "1");
    const email = getUserEmail();

    if (!email) {
      alert("Please login to place an order.");
      window.location.href = "login.html";
      return;
    }

    if (!pincode || !address) {
      alert("Please enter both pincode and address.");
      return;
    }

    const order = {
      name: productName,
      price: productPrice,
      image: productImage,
      pincode: pincode,
      address: address,
      quantity: quantity,
      email: email,
      orderTime: new Date().toLocaleString()
    };

    try {
      const res = await fetch(`${API_BASE}/api/buy`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(order)
      });
      const data = await res.json();
      if (res.ok) {
        alert(data.message || "Order placed successfully!");
        // redirect to orders page to view
        window.location.href = "orders.html";
      } else {
        alert(data.message || "Failed to place order.");
      }
    } catch (err) {
      console.error(err);
      alert("Error connecting to server.");
    }
  });

  // WISHLIST -> backend
  document.getElementById("wishlist-btn").addEventListener("click", async function () {
    const email = getUserEmail();
    if (!email) {
      alert("Please login to add items to wishlist.");
      window.location.href = "login.html";
      return;
    }

    const wishlistItem = {
      name: productName,
      price: productPrice,
      image: productImage,
      email: email
    };

    try {
      const res = await fetch(`${API_BASE}/api/wishlist`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(wishlistItem)
      });
      const data = await res.json();
      if (res.ok) {
        alert(data.message || "Added to wishlist!");
      } else {
        alert(data.message || "Could not add to wishlist.");
      }
    } catch (err) {
      console.error(err);
      alert("Error connecting to server.");
    }
  });
};
