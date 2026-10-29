import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import { LoginPage } from '../pages/LoginPage';
import { AuthProvider } from '../context/AuthContext';
import { CurrencyProvider } from '../context/CurrencyContext';

describe('LoginPage Component', () => {
  it('should render email, password input and One-Click Demo Login button', () => {
    render(
      <AuthProvider>
        <CurrencyProvider>
          <BrowserRouter>
            <LoginPage />
          </BrowserRouter>
        </CurrencyProvider>
      </AuthProvider>
    );

    expect(screen.getByPlaceholderText(/demo@buywise.com/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/••••••••/i)).toBeInTheDocument();
    expect(screen.getByText(/One-Click Demo Login \(Karim Bennani\)/i)).toBeInTheDocument();
  });
});
