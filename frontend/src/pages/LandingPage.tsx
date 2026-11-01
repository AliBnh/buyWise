import React from 'react';
import { Link } from 'react-router-dom';
import {
  ShieldCheck,
  Scale,
  FileSearch,
  CalendarClock,
  Sparkles,
  ArrowRight,
  CheckCircle2,
  Lock,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const LandingPage: React.FC = () => {
  const { isAuthenticated } = useAuth();

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 flex flex-col">
      {/* Navigation Header */}
      <header className="h-20 border-b border-slate-200 bg-white/80 backdrop-blur-md sticky top-0 z-40 px-6 lg:px-12 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-slate-900 flex items-center justify-center text-white shadow-xs">
            <ShieldCheck className="w-6 h-6 text-emerald-400" />
          </div>
          <div>
            <span className="font-extrabold text-xl tracking-tight text-slate-900">BuyWise</span>
            <span className="hidden sm:inline-block ml-2 px-2 py-0.5 bg-emerald-50 text-emerald-700 text-[10px] font-semibold rounded-md border border-emerald-200">
              Purchase Intelligence
            </span>
          </div>
        </div>

        <div className="flex items-center gap-3">
          {isAuthenticated ? (
            <Link
              to="/dashboard"
              className="inline-flex items-center gap-2 px-5 py-2.5 bg-slate-900 hover:bg-slate-800 text-white rounded-xl text-xs font-semibold shadow-xs transition-colors"
            >
              Go to Dashboard
              <ArrowRight className="w-4 h-4" />
            </Link>
          ) : (
            <>
              <Link
                to="/login"
                className="px-4 py-2 text-xs font-semibold text-slate-700 hover:text-slate-900 transition-colors"
              >
                Sign In
              </Link>
              <Link
                to="/register"
                className="inline-flex items-center gap-1.5 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-semibold shadow-xs transition-colors"
              >
                Get Started
                <ArrowRight className="w-3.5 h-3.5" />
              </Link>
            </>
          )}
        </div>
      </header>

      {/* Hero Section */}
      <section className="py-20 lg:py-28 px-6 lg:px-12 max-w-5xl mx-auto text-center">
        <div className="inline-flex items-center gap-2 px-3 py-1 bg-slate-100 border border-slate-200 rounded-full text-xs font-medium text-slate-700 mb-6">
          <span className="w-2 h-2 rounded-full bg-emerald-500" />
          Know what you're really paying for
        </div>

        <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight text-slate-900 leading-[1.15] mb-6">
          Buy with confidence. <br className="hidden sm:block" />
          <span className="text-emerald-600">Never lose track</span> of what you own.
        </h1>

        <p className="text-base sm:text-lg text-slate-600 max-w-2xl mx-auto leading-relaxed mb-10">
          Compare purchasing offers on real specifications, hidden fees, and warranty conditions.
          Store invoices, track return deadlines, and get grounded AI advice on your household purchases.
        </p>

        <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
          <Link
            to="/register"
            className="w-full sm:w-auto px-8 py-3.5 bg-slate-900 hover:bg-slate-800 text-white rounded-xl text-sm font-semibold shadow-md transition-all flex items-center justify-center gap-2"
          >
            Start Comparing Offers
            <ArrowRight className="w-4 h-4" />
          </Link>
          <Link
            to="/login"
            className="w-full sm:w-auto px-8 py-3.5 bg-white hover:bg-slate-100 text-slate-800 border border-slate-200 rounded-xl text-sm font-semibold shadow-xs transition-all flex items-center justify-center gap-2"
          >
            Explore Live Demo Account
          </Link>
        </div>

        <div className="mt-12 flex flex-wrap items-center justify-center gap-6 text-xs text-slate-500 font-medium">
          <span className="flex items-center gap-1.5">
            <CheckCircle2 className="w-4 h-4 text-emerald-600" />
            Deterministic arithmetic (LLMs never compute totals)
          </span>
          <span className="flex items-center gap-1.5">
            <CheckCircle2 className="w-4 h-4 text-emerald-600" />
            Human-in-the-Loop document verification
          </span>
          <span className="flex items-center gap-1.5">
            <CheckCircle2 className="w-4 h-4 text-emerald-600" />
            Multi-currency (MAD default)
          </span>
        </div>
      </section>

      {/* Feature Grid */}
      <section className="py-16 bg-white border-y border-slate-200 px-6 lg:px-12">
        <div className="max-w-6xl mx-auto">
          <div className="text-center max-w-2xl mx-auto mb-16">
            <h2 className="text-2xl sm:text-3xl font-bold text-slate-900 tracking-tight mb-3">
              Built for everyday consumer decisions
            </h2>
            <p className="text-sm text-slate-600 leading-relaxed">
              When buying laptops, appliances, or phones, misleading pricing and vague warranties create costly mistakes.
              BuyWise resolves the trade-offs clearly.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="p-6 bg-slate-50 border border-slate-200 rounded-2xl">
              <div className="w-10 h-10 rounded-xl bg-emerald-100 text-emerald-700 flex items-center justify-center mb-4">
                <Scale className="w-5 h-5" />
              </div>
              <h3 className="font-semibold text-slate-900 text-sm mb-2">Offer Comparison</h3>
              <p className="text-xs text-slate-600 leading-relaxed">
                Compare competing seller quotes side by side. Normalize specs, uncover delivery fees, and rank by lowest total price, warranty, or balanced value.
              </p>
            </div>

            <div className="p-6 bg-slate-50 border border-slate-200 rounded-2xl">
              <div className="w-10 h-10 rounded-xl bg-blue-100 text-blue-700 flex items-center justify-center mb-4">
                <FileSearch className="w-5 h-5" />
              </div>
              <h3 className="font-semibold text-slate-900 text-sm mb-2">Document Understanding</h3>
              <p className="text-xs text-slate-600 leading-relaxed">
                Upload receipts and warranty certificates. BuyWise extracts prices, serial numbers, and exclusion clauses with verbatim text grounding.
              </p>
            </div>

            <div className="p-6 bg-slate-50 border border-slate-200 rounded-2xl">
              <div className="w-10 h-10 rounded-xl bg-amber-100 text-amber-700 flex items-center justify-center mb-4">
                <CalendarClock className="w-5 h-5" />
              </div>
              <h3 className="font-semibold text-slate-900 text-sm mb-2">Deadline Tracking</h3>
              <p className="text-xs text-slate-600 leading-relaxed">
                Automatic alerts 3 days before return windows close and 30 days before manufacturer warranties expire. Never miss a claim window.
              </p>
            </div>

            <div className="p-6 bg-slate-50 border border-slate-200 rounded-2xl">
              <div className="w-10 h-10 rounded-xl bg-purple-100 text-purple-700 flex items-center justify-center mb-4">
                <Sparkles className="w-5 h-5" />
              </div>
              <h3 className="font-semibold text-slate-900 text-sm mb-2">Grounded AI Assistant</h3>
              <p className="text-xs text-slate-600 leading-relaxed">
                Ask questions about your owned items or active comparisons. The assistant cites authentic database records with full traceability.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="mt-auto py-8 px-6 lg:px-12 border-t border-slate-200 bg-slate-50 text-xs text-slate-500 flex flex-col sm:flex-row items-center justify-between gap-4">
        <div>
          © 2026 BuyWise. Consumer Purchase Intelligence & Ownership Platform.
        </div>
        <div className="flex items-center gap-6">
          <Link to="/login" className="hover:text-slate-900">Sign In</Link>
          <Link to="/register" className="hover:text-slate-900">Register</Link>
        </div>
      </footer>
    </div>
  );
};
