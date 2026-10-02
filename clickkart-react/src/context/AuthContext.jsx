import { createContext, useContext, useState, useCallback, useMemo } from 'react';
import { authAPI } from '../services/api';

// ── Context ──────────────────────────────────────────────────────────────────
const AuthContext = createContext(null);

// ── Helper: read stored user from localStorage ──────────────────────────────
const getStoredUser = () => {
  try {
    return JSON.parse(localStorage.getItem('ck_user') || 'null');
  } catch {
    return null;
  }
};

const getStoredToken = () => localStorage.getItem('ck_token');

// ── Provider ─────────────────────────────────────────────────────────────────
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(getStoredUser);
  const [token, setToken] = useState(getStoredToken);

  // ── Save session to state + localStorage ─────────────────────────────────
  const saveSession = useCallback((data) => {
    const userData = {
      userId: data.userId,
      name: data.name,
      email: data.email,
      role: data.role,
    };
    localStorage.setItem('ck_token', data.token);
    localStorage.setItem('ck_user', JSON.stringify(userData));
    setToken(data.token);
    setUser(userData);
  }, []);

  // ── Clear session ────────────────────────────────────────────────────────
  const clearSession = useCallback(() => {
    localStorage.removeItem('ck_token');
    localStorage.removeItem('ck_user');
    localStorage.removeItem('ck_productId');
    setToken(null);
    setUser(null);
  }, []);

  // ── Login ────────────────────────────────────────────────────────────────
  const login = useCallback(async (email, password) => {
    const res = await authAPI.login(email, password);
    saveSession(res.data);
    return res;
  }, [saveSession]);

  // ── Register ─────────────────────────────────────────────────────────────
  const register = useCallback(async (name, email, password) => {
    const res = await authAPI.register(name, email, password);
    saveSession(res.data);
    return res;
  }, [saveSession]);

  // ── Logout ───────────────────────────────────────────────────────────────
  const logout = useCallback(() => {
    clearSession();
    window.location.href = '/login';
  }, [clearSession]);

  // ── Getters ──────────────────────────────────────────────────────────────
  const getCurrentUser = useCallback(() => user, [user]);
  const isAuthenticated = useCallback(() => !!token, [token]);

  // ── Memoized value ───────────────────────────────────────────────────────
  const value = useMemo(
    () => ({
      user,
      token,
      login,
      register,
      logout,
      getCurrentUser,
      isAuthenticated,
    }),
    [user, token, login, register, logout, getCurrentUser, isAuthenticated]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// ── Hook ─────────────────────────────────────────────────────────────────────
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext;
