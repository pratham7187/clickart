import { useNavigate } from 'react-router-dom';
import { FaSearch, FaShoppingCart } from 'react-icons/fa';
import { useState, useEffect, useRef, useCallback } from 'react';
import { productAPI, formatPrice, resolveImageUrl } from '../services/api';
import logo from '../assets/images/logo.png';

const Navbar = () => {
  const navigate = useNavigate();
  const [query, setQuery] = useState('');
  const [suggestions, setSuggestions] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const [loading, setLoading] = useState(false);
  const [activeIdx, setActiveIdx] = useState(-1);
  const wrapperRef = useRef(null);
  const debounceRef = useRef(null);

  // ── Debounced search ──────────────────────────────────────────────────
  const doSearch = useCallback(async (term) => {
    if (!term || term.length < 2) {
      setSuggestions([]);
      setShowDropdown(false);
      return;
    }
    setLoading(true);
    try {
      const res = await productAPI.search(term);
      const products = Array.isArray(res.data)
        ? res.data
        : (res?.data?.content || res?.content || []);
      // If the API response was already unwrapped by the interceptor
      const list = Array.isArray(res) ? res : products;
      const finalList = (Array.isArray(list) ? list : []).slice(0, 8);
      setSuggestions(finalList);
      setShowDropdown(finalList.length > 0);
    } catch {
      setSuggestions([]);
      setShowDropdown(false);
    } finally {
      setLoading(false);
    }
  }, []);

  const handleInputChange = (e) => {
    const val = e.target.value;
    setQuery(val);
    setActiveIdx(-1);

    if (debounceRef.current) clearTimeout(debounceRef.current);

    debounceRef.current = setTimeout(() => {
      doSearch(val.trim());
    }, 250);
  };

  // ── Keyboard navigation ───────────────────────────────────────────────
  const handleKeyDown = (e) => {
    if (!showDropdown) return;

    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setActiveIdx((prev) => (prev < suggestions.length - 1 ? prev + 1 : 0));
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setActiveIdx((prev) => (prev > 0 ? prev - 1 : suggestions.length - 1));
    } else if (e.key === 'Enter' && activeIdx >= 0) {
      e.preventDefault();
      goToProduct(suggestions[activeIdx].id);
    } else if (e.key === 'Escape') {
      setShowDropdown(false);
    }
  };

  // ── Navigate to product ───────────────────────────────────────────────
  const goToProduct = (id) => {
    setShowDropdown(false);
    setQuery('');
    setSuggestions([]);
    navigate(`/product/${id}`);
  };

  // ── Submit form (fallback for Enter with no selection) ────────────────
  const handleSubmit = (e) => {
    e.preventDefault();
    const q = query.trim();
    if (q) {
      setShowDropdown(false);
      navigate(`/search?q=${encodeURIComponent(q)}`);
    }
  };

  // ── Close dropdown on outside click ───────────────────────────────────
  useEffect(() => {
    const handler = (e) => {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target)) {
        setShowDropdown(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  // ── Cleanup debounce timer ────────────────────────────────────────────
  useEffect(() => {
    return () => {
      if (debounceRef.current) clearTimeout(debounceRef.current);
    };
  }, []);

  return (
    <header>
      <div className="navBar">
        <div className="logo" onClick={() => navigate('/')} style={{ cursor: 'pointer' }}>
          <img src={logo} className="clickkart-logo" alt="ClickKart Logo" />
        </div>

        <div className="search-autocomplete" ref={wrapperRef}>
          <form className="search__form" role="search" onSubmit={handleSubmit}>
            <input
              className="input"
              type="search"
              placeholder="Search for products, categories and more"
              aria-label="Search products"
              autoComplete="off"
              value={query}
              onChange={handleInputChange}
              onKeyDown={handleKeyDown}
              onFocus={() => suggestions.length > 0 && setShowDropdown(true)}
            />
            <button className="search__icon" type="submit" aria-label="Search">
              <FaSearch />
            </button>
          </form>

          {/* ── Suggestions dropdown ─────────────────────────────────── */}
          {showDropdown && (
            <div className="search-suggestions" role="listbox" aria-label="Search suggestions">
              {loading ? (
                <div className="search-suggestions__status">Searching…</div>
              ) : suggestions.length === 0 ? (
                <div className="search-suggestions__status">No results found</div>
              ) : (
                suggestions.map((p, idx) => (
                  <div
                    key={p.id}
                    className={`search-suggestion ${idx === activeIdx ? 'is-active' : ''}`}
                    role="option"
                    aria-selected={idx === activeIdx}
                    onClick={() => goToProduct(p.id)}
                    onMouseEnter={() => setActiveIdx(idx)}
                  >
                    <img
                      src={resolveImageUrl(p.imageUrl)}
                      alt={p.name}
                      onError={(e) => { e.target.src = logo; }}
                    />
                    <div className="search-suggestion__details">
                      <span className="search-suggestion__name">{p.name}</span>
                      <span className="search-suggestion__price">{formatPrice(p.price)}</span>
                    </div>
                  </div>
                ))
              )}
            </div>
          )}
        </div>

        <div className="nav-buttons">
          <button className="login-btn" onClick={() => navigate('/cart')}>
            <FaShoppingCart /> Cart
          </button>
        </div>
      </div>
    </header>
  );
};

export default Navbar;
