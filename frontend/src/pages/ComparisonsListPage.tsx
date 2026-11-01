import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { Comparison } from '../types';
import { useCurrency } from '../context/CurrencyContext';
import { Scale, Plus, ArrowRight, Trash2, Calendar, Tag } from 'lucide-react';

export const ComparisonsListPage: React.FC = () => {
  const [comparisons, setComparisons] = useState<Comparison[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { formatPrice } = useCurrency();

  useEffect(() => {
    fetchComparisons();
  }, []);

  const fetchComparisons = async () => {
    setLoading(true);
    try {
      const data = await api.get<Comparison[]>('/comparisons');
      setComparisons(data);
    } catch (e: any) {
      setError(e.message || 'Failed to load comparisons');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (e: React.MouseEvent, id: number) => {
    e.preventDefault();
    e.stopPropagation();
    if (!window.confirm('Are you sure you want to delete this comparison?')) return;

    try {
      await api.delete(`/comparisons/${id}`);
      setComparisons((prev) => prev.filter((c) => c.id !== id));
    } catch (err: any) {
      alert('Failed to delete comparison: ' + err.message);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold text-slate-900 tracking-tight flex items-center gap-2">
            <Scale className="w-6 h-6 text-emerald-600" />
            Offer Comparisons
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Evaluate competing quotes on total delivered price, specifications, and warranty protection.
          </p>
        </div>

        <Link
          to="/comparisons/new"
          className="inline-flex items-center gap-1.5 px-4 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-semibold shadow-xs transition-colors shrink-0"
        >
          <Plus className="w-4 h-4" />
          Create Comparison
        </Link>
      </div>

      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 animate-pulse">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-48 bg-slate-200 rounded-2xl" />
          ))}
        </div>
      ) : error ? (
        <div className="p-6 bg-white border border-slate-200 rounded-2xl text-center text-xs text-rose-600">
          {error}
        </div>
      ) : comparisons.length === 0 ? (
        <div className="p-12 text-center bg-white border border-slate-200 rounded-2xl">
          <Scale className="w-12 h-12 text-slate-300 mx-auto mb-3" />
          <h3 className="text-sm font-bold text-slate-900 mb-1">No comparisons created yet</h3>
          <p className="text-xs text-slate-500 max-w-sm mx-auto mb-4">
            Compare laptops, phones, or appliances across multiple stores before making a purchase.
          </p>
          <Link
            to="/comparisons/new"
            className="inline-flex items-center gap-1.5 px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-xl text-xs font-semibold"
          >
            <Plus className="w-4 h-4" />
            Start First Comparison
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {comparisons.map((comp) => {
            const hasOffers = comp.offers && comp.offers.length > 0;
            const minPrice = hasOffers
              ? Math.min(...comp.offers.map((o) => o.totalPrice))
              : 0;

            return (
              <Link
                key={comp.id}
                to={`/comparisons/${comp.id}`}
                className="group bg-white p-5 rounded-2xl border border-slate-200 hover:border-slate-300 shadow-xs hover:shadow-md transition-all flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-start justify-between gap-2 mb-2">
                    <span className="inline-flex items-center gap-1 px-2 py-0.5 bg-slate-100 text-slate-700 text-[10px] font-semibold rounded-md">
                      <Tag className="w-3 h-3 text-slate-400" />
                      {comp.categoryName}
                    </span>
                    <button
                      onClick={(e) => handleDelete(e, comp.id)}
                      className="text-slate-300 hover:text-rose-600 p-1 rounded-md transition-colors"
                      title="Delete Comparison"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>

                  <h3 className="font-bold text-sm text-slate-900 group-hover:text-emerald-700 transition-colors line-clamp-2 mb-1.5">
                    {comp.title}
                  </h3>

                  {comp.notes && (
                    <p className="text-xs text-slate-500 line-clamp-2 mb-3 leading-relaxed">
                      {comp.notes}
                    </p>
                  )}
                </div>

                <div className="pt-3 border-t border-slate-100 mt-4">
                  <div className="flex items-center justify-between text-xs mb-2">
                    <span className="text-slate-500">{comp.offers?.length || 0} competing offers</span>
                    {hasOffers && (
                      <span className="font-bold text-slate-900">
                        From {formatPrice(minPrice)}
                      </span>
                    )}
                  </div>

                  <div className="flex items-center justify-between text-[11px] text-slate-400 pt-1">
                    <span className="flex items-center gap-1">
                      <Calendar className="w-3 h-3" />
                      {new Date(comp.createdAt).toLocaleDateString()}
                    </span>
                    <span className="text-emerald-600 font-medium flex items-center gap-1 group-hover:translate-x-0.5 transition-transform">
                      Details <ArrowRight className="w-3 h-3" />
                    </span>
                  </div>
                </div>
              </Link>
            );
          })}
        </div>
      )}
    </div>
  );
};
