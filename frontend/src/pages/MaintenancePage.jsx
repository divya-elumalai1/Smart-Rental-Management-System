import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Wrench, Clock, CheckCircle2, AlertTriangle, Plus } from 'lucide-react';
import toast from 'react-hot-toast';
import { maintenanceAPI } from '../utils/api';

const PRIORITY_CFG = {
  LOW: { color: 'text-cyan-300', bg: 'bg-cyan-500/10', ring: 'ring-cyan-500/30' },
  MEDIUM: { color: 'text-amber-300', bg: 'bg-amber-500/10', ring: 'ring-amber-500/30' },
  HIGH: { color: 'text-red-300', bg: 'bg-red-500/10', ring: 'ring-red-500/30' },
  URGENT: { color: 'text-red-300', bg: 'bg-red-500/10', ring: 'ring-red-500/30' },
};

const STATUS_CFG = {
  PENDING: { icon: Clock, color: 'text-amber-300', bg: 'bg-amber-500/10', ring: 'ring-amber-500/30' },
  IN_PROGRESS: { icon: Wrench, color: 'text-cyan-300', bg: 'bg-cyan-500/10', ring: 'ring-cyan-500/30' },
  RESOLVED: { icon: CheckCircle2, color: 'text-emerald-300', bg: 'bg-emerald-500/10', ring: 'ring-emerald-500/30' },
  CANCELLED: { icon: AlertTriangle, color: 'text-slate-400', bg: 'bg-slate-500/10', ring: 'ring-slate-500/30' },
};

export default function MaintenancePage() {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const res = await maintenanceAPI.getAll();
        setRequests(res.data || []);
      } catch {
        toast.error('Could not load maintenance requests');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  if (loading) return <div className="py-20 text-center text-slate-500">Loading maintenance requests…</div>;

  return (
    <div className="space-y-6">
      <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }}
        className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-2xl font-extrabold text-white" style={{ fontFamily: 'Plus Jakarta Sans' }}>Maintenance</h1>
          <p className="mt-1 text-sm text-slate-500">All maintenance requests across units</p>
        </div>
        <button
          onClick={() => toast('New request feature coming soon')}
          className="btn-shimmer flex items-center gap-2 rounded-xl bg-gradient-to-r from-indigo-500 to-violet-500 px-4 py-2.5 text-sm font-semibold text-white shadow-lg shadow-indigo-500/30"
        >
          <Plus className="h-4 w-4" /> New Request
        </button>
      </motion.div>

      {requests.length === 0 ? (
        <div className="py-20 text-center text-slate-500">No maintenance requests found</div>
      ) : (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
          {requests.map((req, i) => {
            const pri = PRIORITY_CFG[req.priority];
            const sts = STATUS_CFG[req.status];
            const StsIcon = sts.icon;
            const unitLabel = req.property_address || req.propertyAddress || `Unit ${req.unit_number || req.unitNumber || ''}`;
            const tenantLabel = req.tenant_name || req.tenantName || '';
            const dateLabel = req.created_at
              ? new Date(req.created_at || req.createdAt).toLocaleDateString('en-IN')
              : '';
            return (
              <motion.div key={req.id} initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.08 }}
                whileHover={{ y: -4 }} className="glass-card p-5">
                <div className="mb-3 flex items-start justify-between">
                  <div>
                    <p className="font-bold text-white">{req.title}</p>
                    <p className="mt-0.5 text-xs text-slate-500">{unitLabel}{tenantLabel ? ` • ${tenantLabel}` : ''}</p>
                  </div>
                  <span className={`rounded-full px-2.5 py-1 text-[10px] font-bold ring-1 ${pri.bg} ${pri.color} ${pri.ring}`}>{req.priority}</span>
                </div>
                <div className="flex items-center justify-between border-t border-white/[0.06] pt-3">
                  <span className={`flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ${sts.bg} ${sts.color} ${sts.ring}`}>
                    <StsIcon className="h-3.5 w-3.5" /> {req.status.replace('_', ' ')}
                  </span>
                  <span className="text-xs text-slate-500">{dateLabel}</span>
                </div>
              </motion.div>
            );
          })}
        </div>
      )}
    </div>
  );
}
