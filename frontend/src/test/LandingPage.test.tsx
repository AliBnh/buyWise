import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import { LandingPage } from '../pages/LandingPage';
import { AuthProvider } from '../context/AuthContext';
import { CurrencyProvider } from '../context/CurrencyContext';

describe('LandingPage Component', () => {
  it('should render the brand header and main value proposition', () => {
    render(
      <AuthProvider>
        <CurrencyProvider>
          <BrowserRouter>
            <LandingPage />
          </BrowserRouter>
        </CurrencyProvider>
      </AuthProvider>
    );

    expect(screen.getAllByText(/BuyWise/i)[0]).toBeInTheDocument();
    expect(screen.getByText(/Buy with confidence/i)).toBeInTheDocument();
    expect(screen.getByText(/Start Comparing Offers/i)).toBeInTheDocument();
  });
});
