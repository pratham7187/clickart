// cart.js — ClickKart Cart Page
// Requires api.js loaded first (provides apiGetCart, apiUpdateCartItem,
// apiRemoveFromCart, apiClearCart, apiPlaceOrder, requireAuth, formatPrice).

(async function () {
  // ── Auth guard ──────────────────────────────────────────────────────────────
  if (!requireAuth()) return;

  // ── DOM refs ────────────────────────────────────────────────────────────────
  const itemsContainer  = document.getElementById("cart-items-container");
  const summaryCount    = document.getElementById("summary-item-count");
  const summarySubtotal = document.getElementById("summary-subtotal");
  const summaryTotal    = document.getElementById("summary-total");
  const checkoutBtn     = document.getElementById("checkout-btn");
  const clearBtn        = document.getElementById("clear-cart-btn");
  const modal           = document.getElementById("checkout-modal");
  const modalPincode    = document.getElementById("modal-pincode");
  const modalAddress    = document.getElementById("modal-address");
  const modalConfirmBtn = document.getElementById("modal-confirm-btn");
  const modalCancelBtn  = document.getElementById("modal-cancel-btn");

  // ── Shared cart state ────────────────────────────────────────────────────────
  // Kept in module scope so quantity buttons can read current qty instantly.
  let cartData = null; // CartResponse { cartId, items, itemCount, totalAmount }

  // ════════════════════════════════════════════════════════════════════════════
  // RENDER
  // ════════════════════════════════════════════════════════════════════════════

  function renderEmptyState() {
    itemsContainer.innerHTML = `
      <div class="state-msg">
        <i class="fa-solid fa-cart-shopping"></i>
        <div>Your cart is empty!</div>
        <a class="continue-link" href="index.html">
          <i class="fa-solid fa-arrow-left"></i>&nbsp; Continue Shopping
        </a>
      </div>`;
    summaryCount.textContent    = "0 items";
    summarySubtotal.textContent = formatPrice(0);
    summaryTotal.textContent    = formatPrice(0);
    checkoutBtn.disabled = true;
    clearBtn.disabled    = true;
  }

  function renderError(message) {
    itemsContainer.innerHTML = `
      <div class="state-msg error">
        <i class="fa-solid fa-circle-exclamation"></i>
        <div>${message}</div>
      </div>`;
    checkoutBtn.disabled = true;
    clearBtn.disabled    = true;
  }

  function renderCart(cart) {
    cartData = cart;
    const items = cart.items || [];

    if (items.length === 0) {
      renderEmptyState();
      return;
    }

    // ── Update summary panel ─────────────────────────────────────────────────
    summaryCount.textContent    = `${cart.itemCount} item${cart.itemCount !== 1 ? "s" : ""}`;
    summarySubtotal.textContent = formatPrice(cart.totalAmount);
    summaryTotal.textContent    = formatPrice(cart.totalAmount);
    checkoutBtn.disabled = false;
    clearBtn.disabled    = false;

    // ── Build item rows ──────────────────────────────────────────────────────
    // CartItemResponse shape from Spring Boot:
    //   { id, product: ProductResponse, quantity, subtotal, addedAt }
    // ProductResponse: { id, name, price, imageUrl, stock, … }

    itemsContainer.innerHTML = "";

    items.forEach(item => {
      const p   = item.product;
      const row = document.createElement("div");
      row.className    = "cart-row";
      row.dataset.productId = p.id;

      row.innerHTML = `
        <img src="${p.imageUrl}" alt="${p.name}" onerror="this.src='images/image/logo.png'" />

        <div class="cart-row-info">
          <div class="item-name">${p.name}</div>
          <div class="item-price">${formatPrice(p.price)} each</div>
          <div class="item-subtotal">Subtotal: <strong>${formatPrice(item.subtotal)}</strong></div>

          <div class="qty-stepper">
            <button class="qty-btn qty-minus" data-pid="${p.id}" ${item.quantity <= 1 ? "disabled" : ""}>−</button>
            <span class="qty-display">${item.quantity}</span>
            <button class="qty-btn qty-plus"  data-pid="${p.id}" ${item.quantity >= p.stock ? "disabled" : ""}>+</button>
          </div>
        </div>

        <button class="remove-item-btn" data-pid="${p.id}" title="Remove from cart">
          <i class="fa-solid fa-xmark"></i>
        </button>
      `;

      itemsContainer.appendChild(row);
    });

    // ── Attach event listeners ───────────────────────────────────────────────
    itemsContainer.querySelectorAll(".qty-minus").forEach(btn => {
      btn.addEventListener("click", () => handleQtyChange(Number(btn.dataset.pid), -1));
    });

    itemsContainer.querySelectorAll(".qty-plus").forEach(btn => {
      btn.addEventListener("click", () => handleQtyChange(Number(btn.dataset.pid), +1));
    });

    itemsContainer.querySelectorAll(".remove-item-btn").forEach(btn => {
      btn.addEventListener("click", () => handleRemoveItem(Number(btn.dataset.pid)));
    });
  }

  // ════════════════════════════════════════════════════════════════════════════
  // LOAD CART
  // ════════════════════════════════════════════════════════════════════════════

  async function loadCart() {
    itemsContainer.innerHTML = `
      <div class="state-msg">
        <i class="fa-solid fa-spinner fa-spin"></i>
        Loading your cart...
      </div>`;
    try {
      const res = await apiGetCart();
      // CartResponse: { cartId, items: CartItemResponse[], itemCount, totalAmount }
      renderCart(res.data);
    } catch (err) {
      renderError("Could not load cart: " + err.message);
    }
  }

  // ════════════════════════════════════════════════════════════════════════════
  // QUANTITY CHANGE
  // PUT /api/cart/update  { productId, quantity }
  // Quantity of 0 would remove — we use the dedicated remove endpoint instead.
  // ════════════════════════════════════════════════════════════════════════════

  async function handleQtyChange(productId, delta) {
    if (!cartData) return;

    const item    = cartData.items.find(i => i.product.id === productId);
    if (!item) return;

    const newQty  = item.quantity + delta;
    if (newQty < 1) return; // guard: shouldn't happen (button disabled at qty=1)

    // Optimistic UI: disable all qty buttons while request in-flight
    setAllQtyButtonsDisabled(true);

    try {
      const res = await apiUpdateCartItem(productId, newQty);
      renderCart(res.data); // backend returns full updated CartResponse
    } catch (err) {
      alert("Could not update quantity: " + err.message);
      setAllQtyButtonsDisabled(false);
    }
  }

  function setAllQtyButtonsDisabled(disabled) {
    itemsContainer.querySelectorAll(".qty-btn").forEach(b => b.disabled = disabled);
  }

  // ════════════════════════════════════════════════════════════════════════════
  // REMOVE ITEM
  // DELETE /api/cart/remove/{productId}
  // ════════════════════════════════════════════════════════════════════════════

  async function handleRemoveItem(productId) {
    // Visual feedback: dim the row immediately
    const row = itemsContainer.querySelector(`[data-product-id="${productId}"]`);
    if (row) row.style.opacity = "0.4";

    try {
      const res = await apiRemoveFromCart(productId);
      renderCart(res.data); // backend returns updated CartResponse
    } catch (err) {
      alert("Could not remove item: " + err.message);
      loadCart(); // re-fetch to restore state
    }
  }

  // ════════════════════════════════════════════════════════════════════════════
  // CLEAR CART
  // DELETE /api/cart/clear
  // ════════════════════════════════════════════════════════════════════════════

  clearBtn.addEventListener("click", async () => {
    if (!confirm("Clear your entire cart? This cannot be undone.")) return;

    clearBtn.disabled = true;
    clearBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i>&nbsp; Clearing...';

    try {
      await apiClearCart();
      renderEmptyState();
    } catch (err) {
      alert("Could not clear cart: " + err.message);
      clearBtn.disabled = false;
      clearBtn.innerHTML = '<i class="fa-solid fa-trash"></i>&nbsp; Clear Cart';
    }
  });

  // ════════════════════════════════════════════════════════════════════════════
  // CHECKOUT FLOW
  // Opens modal → collects address + pincode → POST /api/orders/place
  // ════════════════════════════════════════════════════════════════════════════

  checkoutBtn.addEventListener("click", () => {
    // Reset modal fields
    modalPincode.value = "";
    modalAddress.value = "";
    modalConfirmBtn.disabled  = false;
    modalConfirmBtn.innerHTML = '<i class="fa-solid fa-check"></i>&nbsp; Place Order';
    modal.classList.add("open");
    modalPincode.focus();
  });

  modalCancelBtn.addEventListener("click", () => {
    modal.classList.remove("open");
  });

  // Close modal on backdrop click
  modal.addEventListener("click", e => {
    if (e.target === modal) modal.classList.remove("open");
  });

  modalConfirmBtn.addEventListener("click", async () => {
    const pincode = modalPincode.value.trim();
    const address = modalAddress.value.trim();

    // ── Client-side validation ──────────────────────────────────────────────
    if (!/^\d{6}$/.test(pincode)) {
      modalPincode.focus();
      modalPincode.style.borderColor = "#e53935";
      alert("Please enter a valid 6-digit pincode.");
      return;
    }
    if (address.length < 10) {
      modalAddress.focus();
      modalAddress.style.borderColor = "#e53935";
      alert("Please enter a full delivery address (at least 10 characters).");
      return;
    }

    modalPincode.style.borderColor = "";
    modalAddress.style.borderColor = "";

    // ── POST /api/orders/place ──────────────────────────────────────────────
    modalConfirmBtn.disabled  = true;
    modalConfirmBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i>&nbsp; Placing Order...';

    try {
      const res   = await apiPlaceOrder(address, pincode);
      const order = res.data; // OrderResponse { id, totalAmount, status, … }

      modal.classList.remove("open");
      alert(`✔ Order #${order.id} placed successfully!\nTotal: ${formatPrice(order.totalAmount)}\n\nThank you for shopping with ClickKart!`);
      window.location.href = "orders.html";

    } catch (err) {
      alert("Order failed: " + err.message);
      modalConfirmBtn.disabled  = false;
      modalConfirmBtn.innerHTML = '<i class="fa-solid fa-check"></i>&nbsp; Place Order';
    }
  });

  // ── Keyboard: Escape closes modal ──────────────────────────────────────────
  document.addEventListener("keydown", e => {
    if (e.key === "Escape" && modal.classList.contains("open")) {
      modal.classList.remove("open");
    }
  });

  // ── Initial load ────────────────────────────────────────────────────────────
  loadCart();

})();
