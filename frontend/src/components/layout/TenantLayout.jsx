import { Outlet, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Home, CreditCard, Wrench, FileText, LogOut, ChevronLeft, Building2, IndianRupee, User } from 'lucide-react';
import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';

const TENANT_NAV = [
  { to: '/tenant/dashboard',  label: 'Dashboard',   icon: Home },
  { to: '/tenant/unit',       label: 'My Unit',     icon: Building2 },
  { to: '/tenant/bills',      label: 'My Bills',    icon: IndianRupee },
  { to: '/tenant/payments',   label: 'Payments',    icon: CreditCard },
  { to: '/tenant/maintenance',label: 'Maintenance', icon: Wrench },
  { to: '/tenant/documents',  label: 'Documents',   icon: FileText },
  { to: '/tenant/profile',    label: 'Profile',     icon: User },
];

export default function TenantLayout() {
  const [collapsed, setCollapsed] = useState(false);
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="relative min-h-screen overflow-hidden bg-[#020617]">
      {/* Floating orbs */}
      <div className="orb orb-purple" />
      <div className="orb orb-cyan" />
      <div className="grid-overlay" />

      {/* Simple light sidebar for tenant */}
      <motion.aside
        animate={{ width: collapsed ? 70 : 240 }}
        transition={{ type: 'spring', stiffness: 300, damping: 30 }}
        className="fixed left-0 top-0 z-40 flex h-screen flex-col border-r border-white/[0.08] bg-[#0F172A]/80 backdrop-blur-xl"
      >
        {/* Logo */}
        <div className="flex items-center gap-3 px-5 py-6">
          <motion.div whileHover={{ rotate: 360 }} transition={{ duration: 0.6 }}
            className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br from-cyan-500 to-blue-500 shadow-lg shadow-cyan-500/40">
            <Home className="h-5 w-5 text-white" />
          </motion.div>
          {!collapsed && (
            <div>
              <p className="whitespace-nowrap text-sm font-bold text-white" style={{ fontFamily: 'Plus Jakarta Sans' }}>Sapthagiri</p>
              <p className="whitespace-nowrap text-[10px] uppercase tracking-wider text-slate-500">Tenant Portal</p>
            </div>
          )}
        </div>

        {/* Nav */}
        <nav className="flex-1 space-y-1 px-3 py-2">
          {TENANT_NAV.map((item, i) => {
            const Icon = item.icon;
            return (
              <motion.a
                key={item.to}
                href={item.to}
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: i * 0.05 }}
                whileHover={{ x: 4 }}
                className="flex cursor-pointer items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium text-slate-400 transition-colors hover:text-white hover:bg-white/5"
                onClick={(e) => { e.preventDefault(); navigate(item.to); }}
              >
                <Icon className="h-5 w-5 shrink-0" />
                {!collapsed && <span className="whitespace-nowrap">{item.label}</span>}
              </motion.a>
            );
          })}
        </nav>

        {/* Bottom */}
        <div className="border-t border-white/[0.06] px-3 py-4">
          <div className="mb-3 flex items-center gap-3 rounded-xl px-3 py-2">
            <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-indigo-400 to-violet-500 text-sm font-bold text-white">
              {user?.first_name?.charAt(0) || user?.firstName?.charAt(0) || 'T'}
            </div>
            {!collapsed && (
              <div className="overflow-hidden">
                <p className="truncate text-sm font-semibold text-white">{user?.first_name || user?.firstName || 'Tenant'}</p>
                <p className="truncate text-[10px] text-slate-500">Tenant</p>
              </div>
            )}
          </div>
          <button onClick={handleLogout}
            className="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium text-slate-400 transition-colors hover:bg-red-500/10 hover:text-red-400">
            <LogOut className="h-5 w-5 shrink-0" />
            {!collapsed && <span>Logout</span>}
          </button>
          <button onClick={() => setCollapsed((c) => !c)}
            className="mt-2 flex w-full items-center justify-center rounded-xl px-3 py-2 text-slate-500 hover:bg-white/5 hover:text-white">
            <ChevronLeft className="h-5 w-5" />
          </button>
        </div>
      </motion.aside>

      {/* Content */}
      <div className="relative z-10 transition-all duration-300" style={{ marginLeft: collapsed ? 70 : 240 }}>
        <motion.main initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.4 }} className="p-6">
          <Outlet />
        </motion.main>
      </div>
    </div>
  );
}
