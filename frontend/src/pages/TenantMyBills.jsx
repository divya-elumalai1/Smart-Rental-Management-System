import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { IndianRupee, Droplets, Download, ChevronLeft, ChevronRight } from 'lucide-react';
import { tenantPortalAPI } from '../utils/api';
import toast from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';

const MONTHS = ['January','February','March','April','May','June','July','August','September','October','November','December'];

export default function TenantMyBills() {
  const navigate = useNavigate();
  const now = new Date();
  const [currentMonth, setCurrentMonth] = useState(now.getMonth());
  const [currentYear, setCurrentYear] = useState(now.getFullYear());
  const [bill, setBill] = useState(null);
  const [loading, setLoading] = useState(false);

  const monthStr = `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}`;

  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const res = await tenantPortalAPI.getMyBill(monthStr);
        setBill(res.data);
      } catch (err) {
        setBill(null);
        if (err?.response?.status !== 404) {
          toast.error('Failed to load bill');
        }
      } finally {
        setLoading(false);
      }
    })();
  }, [monthStr]);

  const prevMonth = () => {
    if (currentMonth === 0) { setCurrentMonth(11); setCurrentYear(y => y - 1); }
    else setCurrentMonth(m => m - 1);
  };
  const nextMonth = () => {
    if (currentMonth === 11) { setCurrentMonth(0); setCurrentYear(y => y + 1); }
    else setCurrentMonth(m => m + 1);
  };
  const isCurrentMonth = currentMonth === now.getMonth() && currentYear === now.getFullYear();
  const isFuture = currentYear > now.getFullYear() || (currentYear === now.getFullYear() && currentMonth > now.getMonth());

  return (
    <div className="space-y-6">
      <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }}
        className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-2xl font-extrabold text-white" style={{ fontFamily: 'Plus Jakarta Sans' }}>My Bills</h1>
          <p className="mt-1 text-sm text-slate-500">Monthly rent & water bills</p>
        </div>
      </motion.div>

      <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="glass-card p-6">
        <div className="mb-6 flex items-center justify-between">
          <button onClick={prevMonth} className="rounded-lg p-2 text-slate-400 hover:bg-white/5 hover:text-white">
            <ChevronLeft className="h-5 w-5" />
          </button>
          <h3 className="text-lg font-bold text-white">{MONTHS[currentMonth]} {currentYear}</h3>
          <button onClick={nextMonth} disabled={isCurrentMonth || isFuture}
            className="rounded-lg p-2 text-slate-400 hover:bg-white/5 hover:text-white disabled:opacity-30 disabled:cursor-not-allowed">
            <ChevronRight className="h-5 w-5" />
          </button>
        </div>

        {loading ? (
          <div className="flex h-40 items-center justify-center">
            <div className="h-8 w-8 animate-spin rounded-full border-2 border-indigo-500 border-t-transparent" />
          </div>
        ) : bill ? (
          <div className="space-y-6">
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
              <div className="rounded-xl bg-white/[0.03] p-4">
                <p className="text-xs text-slate-500">Monthly Rent</p>
                <p className="mt-1 text-xl font-bold text-white">₹{Number(bill.rent_amount || 0).toLocaleString('en-IN')}</p>
              </div>
              <div className="rounded-xl bg-white/[0.03] p-4">
                <p className="text-xs text-slate-500">Water Charges</p>
                <p className="mt-1 text-xl font-bold text-cyan-400">₹{Number(bill.water_bill || 0).toLocaleString('en-IN')}</p>
              </div>
              <div className="rounded-xl bg-white/[0.03] p-4">
                <p className="text-xs text-slate-500">Units Consumed</p>
                <p className="mt-1 text-xl font-bold text-white">{Number(bill.units_consumed || 0).toFixed(1)}</p>
              </div>
              <div className="rounded-xl bg-gradient-to-br from-indigo-500/20 to-violet-500/10 p-4 ring-1 ring-indigo-500/20">
                <p className="text-xs text-indigo-300">Total Due</p>
                <p className="mt-1 text-2xl font-extrabold text-white">₹{Number(bill.total_bill || bill.rent_amount || 0).toLocaleString('en-IN')}</p>
              </div>
            </div>

            {bill.water_bill > 0 && (
              <div className="border-t border-white/[0.06] pt-4">
                <div className="mb-3 flex items-center gap-2">
                  <Droplets className="h-4 w-4 text-cyan-400" />
                  <h4 className="text-sm font-semibold text-white">Water Meter Details</h4>
                </div>
                <div className="grid grid-cols-3 gap-4 text-center">
                  <div className="rounded-lg bg-white/[0.03] p-3">
                    <p className="text-[10px] text-slate-500">Previous</p>
                    <p className="text-sm font-bold text-white">{Number(bill.previous_reading || 0).toFixed(1)}</p>
                  </div>
                  <div className="rounded-lg bg-white/[0.03] p-3">
                    <p className="text-[10px] text-slate-500">Current</p>
                    <p className="text-sm font-bold text-white">{Number(bill.current_reading || 0).toFixed(1)}</p>
                  </div>
                  <div className="rounded-lg bg-white/[0.03] p-3">
                    <p className="text-[10px] text-slate-500">Rate</p>
                    <p className="text-sm font-bold text-white">₹{Number(bill.water_rate || 0).toFixed(2)}/unit</p>
                  </div>
                </div>
              </div>
            )}

            <div className="flex gap-3 border-t border-white/[0.06] pt-4">
              <button onClick={() => navigate('/tenant/payments')}
                className="btn-shimmer flex items-center gap-2 rounded-xl bg-gradient-to-r from-indigo-500 to-violet-500 px-5 py-2.5 text-sm font-semibold text-white shadow-lg shadow-indigo-500/30">
                <IndianRupee className="h-4 w-4" /> Pay Now
              </button>
              <button className="flex items-center gap-2 rounded-xl border border-white/[0.08] bg-white/[0.03] px-5 py-2.5 text-sm font-medium text-slate-300 hover:bg-white/5">
                <Download className="h-4 w-4" /> Download Bill
              </button>
            </div>
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center py-12">
            <IndianRupee className="mb-3 h-12 w-12 text-slate-600" />
            <p className="text-sm text-slate-500">No bill available for this month</p>
          </div>
        )}
      </motion.div>
    </div>
  );
}
