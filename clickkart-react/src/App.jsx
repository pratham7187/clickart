import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/Layout';

// ── Global Styles ────────────────────────────────────────────────────────────
import './styles/global.css';
import './styles/pages.css';

// ── Pages ────────────────────────────────────────────────────────────────────
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Men from './pages/Men';
import Women from './pages/Women';
import Kids from './pages/Kids';
import ProductDetail from './pages/ProductDetail';
import Cart from './pages/Cart';
import Wishlist from './pages/Wishlist';
import Orders from './pages/Orders';
import Profile from './pages/Profile';
import Search from './pages/Search';
import NotFound from './pages/NotFound';

// ── Helper: wrap a page in ProtectedRoute + Layout ───────────────────────────
const Protected = ({ children }) => (
  <ProtectedRoute>
    <Layout>{children}</Layout>
  </ProtectedRoute>
);

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* ── Public Routes (no layout) ───────────────────────────── */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* ── Protected Routes (with layout) ─────────────────────── */}
          <Route path="/" element={<Protected><Home /></Protected>} />
          <Route path="/men" element={<Protected><Men /></Protected>} />
          <Route path="/women" element={<Protected><Women /></Protected>} />
          <Route path="/kids" element={<Protected><Kids /></Protected>} />
          <Route path="/product/:id" element={<Protected><ProductDetail /></Protected>} />
          <Route path="/cart" element={<Protected><Cart /></Protected>} />
          <Route path="/wishlist" element={<Protected><Wishlist /></Protected>} />
          <Route path="/orders" element={<Protected><Orders /></Protected>} />
          <Route path="/profile" element={<Protected><Profile /></Protected>} />
          <Route path="/search" element={<Protected><Search /></Protected>} />

          {/* ── 404 Catch-all ───────────────────────────────────── */}
          <Route path="*" element={<Layout><NotFound /></Layout>} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
