import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  FaBox,
  FaHeart,
  FaSignOutAlt,
} from 'react-icons/fa';

const Profile = () => {
  const { user, logout } = useAuth();

  if (!user) return null;

  const displayName = user.name || '—';
  const displayEmail = user.email || '—';
  const displayRole = (user.role || 'USER').replace('ROLE_', '');
  const avatarLetter = displayName.charAt(0).toUpperCase();

  return (
    <div className="profile-page-content">
      <div className="profile-card">
        {/* Pink header with avatar */}
        <div className="profile-card-header">
          <div className="profile-avatar" aria-hidden="true">{avatarLetter}</div>
          <div>
            <h2>{displayName}</h2>
            <p>{displayEmail}</p>
          </div>
        </div>

        {/* Fields */}
        <div className="profile-card-body">
          <div className="profile-field">
            <label>Full Name</label>
            <span>{displayName}</span>
          </div>
          <div className="profile-field">
            <label>Email Address</label>
            <span>{displayEmail}</span>
          </div>
          <div className="profile-field">
            <label>Account Role</label>
            <span>{displayRole}</span>
          </div>
          <div className="profile-field">
            <label>Member Since</label>
            <span>ClickKart Family</span>
          </div>
        </div>

        {/* Quick links */}
        <div className="profile-actions">
          <Link to="/orders" className="profile-link-btn" aria-label="View my orders">
            <FaBox /> My Orders
          </Link>
          <Link to="/wishlist" className="profile-link-btn" aria-label="View my wishlist">
            <FaHeart /> Wishlist
          </Link>
          <button
            className="profile-link-btn outline"
            onClick={logout}
            aria-label="Log out of your account"
          >
            <FaSignOutAlt /> Logout
          </button>
        </div>
      </div>
    </div>
  );
};

export default Profile;
