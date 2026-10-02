import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

// ── Create Axios instance ────────────────────────────────────────────────────
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ── Request Interceptor: inject JWT token ────────────────────────────────────
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('ck_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ── Response Interceptor: handle 401 auto-logout ─────────────────────────────
api.interceptors.response.use(
  (response) => response.data, // unwrap: returns ApiResponse<T> directly
  (error) => {
    if (error.response) {
      const status = error.response.status;

      if (status === 401 || status === 403) {
        // Session expired — clear storage and redirect to login
        localStorage.removeItem('ck_token');
        localStorage.removeItem('ck_user');
        window.location.href = '/login';
      }

      // Extract backend error message if available
      const message =
        error.response.data?.message || `HTTP ${status}`;
      return Promise.reject(new Error(message));
    }

    // Network error — backend unreachable
    return Promise.reject(
      new Error('Cannot reach the server. Is Spring Boot running on port 8080?')
    );
  }
);

// ═══════════════════════════════════════════════════════════════════════════════
//  AUTH API
// ═══════════════════════════════════════════════════════════════════════════════

export const authAPI = {
  register: (name, email, password) =>
    api.post('/auth/register', { name, email, password }),

  login: (email, password) =>
    api.post('/auth/login', { email, password }),
};

// ═══════════════════════════════════════════════════════════════════════════════
//  PRODUCT API
// ═══════════════════════════════════════════════════════════════════════════════

export const productAPI = {
  getAll: (page = 0, size = 50) =>
    api.get(`/products?page=${page}&size=${size}`),

  getById: (productId) =>
    api.get(`/products/${productId}`),

  getByCategory: (categoryId) =>
    api.get(`/products/category/${categoryId}`),

  search: (keyword) =>
    api.get(`/products/search?keyword=${encodeURIComponent(keyword)}`),
};

// ═══════════════════════════════════════════════════════════════════════════════
//  CART API
// ═══════════════════════════════════════════════════════════════════════════════

export const cartAPI = {
  get: () => api.get('/cart'),
  add: (productId, quantity = 1) =>
    api.post('/cart/add', { productId, quantity }),
  update: (productId, quantity) =>
    api.put('/cart/update', { productId, quantity }),
  remove: (productId) => api.delete(`/cart/remove/${productId}`),
  clear: () => api.delete('/cart/clear'),
  count: () => api.get('/cart/count'),
};

// ═══════════════════════════════════════════════════════════════════════════════
//  WISHLIST API
// ═══════════════════════════════════════════════════════════════════════════════

export const wishlistAPI = {
  get: () => api.get('/wishlist'),
  add: (productId) => api.post(`/wishlist/add/${productId}`),
  remove: (productId) => api.delete(`/wishlist/remove/${productId}`),
  check: (productId) => api.get(`/wishlist/check/${productId}`),
};

// ═══════════════════════════════════════════════════════════════════════════════
//  ORDER API
// ═══════════════════════════════════════════════════════════════════════════════

export const orderAPI = {
  place: (address, pincode) =>
    api.post('/orders/place', { address, pincode }),
  getAll: () => api.get('/orders'),
  getById: (orderId) => api.get(`/orders/${orderId}`),
  cancel: (orderId) => api.delete(`/orders/${orderId}/cancel`),
};

// ═══════════════════════════════════════════════════════════════════════════════
//  REVIEW API
// ═══════════════════════════════════════════════════════════════════════════════

export const reviewAPI = {
  getByProduct: (productId) => api.get(`/reviews/${productId}`),
  getSummary: (productId) => api.get(`/reviews/${productId}/summary`),
  submit: (productId, rating, comment) =>
    api.post(`/reviews/${productId}`, { rating, comment }),
  delete: (reviewId) => api.delete(`/reviews/${reviewId}`),
};

// ═══════════════════════════════════════════════════════════════════════════════
//  UTILITY
// ═══════════════════════════════════════════════════════════════════════════════

export const formatPrice = (amount) =>
  '₹' + Number(amount).toLocaleString('en-IN');

export const formatDate = (isoString) => {
  if (!isoString) return '—';
  return new Date(isoString).toLocaleDateString('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  });
};

/**
 * Resolve a product imageUrl from the backend.
 * - Absolute URLs (http/https) are returned as-is.
 * - Relative paths (e.g. "images/imgmentshirt/t1.jpeg") are prefixed
 *   with the backend origin so they load from Spring Boot's static serving
 *   OR from the React public/ folder.
 */
const BACKEND_ORIGIN = (import.meta.env.VITE_API_URL || 'http://localhost:8080/api')
  .replace(/\/api\/?$/, '');

export const resolveImageUrl = (imageUrl) => {
  if (!imageUrl) return '';
  if (/^https?:\/\//i.test(imageUrl)) return imageUrl;
  // Relative path — try public/ first (served by Vite at /)
  // The images are in public/images/... so "/" + relative path works.
  const clean = imageUrl.replace(/^\/+/, '');
  return `/${clean}`;
};

export default api;

