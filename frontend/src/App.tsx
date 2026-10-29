import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { CurrencyProvider } from './context/CurrencyContext';
import { Layout } from './components/layout/Layout';

import { LandingPage } from './pages/LandingPage';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { DashboardPage } from './pages/DashboardPage';
import { ComparisonsListPage } from './pages/ComparisonsListPage';
import { NewComparisonPage } from './pages/NewComparisonPage';
import { ComparisonDetailsPage } from './pages/ComparisonDetailsPage';
import { PurchaseLibraryPage } from './pages/PurchaseLibraryPage';
import { DocumentLibraryPage } from './pages/DocumentLibraryPage';
import { DocumentVerificationPage } from './pages/DocumentVerificationPage';
import { DeadlinesPage } from './pages/DeadlinesPage';
import { AiAssistantPage } from './pages/AiAssistantPage';
import { SettingsPage } from './pages/SettingsPage';

// Protected Route Guard
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center">
        <div className="w-8 h-8 border-4 border-slate-900 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <CurrencyProvider>
        <BrowserRouter>
          <Routes>
            {/* Public Routes */}
            <Route path="/" element={<LandingPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            {/* Authenticated Dashboard App */}
            <Route
              element={
                <ProtectedRoute>
                  <Layout />
                </ProtectedRoute>
              }
            >
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/comparisons" element={<ComparisonsListPage />} />
              <Route path="/comparisons/new" element={<NewComparisonPage />} />
              <Route path="/comparisons/:id" element={<ComparisonDetailsPage />} />
              <Route path="/purchases" element={<PurchaseLibraryPage />} />
              <Route path="/documents" element={<DocumentLibraryPage />} />
              <Route path="/documents/:id/verify" element={<DocumentVerificationPage />} />
              <Route path="/deadlines" element={<DeadlinesPage />} />
              <Route path="/assistant" element={<AiAssistantPage />} />
              <Route path="/settings" element={<SettingsPage />} />
            </Route>

            {/* Fallback */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </BrowserRouter>
      </CurrencyProvider>
    </AuthProvider>
  );
};

export default App;
