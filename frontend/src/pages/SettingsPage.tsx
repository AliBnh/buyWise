import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useCurrency, CurrencyCode } from '../context/CurrencyContext';
import { api } from '../api/client';
import {
  Settings,
  User,
  Coins,
  RotateCcw,
  ShieldCheck,
  CheckCircle2,
  AlertTriangle,
  Cpu,
} from 'lucide-react';

export const SettingsPage: React.FC = () => {
  const { user, updateUserCurrency } = useAuth();
  const { currentCurrency, setCurrency } = useCurrency();
  const [resetting, setResetting] = useState(false);
  const [resetMessage, setResetMessage] = useState<string | null>(null);

  const handleResetDemo = async () => {
    if (!window.confirm('Reset demo data for demo@buywise.com to the initial state? (This will restore Lenovo comparison and appliance purchases).')) {
      return;
    }

    setResetting(true);
    setResetMessage(null);
    try {
      const res = await api.post<any>('/demo/reset');
      setResetMessage(res.message || 'Demo data reset successfully.');
    } catch (e: any) {
      alert('Reset failed: ' + e.message);
    } finally {
      setResetting(false);
    }
  };

  const handleCurrencyChange = (c: CurrencyCode) => {
    setCurrency(c);
    updateUserCurrency(c);
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      {/* Header */}
      <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
        <h1 className="text-xl sm:text-2xl font-bold text-slate-900 tracking-tight flex items-center gap-2">
          <Settings className="w-6 h-6 text-slate-700" />
          Settings & Environment
        </h1>
        <p className="text-xs text-slate-500 mt-1">
          Manage currency preferences, user profile, and demo sandbox data.
        </p>
      </div>

      {resetMessage && (
        <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-2xl flex items-center gap-2.5 text-xs text-emerald-800">
          <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
          <span>{resetMessage}</span>
        </div>
      )}

      {/* User Profile */}
      <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4">
        <h2 className="text-sm font-bold text-slate-900 pb-2 border-b border-slate-100 flex items-center gap-2">
          <User className="w-4 h-4 text-slate-600" />
          Account Profile
        </h2>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
          <div>
            <span className="text-slate-500 block mb-1">Full Name</span>
            <p className="font-semibold text-slate-900 p-2.5 bg-slate-50 border border-slate-200 rounded-xl">
              {user?.fullName}
            </p>
          </div>

          <div>
            <span className="text-slate-500 block mb-1">Email Address</span>
            <p className="font-semibold text-slate-900 p-2.5 bg-slate-50 border border-slate-200 rounded-xl">
              {user?.email}
            </p>
          </div>
        </div>
      </div>

      {/* Currency Preference */}
      <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4">
        <h2 className="text-sm font-bold text-slate-900 pb-2 border-b border-slate-100 flex items-center gap-2">
          <Coins className="w-4 h-4 text-slate-600" />
          Currency & Regional Settings
        </h2>
        <p className="text-xs text-slate-500">
          Moroccan Dirham (MAD) is the default demo currency. You can toggle between currencies with automatic exchange calculations.
        </p>

        <div className="grid grid-cols-3 gap-3">
          {(['MAD', 'EUR', 'USD'] as CurrencyCode[]).map((c) => (
            <button
              key={c}
              onClick={() => handleCurrencyChange(c)}
              className={`p-3.5 rounded-xl border text-center transition-all ${
                currentCurrency === c
                  ? 'border-emerald-600 bg-emerald-50/40 ring-2 ring-emerald-600/10'
                  : 'border-slate-200 hover:bg-slate-50'
              }`}
            >
              <span className="font-bold text-sm text-slate-900 block">{c}</span>
              <span className="text-[11px] text-slate-500">
                {c === 'MAD' ? 'Moroccan Dirham' : c === 'EUR' ? 'Euro (€)' : 'US Dollar ($)'}
              </span>
            </button>
          ))}
        </div>
      </div>

      {/* AI Inference Architecture */}
      <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4">
        <h2 className="text-sm font-bold text-slate-900 pb-2 border-b border-slate-100 flex items-center gap-2">
          <Cpu className="w-4 h-4 text-slate-600" />
          AI Inference & LLM Configuration
        </h2>

        <div className="p-4 bg-slate-50 border border-slate-200 rounded-xl space-y-2 text-xs">
          <div className="flex justify-between">
            <span className="text-slate-500">Inference Provider:</span>
            <span className="font-semibold text-slate-900">Groq Cloud API</span>
          </div>
          <div className="flex justify-between">
            <span className="text-slate-500">Active Model:</span>
            <span className="font-semibold text-emerald-700">openai/gpt-oss-20b</span>
          </div>
          <div className="flex justify-between">
            <span className="text-slate-500">Secondary Fallback Model:</span>
            <span className="font-semibold text-slate-900">openai/gpt-oss-120b</span>
          </div>
          <div className="flex justify-between">
            <span className="text-slate-500">Output Validation:</span>
            <span className="font-semibold text-slate-900">Strict Schema Deserialization + Confidence Bounds</span>
          </div>
          <div className="flex justify-between">
            <span className="text-slate-500">Rate Limiting Strategy:</span>
            <span className="font-semibold text-slate-900">Exponential Backoff with Jitter (max 3 retries)</span>
          </div>
        </div>
      </div>

      {/* Demo Data Reset Sandbox */}
      <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4">
        <h2 className="text-sm font-bold text-slate-900 pb-2 border-b border-slate-100 flex items-center gap-2">
          <RotateCcw className="w-4 h-4 text-amber-600" />
          Demo Scenario Sandbox Controls
        </h2>
        <p className="text-xs text-slate-500">
          Restore the clean demo state with Lenovo IdeaPad Slim 5 comparison, Samsung EcoBubble washing machine, and LG refrigerator with return deadline in 4 days.
        </p>

        <button
          onClick={handleResetDemo}
          disabled={resetting}
          className="px-4 py-2.5 bg-slate-900 hover:bg-slate-800 text-white rounded-xl text-xs font-semibold shadow-xs transition-colors disabled:opacity-50 flex items-center gap-2"
        >
          <RotateCcw className={`w-4 h-4 ${resetting ? 'animate-spin' : ''}`} />
          {resetting ? 'Resetting Demo Records...' : 'Reset Demo Data to Initial Baseline'}
        </button>
      </div>
    </div>
  );
};
