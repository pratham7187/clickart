import { useState, useEffect, useCallback } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { orderAPI, formatPrice, formatDate, resolveImageUrl } from '../services/api';
import Loader from '../components/Loader';
import {
  FaBox,
  FaBoxOpen,
  FaArrowLeft,
  FaCalendarAlt,
  FaMapMarkerAlt,
  FaTruck,
  FaTimes,
  FaExclamationCircle,
} from 'react-icons/fa';
import logo from '../assets/images/logo.png';

const STATUS_STYLE = {
  PLACED:    { bg: '#fff3e0', color: '#e65100', label: '⏳ Placed' },
  CONFIRMED: { bg: '#e3f2fd', color: '#1565c0', label: '✔ Confirmed' },
  SHIPPED:   { bg: '#f3e5f5', color: '#6a1b9a', label: '🚚 Shipped' },
  DELIVERED: { bg: '#e8f5e9', color: '#2e7d32', label: '✅ Delivered' },
  CANCELLED: { bg: '#ffebee', color: '#b71c1c', label: '✖ Cancelled' },
};

const Orders = () => {
  const navigate = useNavigate();

  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [cancellingId, setCancellingId] = useState(null);

  // ── Load orders with full details ──────────────────────────────────────
  const fetchOrders = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const res = await orderAPI.getAll();
      const orderList = res.data || [];

      if (orderList.length === 0) {
        setOrders([]);
        return;
      }

      // Fetch full details (with line items + product info) in parallel
      const detailPromises = orderList.map((o) => orderAPI.getById(o.id));
      const detailResults = await Promise.all(detailPromises);
      setOrders(detailResults.map((r) => r.data));
    } catch (err) {
      setError('Error loading orders: ' + err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  // ── Cancel order ───────────────────────────────────────────────────────
  const handleCancelOrder = async (orderId) => {
    if (!window.confirm(`Cancel Order #${orderId}? This cannot be undone.`))
      return;

    setCancellingId(orderId);
    try {
      await orderAPI.cancel(orderId);
      alert(`Order #${orderId} has been cancelled.`);
      fetchOrders();
    } catch (err) {
      alert('Could not cancel: ' + err.message);
    } finally {
      setCancellingId(null);
    }
  };

  // ── Loading state ──────────────────────────────────────────────────────
  if (loading) {
    return <Loader message="Loading your orders…" />;
  }

  return (
    <div className="orders-page-content">
      <h1>
        <FaBox style={{ color: 'var(--pink)', marginRight: '10px' }} />
        My Orders
      </h1>

      <div id="orders-container">
        {error ? (
          <div className="state-msg error">
            <FaExclamationCircle />
            <div>{error}</div>
          </div>
        ) : orders.length === 0 ? (
          <div className="state-msg">
            <FaBoxOpen />
            You have no orders yet.
            <br />
            <Link className="continue-link" to="/">
              <FaArrowLeft /> &nbsp; Start Shopping
            </Link>
          </div>
        ) : (
          orders.map((order) => {
            const s =
              STATUS_STYLE[order.status] || {
                bg: '#f5f5f5',
                color: '#666',
                label: order.status,
              };
            const date = formatDate(order.orderedAt);
            const items = order.items || [];
            const canCancel =
              order.status === 'PLACED' || order.status === 'CONFIRMED';

            return (
              <div className="order-card" key={order.id}>
                {/* Header */}
                <div className="order-card-header">
                  <span className="order-id">Order #{order.id}</span>
                  <div className="order-meta">
                    <span>
                      <FaCalendarAlt
                        style={{ fontSize: '0.8rem', color: 'var(--pink)' }}
                      />{' '}
                      {date}
                    </span>
                    <span>
                      <FaMapMarkerAlt
                        style={{ fontSize: '0.8rem', color: 'var(--pink)' }}
                      />{' '}
                      {order.pincode}
                    </span>
                    <span
                      className="status-badge"
                      style={{ background: s.bg, color: s.color }}
                    >
                      {s.label}
                    </span>
                  </div>
                </div>

                {/* Product rows */}
                <div className="order-products">
                  {items.map((item, idx) => {
                    const p = item.product;
                    const imgUrl = p ? resolveImageUrl(p.imageUrl) : '';
                    const name = p ? p.name : 'Product';
                    const price =
                      item.priceAtPurchase || (p ? p.price : 0);
                    const qty = item.quantity || 1;
                    const lineTotal = item.lineTotal || price * qty;

                    return (
                      <div
                        className="order-product-row"
                        key={item.id || idx}
                        onClick={() =>
                          p && navigate(`/product/${p.id}`)
                        }
                        style={{ cursor: p ? 'pointer' : 'default' }}
                      >
                        <img
                          src={imgUrl}
                          alt={name}
                          onError={(e) => {
                            e.target.src = logo;
                          }}
                        />
                        <div className="order-product-info">
                          <div className="prod-name">{name}</div>
                          <div className="prod-meta">
                            <span className="prod-price">
                              {formatPrice(price)}
                            </span>{' '}
                            × {qty}
                          </div>
                        </div>
                        <div className="order-product-subtotal">
                          {formatPrice(lineTotal)}
                        </div>
                      </div>
                    );
                  })}
                </div>

                {/* Footer */}
                <div className="order-card-footer">
                  <div className="order-address">
                    <strong>
                      <FaTruck /> Delivery Address
                    </strong>
                    <br />
                    {order.address}
                    <br />
                    PIN: {order.pincode}
                  </div>
                  <div className="order-total-box">
                    <div className="label">Order Total</div>
                    <div className="amount">
                      {formatPrice(order.totalAmount)}
                    </div>
                    {canCancel && (
                      <button
                        className="cancel-order-btn"
                        disabled={cancellingId === order.id}
                        onClick={() => handleCancelOrder(order.id)}
                      >
                        {cancellingId === order.id ? (
                          'Cancelling...'
                        ) : (
                          <>
                            <FaTimes /> Cancel Order
                          </>
                        )}
                      </button>
                    )}
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};

export default Orders;
