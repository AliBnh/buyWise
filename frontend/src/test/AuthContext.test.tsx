import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen, act } from '@testing-library/react';
import React from 'react';
import { AuthProvider, useAuth } from '../context/AuthContext';

const TestComponent = () => {
  const { user, token, isAuthenticated, demoLogin, logout } = useAuth();
  return (
    <div>
      <span data-testid="auth-status">{isAuthenticated ? 'authenticated' : 'unauthenticated'}</span>
      <span data-testid="user-email">{user ? user.email : 'no-user'}</span>
      <span data-testid="token-status">{token ? 'has-token' : 'no-token'}</span>
      <button onClick={() => demoLogin()} data-testid="btn-demo">Demo</button>
      <button onClick={() => logout()} data-testid="btn-logout">Logout</button>
    </div>
  );
};

describe('AuthContext', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('initializes as unauthenticated when localStorage is empty', () => {
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    expect(screen.getByTestId('auth-status').textContent).toBe('unauthenticated');
    expect(screen.getByTestId('user-email').textContent).toBe('no-user');
    expect(screen.getByTestId('token-status').textContent).toBe('no-token');
  });

  it('logs out and clears authentication state', async () => {
    localStorage.setItem('buywise_token', 'sample-jwt-token');
    localStorage.setItem(
      'buywise_user',
      JSON.stringify({ id: '1', email: 'user@test.com', fullName: 'Test User', role: 'ROLE_USER' })
    );

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    expect(screen.getByTestId('auth-status').textContent).toBe('authenticated');
    expect(screen.getByTestId('user-email').textContent).toBe('user@test.com');

    await act(async () => {
      screen.getByTestId('btn-logout').click();
    });

    expect(screen.getByTestId('auth-status').textContent).toBe('unauthenticated');
    expect(localStorage.getItem('buywise_token')).toBeNull();
  });
});
