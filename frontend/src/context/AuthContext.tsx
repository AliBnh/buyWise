import React, { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../api/client';
import { User, AuthResponse } from '../types';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (fullName: string, email: string, password: string, preferredCurrency?: string) => Promise<void>;
  demoLogin: () => Promise<void>;
  logout: () => void;
  updateUserCurrency: (currency: string) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const storedToken = localStorage.getItem('buywise_token');
    const storedUser = localStorage.getItem('buywise_user');

    if (storedToken && storedUser) {
      try {
        setToken(storedToken);
        setUser(JSON.parse(storedUser));
      } catch (e) {
        localStorage.removeItem('buywise_token');
        localStorage.removeItem('buywise_user');
      }
    }
    setIsLoading(false);

    const handleUnauthorized = () => {
      setUser(null);
      setToken(null);
    };

    window.addEventListener('buywise:unauthorized', handleUnauthorized);
    return () => window.removeEventListener('buywise:unauthorized', handleUnauthorized);
  }, []);

  const handleAuthSuccess = (data: AuthResponse) => {
    const userData: User = {
      id: data.id,
      email: data.email,
      fullName: data.fullName,
      role: data.role as any,
      preferredCurrency: data.preferredCurrency,
    };

    localStorage.setItem('buywise_token', data.token);
    localStorage.setItem('buywise_user', JSON.stringify(userData));
    setToken(data.token);
    setUser(userData);
  };

  const login = async (email: string, password: string) => {
    const data = await api.post<AuthResponse>('/auth/login', { email, password });
    handleAuthSuccess(data);
  };

  const register = async (fullName: string, email: string, password: string, preferredCurrency = 'MAD') => {
    const data = await api.post<AuthResponse>('/auth/register', {
      fullName,
      email,
      password,
      preferredCurrency,
    });
    handleAuthSuccess(data);
  };

  const demoLogin = async () => {
    await login('demo@buywise.com', 'Demo123!');
  };

  const logout = () => {
    localStorage.removeItem('buywise_token');
    localStorage.removeItem('buywise_user');
    setUser(null);
    setToken(null);
  };

  const updateUserCurrency = (currency: string) => {
    if (user) {
      const updated = { ...user, preferredCurrency: currency };
      setUser(updated);
      localStorage.setItem('buywise_user', JSON.stringify(updated));
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        isLoading,
        login,
        register,
        demoLogin,
        logout,
        updateUserCurrency,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
