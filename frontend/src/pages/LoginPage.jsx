import { useState } from 'react';
import { motion } from 'framer-motion';
import { Building2, Mail, Lock, ArrowRight, Eye, EyeOff } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';

export default function LoginPage() {
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const user = await login(email.trim(), password.trim());
      const name = user?.first_name || user?.firstName
        || (user?.role === 'TENANT' ? 'Tenant' : 'Elumalai');
      toast.success(`Welcome back, ${name}!`);
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="relative flex min-h-screen items-center justify-center overflow-hidden bg-[#020617] px-6">
      {/* Floating orbs */}
      <div className="orb orb-purple" />
      <div className="orb orb-cyan" />
      <div className="grid-overlay" />

      {/* Glass login card */}
      <motion.div
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        className="glass-card relative z-10 w-full max-w-md p-8"
      >
        {/* Logo */}
        <div className="mb-8 flex items-center gap-3">
          <motion.div
            whileHover={{ rotate: 360 }}
            transition={{ duration: 0.6 }}
            className="flex h-11 w-11 items-center justify-center rounded-xl bg-gradient-to-br from-indigo-500 to-violet-500 shadow-lg shadow-indigo-500/40"
          >
            <Building2 className="h-6 w-6 text-white" />
          </motion.div>
          <div>
            <h1 className="text-lg font-bold text-white" style={{ fontFamily: 'Plus Jakarta Sans' }}>
              Sapthagiri Residency
            </h1>
            <p className="text-xs text-slate-500">Property Management Portal</p>
          </div>
        </div>

        <h2 className="text-2xl font-extrabold text-white" style={{ fontFamily: 'Plus Jakarta Sans' }}>
          Welcome back
        </h2>
        <p className="mt-1 text-sm text-slate-500">Sign in to manage your rental units</p>

        <form onSubmit={handleSubmit} className="mt-8 space-y-5">
          {/* Email */}
          <div className="relative">
            <Mail className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-500" />
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Email address"
              className="input-glow w-full rounded-xl border border-white/[0.08] bg-white/[0.03] py-3.5 pl-12 pr-4 text-sm text-white placeholder:text-slate-500"
            />
          </div>

          {/* Password */}
          <div className="relative">
            <Lock className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-500" />
            <input
              type={showPassword ? 'text' : 'password'}
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Password"
              className="input-glow w-full rounded-xl border border-white/[0.08] bg-white/[0.03] py-3.5 pl-12 pr-12 text-sm text-white placeholder:text-slate-500"
            />
            <button
              type="button"
              onClick={() => setShowPassword((s) => !s)}
              className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300"
            >
              {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
            </button>
          </div>

          {/* Submit */}
          <motion.button
            type="submit"
            disabled={loading}
            whileHover={{ scale: loading ? 1 : 1.02 }}
            whileTap={{ scale: loading ? 1 : 0.98 }}
            className="btn-shimmer flex w-full items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-indigo-500 to-violet-500 py-3.5 text-sm font-semibold text-white shadow-lg shadow-indigo-500/30 transition-shadow hover:shadow-indigo-500/50 disabled:opacity-50"
          >
            {loading ? 'Signing in...' : 'Sign in'}
            {!loading && <ArrowRight className="h-4 w-4" />}
          </motion.button>
        </form>

        <p className="mt-6 text-center text-xs text-slate-600">
          Demo: Owner <span className="text-slate-400">elumalai@sapthagiri.com / owner123</span>
          <br />
          Tenant <span className="text-slate-400">rahul@tenant.com / tenant123</span>
        </p>
      </motion.div>
    </div>
  );
}
