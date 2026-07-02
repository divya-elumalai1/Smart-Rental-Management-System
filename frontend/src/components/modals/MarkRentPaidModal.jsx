import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, CheckCircle2 } from 'lucide-react';

export default function MarkRentPaidModal({ isOpen, onClose, unit, onConfirm }) {
  const [form, setForm] = useState({
    amount: unit?.rentAmount || 0,
    paymentDate: new Date().toISOString().split('T')[0],
    mode: 'UPI',
    reference: '',
    notes: '',
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    onConfirm({ ...form, unitId: unit?.id, currentPaymentId: unit?.currentPaymentId });
    onClose();
  };

  const inputClass = 'w-full rounded-xl border border-white/[0.08] bg-white/[0.03] px-4 py-3 text-sm text-white placeholder:text-slate-500 input-glow';

  return (
    <AnimatePresence>
      {isOpen && (
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4" onClick={onClose}>
          <motion.div initial={{ opacity: 0, scale: 0.95, y: 20 }} animate={{ opacity: 1, scale: 1, y: 0 }} exit={{ opacity: 0, scale: 0.95, y: 20 }}
            className="glass-card relative w-full max-w-md p-6" onClick={(e) => e.stopPropagation()}>
            <div className="mb-6 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-emerald-500 to-green-500">
                  <CheckCircle2 className="h-5 w-5 text-white" />
                </div>
                <div>
                  <h2 className="text-lg font-bold text-white">Mark Rent as Paid</h2>
                  <p className="text-xs text-slate-500">Unit {unit?.name} — {unit?.tenant?.name}</p>
                </div>
              </div>
              <button onClick={onClose} className="rounded-lg p-2 text-slate-400 hover:bg-white/5 hover:text-white"><X className="h-5 w-5" /></button>
            </div>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="mb-1.5 block text-xs font-medium text-slate-400">Amount (₹)</label>
                  <input type="number" required value={form.amount} onChange={(e) => setForm({ ...form, amount: +e.target.value })} className={inputClass} />
                </div>
                <div>
                  <label className="mb-1.5 block text-xs font-medium text-slate-400">Payment Date</label>
                  <input type="date" required value={form.paymentDate} onChange={(e) => setForm({ ...form, paymentDate: e.target.value })} className={inputClass} />
                </div>
              </div>
              <div>
                <label className="mb-1.5 block text-xs font-medium text-slate-400">Payment Mode</label>
                <select value={form.mode} onChange={(e) => setForm({ ...form, mode: e.target.value })} className={inputClass}>
                  <option value="UPI" className="bg-slate-900">UPI</option>
                  <option value="Cash" className="bg-slate-900">Cash</option>
                  <option value="Bank Transfer" className="bg-slate-900">Bank Transfer</option>
                  <option value="Cheque" className="bg-slate-900">Cheque</option>
                </select>
              </div>
              <div>
                <label className="mb-1.5 block text-xs font-medium text-slate-400">Transaction ID / Reference</label>
                <input type="text" value={form.reference} onChange={(e) => setForm({ ...form, reference: e.target.value })}
                  placeholder="e.g. UPI-987654321" className={inputClass} />
              </div>
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={onClose} className="flex-1 rounded-xl border border-white/[0.08] bg-white/[0.03] py-3 text-sm font-medium text-slate-300 hover:bg-white/5">Cancel</button>
                <button type="submit" className="btn-shimmer flex-1 rounded-xl bg-gradient-to-r from-emerald-500 to-green-500 py-3 text-sm font-semibold text-white shadow-lg shadow-emerald-500/30">Confirm Payment</button>
              </div>
            </form>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
