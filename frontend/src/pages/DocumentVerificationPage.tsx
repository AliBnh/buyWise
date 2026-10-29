import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../api/client';
import { DocumentItem, ExtractedFact, VerificationStatus } from '../types';
import {
  FileCheck2,
  ArrowLeft,
  Check,
  Edit2,
  X,
  ShieldCheck,
  AlertTriangle,
  FileText,
  Clock,
  Sparkles,
} from 'lucide-react';

export const DocumentVerificationPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [documentItem, setDocumentItem] = useState<DocumentItem | null>(null);
  const [loading, setLoading] = useState(true);
  const [editingFactId, setEditingFactId] = useState<number | null>(null);
  const [editValue, setEditValue] = useState('');
  const [applyToPurchase, setApplyToPurchase] = useState(true);
  const [actionLoading, setActionLoading] = useState<number | null>(null);

  useEffect(() => {
    fetchDocument();
  }, [id]);

  const fetchDocument = async () => {
    setLoading(true);
    try {
      const data = await api.get<DocumentItem>(`/documents/${id}`);
      setDocumentItem(data);
    } catch (e) {
      console.error('Failed to load document', e);
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyFact = async (
    factId: number,
    status: VerificationStatus,
    customValue?: string
  ) => {
    setActionLoading(factId);
    try {
      const updatedFact = await api.patch<ExtractedFact>(`/documents/facts/${factId}/verify`, {
        verificationStatus: status,
        verifiedValue: customValue,
        applyToLinkedResource: applyToPurchase,
      });

      if (documentItem) {
        setDocumentItem({
          ...documentItem,
          extractedFacts: documentItem.extractedFacts.map((f) =>
            f.id === factId ? updatedFact : f
          ),
        });
      }
      setEditingFactId(null);
    } catch (err: any) {
      alert('Verification update failed: ' + err.message);
    } finally {
      setActionLoading(null);
    }
  };

  if (loading) {
    return (
      <div className="space-y-6 animate-pulse">
        <div className="h-16 bg-slate-200 rounded-2xl w-full" />
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div className="h-96 bg-slate-200 rounded-2xl" />
          <div className="h-96 bg-slate-200 rounded-2xl" />
        </div>
      </div>
    );
  }

  if (!documentItem) {
    return (
      <div className="p-8 text-center bg-white border border-slate-200 rounded-2xl">
        <p className="text-xs text-slate-500 mb-3">Document not found</p>
        <button
          onClick={() => navigate('/documents')}
          className="px-4 py-2 bg-slate-900 text-white rounded-xl text-xs font-semibold"
        >
          Back to Documents
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
        <div>
          <button
            onClick={() => navigate('/documents')}
            className="inline-flex items-center gap-1.5 text-xs text-slate-500 hover:text-slate-900 mb-2 transition-colors"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            Back to Document Library
          </button>
          <div className="flex items-center gap-3">
            <h1 className="text-xl sm:text-2xl font-bold text-slate-900 tracking-tight">
              Fact Verification: {documentItem.fileName}
            </h1>
            <span className="px-2.5 py-0.5 bg-slate-100 text-slate-700 text-xs font-semibold rounded-md">
              {documentItem.documentType}
            </span>
          </div>
          <p className="text-xs text-slate-500 mt-1">
            Human-in-the-Loop review: Approve, correct, or reject extracted facts against the original source text.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <label className="flex items-center gap-2 text-xs font-medium text-slate-700 bg-slate-50 border border-slate-200 px-3 py-2 rounded-xl cursor-pointer">
            <input
              type="checkbox"
              checked={applyToPurchase}
              onChange={(e) => setApplyToPurchase(e.target.checked)}
              className="rounded text-emerald-600 focus:ring-slate-900"
            />
            <span>Apply verified facts to linked purchase</span>
          </label>
        </div>
      </div>

      {/* Side-by-side View */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Left: Document Raw Text / Source Evidence */}
        <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs flex flex-col">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100 mb-4">
            <h3 className="text-xs font-bold text-slate-900 flex items-center gap-2">
              <FileText className="w-4 h-4 text-slate-600" />
              Source Document Text Content
            </h3>
            <span className="text-[11px] text-slate-400 font-mono">
              {(documentItem.fileSizeBytes / 1024).toFixed(1)} KB
            </span>
          </div>

          <div className="flex-1 bg-slate-50 border border-slate-200/80 rounded-xl p-4 font-mono text-xs text-slate-800 leading-relaxed whitespace-pre-wrap overflow-y-auto max-h-[600px]">
            {documentItem.rawTextContent || 'No plain text could be parsed from this file.'}
          </div>
        </div>

        {/* Right: Extracted Facts Review Stream */}
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-xs font-bold text-slate-900 flex items-center gap-2">
              <Sparkles className="w-4 h-4 text-emerald-600" />
              Extracted Facts ({documentItem.extractedFacts.length})
            </h3>
            <span className="text-[11px] text-slate-500">
              {documentItem.extractedFacts.filter((f) => f.verificationStatus === 'VERIFIED').length} verified
            </span>
          </div>

          {documentItem.extractedFacts.length === 0 ? (
            <div className="p-8 text-center bg-white border border-slate-200 rounded-2xl text-xs text-slate-500">
              No facts extracted from this document.
            </div>
          ) : (
            <div className="space-y-3">
              {documentItem.extractedFacts.map((fact) => {
                const isEditing = editingFactId === fact.id;
                const isVerified = fact.verificationStatus === 'VERIFIED';
                const isRejected = fact.verificationStatus === 'REJECTED';
                const isModified = fact.verificationStatus === 'MODIFIED';

                return (
                  <div
                    key={fact.id}
                    className={`bg-white p-4 rounded-xl border shadow-xs transition-all ${
                      isVerified
                        ? 'border-emerald-200 bg-emerald-50/20'
                        : isRejected
                        ? 'border-slate-200 opacity-60'
                        : isModified
                        ? 'border-blue-200 bg-blue-50/20'
                        : 'border-slate-200'
                    }`}
                  >
                    <div className="flex items-start justify-between gap-2 mb-2">
                      <div className="flex items-center gap-2">
                        <span className="px-2 py-0.5 bg-slate-100 text-slate-800 font-mono text-[10px] font-bold rounded-md uppercase">
                          {fact.factKey.replace(/_/g, ' ')}
                        </span>
                        <span className="text-[10px] text-slate-400 font-medium">
                          Confidence: {Math.round(Number(fact.confidenceScore) * 100)}%
                        </span>
                      </div>

                      <span
                        className={`text-[10px] font-bold px-2 py-0.5 rounded-md ${
                          isVerified
                            ? 'bg-emerald-100 text-emerald-800'
                            : isRejected
                            ? 'bg-rose-100 text-rose-800'
                            : isModified
                            ? 'bg-blue-100 text-blue-800'
                            : 'bg-amber-100 text-amber-800'
                        }`}
                      >
                        {fact.verificationStatus.replace(/_/g, ' ')}
                      </span>
                    </div>

                    {/* Value Display / Edit Form */}
                    {isEditing ? (
                      <div className="space-y-2 my-2">
                        <input
                          type="text"
                          value={editValue}
                          onChange={(e) => setEditValue(e.target.value)}
                          className="w-full text-xs p-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-slate-900 bg-white"
                        />
                        <div className="flex gap-2">
                          <button
                            onClick={() => handleVerifyFact(fact.id, 'MODIFIED', editValue)}
                            disabled={actionLoading === fact.id}
                            className="px-3 py-1 bg-emerald-600 text-white rounded-lg text-xs font-semibold"
                          >
                            Save & Verify
                          </button>
                          <button
                            onClick={() => setEditingFactId(null)}
                            className="px-3 py-1 bg-slate-100 text-slate-700 rounded-lg text-xs"
                          >
                            Cancel
                          </button>
                        </div>
                      </div>
                    ) : (
                      <div className="my-2">
                        <p className="text-sm font-bold text-slate-900">
                          {fact.verifiedValue || fact.extractedValue}
                        </p>
                        {fact.verifiedValue && fact.verifiedValue !== fact.extractedValue && (
                          <p className="text-[10px] text-slate-400 mt-0.5">
                            Original raw extraction: {fact.extractedValue}
                          </p>
                        )}
                      </div>
                    )}

                    {/* Verbatim Source Evidence */}
                    {fact.sourceExcerpt && (
                      <div className="p-2.5 bg-slate-50 border border-slate-200/60 rounded-lg text-[11px] text-slate-600 my-2">
                        <span className="font-semibold text-slate-700 block text-[10px] mb-0.5">
                          Evidence in Document (Page {fact.pageNumber}):
                        </span>
                        <p className="italic font-mono text-slate-800">
                          "{fact.sourceExcerpt}"
                        </p>
                      </div>
                    )}

                    {/* Action Buttons */}
                    {!isEditing && (
                      <div className="flex items-center gap-2 pt-2 border-t border-slate-100 justify-end">
                        <button
                          onClick={() => {
                            setEditingFactId(fact.id);
                            setEditValue(fact.verifiedValue || fact.extractedValue);
                          }}
                          className="px-2.5 py-1 text-slate-600 hover:text-slate-900 text-xs font-medium rounded-lg hover:bg-slate-100 flex items-center gap-1 transition-colors"
                        >
                          <Edit2 className="w-3 h-3" />
                          Correct
                        </button>

                        <button
                          onClick={() => handleVerifyFact(fact.id, 'REJECTED')}
                          disabled={actionLoading === fact.id}
                          className="px-2.5 py-1 text-rose-600 hover:text-rose-700 text-xs font-medium rounded-lg hover:bg-rose-50 flex items-center gap-1 transition-colors"
                        >
                          <X className="w-3 h-3" />
                          Reject
                        </button>

                        <button
                          onClick={() => handleVerifyFact(fact.id, 'VERIFIED')}
                          disabled={actionLoading === fact.id || isVerified}
                          className={`px-3 py-1 rounded-lg text-xs font-semibold flex items-center gap-1 transition-colors shadow-xs ${
                            isVerified
                              ? 'bg-emerald-100 text-emerald-800 cursor-default'
                              : 'bg-emerald-600 hover:bg-emerald-700 text-white'
                          }`}
                        >
                          <Check className="w-3 h-3" />
                          {isVerified ? 'Verified' : 'Verify Fact'}
                        </button>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
