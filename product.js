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

  // BUY NOW FUNCTIONALITY
  document.getElementById("buy-now-btn").addEventListener("click", function () {
    const pincode = document.getElementById("pincode-input")?.value;
    const address = document.getElementById("address-input")?.value;

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
      orderTime: new Date().toLocaleString()
    };

    let orders = JSON.parse(localStorage.getItem("orders")) || [];
    orders.push(order);
    localStorage.setItem("orders", JSON.stringify(orders));

    // Show confirmation
    const confirmation = document.createElement("div");
    confirmation.innerHTML = `
      <div style="padding: 15px; text-align: center; color: green;">
        <i class="fa-solid fa-circle-check" style="font-size: 40px;"></i>
        <h3>Order Placed Successfully!</h3>
      </div>
    `;
    document.body.appendChild(confirmation);

    // Optionally redirect to orders page after a delay
    setTimeout(() => {
      window.location.href = "orders.html";
    }, 2000);
  });

  // WISHLIST FUNCTIONALITY
  document.getElementById("wishlist-btn").addEventListener("click", function () {
    const wishlistItem = {
      name: productName,
      price: productPrice,
      image: productImage
    };

    let wishlist = JSON.parse(localStorage.getItem("wishlist")) || [];

    const exists = wishlist.some(item => item.name === wishlistItem.name && item.price === wishlistItem.price);

    if (!exists) {
      wishlist.push(wishlistItem);
      localStorage.setItem("wishlist", JSON.stringify(wishlist));
      alert("Added to wishlist!");
    } else {
      alert("Item is already in your wishlist!");
    }
  });
};
