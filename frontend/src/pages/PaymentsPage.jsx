import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { TrendingUp, Download, IndianRupee, Trash2 } from 'lucide-react';
import { paymentAPI } from '../utils/api';
import toast from 'react-hot-toast';

const MONTH_NAMES = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

export default function PaymentsPage() {
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { loadPayments(); }, []);

  const loadPayments = async () => {
    try {
      const res = await paymentAPI.getAll();
      setPayments(res.data || []);
    } catch (err) {
      toast.error('Failed to load payments');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this payment record?')) return;
    try {
      await paymentAPI.deletePayment(id);
      toast.success('Payment deleted');
      loadPayments();
    } catch {
      toast.error('Failed to delete payment');
    }
  };

  const handleExport = async () => {
    try {
      const res = await paymentAPI.getAll();
      const payments = res.data || [];
      const csv = [
        'Tenant,Unit,Amount,Date,Mode,Status,Receipt',
        ...payments.map(p =>
          `${p.tenantName || '-'},${p.unitNumber || '-'},${p.amount || 0},${p.paymentDate ? new Date(p.paymentDate).toLocaleDateString('en-IN') : '-'},${p.paymentMode || '-'},${p.status || '-'},${p.receiptNumber || '-'}`
        ),
      ].join('\n');
      const blob = new Blob([csv], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url; a.download = `payments-export-${new Date().toISOString().slice(0, 10)}.csv`;
      a.click();
      URL.revokeObjectURL(url);
      toast.success('Exported successfully');
    } catch {
      toast.error('Export failed');
    }
  };

  const totalCollected = payments.reduce((s, p) => s + (p.amount || 0), 0);
  const monthlyData = MONTH_NAMES.map(name => ({
    month: name,
    amount: payments
      .filter(p => p.status === 'COMPLETED' && p.paymentDate)
      .reduce((s, p) => {
        const pm = new Date(p.paymentDate).getMonth();
        return pm === MONTH_NAMES.indexOf(name) ? s + (p.amount || 0) : s;
      }, 0),
  }));
  const monthlyAvg = payments.length > 0
    ? (totalCollected / Math.max(1, new Set(payments.map(p => p.rentPeriod)).size)).toFixed(0)
    : '0';

  const formatDate = (d) => d ? new Date(d).toLocaleDateString('en-IN') : '-';
  const formatCurrency = (v) => `₹${Number(v || 0).toLocaleString('en-IN')}`;

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-2 border-indigo-500 border-t-transparent" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }}
        className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-2xl font-extrabold text-white" style={{ fontFamily: 'Plus Jakarta Sans' }}>Payment History</h1>
          <p className="mt-1 text-sm text-slate-500">All rent payments received</p>
        </div>
        <button onClick={handleExport} className="flex items-center gap-2 rounded-xl border border-white/[0.08] bg-white/[0.03] px-4 py-2.5 text-sm font-medium text-slate-300 hover:bg-white/5">
          <Download className="h-4 w-4" /> Export
        </button>
      </motion.div>

      {/* Summary */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <div className="glass-card p-5">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-emerald-500 to-green-500"><IndianRupee className="h-5 w-5 text-white" /></div>
            <div><p className="text-2xl font-extrabold text-white">{formatCurrency(totalCollected)}</p><p className="text-xs text-slate-500">Total Collected</p></div>
          </div>
        </div>
        <div className="glass-card p-5">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-indigo-500 to-violet-500"><TrendingUp className="h-5 w-5 text-white" /></div>
            <div><p className="text-2xl font-extrabold text-white">{payments.length}</p><p className="text-xs text-slate-500">Total Payments</p></div>
          </div>
        </div>
        <div className="glass-card p-5">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-amber-500 to-orange-500"><TrendingUp className="h-5 w-5 text-white" /></div>
            <div><p className="text-2xl font-extrabold text-white">{formatCurrency(monthlyAvg)}</p><p className="text-xs text-slate-500">Monthly Average</p></div>
          </div>
        </div>
      </div>

      {/* Chart */}
      <div className="glass-card p-6">
        <h3 className="mb-4 font-bold text-white">Monthly Collection</h3>
        <ResponsiveContainer width="100%" height={250}>
          <AreaChart data={monthlyData}>
            <defs><linearGradient id="payGrad" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor="#10B981" stopOpacity={0.3} /><stop offset="100%" stopColor="#10B981" stopOpacity={0} /></linearGradient></defs>
            <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" vertical={false} />
            <XAxis dataKey="month" tick={{ fontSize: 12, fill: '#64748B' }} axisLine={false} tickLine={false} />
            <YAxis tick={{ fontSize: 12, fill: '#64748B' }} axisLine={false} tickLine={false} tickFormatter={(v) => `₹${(v / 1000).toFixed(0)}K`} />
            <Tooltip contentStyle={{ borderRadius: '12px', background: 'rgba(15,23,42,0.9)', border: '1px solid rgba(255,255,255,0.08)', color: '#F8FAFC', fontSize: '13px' }} formatter={(v) => [formatCurrency(v), 'Collected']} />
            <Area type="monotone" dataKey="amount" stroke="#10B981" strokeWidth={2} fill="url(#payGrad)" />
          </AreaChart>
        </ResponsiveContainer>
      </div>

      {/* Table */}
      <div className="glass-card overflow-hidden">
        <table className="w-full">
          <thead>
            <tr className="border-b border-white/[0.06] text-left text-xs uppercase tracking-wider text-slate-500">
              <th className="px-6 py-4">Tenant</th><th className="px-6 py-4">Unit</th><th className="px-6 py-4">Amount</th>
              <th className="px-6 py-4">Date</th><th className="px-6 py-4">Status</th><th className="px-6 py-4">Mode</th><th className="px-6 py-4">Receipt</th><th className="px-6 py-4" />
            </tr>
          </thead>
          <tbody>
            {payments.length === 0 ? (
              <tr><td colSpan={8} className="px-6 py-12 text-center text-sm text-slate-500">No payments found</td></tr>
            ) : (
              payments.map((p, i) => (
                <motion.tr key={p.id} initial={{ opacity: 0, x: -20 }} animate={{ opacity: 1, x: 0 }} transition={{ delay: i * 0.04 }}
                  className="border-b border-white/[0.04] hover:bg-white/[0.02]">
                  <td className="px-6 py-4 text-sm font-semibold text-white">{p.tenantName || '-'}</td>
                  <td className="px-6 py-4 text-sm text-slate-400">{p.unitNumber || '-'}</td>
                  <td className="px-6 py-4 text-sm font-semibold text-emerald-400">{formatCurrency(p.amount)}</td>
                  <td className="px-6 py-4 text-sm text-slate-400">{formatDate(p.paymentDate)}</td>
                  <td className="px-6 py-4">
                    <span className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ${
                      p.status === 'COMPLETED' ? 'bg-emerald-500/10 text-emerald-300 ring-emerald-500/30' :
                      p.status === 'PENDING' ? 'bg-amber-500/10 text-amber-300 ring-amber-500/30' :
                      p.status === 'OVERDUE' ? 'bg-red-500/10 text-red-300 ring-red-500/30' :
                      'bg-slate-500/10 text-slate-400 ring-slate-500/30'
                    }`}>
                      <span className={`h-1.5 w-1.5 rounded-full ${
                        p.status === 'COMPLETED' ? 'bg-emerald-500' :
                        p.status === 'PENDING' ? 'bg-amber-500' :
                        p.status === 'OVERDUE' ? 'bg-red-500' :
                        'bg-slate-400'
                      }`} />
                      {p.status}
                    </span>
                  </td>
                  <td className="px-6 py-4"><span className="rounded-lg bg-white/[0.05] px-2 py-0.5 text-xs text-slate-300">{p.paymentMode || '-'}</span></td>
                  <td className="px-6 py-4 text-xs text-indigo-400">{p.receiptNumber || '-'}</td>
                  <td className="px-6 py-4">
                    <button onClick={() => handleDelete(p.id)} className="rounded-lg p-1.5 text-slate-500 hover:bg-red-500/10 hover:text-red-400">
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </td>
                </motion.tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
