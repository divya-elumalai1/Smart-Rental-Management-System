import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Bell, Mail, Clock } from 'lucide-react';
import toast from 'react-hot-toast';
import { reminderAPI, dashboardAPI } from '../utils/api';

export default function RemindersPage() {
  const [reminders, setReminders] = useState([]);
  const [logs, setLogs] = useState([]);
  const [tenants, setTenants] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      reminderAPI.getAll().catch(() => ({ data: [] })),
      reminderAPI.getLogs().catch(() => ({ data: [] })),
      dashboardAPI.getOwnerUnits().catch(() => ({ data: [] })),
    ]).then(([remRes, logsRes, unitsRes]) => {
      setReminders(remRes.data || []);
      setLogs(logsRes.data || []);
      setTenants(unitsRes.data || []);
    }).catch(() => {}).finally(() => setLoading(false));
  }, []);

  const handleSendReminder = async (id) => {
    try {
      await reminderAPI.send(id);
      toast.success('Reminder sent');
      const [remRes, logsRes] = await Promise.all([
        reminderAPI.getAll(),
        reminderAPI.getLogs(),
      ]);
      setReminders(remRes.data || []);
      setLogs(logsRes.data || []);
    } catch {
      toast.error('Failed to send reminder');
    }
  };

  return (
    <div className="space-y-6">
      <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }}>
        <h1 className="text-2xl font-extrabold text-white" style={{ fontFamily: 'Plus Jakarta Sans' }}>Reminders</h1>
        <p className="mt-1 text-sm text-slate-500">Send rent reminders via Email & WhatsApp</p>
      </motion.div>

      {/* Reminder settings */}
      <div className="glass-card p-6">
        <h3 className="mb-4 font-bold text-white">Automated Reminder Schedule</h3>
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-4">
          {[
            { days: '7 days before', desc: 'Email reminder', icon: Mail, color: 'text-indigo-400' },
            { days: '3 days before', desc: 'Email reminder', icon: Mail, color: 'text-violet-400' },
            { days: 'On due date', desc: 'Email + SMS', icon: Bell, color: 'text-amber-400' },
            { days: '3 days after', desc: 'Overdue alert', icon: Clock, color: 'text-red-400' },
          ].map((s) => {
            const Icon = s.icon;
            return (
              <div key={s.days} className="flex items-center gap-3 rounded-xl bg-white/[0.03] p-3">
                <Icon className={`h-5 w-5 ${s.color}`} />
                <div><p className="text-sm font-semibold text-white">{s.days}</p><p className="text-xs text-slate-500">{s.desc}</p></div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Send reminders */}
      <div className="glass-card p-6">
        <h3 className="mb-4 font-bold text-white">Pending Reminders</h3>
        <div className="space-y-2">
          {reminders.filter((r) => r.status === 'PENDING').length === 0 ? (
            <p className="text-sm text-slate-500">No pending reminders</p>
          ) : (
            reminders.filter((r) => r.status === 'PENDING').map((reminder, i) => (
              <motion.div key={reminder.id} initial={{ opacity: 0, x: -20 }} animate={{ opacity: 1, x: 0 }} transition={{ delay: i * 0.05 }}
                className="flex items-center justify-between rounded-xl bg-white/[0.03] p-3">
                <div className="flex items-center gap-3">
                  <div className="flex h-9 w-9 items-center justify-center rounded-full bg-gradient-to-br from-indigo-400 to-violet-500 text-xs font-bold text-white">
                    {(reminder.tenantName || 'T').charAt(0)}
                  </div>
                  <div>
                    <p className="text-sm font-semibold text-white">{reminder.tenantName}</p>
                    <p className="text-xs text-slate-500">{reminder.type?.replace(/_/g, ' ')} • Due: {reminder.dueDate}</p>
                  </div>
                </div>
                <button onClick={() => handleSendReminder(reminder.id)}
                  className="flex items-center gap-1.5 rounded-lg bg-indigo-500/10 px-3 py-1.5 text-xs font-medium text-indigo-400 hover:bg-indigo-500/20">
                  <Mail className="h-3.5 w-3.5" /> Send
                </button>
              </motion.div>
            ))
          )}
        </div>
      </div>

      {/* Reminder logs */}
      <div className="glass-card overflow-hidden">
        <h3 className="px-6 py-4 font-bold text-white">Reminder Logs</h3>
        {logs.length === 0 ? (
          <div className="px-6 pb-4">
            <p className="text-sm text-slate-500">No reminder logs yet</p>
          </div>
        ) : (
          <table className="w-full">
            <thead><tr className="border-b border-white/[0.06] text-left text-xs uppercase tracking-wider text-slate-500">
              <th className="px-6 py-3">Tenant</th><th className="px-6 py-3">Type</th>
              <th className="px-6 py-3">Channel</th><th className="px-6 py-3">Sent At</th><th className="px-6 py-3">Status</th>
            </tr></thead>
            <tbody>
              {logs.map((log, i) => (
                <motion.tr key={log.id} initial={{ opacity: 0, x: -20 }} animate={{ opacity: 1, x: 0 }} transition={{ delay: i * 0.05 }}
                  className="border-b border-white/[0.04] hover:bg-white/[0.02]">
                  <td className="px-6 py-3 text-sm font-medium text-white">{log.tenantName}</td>
                  <td className="px-6 py-3"><span className="rounded-lg bg-white/[0.05] px-2 py-0.5 text-xs text-slate-300">{log.type?.replace(/_/g, ' ')}</span></td>
                  <td className="px-6 py-3 text-sm text-slate-400">{log.channel || 'EMAIL'}</td>
                  <td className="px-6 py-3 text-sm text-slate-400">{log.sentAt ? new Date(log.sentAt).toLocaleString('en-IN') : '-'}</td>
                  <td className="px-6 py-3">
                    <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${log.status === 'SENT' ? 'bg-emerald-500/10 text-emerald-300 ring-1 ring-emerald-500/30' : 'bg-red-500/10 text-red-300 ring-1 ring-red-500/30'}`}>{log.status}</span>
                  </td>
                </motion.tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
