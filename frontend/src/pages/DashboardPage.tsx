import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { DashboardOverview } from '../types';
import { useCurrency } from '../context/CurrencyContext';
import {
  Scale,
  PackageCheck,
  CalendarClock,
  Sparkles,
  ArrowRight,
  ShieldAlert,
  Clock,
  Plus,
  FileCheck2,
  CheckCircle2,
  AlertTriangle,
} from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const [data, setData] = useState<DashboardOverview | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { formatPrice } = useCurrency();

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    setLoading(true);
    try {
      const overview = await api.get<DashboardOverview>('/dashboard/overview');
      setData(overview);
    } catch (e: any) {
      setError(e.message || 'Failed to load dashboard overview');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="space-y-6 animate-pulse">
        <div className="h-20 bg-slate-200 rounded-2xl w-full" />
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="h-28 bg-slate-200 rounded-2xl" />
          ))}
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div className="h-72 bg-slate-200 rounded-2xl" />
          <div className="h-72 bg-slate-200 rounded-2xl" />
        </div>
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="p-8 text-center bg-white border border-slate-200 rounded-2xl">
        <AlertTriangle className="w-10 h-10 text-amber-500 mx-auto mb-3" />
        <h3 className="text-base font-semibold text-slate-900 mb-1">Could not load dashboard</h3>
        <p className="text-xs text-slate-500 mb-4">{error}</p>
        <button
          onClick={fetchDashboardData}
          className="px-4 py-2 bg-slate-900 text-white rounded-xl text-xs font-semibold"
        >
          Retry
        </button>
      </div>
    );
  }

  // Check for critical upcoming return deadline or expiring warranty
  const criticalDeadline = data.upcomingDeadlines.find(
    (d) => d.daysRemaining <= 7 && !d.isCompleted
  );

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold text-slate-900 tracking-tight">
            Welcome back, {data.userFullName}
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            BuyWise Purchase Intelligence is monitoring your offers and active warranties.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Link
            to="/comparisons/new"
            className="inline-flex items-center gap-1.5 px-3.5 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-semibold shadow-xs transition-colors"
          >
            <Plus className="w-4 h-4" />
            New Comparison
          </Link>
          <Link
            to="/purchases"
            className="inline-flex items-center gap-1.5 px-3.5 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-xl text-xs font-semibold shadow-xs transition-colors"
          >
            <PackageCheck className="w-4 h-4" />
            Register Purchase
          </Link>
        </div>
      </div>

      {/* Urgent Warning Banner */}
      {criticalDeadline && (
        <div className="p-4 bg-amber-50 border border-amber-200 rounded-2xl flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-amber-100 text-amber-700 flex items-center justify-center shrink-0">
              <ShieldAlert className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs font-bold text-amber-900">
                Action Required: {criticalDeadline.title}
              </p>
              <p className="text-xs text-amber-700 mt-0.5">
                Due in {criticalDeadline.daysRemaining} days ({criticalDeadline.dueDate}). Check return conditions or verify appliance operation before deadline passes.
              </p>
            </div>
          </div>
          <Link
            to="/deadlines"
            className="text-xs font-semibold text-amber-900 hover:text-amber-950 underline shrink-0"
          >
            Review Deadline Details →
          </Link>
        </div>
      )}

      {/* Metrics Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-slate-500 mb-2">
            <span className="text-xs font-medium">Owned Assets Value</span>
            <PackageCheck className="w-4 h-4 text-emerald-600" />
          </div>
          <p className="text-2xl font-bold text-slate-900">
            {formatPrice(data.totalExpenditureAmount)}
          </p>
          <p className="text-[11px] text-slate-500 mt-1">
            {data.totalPurchasesCount} registered household purchases
          </p>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-slate-500 mb-2">
            <span className="text-xs font-medium">Active Warranties</span>
            <Clock className="w-4 h-4 text-blue-600" />
          </div>
          <p className="text-2xl font-bold text-slate-900">
            {data.activeWarrantiesCount}
          </p>
          <p className="text-[11px] text-slate-500 mt-1">
            {data.expiringSoonWarrantiesCount > 0 ? (
              <span className="text-amber-600 font-medium">
                {data.expiringSoonWarrantiesCount} expiring in &lt; 30 days
              </span>
            ) : (
              'All protected under warranty'
            )}
          </p>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-slate-500 mb-2">
            <span className="text-xs font-medium">Upcoming Deadlines</span>
            <CalendarClock className="w-4 h-4 text-amber-600" />
          </div>
          <p className="text-2xl font-bold text-slate-900">
            {data.upcomingDeadlines.length}
          </p>
          <p className="text-[11px] text-slate-500 mt-1">
            Return windows & maintenance tasks
          </p>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between text-slate-500 mb-2">
            <span className="text-xs font-medium">Active Comparisons</span>
            <Scale className="w-4 h-4 text-purple-600" />
          </div>
          <p className="text-2xl font-bold text-slate-900">
            {data.activeComparisonsCount}
          </p>
          <p className="text-[11px] text-slate-500 mt-1">
            Evaluated competing quotes
          </p>
        </div>
      </div>

      {/* Main Grid: Comparisons & Purchases */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Comparisons */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between mb-4 pb-3 border-b border-slate-100">
            <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2">
              <Scale className="w-4 h-4 text-slate-700" />
              Recent Offer Comparisons
            </h3>
            <Link
              to="/comparisons"
              className="text-xs font-medium text-emerald-600 hover:text-emerald-700"
            >
              View All ({data.activeComparisonsCount})
            </Link>
          </div>

          {data.recentComparisons.length === 0 ? (
            <div className="py-8 text-center text-xs text-slate-400">
              No comparisons yet. Click "+ New Comparison" to analyze offers.
            </div>
          ) : (
            <div className="space-y-3">
              {data.recentComparisons.map((c) => (
                <Link
                  key={c.id}
                  to={`/comparisons/${c.id}`}
                  className="block p-3.5 bg-slate-50 hover:bg-slate-100 border border-slate-200 rounded-xl transition-colors"
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <h4 className="text-xs font-bold text-slate-900 mb-0.5">{c.title}</h4>
                      <p className="text-[11px] text-slate-500">
                        {c.categoryName} • {c.offers.length} competing offers
                      </p>
                    </div>
                    <span className="px-2 py-0.5 bg-slate-200 text-slate-700 text-[10px] font-semibold rounded-md">
                      {c.status}
                    </span>
                  </div>

                  {c.offers.length > 0 && (
                    <div className="mt-2.5 pt-2 border-t border-slate-200/60 flex items-center justify-between text-[11px]">
                      <span className="text-slate-600">
                        From <strong className="text-slate-900">{formatPrice(c.offers[0].totalPrice)}</strong>
                      </span>
                      <span className="text-emerald-700 font-medium flex items-center gap-1">
                        Inspect Differences <ArrowRight className="w-3 h-3" />
                      </span>
                    </div>
                  )}
                </Link>
              ))}
            </div>
          )}
        </div>

        {/* Ownership Library */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between mb-4 pb-3 border-b border-slate-100">
            <h3 className="text-sm font-bold text-slate-900 flex items-center gap-2">
              <PackageCheck className="w-4 h-4 text-slate-700" />
              Ownership Library
            </h3>
            <Link
              to="/purchases"
              className="text-xs font-medium text-emerald-600 hover:text-emerald-700"
            >
              View Library ({data.totalPurchasesCount})
            </Link>
          </div>

          {data.recentPurchases.length === 0 ? (
            <div className="py-8 text-center text-xs text-slate-400">
              No purchases registered yet.
            </div>
          ) : (
            <div className="space-y-3">
              {data.recentPurchases.map((p) => (
                <div
                  key={p.id}
                  className="p-3.5 bg-slate-50 border border-slate-200 rounded-xl flex items-center justify-between"
                >
                  <div>
                    <h4 className="text-xs font-bold text-slate-900 mb-0.5">{p.productName}</h4>
                    <p className="text-[11px] text-slate-500">
                      {p.sellerName} • Paid {formatPrice(p.pricePaid)} on {p.purchaseDate}
                    </p>
                  </div>

                  <div className="text-right">
                    <span
                      className={`inline-block px-2 py-0.5 text-[10px] font-semibold rounded-md ${
                        p.isWarrantyActive
                          ? 'bg-emerald-100 text-emerald-800'
                          : 'bg-slate-200 text-slate-600'
                      }`}
                    >
                      {p.isWarrantyActive ? `${p.daysUntilWarrantyExpiry}d Warranty` : 'Expired'}
                    </span>
                    {p.returnDeadline && p.isReturnWindowActive && (
                      <p className="text-[10px] text-amber-700 font-medium mt-0.5">
                        Return window active
                      </p>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* AI Assistant Callout */}
      <div className="p-6 bg-gradient-to-r from-slate-900 to-slate-800 rounded-2xl text-white flex flex-col sm:flex-row items-center justify-between gap-4 shadow-sm">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-emerald-500/20 text-emerald-400 flex items-center justify-center shrink-0">
            <Sparkles className="w-5 h-5" />
          </div>
          <div>
            <h4 className="text-sm font-bold tracking-tight">Need grounded purchase advice?</h4>
            <p className="text-xs text-slate-300 mt-0.5">
              Ask BuyWise Assistant about warranty coverage, return deadlines, or offer trade-offs.
            </p>
          </div>
        </div>

        <Link
          to="/assistant"
          className="px-4 py-2 bg-emerald-500 hover:bg-emerald-400 text-slate-950 rounded-xl text-xs font-bold transition-colors shrink-0"
        >
          Open Assistant →
        </Link>
      </div>
    </div>
  );
};
