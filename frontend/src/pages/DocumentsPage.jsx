import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { FileText, Upload, Download, Trash2, X, Loader2 } from 'lucide-react';
import toast from 'react-hot-toast';
import { documentAPI, propertyAPI } from '../utils/api';

const CATEGORIES = [
  { value: 'AGREEMENT', label: 'Rental Agreement', color: 'text-red-400 bg-red-500/10' },
  { value: 'ID_PROOF', label: 'ID Proof (Aadhaar/PAN)', color: 'text-orange-400 bg-orange-500/10' },
  { value: 'NOC', label: 'NOC', color: 'text-cyan-400 bg-cyan-500/10' },
  { value: 'RECEIPT', label: 'Receipt', color: 'text-purple-400 bg-purple-500/10' },
  { value: 'OTHER', label: 'Other', color: 'text-slate-400 bg-slate-500/10' },
];

export default function DocumentsPage() {
  const [documents, setDocuments] = useState([]);
  const [properties, setProperties] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showUpload, setShowUpload] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const [selectedCategory, setSelectedCategory] = useState('AGREEMENT');
  const [selectedProperty, setSelectedProperty] = useState('');
  const [description, setDescription] = useState('');

  const fetchData = async () => {
    try {
      const [docsRes, propsRes] = await Promise.all([
        documentAPI.getAll().catch(() => ({ data: [] })),
        propertyAPI.getAll().catch(() => ({ data: [] })),
      ]);
      setDocuments(docsRes.data || []);
      setProperties(propsRes.data || []);
    } catch {
      // silent
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, []);

  const handleFileSelect = (e) => {
    const file = e.target.files?.[0];
    if (file) {
      if (file.size > 10 * 1024 * 1024) {
        toast.error('File size must be under 10MB');
        return;
      }
      const allowed = ['application/pdf', 'image/jpeg', 'image/png', 'image/gif',
        'application/msword',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
      if (!allowed.includes(file.type)) {
        toast.error('File type not allowed. Please upload PDF, JPG, or PNG');
        return;
      }
      setSelectedFile(file);
    }
  };

  const handleUpload = async () => {
    if (!selectedFile) {
      toast.error('Please select a file');
      return;
    }
    setUploading(true);
    try {
      const formData = new FormData();
      formData.append('file', selectedFile);
      formData.append('category', selectedCategory);
      if (selectedProperty) formData.append('propertyId', selectedProperty);
      if (description.trim()) formData.append('description', description.trim());

      await documentAPI.upload(formData);
      toast.success('Document uploaded successfully');
      setShowUpload(false);
      setSelectedFile(null);
      setSelectedCategory('AGREEMENT');
      setSelectedProperty('');
      setDescription('');
      fetchData();
    } catch (err) {
      const msg = err?.response?.data?.message || 'Failed to upload document';
      toast.error(msg);
    } finally {
      setUploading(false);
    }
  };

  const handleDelete = async (id) => {
    try {
      await documentAPI.delete(id);
      setDocuments((prev) => prev.filter((d) => d.id !== id));
      toast.success('Document deleted');
    } catch {
      toast.error('Failed to delete document');
    }
  };

  const formatFileSize = (bytes) => {
    if (!bytes) return '';
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  };

  const getCategoryStyle = (cat) => {
    return CATEGORIES.find(c => c.value === cat) || CATEGORIES[CATEGORIES.length - 1];
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <div className="h-8 w-8 animate-spin rounded-full border-2 border-indigo-400 border-t-transparent" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }}
        className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-2xl font-extrabold text-white" style={{ fontFamily: 'Plus Jakarta Sans' }}>Documents</h1>
          <p className="mt-1 text-sm text-slate-500">Rental agreements, ID proofs, and receipts per unit</p>
        </div>
        <button onClick={() => setShowUpload(true)}
          className="btn-shimmer flex items-center gap-2 rounded-xl bg-gradient-to-r from-indigo-500 to-violet-500 px-4 py-2.5 text-sm font-semibold text-white shadow-lg shadow-indigo-500/30">
          <Upload className="h-4 w-4" /> Upload Document
        </button>
      </motion.div>

      {/* Upload Modal */}
      {showUpload && (
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm"
          onClick={() => !uploading && setShowUpload(false)}>
          <motion.div initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }}
            className="glass-card w-full max-w-lg p-6"
            onClick={(e) => e.stopPropagation()}>
            <div className="mb-4 flex items-center justify-between">
              <h3 className="text-lg font-bold text-white">Upload Document</h3>
              <button onClick={() => setShowUpload(false)} disabled={uploading}
                className="rounded-lg p-1.5 text-slate-400 hover:bg-white/5 hover:text-white">
                <X className="h-5 w-5" />
              </button>
            </div>

            <div className="space-y-4">
              {/* File picker */}
              <div>
                <label className="mb-1 block text-xs font-medium text-slate-400">File</label>
                <div
                  onClick={() => document.getElementById('file-input')?.click()}
                  className="flex cursor-pointer flex-col items-center justify-center rounded-xl border-2 border-dashed border-white/[0.1] py-8 hover:border-indigo-500/30">
                  {selectedFile ? (
                    <div className="text-center">
                      <FileText className="mx-auto mb-2 h-8 w-8 text-indigo-400" />
                      <p className="text-sm font-medium text-white">{selectedFile.name}</p>
                      <p className="text-xs text-slate-500">{(selectedFile.size / 1024).toFixed(1)} KB</p>
                    </div>
                  ) : (
                    <div className="text-center">
                      <Upload className="mx-auto mb-2 h-8 w-8 text-slate-600" />
                      <p className="text-sm text-slate-400">Click to select a file</p>
                      <p className="mt-1 text-xs text-slate-600">PDF, JPG, PNG — max 10MB</p>
                    </div>
                  )}
                </div>
                <input id="file-input" type="file" accept=".pdf,.jpg,.jpeg,.png,.gif,.doc,.docx"
                  className="hidden" onChange={handleFileSelect} />
              </div>

              {/* Category */}
              <div>
                <label className="mb-1 block text-xs font-medium text-slate-400">Category</label>
                <select value={selectedCategory} onChange={e => setSelectedCategory(e.target.value)}
                  className="w-full rounded-xl border border-white/[0.08] bg-[#0F172A] px-4 py-2.5 text-sm text-white focus:border-indigo-500/50 focus:outline-none">
                  {CATEGORIES.map(cat => (
                    <option key={cat.value} value={cat.value}>{cat.label}</option>
                  ))}
                </select>
              </div>

              {/* Property */}
              <div>
                <label className="mb-1 block text-xs font-medium text-slate-400">Property (optional)</label>
                <select value={selectedProperty} onChange={e => setSelectedProperty(e.target.value)}
                  className="w-full rounded-xl border border-white/[0.08] bg-[#0F172A] px-4 py-2.5 text-sm text-white focus:border-indigo-500/50 focus:outline-none">
                  <option value="">General (no property)</option>
                  {properties.map(p => (
                    <option key={p.id} value={p.id}>
                      {p.unit_number || p.unitNumber || p.address || 'Unit ' + p.id?.substring(0, 8)}
                    </option>
                  ))}
                </select>
              </div>

              {/* Description */}
              <div>
                <label className="mb-1 block text-xs font-medium text-slate-400">Description (optional)</label>
                <input type="text" value={description} onChange={e => setDescription(e.target.value)}
                  placeholder="e.g. Signed rental agreement for Unit G1"
                  className="w-full rounded-xl border border-white/[0.08] bg-white/[0.03] px-4 py-2.5 text-sm text-white placeholder:text-slate-600 focus:border-indigo-500/50 focus:outline-none" />
              </div>

              {/* Actions */}
              <div className="flex gap-3 pt-2">
                <button onClick={handleUpload} disabled={!selectedFile || uploading}
                  className="btn-shimmer flex items-center gap-2 rounded-xl bg-gradient-to-r from-indigo-500 to-violet-500 px-5 py-2.5 text-sm font-semibold text-white shadow-lg shadow-indigo-500/30 disabled:opacity-50">
                  {uploading ? (
                    <><Loader2 className="h-4 w-4 animate-spin" /> Uploading...</>
                  ) : (
                    <><Upload className="h-4 w-4" /> Upload</>
                  )}
                </button>
                <button onClick={() => setShowUpload(false)} disabled={uploading}
                  className="rounded-xl border border-white/[0.08] bg-white/[0.03] px-5 py-2.5 text-sm font-medium text-slate-300 hover:bg-white/5">
                  Cancel
                </button>
              </div>
            </div>
          </motion.div>
        </motion.div>
      )}

      {/* Documents list */}
      {documents.length === 0 ? (
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}
          className="glass-card flex flex-col items-center justify-center py-12">
          <FileText className="mb-3 h-10 w-10 text-slate-600" />
          <p className="text-sm font-medium text-slate-400">No documents uploaded yet</p>
          <button onClick={() => setShowUpload(true)}
            className="mt-4 flex items-center gap-2 rounded-xl bg-white/[0.05] px-4 py-2 text-sm text-slate-300 hover:bg-white/10">
            <Upload className="h-4 w-4" /> Upload your first document
          </button>
        </motion.div>
      ) : (
        <div className="space-y-4">
          {properties.map((property, i) => {
            const propertyDocs = documents.filter((d) => d.property_id === property.id);
            if (propertyDocs.length === 0) return null;
            return (
              <motion.div key={property.id} initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.08 }}
                className="glass-card p-5">
                <div className="mb-3 flex items-center gap-2">
                  <FileText className="h-4 w-4 text-indigo-400" />
                  <h3 className="font-bold text-white">
                    {property.unit_number ? 'Unit ' + property.unit_number : property.address || 'Property'}
                  </h3>
                </div>
                <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
                  {propertyDocs.map((doc) => {
                    const style = getCategoryStyle(doc.category);
                    return (
                      <div key={doc.id} className="flex items-center justify-between rounded-xl bg-white/[0.03] p-3">
                        <div className="flex items-center gap-3 min-w-0">
                          <div className={`rounded-lg p-2 ${style.color}`}>
                            <FileText className="h-4 w-4" />
                          </div>
                          <div className="min-w-0">
                            <p className="truncate text-sm font-medium text-slate-200">{doc.file_name || doc.fileName || 'Document'}</p>
                            <p className="text-xs text-slate-500">{style.label} {doc.file_size ? `• ${formatFileSize(doc.file_size)}` : ''}</p>
                          </div>
                        </div>
                        <div className="flex shrink-0 items-center gap-1">
                          {doc.file_url && (
                            <a href={doc.file_url} target="_blank" rel="noopener noreferrer"
                              className="rounded-lg p-1.5 text-slate-400 hover:bg-white/10 hover:text-white">
                              <Download className="h-4 w-4" />
                            </a>
                          )}
                          <button onClick={() => handleDelete(doc.id)}
                            className="rounded-lg p-1.5 text-slate-400 hover:bg-red-500/10 hover:text-red-400">
                            <Trash2 className="h-4 w-4" />
                          </button>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </motion.div>
            );
          })}
          {/* General documents (no property) */}
          {documents.filter((d) => !d.property_id).length > 0 && (
            <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }}
              className="glass-card p-5">
              <div className="mb-3 flex items-center gap-2">
                <FileText className="h-4 w-4 text-slate-400" />
                <h3 className="font-bold text-white">General Documents</h3>
              </div>
              <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
                {documents.filter((d) => !d.property_id).map((doc) => {
                  const style = getCategoryStyle(doc.category);
                  return (
                    <div key={doc.id} className="flex items-center justify-between rounded-xl bg-white/[0.03] p-3">
                      <div className="flex items-center gap-3 min-w-0">
                        <div className={`rounded-lg p-2 ${style.color}`}>
                          <FileText className="h-4 w-4" />
                        </div>
                        <div className="min-w-0">
                          <p className="truncate text-sm font-medium text-slate-200">{doc.file_name || doc.fileName || 'Document'}</p>
                          <p className="text-xs text-slate-500">{style.label} {doc.file_size ? `• ${formatFileSize(doc.file_size)}` : ''}</p>
                        </div>
                      </div>
                      <div className="flex shrink-0 items-center gap-1">
                        {doc.file_url && (
                          <a href={doc.file_url} target="_blank" rel="noopener noreferrer"
                            className="rounded-lg p-1.5 text-slate-400 hover:bg-white/10 hover:text-white">
                            <Download className="h-4 w-4" />
                          </a>
                        )}
                        <button onClick={() => handleDelete(doc.id)}
                          className="rounded-lg p-1.5 text-slate-400 hover:bg-red-500/10 hover:text-red-400">
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            </motion.div>
          )}
        </div>
      )}
    </div>
  );
}
