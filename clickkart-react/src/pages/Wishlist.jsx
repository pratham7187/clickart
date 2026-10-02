import { useState, useEffect, useCallback } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { wishlistAPI, cartAPI, formatPrice, resolveImageUrl } from '../services/api';
import Loader from '../components/Loader';
import {
  FaHeart,
  FaHeartBroken,
  FaArrowLeft,
  FaExclamationCircle,
  FaShoppingCart,
} from 'react-icons/fa';
import logo from '../assets/images/logo.png';

const Wishlist = () => {
  const navigate = useNavigate();

  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // ── Load wishlist ──────────────────────────────────────────────────────
  const fetchWishlist = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const res = await wishlistAPI.get();
      setItems(res.data || []);
    } catch (err) {
      setError('Error: ' + err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchWishlist();
  }, [fetchWishlist]);

  // ── Remove from wishlist ───────────────────────────────────────────────
  const handleRemove = async (productId) => {
    if (!window.confirm('Remove from wishlist?')) return;
    try {
      await wishlistAPI.remove(productId);
      fetchWishlist();
    } catch (err) {
      alert('Could not remove: ' + err.message);
    }
  };

  // ── Add to cart from wishlist ──────────────────────────────────────────
  const handleAddToCart = async (productId) => {
    try {
      await cartAPI.add(productId, 1);
      alert('Added to cart!');
    } catch (err) {
      alert('Could not add to cart: ' + err.message);
    }
  };

  // ── Loading state ──────────────────────────────────────────────────────
  if (loading) {
    return <Loader message="Loading your wishlist…" />;
  }

  return (
    <div className="wishlist-page-content">
      <h1>
        <FaHeart style={{ color: 'var(--pink)', marginRight: '10px' }} />
        My Wishlist
      </h1>

      <div className="wishlist-items">
        {error ? (
          <div className="state-msg error" style={{ gridColumn: '1 / -1' }}>
            <FaExclamationCircle />
            {error}
          </div>
        ) : items.length === 0 ? (
          <div className="state-msg" style={{ gridColumn: '1 / -1' }}>
            <FaHeartBroken />
            Your wishlist is empty!
            <br />
            <Link className="continue-link" to="/">
              <FaArrowLeft /> &nbsp; Start Shopping
            </Link>
          </div>
        ) : (
          items.map((item) => {
            const p = item.product;
            return (
              <div className="wishlist-item" key={p.id}>
                <img
                  src={resolveImageUrl(p.imageUrl)}
                  alt={p.name}
                  onError={(e) => { e.target.src = logo; }}
                  onClick={() => navigate(`/product/${p.id}`)}
                  style={{ cursor: 'pointer' }}
                />
                <h3>{p.name}</h3>
                <p>{formatPrice(p.price)}</p>
                <p
                  style={{
                    fontSize: '0.78rem',
                    fontWeight: 600,
                    padding: '0 14px 4px',
                    color: p.stock > 0 ? 'var(--success)' : 'var(--error)',
                  }}
                >
                  {p.stock > 0 ? '✔ In Stock' : '✖ Out of Stock'}
                </p>
                <button
                  className="remove-btn"
                  onClick={() => handleRemove(p.id)}
                >
                  Remove
                </button>
                <button
                  className="buy-btn"
                  onClick={() => navigate(`/product/${p.id}`)}
                >
                  View Product
                </button>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};

export default Wishlist;
