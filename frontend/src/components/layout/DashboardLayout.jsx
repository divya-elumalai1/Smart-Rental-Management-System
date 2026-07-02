import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Search, Bell, ChevronDown } from 'lucide-react';
import Sidebar from './Sidebar';
import { useAuth } from '../../context/AuthContext';

export default function DashboardLayout() {
  const { logout } = useAuth();
  const [collapsed, setCollapsed] = useState(false);

  const handleLogout = () => {
    logout();
  };

  return (
    <div className="relative min-h-screen overflow-hidden bg-[#020617]">
      {/* Floating orbs background */}
      <div className="orb orb-purple" />
      <div className="orb orb-cyan" />
      <div className="orb orb-indigo" />
      <div className="grid-overlay" />

      {/* Sidebar */}
      <Sidebar
        collapsed={collapsed}
        onToggle={() => setCollapsed((c) => !c)}
        onLogout={handleLogout}
      />

      {/* Main content */}
      <div
        className="relative z-10 transition-all duration-300"
        style={{ marginLeft: collapsed ? 70 : 260 }}
      >
        {/* Topbar */}
        <header className="sticky top-0 z-30 flex items-center justify-between border-b border-white/[0.06] bg-[#0F172A]/60 px-6 py-4 backdrop-blur-xl">
          {/* Search */}
          <div className="relative w-full max-w-md">
            <Search className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" />
            <input
              type="text"
              placeholder="Search units, tenants, payments..."
              className="input-glow w-full rounded-xl border border-white/[0.08] bg-white/[0.03] py-2.5 pl-11 pr-4 text-sm text-slate-200 placeholder:text-slate-500"
            />
          </div>

          {/* Right */}
          <div className="flex items-center gap-4">
            <motion.button
              whileHover={{ scale: 1.1 }}
              whileTap={{ scale: 0.9, rotate: -10 }}
              className="relative rounded-xl p-2.5 text-slate-400 transition-colors hover:bg-white/5 hover:text-white"
            >
              <Bell className="h-5 w-5" />
              <span className="absolute right-2 top-2 h-2 w-2 rounded-full bg-red-500 ring-2 ring-[#0F172A]" />
            </motion.button>

            <button className="flex items-center gap-3 rounded-xl p-1.5 transition-colors hover:bg-white/5">
              <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-gradient-to-br from-amber-400 to-orange-500 text-sm font-bold text-white">
                E
              </div>
              <div className="hidden text-left md:block">
                <p className="text-sm font-semibold text-white">Elumalai</p>
                <p className="text-xs text-slate-500">Owner</p>
              </div>
              <ChevronDown className="h-4 w-4 text-slate-500" />
            </button>
          </div>
        </header>

        {/* Page content */}
        <motion.main
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4 }}
          className="relative z-10 p-6"
        >
          <Outlet />
        </motion.main>
      </div>
    </div>
  );
}
