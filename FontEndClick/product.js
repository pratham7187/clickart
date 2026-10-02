// product.js — ClickKart Product Detail Page
// Reads ck_productId from localStorage, fetches full product from Spring Boot,
// renders the detail page, and wires Add-to-Cart, Buy Now, Wishlist, and Reviews.
//
// Requires: api.js must be loaded first (provides apiGetProductById,
// apiAddToCart, apiPlaceOrder, apiAddToWishlist, apiGetReviews,
// apiGetReviewSummary, apiSubmitReview, apiDeleteReview, etc.)

window.onload = async function () {
  // ── Auth guard ────────────────────────────────────────────────────────────
  if (!requireAuth()) return;

  // ── Read product ID stored by the listing pages ───────────────────────────
  const productId = new URLSearchParams(window.location.search).get("id")
    || localStorage.getItem("ck_productId");
  if (!productId) {
    alert("No product selected. Please browse products first.");
    history.back();
    return;
  }

  // ── Fetch product from Spring Boot ─────────────────────────────────────────
  let product;
  try {
    const res = await apiGetProductById(productId);
    product = res.data;
  } catch (err) {
    document.getElementById("product-name").innerText = "Could not load product.";
    console.error("Product fetch failed:", err);
    return;
  }

  // ── Render product details ─────────────────────────────────────────────────
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

  const qtyInput = document.getElementById("quantity");
  if (qtyInput && product.stock > 0) {
    qtyInput.max = product.stock;
  }

  if (product.stock === 0) {
    const addCartBtn = document.getElementById("add-cart-btn");
    const buyBtn     = document.getElementById("buy-now-btn");
    if (addCartBtn) { addCartBtn.disabled = true; addCartBtn.style.opacity = "0.5"; }
    if (buyBtn)     { buyBtn.disabled = true;     buyBtn.style.opacity = "0.5"; }
  }

  // ── Load real ratings ─────────────────────────────────────────────────────
  loadRatingAndReviews(productId);

  // ── Helper: get current quantity input value ───────────────────────────────
  function getQty() {
    return parseInt(document.getElementById("quantity")?.value || "1", 10);
  }

  // ── Add to Cart ────────────────────────────────────────────────────────────
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
        await apiAddToCart(product.id, qty);
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
  const wishlistBtn = document.getElementById("wishlist-btn");
  if (wishlistBtn) {
    apiIsWishlisted(product.id)
      .then(wishlisted => {
        if (wishlisted) {
          wishlistBtn.innerHTML = '<i class="fa-solid fa-heart" style="color:#e53935;"></i> Wishlisted';
        }
      })
      .catch(() => {});

    wishlistBtn.addEventListener("click", async () => {
      wishlistBtn.disabled = true;
      try {
        const alreadyWishlisted = wishlistBtn.innerHTML.includes("Wishlisted");

        if (alreadyWishlisted) {
          await apiRemoveFromWishlist(product.id);
          wishlistBtn.innerHTML = '<i class="fa-solid fa-heart"></i> Wishlist';
        } else {
          await apiAddToWishlist(product.id);
          wishlistBtn.innerHTML = '<i class="fa-solid fa-heart" style="color:#e53935;"></i> Wishlisted';
        }
      } catch (err) {
        alert(err.message);
      } finally {
        wishlistBtn.disabled = false;
      }
    });
  }

  // ═══════════════════════════════════════════════════════════════════════════
  //  REVIEWS SYSTEM
  // ═══════════════════════════════════════════════════════════════════════════

  // ── Star input selection ───────────────────────────────────────────────────
  let selectedRating = 0;
  const starBtns = document.querySelectorAll("#star-input .star-btn");
  starBtns.forEach(btn => {
    btn.addEventListener("click", () => {
      selectedRating = parseInt(btn.dataset.rating);
      starBtns.forEach(b => {
        b.classList.toggle("active", parseInt(b.dataset.rating) <= selectedRating);
      });
    });
    btn.addEventListener("mouseenter", () => {
      const hoverVal = parseInt(btn.dataset.rating);
      starBtns.forEach(b => {
        b.classList.toggle("active", parseInt(b.dataset.rating) <= hoverVal);
      });
    });
  });
  document.getElementById("star-input")?.addEventListener("mouseleave", () => {
    starBtns.forEach(b => {
      b.classList.toggle("active", parseInt(b.dataset.rating) <= selectedRating);
    });
  });

  // ── Submit Review ──────────────────────────────────────────────────────────
  const submitBtn = document.getElementById("submit-review-btn");
  if (submitBtn) {
    submitBtn.addEventListener("click", async () => {
      if (selectedRating === 0) {
        alert("Please select a star rating.");
        return;
      }
      const comment = document.getElementById("review-comment")?.value?.trim() || "";
      submitBtn.disabled  = true;
      submitBtn.innerText = "Submitting...";

      try {
        await apiSubmitReview(productId, selectedRating, comment);
        document.getElementById("review-comment").value = "";
        selectedRating = 0;
        starBtns.forEach(b => b.classList.remove("active"));
        loadRatingAndReviews(productId);
      } catch (err) {
        alert("Could not submit review: " + err.message);
      } finally {
        submitBtn.disabled  = false;
        submitBtn.innerText = "Submit Review";
      }
    });
  }
};

// ═══════════════════════════════════════════════════════════════════════════════
//  Load rating summary + review list
// ═══════════════════════════════════════════════════════════════════════════════

function renderStars(rating) {
  let stars = "";
  for (let i = 1; i <= 5; i++) {
    stars += i <= Math.round(rating) ? "★" : "☆";
  }
  return stars;
}

async function loadRatingAndReviews(productId) {
  const summaryEl = document.getElementById("review-summary");
  const listEl    = document.getElementById("review-list");
  const ratingDisplay = document.getElementById("product-rating-display");
  const currentUser = getUser();

  // Load summary
  try {
    const summaryRes = await apiGetReviewSummary(productId);
    const s = summaryRes.data;
    const avg   = s.averageRating || 0;
    const count = s.reviewCount   || 0;

    if (ratingDisplay) {
      if (count > 0) {
        ratingDisplay.innerHTML = `<span class="star-display">${renderStars(avg)}</span> <span style="font-weight:700;">${avg}</span> <span style="color:var(--txt-muted);">(${count} Review${count !== 1 ? 's' : ''})</span>`;
      } else {
        ratingDisplay.innerHTML = `<span style="color:var(--txt-muted);">No reviews yet</span>`;
      }
    }

    if (summaryEl) {
      if (count > 0) {
        summaryEl.innerHTML = `
          <span class="avg-rating">${avg}</span>
          <span class="star-display">${renderStars(avg)}</span>
          <span>${count} Review${count !== 1 ? 's' : ''}</span>
        `;
      } else {
        summaryEl.innerHTML = `<span>No reviews yet. Be the first to review!</span>`;
      }
    }
  } catch (err) {
    console.error("Could not load review summary:", err);
  }

  // Load reviews
  try {
    const reviewsRes = await apiGetReviews(productId);
    const reviews = reviewsRes.data || [];

    if (!listEl) return;

    if (reviews.length === 0) {
      listEl.innerHTML = `<div class="no-reviews"><i class="fa-regular fa-comment-dots" style="font-size:2rem;margin-bottom:8px;display:block;"></i>No reviews yet. Share your thoughts!</div>`;
      return;
    }

    listEl.innerHTML = "";
    reviews.forEach(r => {
      const card = document.createElement("div");
      card.className = "review-card";

      const isOwn = currentUser && currentUser.userId == r.userId;
      const deleteBtn = isOwn
        ? `<button class="delete-review-btn" onclick="deleteReview(${r.id}, ${productId})"><i class="fa-solid fa-trash-can"></i> Delete</button>`
        : "";

      card.innerHTML = `
        <div class="review-card-header">
          <span class="reviewer"><i class="fa-solid fa-user-circle"></i> ${r.userName}</span>
          <span class="review-date">${formatDate(r.createdAt)}</span>
        </div>
        <div class="review-stars">${renderStars(r.rating)} ${r.rating}/5</div>
        ${r.comment ? `<div class="review-comment">${r.comment}</div>` : ""}
        ${deleteBtn}
      `;
      listEl.appendChild(card);
    });
  } catch (err) {
    console.error("Could not load reviews:", err);
    if (listEl) listEl.innerHTML = `<p style="color:var(--error);">Could not load reviews.</p>`;
  }
}

async function deleteReview(reviewId, productId) {
  if (!confirm("Delete this review?")) return;
  try {
    await apiDeleteReview(reviewId);
    loadRatingAndReviews(productId);
  } catch (err) {
    alert("Could not delete review: " + err.message);
  }
}
