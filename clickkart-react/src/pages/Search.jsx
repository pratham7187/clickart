import { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { productAPI, formatPrice, resolveImageUrl } from '../services/api';
import { FaSearch } from 'react-icons/fa';
import Loader from '../components/Loader';
import logo from '../assets/images/logo.png';

const Search = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const query = searchParams.get('q') || '';

  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [searched, setSearched] = useState(false);

  useEffect(() => {
    if (!query.trim()) {
      setResults([]);
      setSearched(false);
      return;
    }

    const doSearch = async () => {
      setLoading(true);
      setError('');
      try {
        const res = await productAPI.search(query);
        const data = res.data;
        const products = Array.isArray(data)
          ? data
          : (data && data.content) || [];
        setResults(products);
      } catch (err) {
        setError('Search failed: ' + err.message);
      } finally {
        setLoading(false);
        setSearched(true);
      }
    };
    doSearch();
  }, [query]);

  if (loading) return <Loader message={`Searching for "${query}"...`} />;

  return (
    <div className="product-section" style={{ minHeight: '60vh' }}>
      <h3>
        <FaSearch style={{ color: 'var(--pink)', marginRight: '8px' }} />
        {searched
          ? `Search results for "${query}" (${results.length})`
          : 'Search Products'}
      </h3>

      {error ? (
        <p style={{ padding: '20px', color: '#e53935' }}>{error}</p>
      ) : searched && results.length === 0 ? (
        <div className="state-msg">
          <FaSearch />
          <div>No products found for "{query}"</div>
          <span className="continue-link" onClick={() => navigate('/')}>
            ← Continue Shopping
          </span>
        </div>
      ) : results.length > 0 ? (
        <div className="product-grid">
          {results.map((p) => (
            <div
              className="product-item"
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
      ) : null}
    </div>
  );
};

export default Search;
