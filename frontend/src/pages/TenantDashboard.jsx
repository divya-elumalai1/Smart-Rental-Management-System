import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import { Home, IndianRupee, CreditCard, Clock, Wrench, FileText, Download } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { dashboardAPI } from '../utils/api';
import toast from 'react-hot-toast';

const STATUS_CONFIG = {
  PAID: { label: 'PAID', color: 'text-emerald-300', bg: 'bg-emerald-500/10', ring: 'ring-emerald-500/30', dot: 'bg-emerald-500' },
  PENDING: { label: 'DUE', color: 'text-amber-300', bg: 'bg-amber-500/10', ring: 'ring-amber-500/30', dot: 'bg-amber-500', pulse: true },
  OVERDUE: { label: 'OVERDUE', color: 'text-red-300', bg: 'bg-red-500/10', ring: 'ring-red-500/30', dot: 'bg-red-500', pulse: true },
};

const MAINT_STATUS = {
  PENDING: 'bg-amber-500/10 text-amber-300 ring-amber-500/30',
  IN_PROGRESS: 'bg-cyan-500/10 text-cyan-300 ring-cyan-500/30',
  RESOLVED: 'bg-emerald-500/10 text-emerald-300 ring-emerald-500/30',
  CANCELLED: 'bg-slate-500/10 text-slate-400 ring-slate-500/30',
};

export default function TenantDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const res = await dashboardAPI.getTenantDashboard();
        setData(res.data);
      } catch (err) {
        toast.error('Failed to load dashboard');
        console.error(err);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const firstName = data?.first_name || user?.first_name || user?.firstName || 'Tenant';
  const unit = data?.unit_number || '-';
  const floor = data?.floor_label || '-';
  const rentAmount = data?.rent_amount || 0;
  const deposit = data?.deposit || 0;
  const dueDate = data?.due_date ? new Date(data.due_date).toLocaleDateString('en-IN', { day: 'numeric', month: 'long', year: 'numeric' }) : '-';
  const status = data?.rent_status || 'PENDING';
  const sts = STATUS_CONFIG[status] || STATUS_CONFIG.PENDING;
  const recentPayments = data?.recent_payments || [];
  const maintenanceRequests = data?.recent_maintenance || [];

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-2 border-indigo-500 border-t-transparent" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }}>
        <h1 className="text-2xl font-extrabold text-white" style={{ fontFamily: 'Plus Jakarta Sans' }}>
          Hello {firstName}! 👋
        </h1>
        <p className="mt-1 text-sm text-slate-500">Unit {unit} • {floor} • Sapthagiri Residency</p>
      </motion.div>

      {/* Rent Card */}
      <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.1 }}
        className="glass-card glow-hover p-6">
        <div className="mb-4 flex items-center gap-2">
          <IndianRupee className="h-5 w-5 text-indigo-400" />
          <h3 className="font-bold text-white">Your Rent</h3>
        </div>
        <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
          <div>
            <p className="text-xs uppercase tracking-wider text-slate-500">Amount</p>
            <p className="mt-1 text-xl font-bold text-white">₹{Number(rentAmount).toLocaleString('en-IN')}</p>
          </div>
          <div>
            <p className="text-xs uppercase tracking-wider text-slate-500">Due Date</p>
            <p className="mt-1 text-sm font-semibold text-white">{dueDate}</p>
          </div>
          <div>
            <p className="text-xs uppercase tracking-wider text-slate-500">Unit</p>
            <p className="mt-1 text-sm font-semibold text-white">{unit}</p>
          </div>
          <div>
            <p className="text-xs uppercase tracking-wider text-slate-500">Status</p>
            <span className={`mt-1 inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ${sts.bg} ${sts.color} ${sts.ring}`}>
              <span className={`h-1.5 w-1.5 rounded-full ${sts.dot} ${sts.pulse ? 'animate-pulse' : ''}`} />
              {sts.label}
            </span>
          </div>
        </div>
        <div className="mt-4 flex gap-3 border-t border-white/[0.06] pt-4">
          <button onClick={() => navigate('/tenant/bills')}
            className="btn-shimmer flex items-center gap-2 rounded-xl bg-gradient-to-r from-indigo-500 to-violet-500 px-5 py-2.5 text-sm font-semibold text-white shadow-lg shadow-indigo-500/30">
            <CreditCard className="h-4 w-4" /> Pay Now
          </button>
          <button onClick={() => navigate('/tenant/bills')}
            className="flex items-center gap-2 rounded-xl border border-white/[0.08] bg-white/[0.03] px-5 py-2.5 text-sm font-medium text-slate-300 hover:bg-white/5">
            <Download className="h-4 w-4" /> View Bills
          </button>
        </div>
      </motion.div>

      {/* Recent Payments + Maintenance side by side */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Recent Payments */}
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }}
          className="glass-card p-6">
          <div className="mb-4 flex items-center gap-2">
            <Clock className="h-4 w-4 text-emerald-400" />
            <h3 className="font-bold text-white">Recent Payments</h3>
          </div>
          <div className="space-y-3">
            {recentPayments.length === 0 ? (
              <p className="text-sm text-slate-500">No payments yet</p>
            ) : (
              recentPayments.map((p, i) => (
                <div key={p.id || i} className="flex items-center justify-between border-b border-white/[0.04] pb-3 last:border-0">
                  <div>
                    <p className="text-sm font-medium text-white">
                      {p.rentPeriod ? new Date(p.rentPeriod).toLocaleDateString('en-IN', { month: 'long', year: 'numeric' }) : '-'}
                    </p>
                    <p className="text-xs text-slate-500">
                      {p.paymentDate ? new Date(p.paymentDate).toLocaleDateString('en-IN') : '-'} • {p.paymentMode || '-'}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-sm font-semibold text-emerald-400">₹{Number(p.amount || 0).toLocaleString('en-IN')}</p>
                    <p className="text-xs text-slate-500">{p.receiptNumber || '-'}</p>
                  </div>
                </div>
              ))
            )}
          </div>
        </motion.div>

        {/* Active Maintenance */}
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.3 }}
          className="glass-card p-6">
          <div className="mb-4 flex items-center gap-2">
            <Wrench className="h-4 w-4 text-amber-400" />
            <h3 className="font-bold text-white">Maintenance Requests</h3>
          </div>
          <div className="space-y-3">
            {maintenanceRequests.length === 0 ? (
              <p className="text-sm text-slate-500">No maintenance requests</p>
            ) : (
              maintenanceRequests.map((req) => (
                <div key={req.id} className="flex items-center justify-between border-b border-white/[0.04] pb-3 last:border-0">
                  <div>
                    <p className="text-sm font-medium text-white">{req.title}</p>
                    <p className="text-xs text-slate-500">
                      {req.createdAt ? new Date(req.createdAt).toLocaleDateString('en-IN') : '-'} • {req.priority || '-'}
                    </p>
                  </div>
                  <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ${MAINT_STATUS[req.status] || MAINT_STATUS.PENDING}`}>
                    {(req.status || '').replace('_', ' ')}
                  </span>
                </div>
              ))
            )}
            <button onClick={() => navigate('/tenant/maintenance')}
              className="mt-2 flex w-full items-center justify-center gap-2 rounded-xl bg-amber-500/10 px-4 py-2 text-sm font-semibold text-amber-400 hover:bg-amber-500/20">
              <Wrench className="h-4 w-4" /> Raise New Request
            </button>
          </div>
        </motion.div>
      </div>

      {/* Quick links */}
      <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
        {[
          { label: 'My Payments', icon: CreditCard, to: '/tenant/payments', color: 'from-emerald-500 to-green-500' },
          { label: 'Maintenance', icon: Wrench, to: '/tenant/maintenance', color: 'from-amber-500 to-orange-500' },
          { label: 'Documents', icon: FileText, to: '/tenant/documents', color: 'from-indigo-500 to-violet-500' },
          { label: 'My Unit', icon: Home, to: '/tenant/unit', color: 'from-cyan-500 to-blue-500' },
        ].map((link, i) => {
          const Icon = link.icon;
          return (
            <motion.button key={link.label} initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.4 + i * 0.05 }}
              whileHover={{ y: -4 }} onClick={() => navigate(link.to)}
              className="glass-card flex flex-col items-center gap-3 p-5">
              <div className={`flex h-11 w-11 items-center justify-center rounded-xl bg-gradient-to-br ${link.color} shadow-lg`}>
                <Icon className="h-5 w-5 text-white" />
              </div>
              <span className="text-sm font-medium text-white">{link.label}</span>
            </motion.button>
          );
        })}
      </div>
    </div>
  );
}
