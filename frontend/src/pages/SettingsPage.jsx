import { motion } from 'framer-motion';
import { User, Bell, Shield, Building2, Mail, Phone } from 'lucide-react';

export default function SettingsPage() {
  return (
    <div className="space-y-6">
      <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }}>
        <h1 className="text-2xl font-extrabold text-white" style={{ fontFamily: 'Plus Jakarta Sans' }}>Settings</h1>
        <p className="mt-1 text-sm text-slate-500">Manage your account and preferences</p>
      </motion.div>

      {/* Profile */}
      <div className="glass-card p-6">
        <div className="mb-4 flex items-center gap-2">
          <User className="h-4 w-4 text-indigo-400" />
          <h3 className="font-bold text-white">Owner Profile</h3>
        </div>
        <div className="flex items-center gap-4">
          <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-amber-400 to-orange-500 text-2xl font-bold text-white">E</div>
          <div>
            <p className="text-lg font-bold text-white">Elumalai</p>
            <p className="text-sm text-slate-500">Owner • Sapthagiri Residency</p>
            <div className="mt-1 flex items-center gap-4 text-xs text-slate-500">
              <span className="flex items-center gap-1"><Mail className="h-3 w-3" /> elumalai@sapthagiri.com</span>
              <span className="flex items-center gap-1"><Phone className="h-3 w-3" /> +91 98456 78900</span>
            </div>
          </div>
        </div>
      </div>

      {/* Building Info */}
      <div className="glass-card p-6">
        <div className="mb-4 flex items-center gap-2">
          <Building2 className="h-4 w-4 text-indigo-400" />
          <h3 className="font-bold text-white">Building Details</h3>
        </div>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div><label className="text-xs uppercase tracking-wider text-slate-500">Building Name</label><p className="mt-1 text-sm font-medium text-white">Sapthagiri Residency</p></div>
          <div><label className="text-xs uppercase tracking-wider text-slate-500">Location</label><p className="mt-1 text-sm font-medium text-white">Bengaluru, Karnataka</p></div>
          <div><label className="text-xs uppercase tracking-wider text-slate-500">Total Units</label><p className="mt-1 text-sm font-medium text-white">10</p></div>
          <div><label className="text-xs uppercase tracking-wider text-slate-500">Occupied</label><p className="mt-1 text-sm font-medium text-white">4</p></div>
        </div>
      </div>

      {/* Notifications */}
      <div className="glass-card p-6">
        <div className="mb-4 flex items-center gap-2">
          <Bell className="h-4 w-4 text-indigo-400" />
          <h3 className="font-bold text-white">Notification Preferences</h3>
        </div>
        <div className="space-y-4">
          {[
            { label: 'Email reminders to tenants', enabled: true },
            { label: 'WhatsApp reminders to tenants', enabled: true },
            { label: 'Overdue payment alerts', enabled: true },
            { label: 'Maintenance request notifications', enabled: true },
            { label: 'Monthly income summary', enabled: false },
          ].map((item) => (
            <div key={item.label} className="flex items-center justify-between">
              <span className="text-sm text-slate-300">{item.label}</span>
              <button className={`relative h-6 w-11 rounded-full transition-colors ${item.enabled ? 'bg-indigo-500' : 'bg-slate-700'}`}>
                <span className={`absolute top-0.5 h-5 w-5 rounded-full bg-white transition-transform ${item.enabled ? 'left-[22px]' : 'left-0.5'}`} />
              </button>
            </div>
          ))}
        </div>
      </div>

      {/* Security */}
      <div className="glass-card p-6">
        <div className="mb-4 flex items-center gap-2">
          <Shield className="h-4 w-4 text-indigo-400" />
          <h3 className="font-bold text-white">Security</h3>
        </div>
        <div className="space-y-3">
          <button className="w-full rounded-xl border border-white/[0.08] bg-white/[0.03] px-4 py-3 text-left text-sm font-medium text-slate-300 hover:bg-white/5">Change Password</button>
          <button className="w-full rounded-xl border border-white/[0.08] bg-white/[0.03] px-4 py-3 text-left text-sm font-medium text-slate-300 hover:bg-white/5">Two-Factor Authentication</button>
          <button className="w-full rounded-xl border border-red-500/20 bg-red-500/5 px-4 py-3 text-left text-sm font-medium text-red-400 hover:bg-red-500/10">Delete Account</button>
        </div>
      </div>
    </div>
  );
}
