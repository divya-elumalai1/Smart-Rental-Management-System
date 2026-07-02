import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Wrench, Plus, AlertCircle, Clock, CheckCircle2, XCircle, MessageSquare } from 'lucide-react';
import { maintenanceAPI, tenantPortalAPI } from '../utils/api';
import toast from 'react-hot-toast';

const STATUS_STYLES = {
  PENDING: { label: 'Pending', color: 'text-amber-300', bg: 'bg-amber-500/10', ring: 'ring-amber-500/30', icon: Clock },
  IN_PROGRESS: { label: 'In Progress', color: 'text-cyan-300', bg: 'bg-cyan-500/10', ring: 'ring-cyan-500/30', icon: AlertCircle },
  RESOLVED: { label: 'Resolved', color: 'text-emerald-300', bg: 'bg-emerald-500/10', ring: 'ring-emerald-500/30', icon: CheckCircle2 },
  CANCELLED: { label: 'Cancelled', color: 'text-slate-400', bg: 'bg-slate-500/10', ring: 'ring-slate-500/30', icon: XCircle },
};

const PRIORITY_STYLES = {
  URGENT: 'text-red-400 bg-red-500/10 ring-red-500/30',
  HIGH: 'text-orange-400 bg-orange-500/10 ring-orange-500/30',
  MEDIUM: 'text-amber-400 bg-amber-500/10 ring-amber-500/30',
  LOW: 'text-slate-400 bg-slate-500/10 ring-slate-500/30',
};

export default function TenantMaintenance() {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ title: '', description: '', priority: 'MEDIUM' });
  const [submitting, setSubmitting] = useState(false);
  const [expandedId, setExpandedId] = useState(null);

  const fetchRequests = async () => {
    try {
      const res = await maintenanceAPI.getAll();
      const all = res.data || [];
      const myId = JSON.parse(localStorage.getItem('user') || '{}')?.id;
      setRequests(myId ? all.filter(r => r.tenant_id === myId) : all);
    } catch (err) {
      toast.error('Failed to load maintenance requests');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchRequests(); }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.title.trim() || !form.description.trim()) {
      toast.error('Please fill in all fields');
      return;
    }
    setSubmitting(true);
    try {
      await tenantPortalAPI.raiseMaintenance(form);
      toast.success('Maintenance request submitted');
      setShowForm(false);
      setForm({ title: '', description: '', priority: 'MEDIUM' });
      fetchRequests();
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Failed to submit request');
    } finally {
      setSubmitting(false);
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
      <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }}
        className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-2xl font-extrabold text-white" style={{ fontFamily: 'Plus Jakarta Sans' }}>Maintenance</h1>
          <p className="mt-1 text-sm text-slate-500">Request and track maintenance</p>
        </div>
        <button onClick={() => setShowForm(true)}
          className="btn-shimmer flex items-center gap-2 rounded-xl bg-gradient-to-r from-amber-500 to-orange-500 px-4 py-2.5 text-sm font-semibold text-white shadow-lg shadow-amber-500/30">
          <Plus className="h-4 w-4" /> New Request
        </button>
      </motion.div>

      {showForm && (
        <motion.div initial={{ opacity: 0, y: -20 }} animate={{ opacity: 1, y: 0 }} className="glass-card p-6">
          <h3 className="mb-4 font-bold text-white">Raise Maintenance Request</h3>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="mb-1 block text-xs font-medium text-slate-400">Title</label>
              <input type="text" value={form.title} onChange={e => setForm(f => ({...f, title: e.target.value}))}
                placeholder="e.g. Leaking tap in bathroom"
                className="w-full rounded-xl border border-white/[0.08] bg-white/[0.03] px-4 py-2.5 text-sm text-white placeholder:text-slate-600 focus:border-indigo-500/50 focus:outline-none" />
            </div>
            <div>
              <label className="mb-1 block text-xs font-medium text-slate-400">Description</label>
              <textarea value={form.description} onChange={e => setForm(f => ({...f, description: e.target.value}))}
                placeholder="Describe the issue in detail"
                rows={3}
                className="w-full rounded-xl border border-white/[0.08] bg-white/[0.03] px-4 py-2.5 text-sm text-white placeholder:text-slate-600 focus:border-indigo-500/50 focus:outline-none" />
            </div>
            <div>
              <label className="mb-1 block text-xs font-medium text-slate-400">Priority</label>
              <select value={form.priority} onChange={e => setForm(f => ({...f, priority: e.target.value}))}
                className="w-full rounded-xl border border-white/[0.08] bg-[#0F172A] px-4 py-2.5 text-sm text-white focus:border-indigo-500/50 focus:outline-none">
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="URGENT">Urgent</option>
              </select>
            </div>
            <div className="flex gap-3">
              <button type="submit" disabled={submitting}
                className="btn-shimmer flex items-center gap-2 rounded-xl bg-gradient-to-r from-amber-500 to-orange-500 px-5 py-2.5 text-sm font-semibold text-white shadow-lg shadow-amber-500/30 disabled:opacity-50">
                {submitting ? 'Submitting...' : 'Submit Request'}
              </button>
              <button type="button" onClick={() => setShowForm(false)}
                className="rounded-xl border border-white/[0.08] bg-white/[0.03] px-5 py-2.5 text-sm font-medium text-slate-300 hover:bg-white/5">
                Cancel
              </button>
            </div>
          </form>
        </motion.div>
      )}

      {requests.length === 0 ? (
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="flex flex-col items-center justify-center py-16">
          <Wrench className="mb-4 h-16 w-16 text-slate-600" />
          <p className="text-lg font-medium text-slate-400">No maintenance requests</p>
          <p className="mt-1 text-sm text-slate-500">Submit a request if you need repairs</p>
        </motion.div>
      ) : (
        <div className="space-y-4">
          {requests.map((req, i) => {
            const sts = STATUS_STYLES[req.status] || STATUS_STYLES.PENDING;
            const Icon = sts.icon;
            const isExpanded = expandedId === req.id;
            return (
              <motion.div key={req.id} initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.05 }}
                className="glass-card p-5">
                <div className="flex items-start justify-between cursor-pointer" onClick={() => setExpandedId(isExpanded ? null : req.id)}>
                  <div className="flex-1">
                    <div className="flex items-center gap-3">
                      <h3 className="font-semibold text-white">{req.title}</h3>
                      <span className={`rounded-full px-2 py-0.5 text-[10px] font-semibold ring-1 ${PRIORITY_STYLES[req.priority] || PRIORITY_STYLES.MEDIUM}`}>
                        {req.priority || 'MEDIUM'}
                      </span>
                    </div>
                    <p className="mt-1 text-xs text-slate-500">
                      {req.created_at ? new Date(req.created_at).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' }) : ''}
                    </p>
                  </div>
                  <span className={`inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-semibold ring-1 ${sts.bg} ${sts.color} ${sts.ring}`}>
                    <Icon className="h-3 w-3" /> {sts.label}
                  </span>
                </div>
                {isExpanded && (
                  <motion.div initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }} className="mt-4 border-t border-white/[0.06] pt-4">
                    <p className="text-sm text-slate-300">{req.description}</p>
                    {req.resolution_notes && (
                      <div className="mt-3 rounded-lg bg-emerald-500/5 p-3">
                        <p className="text-xs font-medium text-emerald-400">Resolution Notes</p>
                        <p className="mt-1 text-sm text-slate-300">{req.resolution_notes}</p>
                      </div>
                    )}
                    {req.comments && req.comments.length > 0 && (
                      <div className="mt-3 space-y-2">
                        <p className="text-xs font-medium text-slate-500 flex items-center gap-1">
                          <MessageSquare className="h-3 w-3" /> Comments
                        </p>
                        {req.comments.map((c, ci) => (
                          <div key={ci} className="rounded-lg bg-white/[0.03] p-3">
                            <p className="text-xs text-slate-500">{c.user_name || 'Unknown'} • {c.created_at ? new Date(c.created_at).toLocaleDateString('en-IN') : ''}</p>
                            <p className="mt-1 text-sm text-slate-300">{c.comment}</p>
                          </div>
                        ))}
                      </div>
                    )}
                  </motion.div>
                )}
              </motion.div>
            );
          })}
        </div>
      )}
    </div>
  );
}
