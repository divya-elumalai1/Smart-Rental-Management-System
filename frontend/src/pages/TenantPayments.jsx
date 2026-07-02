import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { CreditCard, IndianRupee, Clock, Download, CheckCircle, XCircle } from 'lucide-react';
import { tenantPortalAPI, paymentAPI } from '../utils/api';
import toast from 'react-hot-toast';

const PAYMENT_STATUS = {
  COMPLETED: { label: 'Paid', color: 'text-emerald-300', bg: 'bg-emerald-500/10', ring: 'ring-emerald-500/30', icon: CheckCircle },
  PENDING: { label: 'Pending', color: 'text-amber-300', bg: 'bg-amber-500/10', ring: 'ring-amber-500/30', icon: Clock },
  OVERDUE: { label: 'Overdue', color: 'text-red-300', bg: 'bg-red-500/10', ring: 'ring-red-500/30', icon: XCircle },
  FAILED: { label: 'Failed', color: 'text-red-300', bg: 'bg-red-500/10', ring: 'ring-red-500/30', icon: XCircle },
  CANCELLED: { label: 'Cancelled', color: 'text-slate-400', bg: 'bg-slate-500/10', ring: 'ring-slate-500/30', icon: XCircle },
};

export default function TenantPayments() {
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const res = await tenantPortalAPI.getMyPayments();
        setPayments(res.data || []);
      } catch (err) {
        toast.error('Failed to load payments');
        console.error(err);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const handleDownloadReceipt = async (paymentId) => {
    try {
      const res = await paymentAPI.getReceipt(paymentId);
      if (res.data?.receipt_url) {
        window.open(res.data.receipt_url, '_blank');
      } else {
        toast.error('No receipt available');
      }
    } catch (err) {
      toast.error('Failed to download receipt');
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
        <h1 className="text-2xl font-extrabold text-white" style={{ fontFamily: 'Plus Jakarta Sans' }}>Payments</h1>
        <p className="mt-1 text-sm text-slate-500">Your payment history</p>
      </motion.div>

      {payments.length === 0 ? (
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="flex flex-col items-center justify-center py-16">
          <CreditCard className="mb-4 h-16 w-16 text-slate-600" />
          <p className="text-lg font-medium text-slate-400">No payments yet</p>
          <p className="mt-1 text-sm text-slate-500">Your payment history will appear here</p>
        </motion.div>
      ) : (
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="glass-card overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-white/[0.06]">
                  <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-slate-500">Period</th>
                  <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-slate-500">Amount</th>
                  <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-slate-500">Date</th>
                  <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-slate-500">Mode</th>
                  <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-slate-500">Status</th>
                  <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-slate-500">Receipt</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-white/[0.04]">
                {payments.map((p, i) => {
                  const sts = PAYMENT_STATUS[p.status] || PAYMENT_STATUS.PENDING;
                  const Icon = sts.icon;
                  return (
                    <motion.tr key={p.id || i} initial={{ opacity: 0, x: -10 }} animate={{ opacity: 1, x: 0 }} transition={{ delay: i * 0.03 }}
                      className="transition-colors hover:bg-white/[0.02]">
                      <td className="px-4 py-3 text-sm font-medium text-white">
                        {p.rent_period ? new Date(p.rent_period).toLocaleDateString('en-IN', { month: 'long', year: 'numeric' }) : '-'}
                      </td>
                      <td className="px-4 py-3 text-sm font-semibold text-white">₹{Number(p.amount || 0).toLocaleString('en-IN')}</td>
                      <td className="px-4 py-3 text-sm text-slate-400">
                        {p.payment_date ? new Date(p.payment_date).toLocaleDateString('en-IN') : '-'}
                      </td>
                      <td className="px-4 py-3 text-sm text-slate-400">{p.payment_mode || '-'}</td>
                      <td className="px-4 py-3">
                        <span className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ${sts.bg} ${sts.color} ${sts.ring}`}>
                          <Icon className="h-3 w-3" /> {sts.label}
                        </span>
                      </td>
                      <td className="px-4 py-3">
                        {p.status === 'COMPLETED' ? (
                          <button onClick={() => handleDownloadReceipt(p.id)}
                            className="flex items-center gap-1.5 rounded-lg bg-white/[0.05] px-3 py-1.5 text-xs font-medium text-slate-300 hover:bg-white/10">
                            <Download className="h-3 w-3" /> Receipt
                          </button>
                        ) : (
                          <span className="text-xs text-slate-600">-</span>
                        )}
                      </td>
                    </motion.tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </motion.div>
      )}
    </div>
  );
}
