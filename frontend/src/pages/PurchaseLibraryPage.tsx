import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { Purchase, PurchaseStats } from '../types';
import { useCurrency } from '../context/CurrencyContext';
import {
  PackageCheck,
  Plus,
  ShieldCheck,
  Clock,
  Calendar,
  Building,
  Search,
  Filter,
  ArrowRight,
  AlertCircle,
  Tag,
  CheckCircle2,
} from 'lucide-react';

export const PurchaseLibraryPage: React.FC = () => {
  const [purchases, setPurchases] = useState<Purchase[]>([]);
  const [stats, setStats] = useState<PurchaseStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [filterCategory, setFilterCategory] = useState<string>('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [showAddModal, setShowAddModal] = useState(false);
  const { formatPrice } = useCurrency();

  // New purchase modal form state
  const [productName, setProductName] = useState('');
  const [modelNumber, setModelNumber] = useState('');
  const [category, setCategory] = useState('Major Appliances');
  const [sellerName, setSellerName] = useState('Marjane Mall');
  const [purchaseDate, setPurchaseDate] = useState(new Date().toISOString().split('T')[0]);
  const [pricePaid, setPricePaid] = useState('');
  const [warrantyMonths, setWarrantyMonths] = useState(24);
  const [returnDays, setReturnDays] = useState(14);
  const [serialNumber, setSerialNumber] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchPurchasesAndStats();
  }, []);

  const fetchPurchasesAndStats = async () => {
    setLoading(true);
    try {
      const [items, statsData] = await Promise.all([
        api.get<Purchase[]>('/purchases'),
        api.get<PurchaseStats>('/purchases/stats'),
      ]);
      setPurchases(items);
      setStats(statsData);
    } catch (e) {
      console.error('Failed to load purchases', e);
    } finally {
      setLoading(false);
    }
  };

  const handleRegisterPurchase = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!productName || !sellerName || !pricePaid) return;

    setSubmitting(true);
    try {
      const pDate = new Date(purchaseDate);
      const retDeadline = new Date(pDate);
      retDeadline.setDate(retDeadline.getDate() + returnDays);

      await api.post('/purchases', {
        productName: productName.trim(),
        modelNumber: modelNumber.trim() || undefined,
        category,
        sellerName: sellerName.trim(),
        purchaseDate,
        pricePaid: Number(pricePaid),
        currency: 'MAD',
        warrantyMonths: Number(warrantyMonths),
        returnDeadline: retDeadline.toISOString().split('T')[0],
        serialNumber: serialNumber.trim() || undefined,
      });

      setShowAddModal(false);
      // Reset form
      setProductName('');
      setModelNumber('');
      setPricePaid('');
      setSerialNumber('');
      fetchPurchasesAndStats();
    } catch (err: any) {
      alert('Failed to register purchase: ' + err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const filteredPurchases = purchases.filter((p) => {
    const matchesCategory = filterCategory === 'ALL' || p.category === filterCategory;
    const matchesSearch =
      p.productName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      p.sellerName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (p.modelNumber && p.modelNumber.toLowerCase().includes(searchQuery.toLowerCase()));
    return matchesCategory && matchesSearch;
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold text-slate-900 tracking-tight flex items-center gap-2">
            <PackageCheck className="w-6 h-6 text-emerald-600" />
            Ownership Library
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Store your receipts, track active warranty durations, and manage return deadlines.
          </p>
        </div>

        <button
          onClick={() => setShowAddModal(true)}
          className="inline-flex items-center gap-1.5 px-4 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-semibold shadow-xs transition-colors shrink-0"
        >
          <Plus className="w-4 h-4" />
          Register Purchase
        </button>
      </div>

      {/* Stats Summary Bar */}
      {stats && (
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-slate-100 text-slate-900 flex items-center justify-center shrink-0">
              <PackageCheck className="w-5 h-5 text-emerald-600" />
            </div>
            <div>
              <p className="text-xs text-slate-500 font-medium">Total Portfolio Value</p>
              <p className="text-lg font-bold text-slate-900">{formatPrice(stats.totalExpenditure)}</p>
            </div>
          </div>

          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-blue-50 text-blue-700 flex items-center justify-center shrink-0">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs text-slate-500 font-medium">Active Warranties</p>
              <p className="text-lg font-bold text-slate-900">{stats.activeWarrantiesCount} Items</p>
            </div>
          </div>

          <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-amber-50 text-amber-700 flex items-center justify-center shrink-0">
              <Clock className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs text-slate-500 font-medium">Expiring in &lt; 30 Days</p>
              <p className="text-lg font-bold text-amber-900">
                {stats.expiringSoonWarrantiesCount} Items
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Filters and Search Bar */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs flex flex-col sm:flex-row gap-3 items-center justify-between">
        <div className="relative w-full sm:w-72">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search by product, seller, or model..."
            className="w-full text-xs pl-9 pr-3 py-2 border border-slate-200 rounded-lg focus:ring-2 focus:ring-slate-900 focus:outline-hidden"
          />
        </div>

        <div className="flex items-center gap-2 w-full sm:w-auto">
          <Filter className="w-4 h-4 text-slate-400 shrink-0" />
          <select
            value={filterCategory}
            onChange={(e) => setFilterCategory(e.target.value)}
            className="text-xs p-2 border border-slate-200 rounded-lg focus:ring-2 focus:ring-slate-900 focus:outline-hidden bg-white w-full sm:w-auto"
          >
            <option value="ALL">All Categories</option>
            <option value="Major Appliances">Major Appliances</option>
            <option value="Laptops & Computers">Laptops & Computers</option>
            <option value="Smartphones & Tablets">Smartphones & Tablets</option>
            <option value="TVs & Home Entertainment">TVs & Home Entertainment</option>
          </select>
        </div>
      </div>

      {/* Purchases Grid */}
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 animate-pulse">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-44 bg-slate-200 rounded-2xl" />
          ))}
        </div>
      ) : filteredPurchases.length === 0 ? (
        <div className="p-12 text-center bg-white border border-slate-200 rounded-2xl">
          <PackageCheck className="w-12 h-12 text-slate-300 mx-auto mb-3" />
          <h3 className="text-sm font-bold text-slate-900 mb-1">No purchases found</h3>
          <p className="text-xs text-slate-500 max-w-sm mx-auto mb-4">
            Register your purchased appliances, electronics, or devices to track warranties and deadlines.
          </p>
          <button
            onClick={() => setShowAddModal(true)}
            className="inline-flex items-center gap-1.5 px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-xl text-xs font-semibold"
          >
            <Plus className="w-4 h-4" />
            Register First Purchase
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredPurchases.map((p) => (
            <div
              key={p.id}
              className="bg-white p-5 rounded-2xl border border-slate-200 shadow-xs hover:border-slate-300 transition-all flex flex-col justify-between"
            >
              <div>
                <div className="flex items-start justify-between gap-2 mb-2">
                  <span className="px-2 py-0.5 bg-slate-100 text-slate-700 text-[10px] font-semibold rounded-md">
                    {p.category || 'Product'}
                  </span>
                  <span
                    className={`px-2 py-0.5 text-[10px] font-bold rounded-md ${
                      p.isWarrantyActive
                        ? 'bg-emerald-100 text-emerald-800'
                        : 'bg-slate-100 text-slate-600'
                    }`}
                  >
                    {p.isWarrantyActive ? 'Warranty Active' : 'Warranty Expired'}
                  </span>
                </div>

                <h3 className="font-bold text-sm text-slate-900 mb-1 line-clamp-2">
                  {p.productName}
                </h3>
                {p.modelNumber && (
                  <p className="text-[11px] text-slate-500 font-mono mb-2">
                    Model: {p.modelNumber}
                  </p>
                )}

                <div className="p-3 bg-slate-50 border border-slate-200/60 rounded-xl space-y-1.5 text-xs text-slate-600 my-3">
                  <div className="flex justify-between">
                    <span>Retailer:</span>
                    <strong className="text-slate-900">{p.sellerName}</strong>
                  </div>
                  <div className="flex justify-between">
                    <span>Purchase Price:</span>
                    <strong className="text-emerald-700">{formatPrice(p.pricePaid)}</strong>
                  </div>
                  <div className="flex justify-between">
                    <span>Purchase Date:</span>
                    <span>{p.purchaseDate}</span>
                  </div>
                </div>
              </div>

              <div className="pt-3 border-t border-slate-100 text-xs">
                <div className="flex items-center justify-between text-slate-600 mb-1">
                  <span>Warranty Expiry:</span>
                  <span className="font-semibold text-slate-900">{p.warrantyExpirationDate}</span>
                </div>

                {p.isWarrantyActive && (
                  <div className="w-full bg-slate-100 h-1.5 rounded-full overflow-hidden mt-1.5 mb-2">
                    <div
                      className="bg-emerald-500 h-full rounded-full"
                      style={{
                        width: `${Math.min(100, Math.max(10, (p.daysUntilWarrantyExpiry / (p.warrantyMonths * 30)) * 100))}%`,
                      }}
                    />
                  </div>
                )}

                {p.returnDeadline && p.isReturnWindowActive && (
                  <p className="text-[11px] text-amber-700 font-semibold bg-amber-50 p-1.5 rounded-lg border border-amber-200 text-center mt-2">
                    Return Window Open until {p.returnDeadline}
                  </p>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Register Purchase Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-xs">
          <div className="bg-white rounded-2xl shadow-xl border border-slate-200 max-w-lg w-full p-6 max-h-[90vh] overflow-y-auto">
            <h3 className="text-base font-bold text-slate-900 mb-1">
              Register Product Purchase
            </h3>
            <p className="text-xs text-slate-500 mb-4">
              Add details to activate automatic warranty expiration tracking and reminder schedules.
            </p>

            <form onSubmit={handleRegisterPurchase} className="space-y-3.5">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Product Name *
                </label>
                <input
                  type="text"
                  value={productName}
                  onChange={(e) => setProductName(e.target.value)}
                  placeholder="e.g. LG InstaView Refrigerator"
                  required
                  className="w-full text-xs p-2.5 border border-slate-200 rounded-xl focus:ring-2 focus:ring-slate-900"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Model Number
                  </label>
                  <input
                    type="text"
                    value={modelNumber}
                    onChange={(e) => setModelNumber(e.target.value)}
                    placeholder="e.g. GSX961MCVZ"
                    className="w-full text-xs p-2.5 border border-slate-200 rounded-xl"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Category
                  </label>
                  <select
                    value={category}
                    onChange={(e) => setCategory(e.target.value)}
                    className="w-full text-xs p-2.5 border border-slate-200 rounded-xl bg-white"
                  >
                    <option value="Major Appliances">Major Appliances</option>
                    <option value="Laptops & Computers">Laptops & Computers</option>
                    <option value="Smartphones & Tablets">Smartphones & Tablets</option>
                    <option value="TVs & Home Entertainment">TVs & Home Entertainment</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Seller / Retailer *
                  </label>
                  <input
                    type="text"
                    value={sellerName}
                    onChange={(e) => setSellerName(e.target.value)}
                    placeholder="e.g. Marjane Mall"
                    required
                    className="w-full text-xs p-2.5 border border-slate-200 rounded-xl"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Purchase Price (MAD) *
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    value={pricePaid}
                    onChange={(e) => setPricePaid(e.target.value)}
                    placeholder="11490.00"
                    required
                    className="w-full text-xs p-2.5 border border-slate-200 rounded-xl"
                  />
                </div>
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Purchase Date *
                  </label>
                  <input
                    type="date"
                    value={purchaseDate}
                    onChange={(e) => setPurchaseDate(e.target.value)}
                    required
                    className="w-full text-xs p-2.5 border border-slate-200 rounded-xl bg-white"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Warranty (Months)
                  </label>
                  <input
                    type="number"
                    min="0"
                    value={warrantyMonths}
                    onChange={(e) => setWarrantyMonths(Number(e.target.value))}
                    className="w-full text-xs p-2.5 border border-slate-200 rounded-xl"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Return Window (Days)
                  </label>
                  <input
                    type="number"
                    min="0"
                    value={returnDays}
                    onChange={(e) => setReturnDays(Number(e.target.value))}
                    className="w-full text-xs p-2.5 border border-slate-200 rounded-xl"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Serial Number (Optional)
                </label>
                <input
                  type="text"
                  value={serialNumber}
                  onChange={(e) => setSerialNumber(e.target.value)}
                  placeholder="e.g. LG-REF-449102-CAS"
                  className="w-full text-xs p-2.5 border border-slate-200 rounded-xl font-mono"
                />
              </div>

              <div className="flex items-center justify-end gap-2 pt-4 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="px-4 py-2 text-xs font-semibold text-slate-600 hover:text-slate-900"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-5 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-semibold shadow-xs disabled:opacity-50"
                >
                  {submitting ? 'Registering...' : 'Register Purchase'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
