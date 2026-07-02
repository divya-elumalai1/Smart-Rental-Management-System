import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Phone, MessageCircle, Search, UserPlus, Edit2, Trash2, AlertTriangle } from 'lucide-react';
import toast from 'react-hot-toast';
import AddTenantModal from '../components/modals/AddTenantModal';
import EditTenantModal from '../components/modals/EditTenantModal';
import { tenantAPI, dashboardAPI } from '../utils/api';

function mapTenant(t) {
  return {
    leaseId: t.lease_id || t.leaseId,
    tenantId: t.tenant_id || t.tenantId,
    propertyId: t.property_id || t.propertyId,
    name: t.unit_number || t.unitNumber,
    floor: t.floor_label || t.floorLabel,
    tenant: {
      name: t.tenant_name || t.tenantName,
      email: t.email,
      phone: t.phone_number || t.phoneNumber,
      since: t.lease_start || t.leaseStart,
    },
    rentAmount: Number(t.rent_amount ?? t.rentAmount ?? 0),
    deposit: Number(t.deposit ?? 0),
    rentStatus: t.rent_status || t.rentStatus,
    raw: t,
  };
}

export default function TenantsPage() {
  const [tenants, setTenants] = useState([]);
  const [vacantUnits, setVacantUnits] = useState([]);
  const [search, setSearch] = useState('');
  const [showAddModal, setShowAddModal] = useState(false);
  const [editTarget, setEditTarget] = useState(null);
  const [removeTarget, setRemoveTarget] = useState(null);
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    try {
      const [tenantsRes, unitsRes] = await Promise.all([
        tenantAPI.getAll(),
        dashboardAPI.getOwnerUnits(),
      ]);
      setTenants((tenantsRes.data || []).map(mapTenant));
      const vacant = (unitsRes.data || [])
        .filter((u) => {
          const status = u.rent_status || u.rentStatus || u.property_status || u.propertyStatus;
          return status === 'VACANT' || status === 'UNDER_CONSTRUCTION';
        })
        .map((u) => ({
          name: u.unit_number || u.unitNumber,
          area: u.floor_label || u.floorLabel,
          rentAmount: u.rent_amount ?? u.rentAmount,
        }));
      setVacantUnits(vacant);
    } catch {
      toast.error('Could not load tenants — is the backend running?');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadData(); }, []);

  const filtered = tenants.filter((t) =>
    t.tenant.name.toLowerCase().includes(search.toLowerCase())
  );

  const handleAddTenant = async (form) => {
    const [firstName, ...rest] = form.name.trim().split(' ');
    const lastName = rest.join(' ') || firstName;
    try {
      await tenantAPI.assign({
        unit_number: form.unit,
        first_name: firstName,
        last_name: lastName,
        email: form.email,
        phone_number: form.phone,
        rent_amount: +form.rent,
        deposit: form.deposit ? +form.deposit : 0,
        lease_start: form.leaseStart || new Date().toISOString().split('T')[0],
        lease_end: form.leaseEnd || null,
        password: form.password,
      });
      toast.success(`Tenant added to Unit ${form.unit}`);
      setShowAddModal(false);
      loadData();
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Failed to add tenant');
    }
  };

  const handleEditTenant = async (form) => {
    const [firstName, ...rest] = form.name.trim().split(' ');
    const lastName = rest.join(' ') || firstName;
    try {
      await tenantAPI.update(editTarget.leaseId, {
        first_name: firstName,
        last_name: lastName,
        email: form.email,
        phone_number: form.phone,
        rent_amount: +form.rent,
        deposit: form.deposit ? +form.deposit : 0,
        lease_start: form.leaseStart || undefined,
        lease_end: form.leaseEnd || undefined,
      });
      toast.success('Tenant updated');
      setEditTarget(null);
      loadData();
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Failed to update tenant');
    }
  };

  const handleRemoveTenant = async () => {
    if (!removeTarget) return;
    try {
      await tenantAPI.remove(removeTarget.leaseId);
      toast.success(`${removeTarget.tenant.name} removed from Unit ${removeTarget.name}`);
      setRemoveTarget(null);
      loadData();
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Failed to remove tenant');
    }
  };

  if (loading) {
    return <div className="py-20 text-center text-slate-500">Loading tenants…</div>;
  }

  return (
    <div className="space-y-6">
      <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }}
        className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-2xl font-extrabold text-white" style={{ fontFamily: 'Plus Jakarta Sans' }}>Tenants</h1>
          <p className="mt-1 text-sm text-slate-500">{tenants.length} active tenants • {vacantUnits.length} vacant units</p>
        </div>
        <button onClick={() => setShowAddModal(true)}
          className="btn-shimmer flex items-center gap-2 rounded-xl bg-gradient-to-r from-indigo-500 to-violet-500 px-4 py-2.5 text-sm font-semibold text-white shadow-lg shadow-indigo-500/30">
          <UserPlus className="h-4 w-4" /> Add Tenant
        </button>
      </motion.div>

      <div className="relative">
        <Search className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" />
        <input type="text" value={search} onChange={(e) => setSearch(e.target.value)}
          placeholder="Search tenants by name..."
          className="input-glow w-full max-w-md rounded-xl border border-white/[0.08] bg-white/[0.03] py-2.5 pl-11 pr-4 text-sm text-white placeholder:text-slate-500" />
      </div>

      <div className="glass-card overflow-hidden">
        <table className="w-full">
          <thead>
            <tr className="border-b border-white/[0.06] text-left text-xs uppercase tracking-wider text-slate-500">
              <th className="px-6 py-4">Tenant</th><th className="px-6 py-4">Phone</th><th className="px-6 py-4">Unit</th>
              <th className="px-6 py-4">Rent</th><th className="px-6 py-4">Status</th><th className="px-6 py-4 text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map((row, i) => (
              <motion.tr key={row.leaseId} initial={{ opacity: 0, x: -20 }} animate={{ opacity: 1, x: 0 }} transition={{ delay: i * 0.05 }}
                className="border-b border-white/[0.04] transition-colors hover:bg-white/[0.02]">
                <td className="px-6 py-4">
                  <div className="flex items-center gap-3">
                    <div className="flex h-9 w-9 items-center justify-center rounded-full bg-gradient-to-br from-indigo-400 to-violet-500 text-xs font-bold text-white">{row.tenant.name.charAt(0)}</div>
                    <div><p className="text-sm font-semibold text-white">{row.tenant.name}</p><p className="text-xs text-slate-500">{row.tenant.email}</p></div>
                  </div>
                </td>
                <td className="px-6 py-4 text-sm text-slate-300">{row.tenant.phone}</td>
                <td className="px-6 py-4"><span className="rounded-lg bg-white/[0.05] px-2.5 py-1 text-xs font-medium text-slate-300">{row.name}</span></td>
                <td className="px-6 py-4 text-sm font-semibold text-white">₹{row.rentAmount.toLocaleString('en-IN')}</td>
                <td className="px-6 py-4"><span className="flex items-center gap-1.5 rounded-full bg-emerald-500/10 px-2.5 py-1 text-xs font-semibold text-emerald-300 ring-1 ring-emerald-500/30"><span className="h-1.5 w-1.5 rounded-full bg-emerald-500" /> {row.rentStatus || 'Active'}</span></td>
                <td className="px-6 py-4">
                  <div className="flex items-center justify-end gap-2">
                    <a href={`tel:${row.tenant.phone}`} className="rounded-lg bg-white/[0.05] p-2 text-slate-400 hover:bg-white/10 hover:text-white"><Phone className="h-4 w-4" /></a>
                    <a href={`https://wa.me/${row.tenant.phone?.replace(/\D/g, '')}`} target="_blank" rel="noreferrer" className="rounded-lg bg-emerald-500/10 p-2 text-emerald-400 hover:bg-emerald-500/20"><MessageCircle className="h-4 w-4" /></a>
                    <button onClick={() => setEditTarget(row)} className="rounded-lg bg-white/[0.05] p-2 text-slate-400 hover:bg-white/10 hover:text-white"><Edit2 className="h-4 w-4" /></button>
                    <button onClick={() => setRemoveTarget(row)} className="rounded-lg bg-red-500/10 p-2 text-red-400 hover:bg-red-500/20"><Trash2 className="h-4 w-4" /></button>
                  </div>
                </td>
              </motion.tr>
            ))}
          </tbody>
        </table>
      </div>

      <AddTenantModal isOpen={showAddModal} onClose={() => setShowAddModal(false)} onAdd={handleAddTenant} vacantUnits={vacantUnits} />
      <EditTenantModal isOpen={!!editTarget} tenant={editTarget} onClose={() => setEditTarget(null)} onSave={handleEditTenant} />

      <AnimatePresence>
        {removeTarget && (
          <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4" onClick={() => setRemoveTarget(null)}>
            <motion.div initial={{ opacity: 0, scale: 0.95, y: 20 }} animate={{ opacity: 1, scale: 1, y: 0 }} exit={{ opacity: 0, scale: 0.95, y: 20 }}
              className="glass-card w-full max-w-sm p-6" onClick={(e) => e.stopPropagation()}>
              <div className="mb-4 flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-red-500/20"><AlertTriangle className="h-5 w-5 text-red-400" /></div>
                <h2 className="text-lg font-bold text-white">Remove Tenant?</h2>
              </div>
              <p className="mb-6 text-sm text-slate-400">Remove <span className="font-semibold text-white">{removeTarget.tenant.name}</span> from Unit <span className="font-semibold text-white">{removeTarget.name}</span>?</p>
              <div className="flex gap-3">
                <button onClick={() => setRemoveTarget(null)} className="flex-1 rounded-xl border border-white/[0.08] bg-white/[0.03] py-3 text-sm font-medium text-slate-300 hover:bg-white/5">Cancel</button>
                <button onClick={handleRemoveTenant} className="flex-1 rounded-xl bg-red-500 py-3 text-sm font-semibold text-white shadow-lg shadow-red-500/30 hover:bg-red-600">Remove</button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
