import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { User, Mail, Phone, Calendar, Shield, MapPin, CheckCircle, XCircle } from 'lucide-react';
import { authAPI } from '../utils/api';
import toast from 'react-hot-toast';

export default function TenantProfile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const res = await authAPI.me();
        setProfile(res.data);
      } catch (err) {
        toast.error('Failed to load profile');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-2 border-indigo-500 border-t-transparent" />
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="flex flex-col items-center justify-center py-16">
        <User className="mb-4 h-16 w-16 text-slate-600" />
        <p className="text-lg font-medium text-slate-400">Could not load profile</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }}>
        <h1 className="text-2xl font-extrabold text-white" style={{ fontFamily: 'Plus Jakarta Sans' }}>My Profile</h1>
        <p className="mt-1 text-sm text-slate-500">Your account information</p>
      </motion.div>

      <div className="grid gap-6 lg:grid-cols-3">
        <motion.div initial={{ opacity: 0, x: -20 }} animate={{ opacity: 1, x: 0 }} className="glass-card p-6 lg:col-span-1">
          <div className="flex flex-col items-center text-center">
            <div className="mb-4 flex h-24 w-24 items-center justify-center rounded-full bg-gradient-to-br from-indigo-500/20 to-violet-500/20 ring-2 ring-indigo-500/30">
              {profile.profile_image_url ? (
                <img src={profile.profile_image_url} alt="" className="h-full w-full rounded-full object-cover" />
              ) : (
                <User className="h-10 w-10 text-indigo-400" />
              )}
            </div>
            <h2 className="text-xl font-bold text-white">
              {profile.first_name || ''} {profile.last_name || ''}
            </h2>
            <p className="text-sm text-slate-400">{profile.email}</p>
            <span className="mt-3 inline-flex items-center gap-1.5 rounded-full bg-indigo-500/10 px-3 py-1 text-xs font-semibold text-indigo-300 ring-1 ring-indigo-500/30">
              <Shield className="h-3 w-3" /> {profile.role || 'USER'}
            </span>
          </div>
        </motion.div>

        <motion.div initial={{ opacity: 0, x: 20 }} animate={{ opacity: 1, x: 0 }} className="glass-card p-6 lg:col-span-2">
          <h3 className="mb-4 text-lg font-bold text-white">Personal Information</h3>
          <div className="grid gap-4 sm:grid-cols-2">
            <InfoRow icon={User} label="First Name" value={profile.first_name || '-'} />
            <InfoRow icon={User} label="Last Name" value={profile.last_name || '-'} />
            <InfoRow icon={Mail} label="Email" value={profile.email || '-'} />
            <InfoRow icon={Phone} label="Phone" value={profile.phone_number || '-'} />
            <InfoRow icon={Calendar} label="Date of Birth" value={profile.date_of_birth || '-'} />
            <InfoRow icon={MapPin} label="Address" value={profile.address || '-'} />
            <InfoRow icon={MapPin} label="City" value={profile.city || '-'} />
            <InfoRow icon={MapPin} label="State" value={profile.state || '-'} />
            <InfoRow icon={Shield} label="Role" value={profile.role || '-'} />
          </div>

          <h3 className="mb-4 mt-6 text-lg font-bold text-white">Account Status</h3>
          <div className="grid gap-4 sm:grid-cols-3">
            <StatusBadge label="Email Verified" active={profile.email_verified} />
            <StatusBadge label="Phone Verified" active={profile.phone_verified} />
            <StatusBadge label="Active" active={profile.active} />
          </div>
        </motion.div>
      </div>
    </div>
  );
}

function InfoRow({ icon: Icon, label, value }) {
  return (
    <div className="flex items-start gap-3 rounded-xl bg-white/[0.03] p-3">
      <div className="mt-0.5 rounded-lg bg-indigo-500/10 p-2">
        <Icon className="h-4 w-4 text-indigo-400" />
      </div>
      <div>
        <p className="text-[10px] font-medium uppercase tracking-wider text-slate-500">{label}</p>
        <p className="text-sm font-medium text-white">{value}</p>
      </div>
    </div>
  );
}

function StatusBadge({ label, active }) {
  return (
    <div className={`flex items-center gap-2 rounded-xl p-3 ${active ? 'bg-emerald-500/10' : 'bg-red-500/5'}`}>
      {active ? (
        <CheckCircle className="h-4 w-4 text-emerald-400" />
      ) : (
        <XCircle className="h-4 w-4 text-slate-600" />
      )}
      <span className={`text-xs font-medium ${active ? 'text-emerald-300' : 'text-slate-500'}`}>{label}</span>
    </div>
  );
}
