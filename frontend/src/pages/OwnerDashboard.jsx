import { useState, useMemo, useEffect } from 'react';
import { motion } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import { useInView } from 'react-intersection-observer';
import { useCountUp } from '../hooks/useCountUp';
import {
  Building2, Users, IndianRupee, Clock, TrendingUp,
  Phone, MessageCircle, Plus, Home, Download,
  CheckCircle2, AlertCircle,
} from 'lucide-react';
import { UNITS as MOCK_UNITS, FLOORS, BUILDING, STATUS_CONFIG, getDashboardSummary } from '../data/mockData';
import { ownerAPI } from '../utils/api';
import { twMerge } from 'tailwind-merge';
import clsx from 'clsx';
import toast from 'react-hot-toast';

const cn = (...args) => twMerge(clsx(...args));

/* ===========================================
   Animated Stat Card with CountUp
   =========================================== */
function StatCard({ icon: Icon, label, value, sub, color, glow, delay }) {
  const { ref, inView } = useInView({ triggerOnce: true });
  const { value: countValue } = useCountUp(value, 2, inView);

  return (
    <motion.div
      ref={ref}
      initial={{ opacity: 0, y: 30 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay, duration: 0.5 }}
      whileHover={{ y: -4 }}
      className="glass-card glow-hover group relative overflow-hidden p-5"
    >
      {/* Glow underline */}
      <div className={cn('absolute inset-x-0 bottom-0 h-px opacity-50', glow)} />

      <div className="flex items-start justify-between">
        <motion.div
          whileHover={{ rotate: 15, scale: 1.1 }}
          className={cn('flex h-11 w-11 items-center justify-center rounded-xl shadow-lg', color)}
        >
          <Icon className="h-5 w-5 text-white" />
        </motion.div>
      </div>

      <div className="mt-4">
        <p className="tabular text-2xl font-extrabold text-white" style={{ fontFamily: 'Plus Jakarta Sans' }}>
          {inView ? countValue.toLocaleString('en-IN') : '0'}
        </p>
        <p className="mt-1 text-xs font-medium text-slate-400">{label}</p>
        {sub && <p className="mt-0.5 text-[11px] text-slate-500">{sub}</p>}
      </div>
    </motion.div>
  );
}

/* ===========================================
   Unit Card — Glassmorphism with glow status
   =========================================== */
function UnitCard({ unit, index, onMarkPaid }) {
  const [hovered, setHovered] = useState(false);
  const config = STATUS_CONFIG[unit.status];
  const tenant = unit.tenant;

  const glowClass = {
    PAID: 'hover:shadow-[0_0_30px_rgba(16,185,129,0.2)]',
    PENDING: 'hover:shadow-[0_0_30px_rgba(245,158,11,0.2)]',
    OVERDUE: 'hover:shadow-[0_0_30px_rgba(239,68,68,0.2)]',
    VACANT: '',
  }[unit.status];

  return (
    <motion.div
      initial={{ opacity: 0, y: 30 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: index * 0.08, duration: 0.5 }}
      whileHover={{ y: -8 }}
      onHoverStart={() => setHovered(true)}
      onHoverEnd={() => setHovered(false)}
      className={cn(
        'glass-card group relative overflow-hidden p-5',
        glowClass,
        unit.status === 'VACANT' && 'border-dashed'
      )}
    >
      {/* Gradient image area */}
      <div className="relative mb-4 h-24 overflow-hidden rounded-xl">
        <div className={cn(
          'absolute inset-0 bg-gradient-to-br',
          unit.status === 'VACANT'
            ? 'from-slate-700 to-slate-800'
            : 'from-indigo-500/20 via-violet-500/10 to-cyan-500/20'
        )} />
        <div className="absolute bottom-2 left-3">
          <p className="text-xs font-bold text-white/90">{unit.name}</p>
          <p className="text-[10px] text-white/60">{unit.type}</p>
        </div>
        {/* Status badge */}
        <div className={cn(
          'absolute right-2 top-2 flex items-center gap-1.5 rounded-full px-2.5 py-1 text-[10px] font-bold backdrop-blur-md',
          config.bg, config.text, 'ring-1', config.ring,
          unit.status === 'PAID' && 'glow-success',
          unit.status === 'OVERDUE' && 'pulse-danger',
          unit.status === 'PENDING' && 'pulse-warning',
        )}>
          <span className={cn('h-1.5 w-1.5 rounded-full', config.dot)} />
          {config.label}
        </div>
      </div>

      {tenant ? (
        <>
          {/* Tenant */}
          <div className="mb-3 flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-full bg-gradient-to-br from-indigo-400 to-violet-500 text-xs font-bold text-white shadow-lg shadow-indigo-500/20">
              {tenant.name.charAt(0)}
            </div>
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-semibold text-white">{tenant.name}</p>
              <p className="truncate text-[11px] text-slate-500">{tenant.phone}</p>
            </div>
          </div>

          {/* Rent */}
          <div className="flex items-end justify-between border-t border-white/[0.06] pt-3">
            <div>
              <p className="text-[10px] uppercase tracking-wider text-slate-500">Rent</p>
              <p className="tabular text-lg font-bold text-white">₹{unit.rentAmount.toLocaleString('en-IN')}</p>
            </div>
            <div className="text-right">
              <p className="text-[10px] uppercase tracking-wider text-slate-500">Deposit</p>
              <p className="text-sm font-semibold text-slate-300">₹{unit.deposit.toLocaleString('en-IN')}</p>
            </div>
          </div>

          {/* Hover actions */}
          <motion.div
            initial={false}
            animate={{ opacity: hovered ? 1 : 0, height: hovered ? 'auto' : 0 }}
            className="overflow-hidden"
          >
            <div className="mt-3 flex items-center gap-2 border-t border-white/[0.06] pt-3">
              <a
                href={`tel:${tenant.phone}`}
                className="flex flex-1 items-center justify-center gap-1.5 rounded-lg bg-white/[0.05] py-1.5 text-xs font-medium text-slate-300 transition-colors hover:bg-white/10"
              >
                <Phone className="h-3.5 w-3.5" /> Call
              </a>
              <a
                href={`https://wa.me/${tenant.phone.replace(/\D/g, '')}`}
                target="_blank"
                rel="noreferrer"
                className="flex flex-1 items-center justify-center gap-1.5 rounded-lg bg-emerald-500/10 py-1.5 text-xs font-medium text-emerald-400 transition-colors hover:bg-emerald-500/20"
              >
                <MessageCircle className="h-3.5 w-3.5" /> WhatsApp
              </a>
            </div>
            {unit.status === 'PENDING' || unit.status === 'OVERDUE' ? (
              <button
                onClick={() => onMarkPaid(unit)}
                className="mt-2 flex w-full items-center justify-center gap-1.5 rounded-lg bg-emerald-500/10 px-3 py-1.5 text-xs font-semibold text-emerald-400 transition-colors hover:bg-emerald-500/20"
              >
                <CheckCircle2 className="h-3.5 w-3.5" /> Mark as Paid
              </button>
            ) : null}
          </motion.div>
        </>
      ) : unit.status === 'UNDER_CONSTRUCTION' ? (
        <div className="flex flex-col items-center justify-center py-6 text-center">
          <span className="mb-2 text-2xl">🚧</span>
          <p className="text-sm text-cyan-300">Under Construction</p>
        </div>
      ) : (
        <div className="flex flex-col items-center justify-center py-6 text-center">
          <Home className="mb-2 h-8 w-8 text-slate-600" />
          <p className="text-sm text-slate-500">No tenant</p>
          <button className="mt-3 flex items-center gap-1.5 rounded-lg bg-indigo-500/10 px-3 py-1.5 text-xs font-semibold text-indigo-400 transition-colors hover:bg-indigo-500/20">
            <Plus className="h-3.5 w-3.5" /> Assign
          </button>
        </div>
      )}
    </motion.div>
  );
}

/* ===========================================
   Owner Dashboard
   =========================================== */
/** Map API unit DTO to frontend card shape. */
function mapApiUnit(u) {
  const rentStatus = u.rent_status || u.rentStatus || 'VACANT';
  const tenantName = u.tenant_name || u.tenantName;
  const tenantPhone = u.tenant_phone || u.tenantPhone;
  return {
    id: u.id,
    name: u.unit_number || u.unitNumber || u.address,
    type: u.type || 'House',
    area: u.floor_label || u.floorLabel || '',
    floor: u.floor_label || u.floorLabel || 'Other',
    tenant: tenantName ? {
      id: u.tenant_id || u.tenantId,
      name: tenantName,
      phone: tenantPhone || '',
      since: u.lease_start || u.leaseStart || '',
    } : null,
    rentAmount: Number(u.rent_amount ?? u.rentAmount ?? 0),
    deposit: Number(u.deposit ?? 0),
    status: rentStatus,
    currentPaymentId: u.current_payment_id || u.currentPaymentId,
    propertyStatus: u.property_status || u.propertyStatus,
  };
}

function groupByFloor(units) {
  const order = ['Ground Floor', '1st Floor', '2nd Floor', '3rd Floor'];
  const groups = {};
  units.forEach((u) => {
    const floor = u.floor || 'Other';
    if (!groups[floor]) groups[floor] = [];
    groups[floor].push(u);
  });
  return order
    .filter((f) => groups[f])
    .map((floor) => ({ floor, units: groups[floor] }));
}

export default function OwnerDashboard() {
  const navigate = useNavigate();
  const [units, setUnits] = useState(MOCK_UNITS);
  const [floors, setFloors] = useState(FLOORS);
  const [summary, setSummary] = useState(() => getDashboardSummary());
  const [usingApi, setUsingApi] = useState(false);

  const handleMarkPaid = async (unit) => {
    try {
      await ownerAPI.markPaid({
        unitNumber: unit.name,
        paymentMode: 'CASH',
        paymentDate: new Date().toISOString().split('T')[0],
        notes: 'Marked as paid from dashboard',
      });
      toast.success(`Rent marked as paid for ${unit.name}`);
      // Refresh data
      const unitsRes = await ownerAPI.getUnits();
      if (unitsRes.data?.length) {
        const mapped = unitsRes.data.map(mapApiUnit);
        setUnits(mapped);
        setFloors(groupByFloor(mapped));
      }
    } catch (err) {
      toast.error('Failed to mark as paid');
      console.error(err);
    }
  };

  useEffect(() => {
    async function fetchData() {
      try {
        const [unitsRes, dashRes] = await Promise.all([
          ownerAPI.getUnits(),
          ownerAPI.getDashboardStats(),
        ]);
        let mapped = units;
        if (unitsRes.data?.length) {
          mapped = unitsRes.data.map(mapApiUnit);
          setUnits(mapped);
          setFloors(groupByFloor(mapped));
          setUsingApi(true);
        }
        if (dashRes.data) {
          const d = dashRes.data;
          setSummary({
            total: d.total_units ?? d.totalUnits ?? mapped.length,
            occupied: d.occupied ?? 0,
            underConstruction: mapped.filter((u) => u.status === 'UNDER_CONSTRUCTION').length,
            vacant: d.vacant ?? 0,
            paid: mapped.filter((u) => u.status === 'PAID').length,
            pending: mapped.filter((u) => ['PENDING', 'OVERDUE'].includes(u.status)).length,
            collected: Number(d.collected ?? 0),
            pendingAmount: Number(d.pending ?? 0),
            totalExpected: mapped.filter((u) => u.tenant).reduce((s, u) => s + u.rentAmount, 0),
          });
        }
      } catch (err) {
        console.log('Using mock data (backend not available)', err?.message);
      }
    }
    fetchData();
  }, []);

  const collectionRate = Math.round((summary.collected / summary.totalExpected) * 100);
  const { ref, inView } = useInView({ triggerOnce: true });

  const statusCounts = useMemo(() => {
    const counts = { PAID: 0, PENDING: 0, OVERDUE: 0, VACANT: 0 };
    units.forEach((u) => counts[u.status]++);
    return counts;
  }, [units]);

  return (
    <div className="space-y-6">
      {/* Header */}
      <motion.div
        initial={{ opacity: 0, y: -10 }}
        animate={{ opacity: 1, y: 0 }}
        className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center"
      >
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-extrabold text-white" style={{ fontFamily: 'Plus Jakarta Sans' }}>
              Welcome back, Elumalai! <span className="inline-block">👋</span>
            </h1>
          </div>
          <p className="mt-1 text-sm text-slate-500">
            Here's your property overview • {BUILDING.name}, Bengaluru
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button onClick={() => navigate('/owner/payments')}
            className="flex items-center gap-2 rounded-xl border border-white/[0.08] bg-white/[0.03] px-4 py-2.5 text-sm font-medium text-slate-300 transition-colors hover:bg-white/5">
            <Download className="h-4 w-4" /> View Payments
          </button>
          <button onClick={() => navigate('/owner/tenants')}
            className="btn-shimmer flex items-center gap-2 rounded-xl bg-gradient-to-r from-indigo-500 to-violet-500 px-4 py-2.5 text-sm font-semibold text-white shadow-lg shadow-indigo-500/30 transition-shadow hover:shadow-indigo-500/50">
            <Plus className="h-4 w-4" /> Add Tenant
          </button>
        </div>
      </motion.div>

      {/* Stats bar */}
      <div ref={ref} className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard icon={Building2} label="Total Units" value={summary.total}
          sub={`${summary.occupied} occupied • ${summary.vacant} vacant`}
          color="bg-gradient-to-br from-indigo-500 to-blue-500"
          glow="bg-gradient-to-r from-indigo-500/0 via-indigo-500/50 to-indigo-500/0"
          delay={0} />
        <StatCard icon={Users} label="Active Tenants" value={summary.occupied}
          sub={`${summary.vacant} units vacant`}
          color="bg-gradient-to-br from-violet-500 to-purple-500"
          glow="bg-gradient-to-r from-violet-500/0 via-violet-500/50 to-violet-500/0"
          delay={0.1} />
        <StatCard icon={IndianRupee} label="Collected This Month" value={summary.collected}
          sub={`of ₹${summary.totalExpected.toLocaleString('en-IN')} expected`}
          color="bg-gradient-to-br from-emerald-500 to-green-500"
          glow="bg-gradient-to-r from-emerald-500/0 via-emerald-500/50 to-emerald-500/0"
          delay={0.2} />
        <StatCard icon={Clock} label="Pending / Overdue" value={summary.pendingAmount}
          sub={`${summary.pending} tenants • ${collectionRate}% collected`}
          color="bg-gradient-to-br from-amber-500 to-orange-500"
          glow="bg-gradient-to-r from-amber-500/0 via-amber-500/50 to-amber-500/0"
          delay={0.3} />
      </div>

      {/* Collection progress bar */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.4 }}
        className="glass-card p-5"
      >
        <div className="mb-2 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <TrendingUp className="h-4 w-4 text-indigo-400" />
            <p className="text-sm font-semibold text-white">Collection Progress</p>
          </div>
          <p className="tabular text-sm font-bold text-white">
            {collectionRate}%
          </p>
        </div>
        <div className="h-2.5 overflow-hidden rounded-full bg-white/[0.05]">
          <motion.div
            initial={{ width: 0 }}
            animate={{ width: `${collectionRate}%` }}
            transition={{ delay: 0.6, duration: 1.2, ease: 'easeOut' }}
            className="h-full rounded-full bg-gradient-to-r from-indigo-500 via-violet-500 to-cyan-500"
          />
        </div>
      </motion.div>

      {/* Status legend */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.5 }}
        className="flex flex-wrap items-center gap-4"
      >
        {Object.entries(STATUS_CONFIG).map(([key, cfg]) => (
          <div key={key} className="flex items-center gap-2">
            <span className={cn(
              'h-2.5 w-2.5 rounded-full',
              cfg.dot,
              cfg.pulse && key === 'OVERDUE' && 'animate-pulse'
            )} />
            <span className="text-xs font-medium text-slate-500">
              {cfg.label} ({statusCounts[key]})
            </span>
          </div>
        ))}
      </motion.div>

      {/* Unit cards grouped by floor */}
      <div className="space-y-8">
        {(usingApi ? floors : FLOORS).map((floorSection, fi) => (
          <div key={floorSection.floor}>
            {/* Floor divider */}
            <motion.div
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.3 + fi * 0.1 }}
              className="mb-4 flex items-center gap-3"
            >
              <div className="h-px flex-1 bg-gradient-to-r from-indigo-500/30 to-transparent" />
              <span className="whitespace-nowrap text-sm font-bold uppercase tracking-wider text-slate-400">
                {floorSection.floor}
              </span>
              <div className="h-px flex-1 bg-gradient-to-l from-indigo-500/30 to-transparent" />
            </motion.div>
            {/* Floor unit cards */}
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
              {floorSection.units.map((unit, i) => (
                <UnitCard key={unit.id} unit={unit} index={fi * 4 + i} onMarkPaid={handleMarkPaid} />
              ))}
            </div>
          </div>
        ))}
      </div>

      {/* Alert banner */}
      {(statusCounts.OVERDUE > 0 || statusCounts.PENDING > 0) && (
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.8 }}
          className="glass-card flex items-start gap-3 border-amber-500/20 p-4"
        >
          <AlertCircle className="mt-0.5 h-5 w-5 shrink-0 text-amber-500" />
          <div>
            <p className="text-sm font-semibold text-amber-300">
              {statusCounts.OVERDUE > 0 && `${statusCounts.OVERDUE} tenant(s) overdue — `}
              {statusCounts.PENDING > 0 && `${statusCounts.PENDING} tenant(s) pending payment`}
            </p>
            <p className="mt-0.5 text-xs text-amber-500/70">
              Total pending: ₹{summary.pendingAmount.toLocaleString('en-IN')} • Send reminders from the Reminders page
            </p>
          </div>
        </motion.div>
      )}
    </div>
  );
}
