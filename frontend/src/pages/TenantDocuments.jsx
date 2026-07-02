import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { FileText, Download, Eye, Loader2 } from 'lucide-react';
import { documentAPI, tenantPortalAPI } from '../utils/api';
import toast from 'react-hot-toast';

const FILE_ICONS = {
  'application/pdf': { icon: FileText, color: 'text-red-400 bg-red-500/10' },
  'image/jpeg': { icon: FileText, color: 'text-blue-400 bg-blue-500/10' },
  'image/png': { icon: FileText, color: 'text-green-400 bg-green-500/10' },
  'application/msword': { icon: FileText, color: 'text-indigo-400 bg-indigo-500/10' },
  default: { icon: FileText, color: 'text-slate-400 bg-slate-500/10' },
};

export default function TenantDocuments() {
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [unitInfo, setUnitInfo] = useState(null);

  useEffect(() => {
    (async () => {
      try {
        const unitRes = await tenantPortalAPI.getMyUnit();
        const unit = unitRes.data;
        setUnitInfo(unit);
        if (unit?.id) {
          try {
            const docRes = await documentAPI.getByProperty(unit.id);
            setDocuments(docRes.data || []);
          } catch {
            setDocuments([]);
          }
        }
      } catch (err) {
        toast.error('Failed to load documents');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const handleDownload = async (doc) => {
    if (doc.document_url) {
      window.open(doc.document_url, '_blank');
    } else if (doc.id) {
      try {
        const res = await documentAPI.getByProperty(doc.property_id);
        const updated = res.data?.find(d => d.id === doc.id);
        if (updated?.document_url) {
          window.open(updated.document_url, '_blank');
        } else {
          toast.error('Document URL not available');
        }
      } catch {
        toast.error('Failed to retrieve document');
      }
    } else {
      toast.error('Document not available');
    }
  };

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-2 border-indigo-500 border-t-transparent" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }}>
        <h1 className="text-2xl font-extrabold text-white" style={{ fontFamily: 'Plus Jakarta Sans' }}>Documents</h1>
        <p className="mt-1 text-sm text-slate-500">Property documents & agreements</p>
      </motion.div>

      {unitInfo && (
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="glass-card p-4">
          <p className="text-xs text-slate-500">Property</p>
          <p className="text-sm font-medium text-white">
            {unitInfo.unit_number && `Unit ${unitInfo.unit_number}`}{unitInfo.unit_number && unitInfo.address ? ' — ' : ''}{unitInfo.address || ''}
          </p>
        </motion.div>
      )}

      {documents.length === 0 ? (
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="flex flex-col items-center justify-center py-16">
          <FileText className="mb-4 h-16 w-16 text-slate-600" />
          <p className="text-lg font-medium text-slate-400">No documents available</p>
          <p className="mt-1 text-sm text-slate-500">Your lease agreement and other documents will appear here</p>
        </motion.div>
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {documents.map((doc, i) => {
            const mime = doc.file_type || doc.document_type || 'default';
            const fileIcon = FILE_ICONS[mime] || FILE_ICONS.default;
            const Icon = fileIcon.icon;
            return (
              <motion.div key={doc.id} initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.05 }}
                className="glass-card p-5">
                <div className="mb-3 flex items-start justify-between">
                  <div className={`rounded-xl p-3 ${fileIcon.color}`}>
                    <Icon className="h-6 w-6" />
                  </div>
                </div>
                <h3 className="font-semibold text-white">{doc.title || doc.file_name || 'Document'}</h3>
                {doc.file_size && (
                  <p className="mt-1 text-xs text-slate-500">
                    {(Number(doc.file_size) / 1024).toFixed(1)} KB
                  </p>
                )}
                <p className="mt-0.5 text-[10px] text-slate-600">
                  {doc.uploaded_at ? new Date(doc.uploaded_at).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' }) : ''}
                </p>
                <div className="mt-4 flex gap-2">
                  <button onClick={() => handleDownload(doc)}
                    className="flex items-center gap-1.5 rounded-lg bg-indigo-500/10 px-3 py-1.5 text-xs font-medium text-indigo-300 hover:bg-indigo-500/20">
                    <Eye className="h-3 w-3" /> View
                  </button>
                  <button onClick={() => handleDownload(doc)}
                    className="flex items-center gap-1.5 rounded-lg bg-white/[0.05] px-3 py-1.5 text-xs font-medium text-slate-300 hover:bg-white/10">
                    <Download className="h-3 w-3" /> Download
                  </button>
                </div>
              </motion.div>
            );
          })}
        </div>
      )}
    </div>
  );
}
