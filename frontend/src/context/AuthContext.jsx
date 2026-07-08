import { createContext, useState, useCallback, useEffect } from 'react';
import api from '../services/api';

export const AuthContext = createContext(null);

const USER_KEY = 'sahasathi_user';
const TOKEN_KEY = 'sahasathi_token';

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem(USER_KEY);
    return stored ? JSON.parse(stored) : null;
  });
  const [loading, setLoading] = useState(false);

  const token = localStorage.getItem(TOKEN_KEY);

  useEffect(() => {
    if (token) {
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    }
  }, [token]);

  const login = useCallback(async (idToken) => {
    setLoading(true);
    try {
      const res = await api.post('/auth/register', { idToken });
      const userData = res.data.data;
      localStorage.setItem(TOKEN_KEY, idToken);
      localStorage.setItem(USER_KEY, JSON.stringify(userData));
      api.defaults.headers.common['Authorization'] = `Bearer ${idToken}`;
      setUser(userData);
      return userData;
    } finally {
      setLoading(false);
    }
  }, []);

  const updateUser = useCallback((userData) => {
    localStorage.setItem(USER_KEY, JSON.stringify(userData));
    setUser(userData);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    delete api.defaults.headers.common['Authorization'];
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, token, loading, login, logout, updateUser }}>
      {children}
    </AuthContext.Provider>
  );
}
