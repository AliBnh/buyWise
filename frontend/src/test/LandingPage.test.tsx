import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { BrowserRouter } from 'react-router-dom';
import { LandingPage } from '../pages/LandingPage';
import { AuthProvider } from '../context/AuthContext';
import { CurrencyProvider } from '../context/CurrencyContext';

/**
 * Frontend Component Test Suite for {@link LandingPage}.
 * 
 * Demonstrates:
 * - React Testing Library Best Practices: Tests user-visible behavior (text content, accessibility queries)
 *   rather than implementation details or component state.
 * - Context & Router Mock Wrapping: Emulates providers (Auth, Currency, Routing) required by tree dependencies.
 */
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
