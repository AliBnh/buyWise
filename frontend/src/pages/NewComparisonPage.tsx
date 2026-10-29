import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import { ProductCategory, Seller } from '../types';
import { Scale, Plus, Trash2, ArrowLeft, ArrowRight, ShieldCheck, Check } from 'lucide-react';

interface OfferFormState {
  sellerName: string;
  title: string;
  productModel: string;
  basePrice: number | string;
  deliveryFee: number | string;
  warrantyMonths: number;
  returnWindowDays: number;
  processor?: string;
  ram?: string;
  storage?: string;
  display?: string;
  warrantyConditions?: string;
}

export const NewComparisonPage: React.FC = () => {
  const navigate = useNavigate();
  const [categories, setCategories] = useState<ProductCategory[]>([]);
  const [sellers, setSellers] = useState<Seller[]>([]);
  const [title, setTitle] = useState('');
  const [categoryName, setCategoryName] = useState('Laptops & Computers');
  const [notes, setNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [offers, setOffers] = useState<OfferFormState[]>([
    {
      sellerName: 'ElectroMaroc',
      title: 'Offer A',
      productModel: '',
      basePrice: '',
      deliveryFee: 0,
      warrantyMonths: 12,
      returnWindowDays: 14,
      processor: '',
      ram: '',
      storage: '',
      display: '',
    },
    {
      sellerName: 'TechHub Casablanca',
      title: 'Offer B',
      productModel: '',
      basePrice: '',
      deliveryFee: 0,
      warrantyMonths: 24,
      returnWindowDays: 7,
      processor: '',
      ram: '',
      storage: '',
      display: '',
    },
  ]);

  useEffect(() => {
    fetchCatalog();
  }, []);

  const fetchCatalog = async () => {
    try {
      const [cats, sels] = await Promise.all([
        api.get<ProductCategory[]>('/catalog/categories'),
        api.get<Seller[]>('/catalog/sellers'),
      ]);
      setCategories(cats);
      setSellers(sels);
      if (cats.length > 0) {
        setCategoryName(cats[0].name);
      }
    } catch (e) {
      console.warn('Could not load catalog defaults', e);
    }
  };

  const handleAddOffer = () => {
    setOffers([
      ...offers,
      {
        sellerName: '',
        title: `Offer ${String.fromCharCode(65 + offers.length)}`,
        productModel: '',
        basePrice: '',
        deliveryFee: 0,
        warrantyMonths: 12,
        returnWindowDays: 14,
      },
    ]);
  };

  const handleRemoveOffer = (index: number) => {
    if (offers.length <= 2) {
      alert('A comparison requires at least 2 offers.');
      return;
    }
    setOffers(offers.filter((_, i) => i !== index));
  };

  const handleOfferChange = (index: number, field: keyof OfferFormState, value: any) => {
    const updated = [...offers];
    updated[index] = { ...updated[index], [field]: value };
    setOffers(updated);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    // Validation
    if (!title.trim()) {
      setError('Comparison title is required.');
      return;
    }

    for (let i = 0; i < offers.length; i++) {
      const o = offers[i];
      if (!o.sellerName.trim() || !o.title.trim() || o.basePrice === '') {
        setError(`Please fill in seller name, title, and base price for Offer ${i + 1}.`);
        return;
      }
    }

    setSubmitting(true);
    try {
      const formattedOffers = offers.map((o) => {
        const specs: Record<string, string> = {};
        if (o.processor) specs['Processor'] = o.processor;
        if (o.ram) specs['RAM'] = o.ram;
        if (o.storage) specs['Storage'] = o.storage;
        if (o.display) specs['Display'] = o.display;

        return {
          sellerName: o.sellerName.trim(),
          title: o.title.trim(),
          productModel: o.productModel || undefined,
          basePrice: Number(o.basePrice),
          deliveryFee: Number(o.deliveryFee) || 0,
          warrantyMonths: Number(o.warrantyMonths) || 12,
          returnWindowDays: Number(o.returnWindowDays) || 14,
          specifications: Object.keys(specs).length > 0 ? specs : undefined,
          warrantyConditions: o.warrantyConditions,
        };
      });

      const response = await api.post<any>('/comparisons', {
        title: title.trim(),
        categoryName,
        notes: notes.trim() || undefined,
        initialOffers: formattedOffers,
      });

      navigate(`/comparisons/${response.id}`);
    } catch (err: any) {
      setError(err.message || 'Failed to create comparison.');
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Back button & Title */}
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate('/comparisons')}
          className="p-2 text-slate-500 hover:text-slate-800 hover:bg-white rounded-xl transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <div>
          <h1 className="text-xl sm:text-2xl font-bold text-slate-900 tracking-tight">
            Create Offer Comparison
          </h1>
          <p className="text-xs text-slate-500">
            Enter product details and competing quotes from Moroccan or international retailers.
          </p>
        </div>
      </div>

      {error && (
        <div className="p-4 bg-rose-50 border border-rose-200 rounded-xl text-xs text-rose-700">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Step 1: Comparison Header Info */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4">
          <h2 className="text-sm font-bold text-slate-900 pb-2 border-b border-slate-100 flex items-center gap-2">
            <span className="w-5 h-5 rounded-full bg-slate-900 text-white flex items-center justify-center text-[11px]">
              1
            </span>
            Product & Category Information
          </h2>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Comparison Title *
              </label>
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="e.g. Lenovo IdeaPad Slim 5 — Work & Coding Laptop"
                className="w-full text-xs p-2.5 border border-slate-200 rounded-xl focus:ring-2 focus:ring-slate-900 focus:outline-hidden"
                required
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Category *
              </label>
              <select
                value={categoryName}
                onChange={(e) => setCategoryName(e.target.value)}
                className="w-full text-xs p-2.5 border border-slate-200 rounded-xl focus:ring-2 focus:ring-slate-900 focus:outline-hidden bg-white"
              >
                {categories.length > 0 ? (
                  categories.map((c) => (
                    <option key={c.id} value={c.name}>
                      {c.name}
                    </option>
                  ))
                ) : (
                  <>
                    <option value="Laptops & Computers">Laptops & Computers</option>
                    <option value="Major Appliances">Major Appliances</option>
                    <option value="Smartphones & Tablets">Smartphones & Tablets</option>
                    <option value="TVs & Home Entertainment">TVs & Home Entertainment</option>
                  </>
                )}
              </select>
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">
              Personal Notes & Decision Priorities (Optional)
            </label>
            <textarea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              rows={2}
              placeholder="e.g. Looking for best long-term reliability. Must have at least 16GB RAM for programming."
              className="w-full text-xs p-2.5 border border-slate-200 rounded-xl focus:ring-2 focus:ring-slate-900 focus:outline-hidden"
            />
          </div>
        </div>

        {/* Step 2: Competing Offers */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4">
          <div className="flex items-center justify-between pb-2 border-b border-slate-100">
            <h2 className="text-sm font-bold text-slate-900 flex items-center gap-2">
              <span className="w-5 h-5 rounded-full bg-slate-900 text-white flex items-center justify-center text-[11px]">
                2
              </span>
              Competing Offers ({offers.length})
            </h2>

            <button
              type="button"
              onClick={handleAddOffer}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-800 rounded-lg text-xs font-semibold transition-colors"
            >
              <Plus className="w-3.5 h-3.5" />
              Add Another Offer
            </button>
          </div>

          <div className="space-y-6">
            {offers.map((offer, idx) => (
              <div
                key={idx}
                className="p-5 bg-slate-50 border border-slate-200 rounded-2xl relative space-y-4"
              >
                <div className="flex items-center justify-between">
                  <h3 className="text-xs font-bold text-slate-900 flex items-center gap-2">
                    <span className="px-2 py-0.5 bg-slate-200 text-slate-800 rounded-md">
                      Offer {String.fromCharCode(65 + idx)}
                    </span>
                    {offer.sellerName ? offer.sellerName : 'Retailer'}
                  </h3>

                  {offers.length > 2 && (
                    <button
                      type="button"
                      onClick={() => handleRemoveOffer(idx)}
                      className="text-slate-400 hover:text-rose-600 p-1 rounded-md"
                      title="Remove offer"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  )}
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                  <div>
                    <label className="block text-[11px] font-semibold text-slate-700 mb-1">
                      Seller / Store Name *
                    </label>
                    <input
                      type="text"
                      list={`sellers-${idx}`}
                      value={offer.sellerName}
                      onChange={(e) => handleOfferChange(idx, 'sellerName', e.target.value)}
                      placeholder="e.g. ElectroMaroc"
                      className="w-full text-xs p-2 bg-white border border-slate-200 rounded-lg focus:ring-2 focus:ring-slate-900 focus:outline-hidden"
                      required
                    />
                    <datalist id={`sellers-${idx}`}>
                      {sellers.map((s) => (
                        <option key={s.id} value={s.name} />
                      ))}
                    </datalist>
                  </div>

                  <div>
                    <label className="block text-[11px] font-semibold text-slate-700 mb-1">
                      Offer Title *
                    </label>
                    <input
                      type="text"
                      value={offer.title}
                      onChange={(e) => handleOfferChange(idx, 'title', e.target.value)}
                      placeholder="e.g. Lenovo IdeaPad Slim 5 (16GB/1TB)"
                      className="w-full text-xs p-2 bg-white border border-slate-200 rounded-lg focus:ring-2 focus:ring-slate-900 focus:outline-hidden"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-[11px] font-semibold text-slate-700 mb-1">
                      Product Model Number
                    </label>
                    <input
                      type="text"
                      value={offer.productModel}
                      onChange={(e) => handleOfferChange(idx, 'productModel', e.target.value)}
                      placeholder="e.g. 16ABR8"
                      className="w-full text-xs p-2 bg-white border border-slate-200 rounded-lg focus:ring-2 focus:ring-slate-900 focus:outline-hidden"
                    />
                  </div>
                </div>

                {/* Price & Delivery Details */}
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-2 border-t border-slate-200/60">
                  <div>
                    <label className="block text-[11px] font-semibold text-slate-700 mb-1">
                      Base Price (MAD) *
                    </label>
                    <input
                      type="number"
                      step="0.01"
                      min="0"
                      value={offer.basePrice}
                      onChange={(e) => handleOfferChange(idx, 'basePrice', e.target.value)}
                      placeholder="8499.00"
                      className="w-full text-xs p-2 bg-white border border-slate-200 rounded-lg focus:ring-2 focus:ring-slate-900 focus:outline-hidden"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-[11px] font-semibold text-slate-700 mb-1">
                      Delivery Fee (MAD)
                    </label>
                    <input
                      type="number"
                      step="0.01"
                      min="0"
                      value={offer.deliveryFee}
                      onChange={(e) => handleOfferChange(idx, 'deliveryFee', e.target.value)}
                      placeholder="0.00"
                      className="w-full text-xs p-2 bg-white border border-slate-200 rounded-lg focus:ring-2 focus:ring-slate-900 focus:outline-hidden"
                    />
                  </div>

                  <div>
                    <label className="block text-[11px] font-semibold text-slate-700 mb-1">
                      Warranty (Months)
                    </label>
                    <input
                      type="number"
                      min="0"
                      value={offer.warrantyMonths}
                      onChange={(e) => handleOfferChange(idx, 'warrantyMonths', Number(e.target.value))}
                      className="w-full text-xs p-2 bg-white border border-slate-200 rounded-lg focus:ring-2 focus:ring-slate-900 focus:outline-hidden"
                    />
                  </div>

                  <div>
                    <label className="block text-[11px] font-semibold text-slate-700 mb-1">
                      Return Window (Days)
                    </label>
                    <input
                      type="number"
                      min="0"
                      value={offer.returnWindowDays}
                      onChange={(e) => handleOfferChange(idx, 'returnWindowDays', Number(e.target.value))}
                      className="w-full text-xs p-2 bg-white border border-slate-200 rounded-lg focus:ring-2 focus:ring-slate-900 focus:outline-hidden"
                    />
                  </div>
                </div>

                {/* Key Specs */}
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-2 border-t border-slate-200/60">
                  <div>
                    <label className="block text-[10px] font-medium text-slate-600 mb-1">
                      Processor / Engine
                    </label>
                    <input
                      type="text"
                      value={offer.processor || ''}
                      onChange={(e) => handleOfferChange(idx, 'processor', e.target.value)}
                      placeholder="Ryzen 7 7730U"
                      className="w-full text-xs p-2 bg-white border border-slate-200 rounded-lg"
                    />
                  </div>

                  <div>
                    <label className="block text-[10px] font-medium text-slate-600 mb-1">
                      RAM / Capacity
                    </label>
                    <input
                      type="text"
                      value={offer.ram || ''}
                      onChange={(e) => handleOfferChange(idx, 'ram', e.target.value)}
                      placeholder="16 GB"
                      className="w-full text-xs p-2 bg-white border border-slate-200 rounded-lg"
                    />
                  </div>

                  <div>
                    <label className="block text-[10px] font-medium text-slate-600 mb-1">
                      Storage
                    </label>
                    <input
                      type="text"
                      value={offer.storage || ''}
                      onChange={(e) => handleOfferChange(idx, 'storage', e.target.value)}
                      placeholder="512 GB SSD"
                      className="w-full text-xs p-2 bg-white border border-slate-200 rounded-lg"
                    />
                  </div>

                  <div>
                    <label className="block text-[10px] font-medium text-slate-600 mb-1">
                      Display / Dimensions
                    </label>
                    <input
                      type="text"
                      value={offer.display || ''}
                      onChange={(e) => handleOfferChange(idx, 'display', e.target.value)}
                      placeholder="15.6 FHD IPS"
                      className="w-full text-xs p-2 bg-white border border-slate-200 rounded-lg"
                    />
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Submit Button */}
        <div className="flex items-center justify-end gap-3 pt-4">
          <button
            type="button"
            onClick={() => navigate('/comparisons')}
            className="px-5 py-2.5 text-xs font-semibold text-slate-700 hover:text-slate-900"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={submitting}
            className="px-6 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-semibold shadow-xs transition-colors flex items-center gap-2 disabled:opacity-50"
          >
            {submitting ? 'Creating Comparison...' : 'Save & Analyze Comparison'}
            <ArrowRight className="w-4 h-4" />
          </button>
        </div>
      </form>
    </div>
  );
};
