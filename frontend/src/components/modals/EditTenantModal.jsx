import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, Edit2 } from 'lucide-react';

export default function EditTenantModal({ isOpen, tenant, onClose, onSave }) {
  const [form, setForm] = useState({ name: '', phone: '', email: '', rent: '', deposit: '', leaseStart: '', leaseEnd: '' });

  useEffect(() => {
    if (tenant) {
      setForm({
        name: tenant.tenant?.name || '',
        phone: tenant.tenant?.phone || '',
        email: tenant.tenant?.email || '',
        rent: tenant.rentAmount || '',
        deposit: tenant.deposit || '',
        leaseStart: tenant.tenant?.since || '',
        leaseEnd: '',
      });
    }
  }, [tenant]);

  const inputClass = 'w-full rounded-xl border border-white/[0.08] bg-white/[0.03] px-4 py-3 text-sm text-white placeholder:text-slate-500 input-glow';

  const handleSubmit = (e) => {
    e.preventDefault();
    onSave(form);
  };

  return (
    <AnimatePresence>
      {isOpen && tenant && (
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4" onClick={onClose}>
          <motion.div initial={{ opacity: 0, scale: 0.95, y: 20 }} animate={{ opacity: 1, scale: 1, y: 0 }}
            className="glass-card w-full max-w-lg p-6" onClick={(e) => e.stopPropagation()}>
            <div className="mb-6 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-indigo-500 to-violet-500">
                  <Edit2 className="h-5 w-5 text-white" />
                </div>
                <div>
                  <h2 className="text-lg font-bold text-white">Edit Tenant</h2>
                  <p className="text-xs text-slate-500">Unit {tenant.name}</p>
                </div>
              </div>
              <button onClick={onClose} className="rounded-lg p-2 text-slate-400 hover:bg-white/5"><X className="h-5 w-5" /></button>
            </div>
            <form onSubmit={handleSubmit} className="space-y-4">
              <input type="text" required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="Full name" className={inputClass} />
              <div className="grid grid-cols-2 gap-3">
                <input type="tel" required value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} placeholder="Phone" className={inputClass} />
                <input type="email" required value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} placeholder="Email" className={inputClass} />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <input type="number" required value={form.rent} onChange={(e) => setForm({ ...form, rent: e.target.value })} placeholder="Rent" className={inputClass} />
                <input type="number" value={form.deposit} onChange={(e) => setForm({ ...form, deposit: e.target.value })} placeholder="Deposit" className={inputClass} />
              </div>
              <div className="flex gap-3 pt-2">
                <button type="button" onClick={onClose} className="flex-1 rounded-xl border border-white/[0.08] py-3 text-sm text-slate-300">Cancel</button>
                <button type="submit" className="btn-shimmer flex-1 rounded-xl bg-gradient-to-r from-indigo-500 to-violet-500 py-3 text-sm font-semibold text-white">Save Changes</button>
              </div>
            </form>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
