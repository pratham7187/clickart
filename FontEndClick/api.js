// ============================================================
//  api.js — ClickKart Central API Client
//  Target backend: Spring Boot 3 on http://localhost:8080
//
//  USAGE: include this file FIRST in every page's <head>
//    <script src="api.js"></script>
//
//  All backend communication goes through this file.
//  Every function returns the parsed JSON from the backend.
//  Protected functions automatically attach the JWT header.
//  On 401 / 403 the session is cleared and user is redirected
//  to index.html.
// ============================================================

const API_BASE = "http://localhost:8080";

// ── Session helpers ────────────────────────────────────────────────────────────
// ck_token  → raw JWT string
// ck_user   → JSON { userId, name, email, role }

function getToken()   { return localStorage.getItem("ck_token"); }
function getUser()    { return JSON.parse(localStorage.getItem("ck_user") || "null"); }
function isLoggedIn() { return !!getToken(); }

/**
 * Persist a successful login / register response.
 * @param {Object} data  - AuthResponse.data from the backend
 *   { token, tokenType, userId, name, email, role }
 */
function saveSession(data) {
  localStorage.setItem("ck_token", data.token);
  localStorage.setItem("ck_user", JSON.stringify({
    userId : data.userId,
    name   : data.name,
    email  : data.email,
    role   : data.role
  }));
}

/** Remove all session data (call on logout). */
function clearSession() {
  localStorage.removeItem("ck_token");
  localStorage.removeItem("ck_user");
  // Also clean up old Flask-era keys so they don't cause confusion
  localStorage.removeItem("clickkart-user");
  localStorage.removeItem("productName");
  localStorage.removeItem("productPrice");
  localStorage.removeItem("productImage");
}

/**
 * Auth guard — call at the top of every protected page.
 * Redirects to index.html if no token is found.
 * @returns {boolean} true if the user is authenticated
 */
function requireAuth() {
  if (!isLoggedIn()) {
    alert("Please login to continue.");
    window.location.href = "index.html";
    return false;
  }
  return true;
}

// ── Core fetch wrapper ─────────────────────────────────────────────────────────

/**
 * Make an authenticated (or public) API request.
 *
 * @param {string}  path     - e.g. "/api/products/1"
 * @param {Object}  options  - standard fetch options (method, body, headers…)
 * @returns {Promise<Object>} - the parsed ApiResponse JSON body
 * @throws  {Error}  message from the backend or a network error
 */
async function apiFetch(path, options = {}) {
  const token = getToken();

  const headers = {
    "Content-Type": "application/json",
    ...options.headers
  };

  // Attach JWT if present
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  let res;
  try {
    res = await fetch(`${API_BASE}${path}`, { ...options, headers });
  } catch (networkErr) {
    throw new Error("Cannot reach the server. Is Spring Boot running on port 8080?");
  }

  // Handle session expiry
  if (res.status === 401 || res.status === 403) {
    clearSession();
    alert("Your session has expired. Please login again.");
    window.location.href = "index.html";
    throw new Error("Unauthorized");
  }

  const json = await res.json();

  if (!res.ok) {
    // Backend returns { message: "..." } on error
    throw new Error(json.message || `HTTP ${res.status}`);
  }

  return json; // ApiResponse<T> { success, message, data, timestamp }
}

// ── Auth API ───────────────────────────────────────────────────────────────────

/**
 * Register a new user account.
 * POST /api/auth/register
 * @returns {Promise<Object>} ApiResponse whose .data is AuthResponse
 */
async function apiRegister(name, email, password) {
  return apiFetch("/api/auth/register", {
    method : "POST",
    body   : JSON.stringify({ name, email, password })
  });
}

/**
 * Login with email + password.
 * POST /api/auth/login
 * @returns {Promise<Object>} ApiResponse whose .data is AuthResponse
 */
async function apiLogin(email, password) {
  return apiFetch("/api/auth/login", {
    method : "POST",
    body   : JSON.stringify({ email, password })
  });
}

// ── Product API ────────────────────────────────────────────────────────────────

/**
 * Get all active products for a category (paginated, page 0, size 50).
 * GET /api/products/category/{categoryId}
 *
 * Category IDs (from DB seed data):
 *   1 = Men  |  2 = Women  |  3 = Kids
 */
async function apiGetProductsByCategory(categoryId) {
  return apiFetch(`/api/products/category/${categoryId}`);
}

/**
 * Get a single product by its database ID.
 * GET /api/products/{id}
 */
async function apiGetProductById(productId) {
  return apiFetch(`/api/products/${productId}`);
}

/**
 * Full-text keyword search across product name and subcategory.
 * GET /api/products/search?keyword={keyword}
 */
async function apiSearchProducts(keyword) {
  return apiFetch(`/api/products/search?keyword=${encodeURIComponent(keyword)}`);
}

/**
 * Get all active products (paginated).
 * GET /api/products?page={page}&size={size}
 */
async function apiGetAllProducts(page = 0, size = 50) {
  return apiFetch(`/api/products?page=${page}&size=${size}`);
}

// ── Cart API ───────────────────────────────────────────────────────────────────

/**
 * Get the current user's full cart with all items and totals.
 * GET /api/cart   [JWT required]
 */
async function apiGetCart() {
  return apiFetch("/api/cart");
}

/**
 * Add a product to the cart (or increment quantity if already present).
 * POST /api/cart/add   [JWT required]
 * @param {number} productId
 * @param {number} quantity  (default 1)
 */
async function apiAddToCart(productId, quantity = 1) {
  return apiFetch("/api/cart/add", {
    method : "POST",
    body   : JSON.stringify({ productId, quantity })
  });
}

/**
 * Set the absolute quantity for a cart line.
 * PUT /api/cart/update   [JWT required]
 * Sending quantity = 0 removes the item.
 */
async function apiUpdateCartItem(productId, quantity) {
  return apiFetch("/api/cart/update", {
    method : "PUT",
    body   : JSON.stringify({ productId, quantity })
  });
}

/**
 * Remove a product line from the cart.
 * DELETE /api/cart/remove/{productId}   [JWT required]
 */
async function apiRemoveFromCart(productId) {
  return apiFetch(`/api/cart/remove/${productId}`, { method: "DELETE" });
}

/**
 * Empty the cart (all items removed, cart header preserved).
 * DELETE /api/cart/clear   [JWT required]
 */
async function apiClearCart() {
  return apiFetch("/api/cart/clear", { method: "DELETE" });
}

/**
 * Get just the count of distinct product lines in the cart.
 * GET /api/cart/count   [JWT required]
 * Useful for the nav badge.
 */
async function apiGetCartCount() {
  return apiFetch("/api/cart/count");
}

// ── Wishlist API ───────────────────────────────────────────────────────────────

/**
 * Get all wishlist items for the current user.
 * GET /api/wishlist   [JWT required]
 */
async function apiGetWishlist() {
  return apiFetch("/api/wishlist");
}

/**
 * Add a product to the wishlist.
 * POST /api/wishlist/add/{productId}   [JWT required]
 * Throws if product is already in the wishlist (409 Conflict from backend).
 */
async function apiAddToWishlist(productId) {
  return apiFetch(`/api/wishlist/add/${productId}`, { method: "POST" });
}

/**
 * Remove a product from the wishlist.
 * DELETE /api/wishlist/remove/{productId}   [JWT required]
 */
async function apiRemoveFromWishlist(productId) {
  return apiFetch(`/api/wishlist/remove/${productId}`, { method: "DELETE" });
}

/**
 * Check if a specific product is in the current user's wishlist.
 * GET /api/wishlist/check/{productId}   [JWT required]
 * @returns {Promise<boolean>}  ApiResponse.data = true / false
 */
async function apiIsWishlisted(productId) {
  const res = await apiFetch(`/api/wishlist/check/${productId}`);
  return res.data; // boolean
}

// ── Orders API ─────────────────────────────────────────────────────────────────

/**
 * Place an order from the current cart contents.
 * POST /api/orders/place   [JWT required]
 *
 * This is the CHECKOUT endpoint. It:
 *   1. Validates the cart is not empty
 *   2. Atomically decrements stock for each item
 *   3. Snapshots prices → priceAtPurchase
 *   4. Clears the cart
 *   5. Returns the new Order with all line items
 *
 * @param {string} address  - delivery address (10–500 chars)
 * @param {string} pincode  - 6-digit Indian PIN code
 */
async function apiPlaceOrder(address, pincode) {
  return apiFetch("/api/orders/place", {
    method : "POST",
    body   : JSON.stringify({ address, pincode })
  });
}

/**
 * Get the current user's order history (summary, no line items).
 * GET /api/orders   [JWT required]
 */
async function apiGetOrders() {
  return apiFetch("/api/orders");
}

/**
 * Get a single order with all line items and product details.
 * GET /api/orders/{id}   [JWT required]
 * Scoped to the current user — cannot access another user's orders.
 */
async function apiGetOrderById(orderId) {
  return apiFetch(`/api/orders/${orderId}`);
}

/**
 * Cancel an order (PLACED or CONFIRMED only).
 * DELETE /api/orders/{id}/cancel   [JWT required]
 * Throws OrderCancellationException (→ 400) for SHIPPED / DELIVERED.
 */
async function apiCancelOrder(orderId) {
  return apiFetch(`/api/orders/${orderId}/cancel`, { method: "DELETE" });
}

// ── Utility ────────────────────────────────────────────────────────────────────

/**
 * Format a number as Indian Rupee string.
 * e.g. 1499 → "₹1,499"
 */
function formatPrice(amount) {
  return "₹" + Number(amount).toLocaleString("en-IN");
}

/**
 * Format an ISO date string as a readable Indian date.
 * e.g. "2025-06-13T12:00:00" → "13 Jun 2025"
 */
function formatDate(isoString) {
  if (!isoString) return "—";
  return new Date(isoString).toLocaleDateString("en-IN", {
    day   : "numeric",
    month : "short",
    year  : "numeric"
  });
}
