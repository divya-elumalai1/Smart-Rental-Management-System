import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, UserPlus } from 'lucide-react';
import toast from 'react-hot-toast';

export default function AddTenantModal({ isOpen, onClose, onAdd, vacantUnits = [] }) {
  const [form, setForm] = useState({
    name: '', phone: '', email: '', unit: '', rent: '', deposit: '',
    leaseStart: '', leaseEnd: '', password: '', notes: '',
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!form.name || !form.phone || !form.email || !form.unit || !form.password) {
      toast.error('Please fill all required fields');
      return;
    }
    onAdd(form);
    setForm({ name: '', phone: '', email: '', unit: '', rent: '', deposit: '', leaseStart: '', leaseEnd: '', password: '', notes: '' });
  };

  const inputClass = 'w-full rounded-xl border border-white/[0.08] bg-white/[0.03] px-4 py-3 text-sm text-white placeholder:text-slate-500 input-glow';

  return (
    <AnimatePresence>
      {isOpen && (
        <motion.div
          initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4"
          onClick={onClose}
        >
          <motion.div
            initial={{ opacity: 0, scale: 0.95, y: 20 }} animate={{ opacity: 1, scale: 1, y: 0 }} exit={{ opacity: 0, scale: 0.95, y: 20 }}
            className="glass-card relative max-h-[90vh] w-full max-w-lg overflow-y-auto p-6"
            onClick={(e) => e.stopPropagation()}
          >
            {/* Header */}
            <div className="mb-6 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-indigo-500 to-violet-500">
                  <UserPlus className="h-5 w-5 text-white" />
                </div>
                <div>
                  <h2 className="text-lg font-bold text-white">Add New Tenant</h2>
                  <p className="text-xs text-slate-500">Create tenant account & assign unit</p>
                </div>
              </div>
              <button onClick={onClose} className="rounded-lg p-2 text-slate-400 hover:bg-white/5 hover:text-white">
                <X className="h-5 w-5" />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
              {/* Name */}
              <div>
                <label className="mb-1.5 block text-xs font-medium text-slate-400">Full Name *</label>
                <div className="relative">
                  <input type="text" required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })}
                    placeholder="e.g. Rahul Sharma" className={inputClass} />
                </div>
              </div>

              {/* Phone + Email */}
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="mb-1.5 block text-xs font-medium text-slate-400">Phone *</label>
                  <input type="tel" required value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })}
                    placeholder="+91 98765 43210" className={inputClass} />
                </div>
                <div>
                  <label className="mb-1.5 block text-xs font-medium text-slate-400">Email *</label>
                  <input type="email" required value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })}
                    placeholder="tenant@email.com" className={inputClass} />
                </div>
              </div>

              {/* Unit dropdown */}
              <div>
                <label className="mb-1.5 block text-xs font-medium text-slate-400">Assign Unit *</label>
                <select required value={form.unit} onChange={(e) => setForm({ ...form, unit: e.target.value })}
                  className={inputClass}>
                  <option value="">Select vacant unit...</option>
                  {vacantUnits.map((u) => (
                    <option key={u.name} value={u.name} className="bg-slate-900">{u.name} — {u.area}</option>
                  ))}
                </select>
              </div>

              {/* Rent + Deposit */}
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="mb-1.5 block text-xs font-medium text-slate-400">Monthly Rent (₹) *</label>
                  <input type="number" required value={form.rent} onChange={(e) => setForm({ ...form, rent: e.target.value })}
                    placeholder="10500" className={inputClass} />
                </div>
                <div>
                  <label className="mb-1.5 block text-xs font-medium text-slate-400">Security Deposit (₹)</label>
                  <input type="number" value={form.deposit} onChange={(e) => setForm({ ...form, deposit: e.target.value })}
                    placeholder="21000" className={inputClass} />
                </div>
              </div>

              {/* Lease dates */}
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="mb-1.5 block text-xs font-medium text-slate-400">Lease Start Date</label>
                  <input type="date" value={form.leaseStart} onChange={(e) => setForm({ ...form, leaseStart: e.target.value })}
                    className={inputClass} />
                </div>
                <div>
                  <label className="mb-1.5 block text-xs font-medium text-slate-400">Lease End Date</label>
                  <input type="date" value={form.leaseEnd} onChange={(e) => setForm({ ...form, leaseEnd: e.target.value })}
                    className={inputClass} />
                </div>
              </div>

              {/* Password */}
              <div>
                <label className="mb-1.5 block text-xs font-medium text-slate-400">Login Password *</label>
                <input type="text" required value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })}
                  placeholder="Set a password for tenant login" className={inputClass} />
              </div>

              {/* Notes */}
              <div>
                <label className="mb-1.5 block text-xs font-medium text-slate-400">Notes (optional)</label>
                <textarea value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })}
                  placeholder="Any additional notes about this tenant..." rows={2} className={inputClass} />
              </div>

              {/* Buttons */}
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={onClose}
                  className="flex-1 rounded-xl border border-white/[0.08] bg-white/[0.03] py-3 text-sm font-medium text-slate-300 hover:bg-white/5">
                  Cancel
                </button>
                <button type="submit"
                  className="btn-shimmer flex-1 rounded-xl bg-gradient-to-r from-indigo-500 to-violet-500 py-3 text-sm font-semibold text-white shadow-lg shadow-indigo-500/30">
                  Add Tenant
                </button>
              </div>
            </form>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
