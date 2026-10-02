import { useState, useEffect, useCallback } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { cartAPI, orderAPI, formatPrice, resolveImageUrl } from '../services/api';
import Loader from '../components/Loader';
import {
  FaShoppingCart,
  FaArrowLeft,
  FaBolt,
  FaTrash,
  FaTruck,
  FaCheck,
  FaTimes,
  FaExclamationCircle,
} from 'react-icons/fa';
import logo from '../assets/images/logo.png';

const Cart = () => {
  const navigate = useNavigate();

  const [cartData, setCartData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [updatingQty, setUpdatingQty] = useState(false);
  const [clearingCart, setClearingCart] = useState(false);

  // Checkout modal state
  const [showModal, setShowModal] = useState(false);
  const [pincode, setPincode] = useState('');
  const [address, setAddress] = useState('');
  const [placingOrder, setPlacingOrder] = useState(false);
  const [pincodeError, setPincodeError] = useState(false);
  const [addressError, setAddressError] = useState(false);

  // ── Load cart ──────────────────────────────────────────────────────────
  const loadCart = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const res = await cartAPI.get();
      setCartData(res.data);
    } catch (err) {
      setError('Could not load cart: ' + err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadCart();
  }, [loadCart]);

  // ── Quantity change ────────────────────────────────────────────────────
  const handleQtyChange = async (productId, delta) => {
    if (!cartData) return;
    const item = cartData.items.find((i) => i.product.id === productId);
    if (!item) return;
    const newQty = item.quantity + delta;
    if (newQty < 1) return;

    setUpdatingQty(true);
    try {
      const res = await cartAPI.update(productId, newQty);
      setCartData(res.data);
    } catch (err) {
      alert('Could not update quantity: ' + err.message);
    } finally {
      setUpdatingQty(false);
    }
  };

  // ── Remove item ────────────────────────────────────────────────────────
  const handleRemoveItem = async (productId) => {
    try {
      const res = await cartAPI.remove(productId);
      setCartData(res.data);
    } catch (err) {
      alert('Could not remove item: ' + err.message);
      loadCart();
    }
  };

  // ── Clear cart ─────────────────────────────────────────────────────────
  const handleClearCart = async () => {
    if (!window.confirm('Clear your entire cart? This cannot be undone.')) return;
    setClearingCart(true);
    try {
      await cartAPI.clear();
      setCartData({ items: [], itemCount: 0, totalAmount: 0 });
    } catch (err) {
      alert('Could not clear cart: ' + err.message);
    } finally {
      setClearingCart(false);
    }
  };

  // ── Checkout ───────────────────────────────────────────────────────────
  const openCheckoutModal = () => {
    setPincode('');
    setAddress('');
    setPincodeError(false);
    setAddressError(false);
    setPlacingOrder(false);
    setShowModal(true);
  };

  const closeModal = () => {
    if (!placingOrder) setShowModal(false);
  };

  const handlePlaceOrder = async () => {
    const trimmedPincode = pincode.trim();
    const trimmedAddress = address.trim();

    let hasError = false;
    if (!/^\d{6}$/.test(trimmedPincode)) {
      setPincodeError(true);
      hasError = true;
    } else {
      setPincodeError(false);
    }
    if (trimmedAddress.length < 10) {
      setAddressError(true);
      hasError = true;
    } else {
      setAddressError(false);
    }
    if (hasError) return;

    setPlacingOrder(true);
    try {
      const res = await orderAPI.place(trimmedAddress, trimmedPincode);
      const order = res.data;
      setShowModal(false);
      alert(
        `✔ Order #${order.id} placed successfully!\nTotal: ${formatPrice(order.totalAmount)}\n\nThank you for shopping with ClickKart!`
      );
      navigate('/orders');
    } catch (err) {
      alert('Order failed: ' + err.message);
      setPlacingOrder(false);
    }
  };

  // ── Escape key closes modal ────────────────────────────────────────────
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'Escape' && showModal && !placingOrder) {
        setShowModal(false);
      }
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [showModal, placingOrder]);

  // ── Loading state ──────────────────────────────────────────────────────
  if (loading) {
    return <Loader message="Loading your cart..." />;
  }

  const items = cartData?.items || [];
  const itemCount = cartData?.itemCount || 0;
  const totalAmount = cartData?.totalAmount || 0;
  const isEmpty = items.length === 0;

  return (
    <>
      <div className="cart-page">
        {/* Left panel: cart items */}
        <div className="cart-items-panel">
          <h1>
            <FaShoppingCart style={{ color: '#fe119f', marginRight: '10px' }} />
            Your Cart
          </h1>

          {error ? (
            <div className="state-msg error">
              <FaExclamationCircle />
              <div>{error}</div>
            </div>
          ) : isEmpty ? (
            <div className="state-msg">
              <FaShoppingCart />
              <div>Your cart is empty!</div>
              <Link className="continue-link" to="/">
                <FaArrowLeft /> &nbsp; Continue Shopping
              </Link>
            </div>
          ) : (
            <div id="cart-items-container">
              {items.map((item) => {
                const p = item.product;
                return (
                  <div
                    className="cart-row"
                    key={p.id}
                    data-product-id={p.id}
                  >
                    <img
                      src={resolveImageUrl(p.imageUrl)}
                      alt={p.name}
                      onError={(e) => { e.target.src = logo; }}
                      onClick={() => navigate(`/product/${p.id}`)}
                      style={{ cursor: 'pointer' }}
                    />

                    <div className="cart-row-info">
                      <div
                        className="item-name"
                        onClick={() => navigate(`/product/${p.id}`)}
                        style={{ cursor: 'pointer' }}
                      >
                        {p.name}
                      </div>
                      <div className="item-price">
                        {formatPrice(p.price)} each
                      </div>
                      <div className="item-subtotal">
                        Subtotal: <strong>{formatPrice(item.subtotal)}</strong>
                      </div>

                      <div className="qty-stepper">
                        <button
                          className="qty-btn qty-minus"
                          disabled={item.quantity <= 1 || updatingQty}
                          onClick={() => handleQtyChange(p.id, -1)}
                        >
                          −
                        </button>
                        <span className="qty-display">{item.quantity}</span>
                        <button
                          className="qty-btn qty-plus"
                          disabled={item.quantity >= p.stock || updatingQty}
                          onClick={() => handleQtyChange(p.id, +1)}
                        >
                          +
                        </button>
                      </div>
                    </div>

                    <button
                      className="remove-item-btn"
                      title="Remove from cart"
                      onClick={() => handleRemoveItem(p.id)}
                    >
                      <FaTimes />
                    </button>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Right panel: order summary */}
        <div className="cart-summary-panel">
          <div className="summary-card">
            <h2>Order Summary</h2>

            <div className="summary-row">
              <span>Items</span>
              <span>
                {isEmpty
                  ? '0 items'
                  : `${itemCount} item${itemCount !== 1 ? 's' : ''}`}
              </span>
            </div>
            <div className="summary-row">
              <span>Price</span>
              <span>{formatPrice(totalAmount)}</span>
            </div>
            <div className="summary-row">
              <span>Delivery</span>
              <span style={{ color: '#4caf50', fontWeight: 600 }}>FREE</span>
            </div>
            <div className="summary-row total">
              <span>Total</span>
              <span>{formatPrice(totalAmount)}</span>
            </div>

            <button
              className="btn-checkout"
              disabled={isEmpty}
              onClick={openCheckoutModal}
            >
              <FaBolt /> &nbsp; Proceed to Checkout
            </button>
            <button
              className="btn-clear"
              disabled={isEmpty || clearingCart}
              onClick={handleClearCart}
            >
              <FaTrash /> &nbsp;{' '}
              {clearingCart ? 'Clearing...' : 'Clear Cart'}
            </button>
          </div>
        </div>
      </div>

      {/* ── Checkout Modal ────────────────────────────────────────────── */}
      {showModal && (
        <div
          className="checkout-modal open"
          onClick={(e) => {
            if (e.target === e.currentTarget) closeModal();
          }}
        >
          <div className="checkout-modal-box">
            <h3>
              <FaTruck /> &nbsp; Delivery Details
            </h3>

            <label htmlFor="modal-pincode">Pincode</label>
            <input
              type="text"
              id="modal-pincode"
              placeholder="6-digit pincode"
              maxLength="6"
              value={pincode}
              onChange={(e) => setPincode(e.target.value)}
              style={pincodeError ? { borderColor: '#e53935' } : {}}
              autoFocus
            />

            <label htmlFor="modal-address">Delivery Address</label>
            <textarea
              id="modal-address"
              placeholder="House no., street, area, city…"
              value={address}
              onChange={(e) => setAddress(e.target.value)}
              style={addressError ? { borderColor: '#e53935' } : {}}
            />

            <div className="modal-actions">
              <button
                className="btn-modal-cancel"
                onClick={closeModal}
                disabled={placingOrder}
              >
                Cancel
              </button>
              <button
                className="btn-modal-confirm"
                onClick={handlePlaceOrder}
                disabled={placingOrder}
              >
                {placingOrder ? (
                  'Placing Order...'
                ) : (
                  <>
                    <FaCheck /> &nbsp; Place Order
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default Cart;
