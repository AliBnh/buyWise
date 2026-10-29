import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import {
  Comparison,
  StrategyType,
  OfferAnalysisAiResult,
} from '../types';
import { useCurrency } from '../context/CurrencyContext';
import {
  Scale,
  Sparkles,
  ArrowLeft,
  CheckCircle2,
  AlertTriangle,
  Clock,
  ShieldCheck,
  Tag,
  Building,
  RefreshCw,
  Trophy,
  ExternalLink,
  Plus,
} from 'lucide-react';

export const ComparisonDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { formatPrice } = useCurrency();

  const [comparison, setComparison] = useState<Comparison | null>(null);
  const [loading, setLoading] = useState(true);
  const [analyzingAi, setAnalyzingAi] = useState(false);
  const [selectedStrategy, setSelectedStrategy] = useState<StrategyType>('BEST_VALUE');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchComparison();
  }, [id]);

  const fetchComparison = async () => {
    setLoading(true);
    try {
      const data = await api.get<Comparison>(`/comparisons/${id}`);
      setComparison(data);
    } catch (e: any) {
      setError(e.message || 'Failed to load comparison details');
    } finally {
      setLoading(false);
    }
  };

  const handleRunAiAnalysis = async () => {
    if (!id) return;
    setAnalyzingAi(true);
    try {
      const result = await api.post<OfferAnalysisAiResult>(`/ai/comparisons/${id}/analyze`);
      // Update local state with fresh AI analysis
      if (comparison) {
        setComparison({
          ...comparison,
          aiAnalysisJson: JSON.stringify(result),
          status: 'ANALYZED',
        });
      }
    } catch (e: any) {
      alert('AI Analysis warning: ' + e.message);
    } finally {
      setAnalyzingAi(false);
    }
  };

  if (loading) {
    return (
      <div className="space-y-6 animate-pulse">
        <div className="h-20 bg-slate-200 rounded-2xl w-full" />
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="h-64 bg-slate-200 rounded-2xl" />
          <div className="h-64 bg-slate-200 rounded-2xl" />
        </div>
      </div>
    );
  }

  if (error || !comparison) {
    return (
      <div className="p-8 text-center bg-white border border-slate-200 rounded-2xl">
        <AlertTriangle className="w-10 h-10 text-amber-500 mx-auto mb-3" />
        <h3 className="text-base font-semibold text-slate-900 mb-1">Could not find comparison</h3>
        <p className="text-xs text-slate-500 mb-4">{error}</p>
        <button
          onClick={() => navigate('/comparisons')}
          className="px-4 py-2 bg-slate-900 text-white rounded-xl text-xs font-semibold"
        >
          Return to Comparisons
        </button>
      </div>
    );
  }

  const math = comparison.deterministicSummary;
  const parsedAiAnalysis: OfferAnalysisAiResult | null = comparison.aiAnalysisJson
    ? JSON.parse(comparison.aiAnalysisJson)
    : null;

  const currentRankings = math?.rankingsByStrategy?.[selectedStrategy] || [];

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <button
            onClick={() => navigate('/comparisons')}
            className="inline-flex items-center gap-1.5 text-xs text-slate-500 hover:text-slate-900 mb-2 transition-colors"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            Back to comparisons
          </button>
          <div className="flex items-center gap-3">
            <h1 className="text-xl sm:text-2xl font-bold text-slate-900 tracking-tight">
              {comparison.title}
            </h1>
            <span className="px-2.5 py-0.5 bg-slate-100 text-slate-700 text-xs font-semibold rounded-md">
              {comparison.categoryName}
            </span>
          </div>
          {comparison.notes && (
            <p className="text-xs text-slate-500 mt-1 max-w-2xl leading-relaxed">
              {comparison.notes}
            </p>
          )}
        </div>

        <div className="flex items-center gap-2 shrink-0">
          <button
            onClick={handleRunAiAnalysis}
            disabled={analyzingAi}
            className="inline-flex items-center gap-2 px-4 py-2 bg-slate-900 hover:bg-slate-800 text-white rounded-xl text-xs font-semibold shadow-xs transition-colors disabled:opacity-50"
          >
            <Sparkles className={`w-4 h-4 text-emerald-400 ${analyzingAi ? 'animate-spin' : ''}`} />
            {analyzingAi ? 'Analyzing Trade-offs...' : parsedAiAnalysis ? 'Re-run AI Analysis' : 'Run AI Strategic Analysis'}
          </button>
        </div>
      </div>

      {/* Deterministic Mathematical Breakdown Card */}
      {math && (
        <div className="bg-gradient-to-br from-white to-slate-50 p-6 rounded-2xl border border-slate-200 shadow-xs">
          <div className="flex items-center justify-between pb-3 border-b border-slate-200/80 mb-4">
            <div className="flex items-center gap-2">
              <span className="p-1.5 bg-emerald-100 text-emerald-800 rounded-lg">
                <Scale className="w-4 h-4" />
              </span>
              <h2 className="text-sm font-bold text-slate-900">
                Deterministic Cost & Value Breakdown
              </h2>
            </div>
            <span className="text-[10px] text-slate-500 font-medium bg-slate-100 px-2.5 py-1 rounded-full">
              Calculated Strictly via Backend Math Engine
            </span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="p-4 bg-white rounded-xl border border-slate-200/80">
              <span className="text-[11px] font-medium text-slate-500 block mb-1">
                Lowest Delivered Price
              </span>
              <p className="text-lg font-extrabold text-emerald-700">
                {formatPrice(math.lowestTotalPrice)}
              </p>
              <p className="text-[11px] text-slate-600 mt-0.5">
                Offered by <strong>{math.cheapestOfferSeller}</strong>
              </p>
            </div>

            <div className="p-4 bg-white rounded-xl border border-slate-200/80">
              <span className="text-[11px] font-medium text-slate-500 block mb-1">
                Net Price Difference
              </span>
              <p className="text-lg font-extrabold text-slate-900">
                +{formatPrice(math.absolutePriceDifference)}
              </p>
              <p className="text-[11px] text-slate-600 mt-0.5">
                {math.percentagePriceDifference}% difference across sellers
              </p>
            </div>

            <div className="p-4 bg-white rounded-xl border border-slate-200/80">
              <span className="text-[11px] font-medium text-slate-500 block mb-1">
                Warranty Protection Spread
              </span>
              <p className="text-lg font-extrabold text-blue-700">
                {math.longestWarrantyMonths} Months
              </p>
              <p className="text-[11px] text-slate-600 mt-0.5">
                +{math.warrantyDifferenceMonths} months extra coverage
              </p>
            </div>

            <div className="p-4 bg-white rounded-xl border border-slate-200/80">
              <span className="text-[11px] font-medium text-slate-500 block mb-1">
                Return Windows
              </span>
              <p className="text-lg font-extrabold text-purple-700">
                {Math.max(...comparison.offers.map((o) => o.returnWindowDays))} Days
              </p>
              <p className="text-[11px] text-slate-600 mt-0.5">
                Money-back evaluation window
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Strategy Switcher & Ranked Cards */}
      <div className="space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div>
            <h3 className="text-sm font-bold text-slate-900">
              Competing Offers ({comparison.offers.length})
            </h3>
            <p className="text-xs text-slate-500">
              Switch priorities below to see how rankings adapt transparently.
            </p>
          </div>

          {/* Strategy Tabs */}
          <div className="flex items-center bg-white p-1 rounded-xl border border-slate-200 text-xs font-semibold overflow-x-auto">
            {(
              [
                { type: 'BEST_VALUE', label: 'Best Value Index' },
                { type: 'LOWEST_PRICE', label: 'Lowest Price' },
                { type: 'BEST_WARRANTY', label: 'Best Warranty' },
                { type: 'BALANCED', label: 'Balanced 2-Year' },
              ] as { type: StrategyType; label: string }[]
            ).map((s) => (
              <button
                key={s.type}
                onClick={() => setSelectedStrategy(s.type)}
                className={`px-3 py-1.5 rounded-lg transition-colors whitespace-nowrap ${
                  selectedStrategy === s.type
                    ? 'bg-slate-900 text-white shadow-xs'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                {s.label}
              </button>
            ))}
          </div>
        </div>

        {/* Side-by-Side Offer Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {comparison.offers.map((offer) => {
            const ranking = currentRankings.find((r) => r.offerId === offer.id);
            const isWinner = ranking?.isWinner || false;

            return (
              <div
                key={offer.id}
                className={`bg-white rounded-2xl p-5 border transition-all flex flex-col justify-between ${
                  isWinner
                    ? 'border-emerald-500 ring-2 ring-emerald-500/10 shadow-md'
                    : 'border-slate-200 shadow-xs'
                }`}
              >
                <div>
                  {/* Top Bar with Seller and Winner Badge */}
                  <div className="flex items-start justify-between gap-2 mb-3">
                    <span className="flex items-center gap-1.5 text-xs font-bold text-slate-800">
                      <Building className="w-4 h-4 text-slate-400" />
                      {offer.sellerName}
                    </span>

                    {isWinner && (
                      <span className="inline-flex items-center gap-1 px-2.5 py-1 bg-emerald-50 text-emerald-800 border border-emerald-200 rounded-full text-[11px] font-bold">
                        <Trophy className="w-3 h-3 text-emerald-600" />
                        Top Pick
                      </span>
                    )}
                  </div>

                  <h4 className="font-bold text-sm text-slate-900 mb-2 leading-tight">
                    {offer.title}
                  </h4>

                  {/* Pricing Breakdown */}
                  <div className="bg-slate-50 p-3 rounded-xl border border-slate-200/60 mb-4 space-y-1 text-xs">
                    <div className="flex justify-between text-slate-500">
                      <span>Base Retail Price</span>
                      <span>{formatPrice(offer.basePrice)}</span>
                    </div>
                    <div className="flex justify-between text-slate-500">
                      <span>Delivery Fee</span>
                      <span>
                        {offer.deliveryFee === 0 ? (
                          <span className="text-emerald-600 font-semibold">Free</span>
                        ) : (
                          formatPrice(offer.deliveryFee)
                        )}
                      </span>
                    </div>
                    {offer.discountAmount > 0 && (
                      <div className="flex justify-between text-emerald-600">
                        <span>Discount</span>
                        <span>-{formatPrice(offer.discountAmount)}</span>
                      </div>
                    )}
                    <div className="flex justify-between font-extrabold text-slate-900 pt-1.5 border-t border-slate-200 text-sm">
                      <span>Net Total Delivered</span>
                      <span className="text-emerald-700">{formatPrice(offer.totalPrice)}</span>
                    </div>
                  </div>

                  {/* Terms & Warranty */}
                  <div className="space-y-2 text-xs mb-4">
                    <div className="flex items-center justify-between p-2 bg-blue-50/60 border border-blue-100 rounded-lg">
                      <span className="text-blue-900 font-medium flex items-center gap-1.5">
                        <ShieldCheck className="w-3.5 h-3.5 text-blue-600" />
                        Warranty Coverage
                      </span>
                      <span className="font-bold text-blue-950">
                        {offer.warrantyMonths} Months
                      </span>
                    </div>

                    <div className="flex items-center justify-between p-2 bg-purple-50/60 border border-purple-100 rounded-lg">
                      <span className="text-purple-900 font-medium flex items-center gap-1.5">
                        <Clock className="w-3.5 h-3.5 text-purple-600" />
                        Return Policy Window
                      </span>
                      <span className="font-bold text-purple-950">
                        {offer.returnWindowDays} Days
                      </span>
                    </div>
                  </div>

                  {/* Specifications Preview */}
                  {offer.specifications && Object.keys(offer.specifications).length > 0 && (
                    <div className="pt-3 border-t border-slate-100">
                      <p className="text-[10px] font-bold text-slate-400 uppercase tracking-wide mb-2">
                        Key Specifications
                      </p>
                      <div className="space-y-1.5 text-xs text-slate-700">
                        {Object.entries(offer.specifications).slice(0, 4).map(([k, v]) => (
                          <div key={k} className="flex justify-between">
                            <span className="text-slate-500">{k}:</span>
                            <span className="font-medium text-slate-900 text-right">{String(v)}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </div>

                {/* Strategy Evaluation Rationale */}
                {ranking && (
                  <div className="mt-4 pt-3 border-t border-slate-100 text-[11px] text-slate-600">
                    <p className="font-semibold text-slate-900 mb-0.5">
                      Strategy Score: {ranking.score}/100 (Rank #{ranking.rank})
                    </p>
                    <p className="text-slate-500 leading-tight">{ranking.rationale}</p>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* Specifications Comparison Matrix Table */}
      {math?.specificationComparisonMatrix && Object.keys(math.specificationComparisonMatrix).length > 0 && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
          <div className="p-4 sm:p-5 border-b border-slate-200 flex items-center justify-between">
            <h3 className="text-sm font-bold text-slate-900">
              Normalized Specification Comparison Matrix
            </h3>
            <span className="text-xs text-slate-500">
              Attribute-by-attribute side comparison
            </span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-xs text-left">
              <thead className="bg-slate-50 border-b border-slate-200 text-slate-500 uppercase tracking-wider text-[10px]">
                <tr>
                  <th className="py-3 px-4 font-semibold">Specification</th>
                  {comparison.offers.map((o) => (
                    <th key={o.id} className="py-3 px-4 font-semibold">
                      {o.sellerName}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {Object.entries(math.specificationComparisonMatrix).map(([attr, offerValues]) => (
                  <tr key={attr} className="hover:bg-slate-50/70 transition-colors">
                    <td className="py-2.5 px-4 font-semibold text-slate-800 bg-slate-50/40 w-1/4">
                      {attr}
                    </td>
                    {comparison.offers.map((o) => {
                      const val = offerValues[o.id] || '—';
                      return (
                        <td key={o.id} className="py-2.5 px-4 text-slate-700">
                          {val}
                        </td>
                      );
                    })}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* AI Strategic Analysis Report Panel */}
      {parsedAiAnalysis && (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xs p-6 space-y-6">
          <div className="flex items-center justify-between pb-3 border-b border-slate-200">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-xl bg-slate-900 text-emerald-400 flex items-center justify-center">
                <Sparkles className="w-4 h-4" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-slate-900">
                  BuyWise AI Strategic Trade-off Analysis
                </h3>
                <p className="text-[11px] text-slate-500">
                  Grounded in verified product specifications and warranty terms
                </p>
              </div>
            </div>

            <span className="px-2.5 py-1 bg-emerald-50 text-emerald-800 border border-emerald-200 text-xs font-semibold rounded-lg">
              Confidence: {Math.round(parsedAiAnalysis.confidenceScore * 100)}%
            </span>
          </div>

          {/* Executive Summary */}
          <div className="p-4 bg-slate-50 border border-slate-200 rounded-xl">
            <p className="text-xs font-bold text-slate-900 mb-1">Executive Recommendation</p>
            <p className="text-xs text-slate-700 leading-relaxed">
              {parsedAiAnalysis.executiveSummary}
            </p>
            {parsedAiAnalysis.recommendationRationale && (
              <p className="text-xs text-emerald-800 font-medium mt-2 pt-2 border-t border-slate-200">
                Rationale: {parsedAiAnalysis.recommendationRationale}
              </p>
            )}
          </div>

          {/* Key Spec Differences & Risk Alerts */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="p-4 bg-blue-50/50 border border-blue-200/60 rounded-xl">
              <h4 className="text-xs font-bold text-blue-900 mb-2 flex items-center gap-1.5">
                <CheckCircle2 className="w-4 h-4 text-blue-600" />
                Key Specification Differences
              </h4>
              <ul className="space-y-1.5 text-xs text-blue-950">
                {parsedAiAnalysis.keySpecificationDifferences.map((diff, i) => (
                  <li key={i} className="flex items-start gap-2">
                    <span className="w-1.5 h-1.5 rounded-full bg-blue-500 mt-1.5 shrink-0" />
                    <span>{diff}</span>
                  </li>
                ))}
              </ul>
            </div>

            <div className="p-4 bg-amber-50/50 border border-amber-200/60 rounded-xl">
              <h4 className="text-xs font-bold text-amber-900 mb-2 flex items-center gap-1.5">
                <AlertTriangle className="w-4 h-4 text-amber-600" />
                Missing Information & Purchasing Risks
              </h4>
              <ul className="space-y-1.5 text-xs text-amber-950">
                {parsedAiAnalysis.missingInformationAlerts?.map((alert, i) => (
                  <li key={`alert-${i}`} className="flex items-start gap-2">
                    <span className="w-1.5 h-1.5 rounded-full bg-amber-500 mt-1.5 shrink-0" />
                    <span>{alert}</span>
                  </li>
                ))}
                {parsedAiAnalysis.purchasingRisks?.map((risk, i) => (
                  <li key={`risk-${i}`} className="flex items-start gap-2 text-amber-900 font-medium">
                    <span className="w-1.5 h-1.5 rounded-full bg-rose-500 mt-1.5 shrink-0" />
                    <span>Risk: {risk}</span>
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
