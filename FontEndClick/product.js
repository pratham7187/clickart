// product.js — ClickKart Product Detail Page
// Reads ck_productId from localStorage, fetches full product from Spring Boot,
// renders the detail page, and wires Add-to-Cart, Buy Now, and Wishlist buttons.
//
// Requires: api.js must be loaded first (provides apiGetProductById,
// apiAddToCart, apiPlaceOrder, apiAddToWishlist, requireAuth, formatPrice, etc.)

window.onload = async function () {
  // ── Auth guard ────────────────────────────────────────────────────────────
  if (!requireAuth()) return;

  // ── Read product ID stored by the listing pages ───────────────────────────
  // CHANGED: was reading productName / productPrice / productImage strings.
  // Now reads the numeric product ID and fetches full details from the API.
  const productId = localStorage.getItem("ck_productId");
  if (!productId) {
    alert("No product selected. Please browse products first.");
    history.back();
    return;
  }

  // ── Fetch product from Spring Boot ─────────────────────────────────────────
  let product;
  try {
    const res = await apiGetProductById(productId);
    product = res.data; // ProductResponse shape from backend
  } catch (err) {
    document.getElementById("product-name").innerText = "Could not load product.";
    console.error("Product fetch failed:", err);
    return;
  }

  // ── Render product details ─────────────────────────────────────────────────
  // ProductResponse fields: id, name, price, imageUrl, stock, description,
  // subcategory, categoryName, categoryDisplayName, active, createdAt
  document.getElementById("product-img").src          = product.imageUrl || "";
  document.getElementById("product-img").alt          = product.name;
  document.getElementById("product-name").innerText   = product.name;
  document.getElementById("product-price").innerText  = formatPrice(product.price);

  const stockEl = document.getElementById("product-stock");
  if (stockEl) {
    stockEl.innerText   = product.stock > 0 ? `✔ In Stock (${product.stock} available)` : "✘ Out of Stock";
    stockEl.style.color = product.stock > 0 ? "#4caf50" : "#f44336";
  }

  const descEl = document.getElementById("product-description");
  if (descEl) {
    descEl.innerText = product.description || "";
  }

  // Clamp quantity max to available stock
  const qtyInput = document.getElementById("quantity");
  if (qtyInput && product.stock > 0) {
    qtyInput.max = product.stock;
  }

  // Disable action buttons if out of stock
  if (product.stock === 0) {
    const addCartBtn = document.getElementById("add-cart-btn");
    const buyBtn     = document.getElementById("buy-now-btn");
    if (addCartBtn) { addCartBtn.disabled = true; addCartBtn.style.opacity = "0.5"; }
    if (buyBtn)     { buyBtn.disabled = true;     buyBtn.style.opacity = "0.5"; }
  }

  // ── Helper: get current quantity input value ───────────────────────────────
  function getQty() {
    return parseInt(document.getElementById("quantity")?.value || "1", 10);
  }

  // ── Add to Cart ────────────────────────────────────────────────────────────
  // CHANGED: was POST /api/buy (Flask, single-product order).
  // Now uses POST /api/cart/add { productId, quantity } then stays on page.
  const addCartBtn = document.getElementById("add-cart-btn");
  if (addCartBtn) {
    addCartBtn.addEventListener("click", async () => {
      const qty = getQty();
      addCartBtn.disabled  = true;
      addCartBtn.innerText = "Adding...";
      try {
        await apiAddToCart(product.id, qty);
        addCartBtn.innerText = "✔ Added to Cart!";
        setTimeout(() => { addCartBtn.innerText = "Add to Cart"; }, 2000);
      } catch (err) {
        alert("Could not add to cart: " + err.message);
        addCartBtn.innerText = "Add to Cart";
      } finally {
        addCartBtn.disabled = false;
      }
    });
  }

  // ── Buy Now (add to cart → place order) ───────────────────────────────────
  // CHANGED: was POST /api/buy with full product data body.
  // New flow: add to cart first, then POST /api/orders/place { address, pincode }.
  // This is the correct Spring Boot checkout flow.
  const buyNowBtn = document.getElementById("buy-now-btn");
  if (buyNowBtn) {
    buyNowBtn.addEventListener("click", async () => {
      const pincode = document.getElementById("pincode-input")?.value?.trim();
      const address = document.getElementById("address-input")?.value?.trim();
      const qty     = getQty();

      if (!pincode || !address) {
        alert("Please enter both your delivery address and pincode before buying.");
        return;
      }

      buyNowBtn.disabled  = true;
      buyNowBtn.innerText = "Placing Order...";

      try {
        // Step 1: add to cart (creates cart if not exists)
        await apiAddToCart(product.id, qty);

        // Step 2: checkout — Spring Boot atomically decrements stock,
        // snapshots prices, clears cart, and returns the new Order
        const orderRes = await apiPlaceOrder(address, pincode);
        const order    = orderRes.data;

        alert(`✔ Order #${order.id} placed successfully! Total: ${formatPrice(order.totalAmount)}`);
        window.location.href = "orders.html";
      } catch (err) {
        alert("Order failed: " + err.message);
        buyNowBtn.innerText = "Buy Now";
        buyNowBtn.disabled  = false;
      }
    });
  }

  // ── Wishlist ───────────────────────────────────────────────────────────────
  // CHANGED: was POST /api/wishlist with { name, price, image, email }.
  // Now uses POST /api/wishlist/add/{productId} with JWT header (no email needed).
  const wishlistBtn = document.getElementById("wishlist-btn");
  if (wishlistBtn) {
    // Check current wishlist status and update button appearance on load
    apiIsWishlisted(product.id)
      .then(wishlisted => {
        if (wishlisted) {
          wishlistBtn.innerHTML = '<i class="fa-solid fa-heart" style="color:#e53935;"></i> Wishlisted';
        }
      })
      .catch(() => {}); // Non-critical — ignore errors on status check

    wishlistBtn.addEventListener("click", async () => {
      wishlistBtn.disabled = true;
      try {
        const alreadyWishlisted = wishlistBtn.innerHTML.includes("Wishlisted");

        if (alreadyWishlisted) {
          // Toggle off — remove from wishlist
          await apiRemoveFromWishlist(product.id);
          wishlistBtn.innerHTML = '<i class="fa-solid fa-heart"></i> Wishlist';
        } else {
          // Toggle on — add to wishlist
          await apiAddToWishlist(product.id);
          wishlistBtn.innerHTML = '<i class="fa-solid fa-heart" style="color:#e53935;"></i> Wishlisted';
        }
      } catch (err) {
        alert(err.message); // e.g. "Already in wishlist" or conflict message
      } finally {
        wishlistBtn.disabled = false;
      }
    });
  }
};