import React, { createContext, useContext, useState, useEffect } from 'react';
import { useAuth } from './AuthContext';

export type CurrencyCode = 'MAD' | 'USD' | 'EUR';

interface CurrencyContextType {
  currentCurrency: CurrencyCode;
  setCurrency: (code: CurrencyCode) => void;
  formatPrice: (amount: number, sourceCurrency?: string) => string;
}

const CurrencyContext = createContext<CurrencyContextType | undefined>(undefined);

// Static conversion factors relative to MAD (1 MAD base)
const EXCHANGE_RATES: Record<CurrencyCode, number> = {
  MAD: 1.0,
  EUR: 0.0926, // 1 EUR = ~10.8 MAD
  USD: 0.1000, // 1 USD = ~10.0 MAD
};

export const CurrencyProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user } = useAuth();
  const [currentCurrency, setCurrentCurrency] = useState<CurrencyCode>('MAD');

  useEffect(() => {
    if (user?.preferredCurrency && ['MAD', 'USD', 'EUR'].includes(user.preferredCurrency)) {
      setCurrentCurrency(user.preferredCurrency as CurrencyCode);
    }
  }, [user]);

  const setCurrency = (code: CurrencyCode) => {
    setCurrentCurrency(code);
    localStorage.setItem('buywise_currency', code);
  };

  const formatPrice = (amount: number, sourceCurrency = 'MAD'): string => {
    if (amount === undefined || amount === null || isNaN(amount)) {
      return `0.00 ${currentCurrency}`;
    }

    // Convert source to MAD first if needed
    let amountInMad = amount;
    if (sourceCurrency === 'EUR') {
      amountInMad = amount / EXCHANGE_RATES.EUR;
    } else if (sourceCurrency === 'USD') {
      amountInMad = amount / EXCHANGE_RATES.USD;
    }

    // Convert MAD to current target currency
    const converted = amountInMad * EXCHANGE_RATES[currentCurrency];

    const formatted = new Intl.NumberFormat('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(converted);

    switch (currentCurrency) {
      case 'MAD':
        return `${formatted} MAD`;
      case 'EUR':
        return `€${formatted}`;
      case 'USD':
        return `$${formatted}`;
      default:
        return `${formatted} ${currentCurrency}`;
    }
  };

  return (
    <CurrencyContext.Provider value={{ currentCurrency, setCurrency, formatPrice }}>
      {children}
    </CurrencyContext.Provider>
  );
};

export const useCurrency = () => {
  const context = useContext(CurrencyContext);
  if (!context) {
    throw new Error('useCurrency must be used within a CurrencyProvider');
  }
  return context;
};
