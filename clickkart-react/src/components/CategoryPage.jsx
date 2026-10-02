import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { productAPI, formatPrice, resolveImageUrl } from '../services/api';
import Loader from '../components/Loader';
import logo from '../assets/images/logo.png';

/**
 * CategoryPage — reusable product listing used by Men, Women, Kids pages.
 *
 * @param {number}   categoryId     — 1=Men, 2=Women, 3=Kids
 * @param {string}   title          — e.g. "Men's Fashion"
 * @param {string}   gridTitle      — e.g. "All Men's Products"
 * @param {Array}    subcategories  — [{id, image, label, filter}]
 */
const CategoryPage = ({ categoryId, title, gridTitle, subcategories = [] }) => {
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeFilter, setActiveFilter] = useState(null);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const res = await productAPI.getByCategory(categoryId);
        setProducts(res.data || []);
      } catch (err) {
        setError('Failed to load products: ' + err.message);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [categoryId]);

  const filtered = activeFilter
    ? products.filter(
        (p) =>
          (p.subcategory || '').toLowerCase() === activeFilter.toLowerCase()
      )
    : products;

  if (loading) return <Loader message={`Loading ${title}...`} />;

  return (
    <>
      {/* Page Header */}
      <div className="page-header">
        <h1>{title}</h1>
      </div>

      {/* Sub-category filter */}
      {subcategories.length > 0 && (
        <section className="shop-category">
          <h2>Shop by Category</h2>
          <div className="category-row">
            {subcategories.map((cat) => (
              <div
                className="category-block"
                key={cat.filter}
                onClick={() =>
                  setActiveFilter(
                    activeFilter === cat.filter ? null : cat.filter
                  )
                }
                style={{
                  opacity: activeFilter && activeFilter !== cat.filter ? 0.5 : 1,
                  cursor: 'pointer',
                }}
              >
                <img src={cat.image} alt={cat.label} />
                <p>{cat.label}</p>
              </div>
            ))}
          </div>
          {activeFilter && (
            <p
              style={{
                textAlign: 'center',
                marginTop: '16px',
                fontSize: '0.82rem',
                color: 'var(--pink)',
                cursor: 'pointer',
              }}
              onClick={() => setActiveFilter(null)}
            >
              ✕ Clear filter — Show all
            </p>
          )}
        </section>
      )}

      {/* Product Grid */}
      <section className="product-section">
        <h3>{gridTitle}</h3>
        {error ? (
          <p style={{ padding: '20px', color: '#e53935' }}>{error}</p>
        ) : filtered.length === 0 ? (
          <p style={{ padding: '20px', color: '#888' }}>No products found.</p>
        ) : (
          <div className="product-grid" id="product-grid">
            {filtered.map((p) => (
              <div
                className={`product-item ${(p.subcategory || '').toLowerCase()}`}
                key={p.id}
                onClick={() => navigate(`/product/${p.id}`)}
                style={{ cursor: 'pointer' }}
              >
                <img
                  src={resolveImageUrl(p.imageUrl)}
                  alt={p.name}
                  onError={(e) => { e.target.src = logo; }}
                />
                <div className="product-item-body">
                  <div className="product-name">{p.name}</div>
                  <div className="product-price">{formatPrice(p.price)}</div>
                  <span className={`stock-badge ${p.stock > 0 ? 'in' : 'out'}`}>
                    {p.stock > 0 ? 'In Stock' : 'Out of Stock'}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>
    </>
  );
};

export default CategoryPage;
