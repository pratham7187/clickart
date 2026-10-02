import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { productAPI, reviewAPI, cartAPI, formatPrice, resolveImageUrl } from '../services/api';
import { FaShoppingBag } from 'react-icons/fa';
import Loader from '../components/Loader';
import logo from '../assets/images/logo.png';

// Category images
import menImg from '../assets/images/men.jpg';
import womenImg from '../assets/images/women.jpg';
import kidImg from '../assets/images/kid.jpg';

// Carousel images
import img1 from '../assets/images/image1.png';
import img2 from '../assets/images/image2.png';
import img3 from '../assets/images/image3.png';
import img4 from '../assets/images/image4.png';

const carouselImages = [img1, img2, img3, img4];

const categories = [
  { name: 'Men',   image: menImg,   to: '/men' },
  { name: 'Women', image: womenImg, to: '/women' },
  { name: 'Kids',  image: kidImg,   to: '/kids' },
];

const Home = () => {
  const navigate = useNavigate();
  const [featured, setFeatured] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadFeatured = async () => {
      try {
        const res = await productAPI.getAll(0, 80);
        let products = Array.isArray(res.data)
          ? res.data
          : (res.data && res.data.content) || [];

        // Shuffle and pick 6
        products = products.sort(() => Math.random() - 0.5).slice(0, 6);

        // Fetch real ratings
        const withRatings = await Promise.all(
          products.map(async (p) => {
            let rating = 0, reviews = 0;
            try {
              const s = await reviewAPI.getSummary(p.id);
              rating  = s.data?.averageRating || 0;
              reviews = s.data?.reviewCount   || 0;
            } catch { /* ignore */ }
            return { ...p, rating, reviews };
          })
        );
        setFeatured(withRatings);
      } catch (err) {
        // Silently fail — empty featured grid shown
      } finally {
        setLoading(false);
      }
    };
    loadFeatured();
  }, []);

  const addToCart = async (e, productId) => {
    e.preventDefault();
    e.stopPropagation();
    try {
      await cartAPI.add(productId, 1);
      alert('Added to cart!');
    } catch (err) {
      alert('Could not add to cart: ' + err.message);
    }
  };

  return (
    <>
      {/* ── Hero Banner ─────────────────────────────────────────── */}
      <section className="hero-banner">
        <div className="hero-content">
          <span className="hero-label">✨ New Arrivals 2025</span>
          <h1 className="hero-title">Summer Collection<br />Is Here</h1>
          <p className="hero-subtitle">
            Discover the latest trends in fashion — curated just for you.
            From casual wear to ethnic elegance, shop it all on ClickKart.
          </p>
          <a href="#categories" className="hero-btn">
            <FaShoppingBag /> Shop Now
          </a>
        </div>
      </section>

      {/* ── Trending Carousel ───────────────────────────────────── */}
      <section className="trending-section">
        <h2 className="trending-heading">Trending Now</h2>
        <div className="carousel-wrapper">
          <div className="carousel-track">
            {carouselImages.map((src, i) => (
              <div className="carousel-item" key={i}>
                <img src={src} alt={`Trending ${i + 1}`} />
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ── Featured Products ───────────────────────────────────── */}
      <section className="featured-section">
        <h2>Featured Products</h2>
        <p className="featured-subtitle">Handpicked styles loved by our customers</p>
        <div className="featured-grid" id="featured-grid">
          {loading ? (
            <div style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '40px', color: '#888' }}>
              Loading featured products...
            </div>
          ) : featured.length === 0 ? (
            <div style={{ gridColumn: '1 / -1', textAlign: 'center', padding: '40px', color: '#888' }}>
              No products available.
            </div>
          ) : (
            featured.map((p) => (
              <div
                className="featured-card"
                key={p.id}
                onClick={() => navigate(`/product/${p.id}`)}
                style={{ cursor: 'pointer' }}
              >
                <img
                  src={resolveImageUrl(p.imageUrl)}
                  alt={p.name}
                  onError={(e) => { e.target.src = logo; }}
                />
                <div className="featured-card-body">
                  <div className="product-name">{p.name}</div>
                  <div className="product-price">{formatPrice(p.price)}</div>
                  <div className="rating">
                    {p.reviews > 0 ? (
                      <>
                        <span className="star">⭐</span> {p.rating} ({p.reviews})
                      </>
                    ) : (
                      <span style={{ color: 'var(--txt-muted)' }}>No reviews</span>
                    )}
                  </div>
                </div>
                <button
                  className="add-cart-btn"
                  onClick={(e) => addToCart(e, p.id)}
                >
                  Add to Cart
                </button>
              </div>
            ))
          )}
        </div>
      </section>

      {/* ── Shop by Category ────────────────────────────────────── */}
      <section className="category-menu" id="categories">
        <h2>Shop by Category</h2>
        <div className="categories">
          {categories.map((cat) => (
            <div
              className="category"
              key={cat.name}
              onClick={() => navigate(cat.to)}
              style={{ cursor: 'pointer' }}
            >
              <img src={cat.image} alt={cat.name} />
              <h3>{cat.name}</h3>
            </div>
          ))}
        </div>
      </section>
    </>
  );
};

export default Home;
