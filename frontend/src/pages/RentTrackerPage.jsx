import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { CheckCircle2 } from 'lucide-react';
import toast from 'react-hot-toast';
import { STATUS_CONFIG } from '../data/mockData';
import MarkRentPaidModal from '../components/modals/MarkRentPaidModal';
import { dashboardAPI, paymentAPI } from '../utils/api';
import { twMerge } from 'tailwind-merge';
import clsx from 'clsx';

const cn = (...args) => twMerge(clsx(...args));

export default function RentTrackerPage() {
  const [units, setUnits] = useState([]);
  const [paidModal, setPaidModal] = useState({ open: false, unit: null });
  const [loading, setLoading] = useState(true);

  const loadUnits = async () => {
    try {
      const res = await dashboardAPI.getOwnerUnits();
      const mapped = (res.data || [])
        .filter((u) => u.tenant_name || u.tenantName)
        .map((u) => ({
          id: u.id,
          name: u.unit_number || u.unitNumber,
          tenant: { name: u.tenant_name || u.tenantName },
          rentAmount: Number(u.rent_amount ?? u.rentAmount ?? 0),
          status: u.rent_status || u.rentStatus,
          currentPaymentId: u.current_payment_id || u.currentPaymentId,
        }));
      setUnits(mapped);
    } catch {
      toast.error('Could not load rent tracker data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadUnits(); }, []);

  const markPaid = async (data) => {
    if (!data.currentPaymentId && !paidModal.unit?.currentPaymentId) {
      toast.error('No pending payment found for this unit');
      return;
    }
    const paymentId = data.currentPaymentId || paidModal.unit.currentPaymentId;
    try {
      await paymentAPI.markPaid(paymentId, {
        payment_mode: data.mode,
        reference: data.reference || undefined,
        payment_date: data.paymentDate,
        notes: data.notes || undefined,
      });
      toast.success(`Rent marked as paid for ${paidModal.unit?.name}`);
      loadUnits();
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Failed to mark payment');
    }
  };

  if (loading) return <div className="py-20 text-center text-slate-500">Loading rent tracker…</div>;

  return (
    <div className="space-y-6">
      <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }}>
        <h1 className="text-2xl font-extrabold text-white" style={{ fontFamily: 'Plus Jakarta Sans' }}>Rent Tracker</h1>
        <p className="mt-1 text-sm text-slate-500">Track rent payments for all units this month</p>
      </motion.div>

      <div className="glass-card overflow-hidden">
        <table className="w-full">
          <thead>
            <tr className="border-b border-white/[0.06] text-left text-xs uppercase tracking-wider text-slate-500">
              <th className="px-6 py-4">Unit</th><th className="px-6 py-4">Tenant</th><th className="px-6 py-4">Rent Due</th>
              <th className="px-6 py-4">Due Date</th><th className="px-6 py-4">Status</th><th className="px-6 py-4 text-right">Action</th>
            </tr>
          </thead>
          <tbody>
            {units.map((unit, i) => {
              const config = STATUS_CONFIG[unit.status] || STATUS_CONFIG.PENDING;
              return (
                <motion.tr key={unit.id} initial={{ opacity: 0, x: -20 }} animate={{ opacity: 1, x: 0 }} transition={{ delay: i * 0.05 }}
                  className="border-b border-white/[0.04] transition-colors hover:bg-white/[0.02]">
                  <td className="px-6 py-4 text-sm font-semibold text-white">{unit.name}</td>
                  <td className="px-6 py-4 text-sm text-slate-300">{unit.tenant.name}</td>
                  <td className="px-6 py-4 text-sm font-semibold text-white">₹{unit.rentAmount.toLocaleString('en-IN')}</td>
                  <td className="px-6 py-4 text-sm text-slate-400">1st of month</td>
                  <td className="px-6 py-4">
                    <span className={cn('flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold ring-1', config.bg, config.text, config.ring)}>
                      <span className={cn('h-1.5 w-1.5 rounded-full', config.dot, config.pulse && 'animate-pulse')} />{config.label}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-right">
                    {unit.status !== 'PAID' && (
                      <button onClick={() => setPaidModal({ open: true, unit })}
                        className="flex items-center gap-1.5 rounded-lg bg-emerald-500/10 px-3 py-1.5 text-xs font-semibold text-emerald-400 hover:bg-emerald-500/20">
                        <CheckCircle2 className="h-3.5 w-3.5" /> Mark Paid
                      </button>
                    )}
                    {unit.status === 'PAID' && <span className="text-xs font-medium text-emerald-400">✓ Paid</span>}
                  </td>
                </motion.tr>
              );
            })}
          </tbody>
        </table>
      </div>

      <MarkRentPaidModal
        isOpen={paidModal.open}
        onClose={() => setPaidModal({ open: false, unit: null })}
        unit={paidModal.unit}
        onConfirm={(form) => markPaid({ ...form, currentPaymentId: paidModal.unit?.currentPaymentId })}
      />
    </div>
  );
}
