import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { productAPI, cartAPI, wishlistAPI, formatPrice, resolveImageUrl } from '../services/api';
import { FaHeart, FaShoppingCart } from 'react-icons/fa';
import Loader from '../components/Loader';
import ReviewSection from '../components/ReviewSection';
import logo from '../assets/images/logo.png';

const ProductDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [quantity, setQuantity] = useState(1);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const res = await productAPI.getById(id);
        setProduct(res.data);
      } catch (err) {
        setError('Product not found: ' + err.message);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [id]);

  const handleAddToCart = async () => {
    try {
      await cartAPI.add(product.id, quantity);
      alert('Added to cart!');
    } catch (err) {
      alert('Could not add to cart: ' + err.message);
    }
  };

  const handleBuyNow = async () => {
    try {
      await cartAPI.add(product.id, quantity);
      navigate('/cart');
    } catch (err) {
      alert('Could not add to cart: ' + err.message);
    }
  };

  const handleWishlist = async () => {
    try {
      await wishlistAPI.add(product.id);
      alert('Added to wishlist!');
    } catch (err) {
      alert(err.message || 'Could not add to wishlist');
    }
  };

  if (loading) return <Loader message="Loading product..." />;

  if (error) {
    return (
      <div className="state-msg error" role="alert">
        <div>{error}</div>
        <button className="continue-link" onClick={() => navigate('/')} aria-label="Go back to home">
          ← Back to Home
        </button>
      </div>
    );
  }

  if (!product) return null;

  const inStock = product.stock > 0;

  return (
    <section className="product-details">
      <div className="product-container" style={{ flexDirection: 'column', alignItems: 'center', gap: 0 }}>
        <div className="product-image" style={{ maxWidth: '420px', width: '100%' }}>
          <img
            id="product-img"
            src={resolveImageUrl(product.imageUrl)}
            alt={product.name}
            onError={(e) => { e.target.src = logo; }}
          />
        </div>

        <div className="product-info" style={{ width: '100%', maxWidth: '700px' }}>
          <h2 id="product-name">{product.name}</h2>
          <p id="product-price">{formatPrice(product.price)}</p>

          <p
            id="product-stock"
            style={{
              fontSize: '13px',
              fontWeight: 600,
              color: inStock ? 'var(--success)' : 'var(--error)',
            }}
          >
            {inStock ? `✔ In Stock (${product.stock} left)` : '✖ Out of Stock'}
          </p>

          <p id="product-description">{product.description}</p>

          <div className="product-actions">
            <button
              className="buy-btn"
              style={{ background: '#ff9800' }}
              onClick={handleAddToCart}
              disabled={!inStock}
              aria-label="Add to cart"
            >
              <FaShoppingCart /> Add to Cart
            </button>
            <button
              className="buy-btn"
              onClick={handleBuyNow}
              disabled={!inStock}
              aria-label="Buy now"
            >
              Buy Now
            </button>
            <button
              className="wishlist-btn"
              onClick={handleWishlist}
              aria-label="Add to wishlist"
            >
              <FaHeart /> Wishlist
            </button>
          </div>

          <div className="product-quantity">
            <label htmlFor="quantity">Quantity:</label>
            <input
              type="number"
              id="quantity"
              value={quantity}
              min="1"
              max={Math.min(10, product.stock)}
              onChange={(e) => setQuantity(Math.max(1, Number(e.target.value)))}
              aria-label="Product quantity"
            />
          </div>

          {/* ── Reviews & Ratings Section ─────────────────────────────── */}
          <ReviewSection productId={Number(id)} />
        </div>
      </div>
    </section>
  );
};

export default ProductDetail;
