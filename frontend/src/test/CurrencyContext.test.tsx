import React from 'react';
import { renderHook, act } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { CurrencyProvider, useCurrency } from '../context/CurrencyContext';
import { AuthProvider } from '../context/AuthContext';

const wrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <AuthProvider>
    <CurrencyProvider>{children}</CurrencyProvider>
  </AuthProvider>
);

describe('CurrencyContext Formatting', () => {
  it('should default to Moroccan Dirham (MAD) and format amounts correctly', () => {
    const { result } = renderHook(() => useCurrency(), { wrapper });

    expect(result.current.currentCurrency).toBe('MAD');
    expect(result.current.formatPrice(8499)).toBe('8,499.00 MAD');
  });

  it('should convert and format to EUR currency', () => {
    const { result } = renderHook(() => useCurrency(), { wrapper });

    act(() => {
      result.current.setCurrency('EUR');
    });

    expect(result.current.currentCurrency).toBe('EUR');
    // 1080 MAD / ~10.8 = ~100 EUR
    const formatted = result.current.formatPrice(1080);
    expect(formatted).toContain('€');
  });

  it('should convert and format to USD currency', () => {
    const { result } = renderHook(() => useCurrency(), { wrapper });

    act(() => {
      result.current.setCurrency('USD');
    });

    expect(result.current.currentCurrency).toBe('USD');
    // 1000 MAD / 10 = $100
    const formatted = result.current.formatPrice(1000);
    expect(formatted).toBe('$100.00');
  });
});
