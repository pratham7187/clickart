import { NavLink, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  FaHome,
  FaShoppingCart,
  FaHeart,
  FaUser,
  FaBox,
  FaSignOutAlt,
} from 'react-icons/fa';

const navItems = [
  { to: '/',         icon: FaHome,         label: 'Home' },
  { to: '/cart',     icon: FaShoppingCart,  label: 'Cart' },
  { to: '/wishlist', icon: FaHeart,        label: 'Wishlist' },
  { to: '/profile',  icon: FaUser,         label: 'Profile' },
  { to: '/orders',   icon: FaBox,          label: 'Orders' },
];

const Sidebar = () => {
  const { logout } = useAuth();
  const location = useLocation();

  return (
    <div className="menu">
      <ul className="menu-content">
        {navItems.map((item) => {
          const isActive = item.to === '/'
            ? location.pathname === '/'
            : location.pathname.startsWith(item.to);

          return (
            <li key={item.to} className={isActive ? 'active' : ''}>
              <NavLink to={item.to}>
                <span><item.icon /></span>
                <span>{item.label}</span>
              </NavLink>
            </li>
          );
        })}
        <li>
          <button
            className="sidebar-logout-btn"
            onClick={logout}
            aria-label="Log out"
          >
            <span><FaSignOutAlt /></span>
            <span>Logout</span>
          </button>
        </li>
      </ul>
    </div>
  );
};

export default Sidebar;
