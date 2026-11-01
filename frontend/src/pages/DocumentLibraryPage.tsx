import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { DocumentItem, DocumentType } from '../types';
import {
  FileCheck2,
  Upload,
  FileText,
  Clock,
  ArrowRight,
  ShieldCheck,
  CheckCircle2,
  AlertCircle,
  Trash2,
  Download,
} from 'lucide-react';

export const DocumentLibraryPage: React.FC = () => {
  const [documents, setDocuments] = useState<DocumentItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [fileToUpload, setFileToUpload] = useState<File | null>(null);
  const [docType, setDocType] = useState<DocumentType>('INVOICE');
  const [uploadError, setUploadError] = useState<string | null>(null);

  useEffect(() => {
    fetchDocuments();
  }, []);

  const fetchDocuments = async () => {
    setLoading(true);
    try {
      const data = await api.get<DocumentItem[]>('/documents');
      setDocuments(data);
    } catch (e) {
      console.error('Failed to load documents', e);
    } finally {
      setLoading(false);
    }
  };

  const handleFileUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!fileToUpload) return;

    setUploading(true);
    setUploadError(null);

    const formData = new FormData();
    formData.append('file', fileToUpload);
    formData.append('documentType', docType);

    try {
      await api.upload('/documents/upload', formData);
      setFileToUpload(null);
      // Reset input
      const fileInput = document.getElementById('file-upload-input') as HTMLInputElement;
      if (fileInput) fileInput.value = '';
      fetchDocuments();
    } catch (err: any) {
      setUploadError(err.message || 'Failed to upload document');
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async (e: React.MouseEvent, id: number) => {
    e.preventDefault();
    if (!window.confirm('Delete this document and all associated extracted facts?')) return;

    try {
      await api.delete(`/documents/${id}`);
      setDocuments((prev) => prev.filter((d) => d.id !== id));
    } catch (err: any) {
      alert('Failed to delete document: ' + err.message);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold text-slate-900 tracking-tight flex items-center gap-2">
            <FileCheck2 className="w-6 h-6 text-emerald-600" />
            Document Understanding & Verification
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Upload purchase receipts, warranty terms, and invoices. Review and verify AI-extracted facts before they become trusted application data.
          </p>
        </div>
      </div>

      {/* Upload Box */}
      <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs">
        <h2 className="text-sm font-bold text-slate-900 mb-1 flex items-center gap-2">
          <Upload className="w-4 h-4 text-slate-700" />
          Upload Document for AI Fact Extraction
        </h2>
        <p className="text-xs text-slate-500 mb-4">
          Supports text, PDF receipts, warranty clauses, or return policies.
        </p>

        {uploadError && (
          <div className="mb-4 p-3 bg-rose-50 border border-rose-200 rounded-xl text-xs text-rose-700 flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            {uploadError}
          </div>
        )}

        <form onSubmit={handleFileUpload} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div className="sm:col-span-2">
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Choose Document File
              </label>
              <input
                id="file-upload-input"
                type="file"
                onChange={(e) => setFileToUpload(e.target.files?.[0] || null)}
                required
                className="block w-full text-xs text-slate-500 file:mr-4 file:py-2 file:px-4 file:rounded-xl file:border-0 file:text-xs file:font-semibold file:bg-slate-900 file:text-white hover:file:bg-slate-800 cursor-pointer border border-slate-200 rounded-xl p-1 bg-slate-50"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Document Classification
              </label>
              <select
                value={docType}
                onChange={(e) => setDocType(e.target.value as DocumentType)}
                className="w-full text-xs p-2.5 border border-slate-200 rounded-xl bg-white focus:ring-2 focus:ring-slate-900"
              >
                <option value="INVOICE">Official Invoice / Facture</option>
                <option value="RECEIPT">Store Purchase Receipt</option>
                <option value="WARRANTY">Warranty Deed / Certificate</option>
                <option value="RETURN_POLICY">Return Policy Terms</option>
                <option value="SPEC_SHEET">Technical Specification Sheet</option>
              </select>
            </div>
          </div>

          <div className="flex justify-end">
            <button
              type="submit"
              disabled={uploading || !fileToUpload}
              className="px-5 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-semibold shadow-xs transition-colors disabled:opacity-50 flex items-center gap-2"
            >
              <Upload className="w-4 h-4" />
              {uploading ? 'Processing & Extracting Facts...' : 'Upload & Extract Facts'}
            </button>
          </div>
        </form>
      </div>

      {/* Documents List */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="p-5 border-b border-slate-100 flex items-center justify-between">
          <h3 className="text-sm font-bold text-slate-900">
            Stored Documents & Verification Status ({documents.length})
          </h3>
          <span className="text-xs text-slate-500">
            Click any document to inspect and verify extracted facts
          </span>
        </div>

        {loading ? (
          <div className="p-8 space-y-3 animate-pulse">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-16 bg-slate-100 rounded-xl" />
            ))}
          </div>
        ) : documents.length === 0 ? (
          <div className="p-12 text-center">
            <FileText className="w-10 h-10 text-slate-300 mx-auto mb-2" />
            <p className="text-xs text-slate-500">
              No documents uploaded yet. Upload a receipt or invoice above to test fact extraction.
            </p>
          </div>
        ) : (
          <div className="divide-y divide-slate-100">
            {documents.map((doc) => {
              const pendingCount = doc.extractedFacts?.filter(
                (f) => f.verificationStatus === 'PENDING_VERIFICATION'
              ).length || 0;
              const verifiedCount = doc.extractedFacts?.filter(
                (f) => f.verificationStatus === 'VERIFIED'
              ).length || 0;

              return (
                <div
                  key={doc.id}
                  className="p-4 sm:p-5 hover:bg-slate-50 transition-colors flex flex-col sm:flex-row sm:items-center justify-between gap-4"
                >
                  <div className="flex items-start gap-3">
                    <div className="w-10 h-10 rounded-xl bg-slate-100 text-slate-700 flex items-center justify-center shrink-0">
                      <FileText className="w-5 h-5 text-emerald-600" />
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <h4 className="text-xs font-bold text-slate-900">{doc.fileName}</h4>
                        <span className="px-2 py-0.5 bg-slate-100 text-slate-700 text-[10px] font-semibold rounded-md">
                          {doc.documentType}
                        </span>
                      </div>
                      <p className="text-[11px] text-slate-500 mt-0.5">
                        Uploaded {new Date(doc.createdAt).toLocaleDateString()} • {(doc.fileSizeBytes / 1024).toFixed(1)} KB
                      </p>

                      <div className="flex items-center gap-2 mt-2">
                        {pendingCount > 0 ? (
                          <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-amber-700 bg-amber-50 px-2 py-0.5 rounded-md border border-amber-200">
                            <Clock className="w-3 h-3" />
                            {pendingCount} Facts Pending Review
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1 text-[11px] font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-md border border-emerald-200">
                            <CheckCircle2 className="w-3 h-3" />
                            {verifiedCount} Facts Verified
                          </span>
                        )}
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center gap-2 shrink-0">
                    <Link
                      to={`/documents/${doc.id}/verify`}
                      className="px-3.5 py-1.5 bg-slate-900 hover:bg-slate-800 text-white rounded-xl text-xs font-semibold shadow-xs transition-colors flex items-center gap-1.5"
                    >
                      Inspect & Verify Facts
                      <ArrowRight className="w-3.5 h-3.5" />
                    </Link>

                    <button
                      onClick={(e) => handleDelete(e, doc.id)}
                      className="p-1.5 text-slate-400 hover:text-rose-600 rounded-lg hover:bg-slate-100 transition-colors"
                      title="Delete document"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};
