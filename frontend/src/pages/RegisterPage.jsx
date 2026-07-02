import { useState, useMemo } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Building2, Mail, Lock, User, Phone, ArrowRight, Home, KeyRound, Check } from 'lucide-react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import GradientButton from '../components/ui/GradientButton';
import FloatingLabelInput from '../components/ui/FloatingLabelInput';
import { cn } from '../utils/cn';
import { authAPI } from '../utils/api';

/* Password strength calculator */
function getStrength(pw) {
  let score = 0;
  if (pw.length >= 8) score++;
  if (/[A-Z]/.test(pw)) score++;
  if (/[a-z]/.test(pw)) score++;
  if (/[0-9]/.test(pw)) score++;
  if (/[^A-Za-z0-9]/.test(pw)) score++;
  return score;
}

const STRENGTH_LABELS = ['Very weak', 'Weak', 'Fair', 'Good', 'Strong', 'Excellent'];
const STRENGTH_COLORS = ['#EF4444', '#F59E0B', '#F59E0B', '#06B6D4', '#10B981', '#10B981'];

export default function RegisterPage() {
  const navigate = useNavigate();
  const [role, setRole] = useState('TENANT');
  const [password, setPassword] = useState('');
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm();

  const strength = useMemo(() => getStrength(password), [password]);

  const onSubmit = async (data) => {
    try {
      const payload = { ...data, role };
      const res = await authAPI.register(payload);
      const accessToken = res.data.access_token || res.data.accessToken;
      const refreshToken = res.data.refresh_token || res.data.refreshToken;
      const userData = res.data.user;
      if (accessToken) {
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken || '');
        localStorage.setItem('user', JSON.stringify(userData));
      }
      toast.success('Account created! Welcome to SmartRental.');
      const userRole = (userData?.role || role || '').toUpperCase();
      if (userRole === 'OWNER' || userRole === 'ADMIN') {
        navigate('/owner/dashboard');
      } else {
        navigate('/tenant/dashboard');
      }
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || 'Registration failed. Please try again.';
      toast.error(msg);
    }
  };

  return (
    <div className="flex min-h-screen">
      {/* Left — animated gradient panel */}
      <div className="relative hidden w-1/2 overflow-hidden lg:block">
        <div className="absolute inset-0 animated-gradient-bg" />
        <motion.div
          animate={{ y: [0, -30, 0] }}
          transition={{ duration: 6, repeat: Infinity }}
          className="absolute left-20 top-20 h-72 w-72 rounded-full bg-white/20 blur-3xl"
        />
        <motion.div
          animate={{ y: [0, 40, 0] }}
          transition={{ duration: 8, repeat: Infinity }}
          className="absolute bottom-20 right-10 h-96 w-96 rounded-full bg-cyan-300/20 blur-3xl"
        />

        <div className="relative z-10 flex h-full flex-col justify-between p-12">
          <Link to="/" className="flex items-center gap-2.5">
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-white/20 backdrop-blur-md">
              <Building2 className="h-5 w-5 text-white" />
            </div>
            <span className="text-lg font-bold text-white">SmartRental</span>
          </Link>

          <div>
            <motion.h1
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              className="text-4xl font-extrabold leading-tight text-white"
            >
              Start your rental
              <br />journey today
            </motion.h1>
            <p className="mt-4 max-w-md text-white/80">
              Whether you're a landlord with 50 properties or a tenant looking for a home —
              we've got you covered.
            </p>

            <div className="mt-12 space-y-3">
              {['Free 30-day trial', 'No credit card required', 'Cancel anytime'].map((item, i) => (
                <motion.div
                  key={item}
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: 0.3 + i * 0.15 }}
                  className="flex items-center gap-3 rounded-xl bg-white/10 px-5 py-3 backdrop-blur-md"
                >
                  <div className="flex h-6 w-6 items-center justify-center rounded-full bg-emerald-400">
                    <Check className="h-4 w-4 text-white" />
                  </div>
                  <span className="text-sm font-medium text-white">{item}</span>
                </motion.div>
              ))}
            </div>
          </div>

          <p className="text-sm text-white/60">© 2026 SmartRental</p>
        </div>
      </div>

      {/* Right — form */}
      <div className="flex w-full items-center justify-center px-6 py-12 lg:w-1/2">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="w-full max-w-md"
        >
          {/* Mobile logo */}
          <Link to="/" className="mb-8 flex items-center gap-2.5 lg:hidden">
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-indigo-500 to-violet-500">
              <Building2 className="h-5 w-5 text-white" />
            </div>
            <span className="text-lg font-bold text-slate-900">SmartRental</span>
          </Link>

          <h2 className="text-3xl font-extrabold text-slate-900">Create account</h2>
          <p className="mt-2 text-sm text-slate-500">
            Already have one?{' '}
            <Link to="/login" className="font-semibold text-indigo-600 hover:underline">
              Sign in
            </Link>
          </p>

          {/* Role selector */}
          <div className="mt-6 grid grid-cols-2 gap-4">
            {[
              { id: 'TENANT', label: 'Tenant', desc: 'Rent properties', icon: Home },
              { id: 'OWNER', label: 'Owner', desc: 'Manage properties', icon: KeyRound },
            ].map((opt) => {
              const Icon = opt.icon;
              const isActive = role === opt.id;
              return (
                <motion.button
                  key={opt.id}
                  type="button"
                  whileHover={{ y: -2 }}
                  whileTap={{ scale: 0.97 }}
                  onClick={() => setRole(opt.id)}
                  className={cn(
                    'relative flex flex-col items-center gap-2 rounded-2xl border-2 p-5 text-center transition-all',
                    isActive
                      ? 'border-indigo-500 bg-indigo-50 shadow-lg shadow-indigo-500/10'
                      : 'border-slate-200 bg-white hover:border-slate-300'
                  )}
                >
                  {isActive && (
                    <motion.div
                      layoutId="role-active"
                      className="absolute right-3 top-3 flex h-5 w-5 items-center justify-center rounded-full bg-indigo-500"
                    >
                      <Check className="h-3 w-3 text-white" />
                    </motion.div>
                  )}
                  <div className={cn(
                    'flex h-12 w-12 items-center justify-center rounded-xl transition-colors',
                    isActive ? 'bg-gradient-to-br from-indigo-500 to-violet-500' : 'bg-slate-100'
                  )}>
                    <Icon className={cn('h-6 w-6', isActive ? 'text-white' : 'text-slate-400')} />
                  </div>
                  <p className={cn('font-semibold', isActive ? 'text-indigo-600' : 'text-slate-700')}>{opt.label}</p>
                  <p className="text-xs text-slate-400">{opt.desc}</p>
                </motion.button>
              );
            })}
          </div>

          <form onSubmit={handleSubmit(onSubmit)} className="mt-6 space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <FloatingLabelInput
                label="First name"
                name="firstName"
                icon={User}
                error={errors.firstName}
                {...register('firstName', { required: 'First name is required' })}
              />
              <FloatingLabelInput
                label="Last name"
                name="lastName"
                icon={User}
                error={errors.lastName}
                {...register('lastName', { required: 'Last name is required' })}
              />
            </div>

            <FloatingLabelInput
              label="Email address"
              name="email"
              type="email"
              icon={Mail}
              error={errors.email}
              {...register('email', { required: 'Email is required' })}
            />

            <FloatingLabelInput
              label="Phone number"
              name="phone"
              type="tel"
              icon={Phone}
              error={errors.phone}
              {...register('phone', { required: 'Phone is required' })}
            />

            <div>
              <FloatingLabelInput
                label="Password"
                name="password"
                type="password"
                icon={Lock}
                error={errors.password}
                {...register('password', {
                  required: 'Password is required',
                  minLength: { value: 8, message: 'Min 8 characters' },
                })}
              />
              {/* Password strength bar */}
              <AnimatePresence>
                {password.length > 0 && (
                  <motion.div
                    initial={{ opacity: 0, height: 0 }}
                    animate={{ opacity: 1, height: 'auto' }}
                    exit={{ opacity: 0, height: 0 }}
                    className="mt-2"
                  >
                    <div className="flex gap-1">
                      {[0, 1, 2, 3, 4].map((i) => (
                        <div
                          key={i}
                          className="h-1.5 flex-1 rounded-full transition-colors"
                          style={{
                            backgroundColor: i < strength ? STRENGTH_COLORS[strength] : '#E2E8F0',
                          }}
                        />
                      ))}
                    </div>
                    <p className="mt-1 text-xs" style={{ color: STRENGTH_COLORS[strength] }}>
                      {STRENGTH_LABELS[strength]}
                    </p>
                  </motion.div>
                )}
              </AnimatePresence>
            </div>

            {/* Hidden role field for form submission */}
            <input type="hidden" value={role} {...register('role')} />

            <p className="text-xs text-slate-400">
              By signing up, you agree to our{' '}
              <a href="#" className="font-medium text-indigo-600 hover:underline">Terms</a> and{' '}
              <a href="#" className="font-medium text-indigo-600 hover:underline">Privacy Policy</a>.
            </p>

            <GradientButton type="submit" size="lg" className="w-full" disabled={isSubmitting}>
              {isSubmitting ? 'Creating account...' : 'Create account'}
              {!isSubmitting && <ArrowRight className="h-5 w-5" />}
            </GradientButton>
          </form>
        </motion.div>
      </div>
    </div>
  );
}
