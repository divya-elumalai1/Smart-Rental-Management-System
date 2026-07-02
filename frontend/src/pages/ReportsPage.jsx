import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { BarChart, Bar, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { TrendingUp, IndianRupee, Download } from 'lucide-react';
import { paymentAPI, ownerAPI } from '../utils/api';
import toast from 'react-hot-toast';

const MONTH_LABELS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

export default function ReportsPage() {
  const [monthlyData, setMonthlyData] = useState([]);
  const [yearlyData, setYearlyData] = useState([]);
  const [summary, setSummary] = useState({ ytdIncome: 0, collectionRate: 0, monthlyAvg: 0 });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const [paymentsRes, statsRes] = await Promise.all([
          paymentAPI.getAll(),
          ownerAPI.getDashboardStats(),
        ]);
        const payments = paymentsRes.data || [];
        const stats = statsRes.data || {};

        const completed = payments.filter(p => p.status === 'COMPLETED');
        const curYear = new Date().getFullYear();
        const ytdPayments = completed.filter(p => p.paymentDate && new Date(p.paymentDate).getFullYear() === curYear);
        const ytdIncome = ytdPayments.reduce((s, p) => s + (p.amount || 0), 0);

        const monthly = MONTH_LABELS.map((month, i) => {
          const amount = completed
            .filter(p => p.paymentDate && new Date(p.paymentDate).getMonth() === i && new Date(p.paymentDate).getFullYear() === curYear)
            .reduce((s, p) => s + (p.amount || 0), 0);
          return { month, amount, expected: stats.totalUnits ? (stats.totalUnits - stats.vacant) * ((stats.collected || 0) / Math.max(1, stats.occupied)) : amount };
        });

        const monthlyAvg = monthly.reduce((s, m) => s + m.amount, 0) / Math.max(1, monthly.filter(m => m.amount > 0).length);
        const totalExpected = monthly.reduce((s, m) => s + m.expected, 0);
        const collectionRate = totalExpected > 0 ? Math.round((monthly.reduce((s, m) => s + m.amount, 0) / totalExpected) * 100) : 0;

        setMonthlyData(monthly);
        setYearlyData([{ year: String(curYear), amount: ytdIncome }]);
        setSummary({ ytdIncome, collectionRate, monthlyAvg: Math.round(monthlyAvg) });
      } catch (err) {
        toast.error('Failed to load reports');
        console.error(err);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

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

  const fmt = (v) => `₹${Number(v || 0).toLocaleString('en-IN')}`;
  const shortFmt = (v) => {
    const n = Number(v || 0);
    if (n >= 100000) return `₹${(n / 100000).toFixed(2)}L`;
    if (n >= 1000) return `₹${(n / 1000).toFixed(0)}K`;
    return fmt(n);
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
      <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }}
        className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-2xl font-extrabold text-white" style={{ fontFamily: 'Plus Jakarta Sans' }}>Reports</h1>
          <p className="mt-1 text-sm text-slate-500">Sapthagiri Residency — Income & Payment Reports</p>
        </div>
        <button onClick={handleExport} className="flex items-center gap-2 rounded-xl border border-white/[0.08] bg-white/[0.03] px-4 py-2.5 text-sm font-medium text-slate-300 hover:bg-white/5">
          <Download className="h-4 w-4" /> Export CSV
        </button>
      </motion.div>

      {/* Summary cards */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <div className="glass-card p-5">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-emerald-500 to-green-500"><IndianRupee className="h-5 w-5 text-white" /></div>
            <div><p className="text-2xl font-extrabold text-white">{shortFmt(summary.ytdIncome)}</p><p className="text-xs text-slate-500">{`Income (${new Date().getFullYear()} YTD)`}</p></div>
          </div>
        </div>
        <div className="glass-card p-5">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-indigo-500 to-violet-500"><TrendingUp className="h-5 w-5 text-white" /></div>
            <div><p className="text-2xl font-extrabold text-white">{summary.collectionRate}%</p><p className="text-xs text-slate-500">Collection Rate</p></div>
          </div>
        </div>
        <div className="glass-card p-5">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-amber-500 to-orange-500"><IndianRupee className="h-5 w-5 text-white" /></div>
            <div><p className="text-2xl font-extrabold text-white">{fmt(summary.monthlyAvg)}</p><p className="text-xs text-slate-500">Monthly Average</p></div>
          </div>
        </div>
      </div>

      {/* Monthly bar chart */}
      <div className="glass-card p-6">
        <h3 className="mb-4 font-bold text-white">{`Monthly Income (${new Date().getFullYear()})`}</h3>
        <ResponsiveContainer width="100%" height={280}>
          <BarChart data={monthlyData}>
            <defs><linearGradient id="barGrad" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor="#6366F1" stopOpacity={0.8} /><stop offset="100%" stopColor="#6366F1" stopOpacity={0.1} /></linearGradient></defs>
            <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" vertical={false} />
            <XAxis dataKey="month" tick={{ fontSize: 12, fill: '#64748B' }} axisLine={false} tickLine={false} />
            <YAxis tick={{ fontSize: 12, fill: '#64748B' }} axisLine={false} tickLine={false} tickFormatter={(v) => shortFmt(v)} />
            <Tooltip contentStyle={{ borderRadius: '12px', background: 'rgba(15,23,42,0.9)', border: '1px solid rgba(255,255,255,0.08)', color: '#F8FAFC', fontSize: '13px' }} formatter={(v) => [fmt(v), 'Income']} />
            <Bar dataKey="amount" radius={[8, 8, 0, 0]} fill="url(#barGrad)" />
          </BarChart>
        </ResponsiveContainer>
      </div>

      {/* Annual line chart */}
      <div className="glass-card p-6">
        <h3 className="mb-4 font-bold text-white">Annual Income Trend</h3>
        <ResponsiveContainer width="100%" height={250}>
          <LineChart data={yearlyData}>
            <defs><linearGradient id="lineGrad" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor="#10B981" stopOpacity={0.5} /><stop offset="100%" stopColor="#10B981" stopOpacity={0} /></linearGradient></defs>
            <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" vertical={false} />
            <XAxis dataKey="year" tick={{ fontSize: 12, fill: '#64748B' }} axisLine={false} tickLine={false} />
            <YAxis tick={{ fontSize: 12, fill: '#64748B' }} axisLine={false} tickLine={false} tickFormatter={(v) => shortFmt(v)} />
            <Tooltip contentStyle={{ borderRadius: '12px', background: 'rgba(15,23,42,0.9)', border: '1px solid rgba(255,255,255,0.08)', color: '#F8FAFC', fontSize: '13px' }} formatter={(v) => [fmt(v), 'Annual Income']} />
            <Line type="monotone" dataKey="amount" stroke="#10B981" strokeWidth={3} dot={{ fill: '#10B981', r: 5 }} />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
