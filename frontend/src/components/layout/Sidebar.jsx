import { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
  LayoutDashboard, Building2, Users, CreditCard,
  FileText, Bell, BarChart3, Wrench, Settings,
  ChevronLeft, ChevronRight, LogOut, IndianRupee,
} from 'lucide-react';
import { twMerge } from 'tailwind-merge';
import clsx from 'clsx';

const cn = (...args) => twMerge(clsx(...args));

const NAV_ITEMS = [
  { to: '/owner/dashboard',     label: 'Dashboard',      icon: LayoutDashboard },
  { to: '/owner/tenants',       label: 'Tenants',        icon: Users },
  { to: '/owner/rent',          label: 'Rent Tracker',   icon: CreditCard },
  { to: '/owner/payments',      label: 'Payments',       icon: IndianRupee },
  { to: '/owner/documents',     label: 'Documents',      icon: FileText },
  { to: '/owner/reminders',     label: 'Reminders',      icon: Bell },
  { to: '/owner/reports',       label: 'Reports',        icon: BarChart3 },
  { to: '/owner/maintenance',   label: 'Maintenance',    icon: Wrench },
  { to: '/owner/settings',      label: 'Settings',       icon: Settings },
];

export default function Sidebar({ collapsed, onToggle, onLogout }) {
  const [hoveredItem, setHoveredItem] = useState(null);

  return (
    <motion.aside
      animate={{ width: collapsed ? 70 : 260 }}
      transition={{ type: 'spring', stiffness: 300, damping: 30 }}
      className="fixed left-0 top-0 z-40 flex h-screen flex-col border-r border-white/[0.08] bg-[#0F172A]/80 backdrop-blur-xl"
    >
      {/* Logo */}
      <div className="flex items-center gap-3 px-5 py-6">
        <motion.div
          whileHover={{ rotate: 360, scale: 1.1 }}
          transition={{ duration: 0.6 }}
          className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br from-indigo-500 to-violet-500 shadow-lg shadow-indigo-500/40"
        >
          <Building2 className="h-5 w-5 text-white" />
        </motion.div>

        <AnimatePresence>
          {!collapsed && (
            <motion.div
              initial={{ opacity: 0, x: -10 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -10 }}
              className="overflow-hidden"
            >
              <p className="whitespace-nowrap text-sm font-bold text-white" style={{ fontFamily: 'Plus Jakarta Sans' }}>
                Sapthagiri Residency
              </p>
              <p className="whitespace-nowrap text-[10px] uppercase tracking-wider text-slate-500">
                Owner Panel
              </p>
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      {/* Nav */}
      <nav className="flex-1 space-y-1 overflow-y-auto px-3 py-2">
        {NAV_ITEMS.map((item, i) => {
          const Icon = item.icon;
          return (
            <div
              key={item.to}
              className="relative"
              onMouseEnter={() => setHoveredItem(item.to)}
              onMouseLeave={() => setHoveredItem(null)}
            >
              <NavLink to={item.to}>
                {({ isActive }) => (
                  <motion.div
                    initial={{ opacity: 0, x: -20 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ delay: i * 0.05 }}
                    whileHover={{ x: 4 }}
                    className={cn(
                      'flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-colors',
                      isActive
                        ? 'bg-gradient-to-r from-indigo-500/20 to-violet-500/10 text-white'
                        : 'text-slate-400 hover:text-white hover:bg-white/5'
                    )}
                  >
                    {isActive && (
                      <motion.div
                        layoutId="sidebar-active-glow"
                        className="absolute left-0 h-7 w-[3px] rounded-r-full bg-gradient-to-b from-indigo-400 to-violet-500 shadow-[0_0_8px_rgba(99,102,241,0.6)]"
                      />
                    )}
                    <motion.div whileHover={{ y: -2 }}>
                      <Icon className={cn('h-5 w-5 shrink-0', isActive && 'text-indigo-400')} />
                    </motion.div>
                    <AnimatePresence>
                      {!collapsed && (
                        <motion.span
                          initial={{ opacity: 0 }}
                          animate={{ opacity: 1 }}
                          exit={{ opacity: 0 }}
                          className="whitespace-nowrap"
                        >
                          {item.label}
                        </motion.span>
                      )}
                    </AnimatePresence>
                  </motion.div>
                )}
              </NavLink>

              {/* Tooltip */}
              <AnimatePresence>
                {collapsed && hoveredItem === item.to && (
                  <motion.div
                    initial={{ opacity: 0, x: -8 }}
                    animate={{ opacity: 1, x: 0 }}
                    exit={{ opacity: 0, x: -8 }}
                    className="glass absolute left-full ml-3 top-1/2 z-50 -translate-y-1/2 whitespace-nowrap rounded-lg px-3 py-1.5 text-xs font-medium text-white"
                  >
                    {item.label}
                  </motion.div>
                )}
              </AnimatePresence>
            </div>
          );
        })}
      </nav>

      {/* Bottom — owner + logout */}
      <div className="border-t border-white/[0.06] px-3 py-4">
        {!collapsed ? (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            className="mb-3 flex items-center gap-3 rounded-xl px-3 py-2"
          >
            <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-amber-400 to-orange-500 text-sm font-bold text-white">
              E
            </div>
            <div className="overflow-hidden">
              <p className="truncate text-sm font-semibold text-white">Elumalai</p>
              <p className="truncate text-[10px] text-slate-500">Owner</p>
            </div>
          </motion.div>
        ) : (
          <div className="mb-3 flex justify-center">
            <div className="flex h-9 w-9 items-center justify-center rounded-full bg-gradient-to-br from-amber-400 to-orange-500 text-sm font-bold text-white">
              E
            </div>
          </div>
        )}

        <button
          onClick={onLogout}
          className="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium text-slate-400 transition-colors hover:bg-red-500/10 hover:text-red-400"
        >
          <LogOut className="h-5 w-5 shrink-0" />
          {!collapsed && <span>Logout</span>}
        </button>

        <button
          onClick={onToggle}
          className="mt-2 flex w-full items-center justify-center rounded-xl px-3 py-2 text-slate-500 transition-colors hover:bg-white/5 hover:text-white"
        >
          {collapsed ? <ChevronRight className="h-5 w-5" /> : <ChevronLeft className="h-5 w-5" />}
        </button>
      </div>
    </motion.aside>
  );
}
