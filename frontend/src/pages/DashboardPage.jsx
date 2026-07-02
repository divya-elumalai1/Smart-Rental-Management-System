import { motion } from 'framer-motion';
import {
  AreaChart, Area, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
} from 'recharts';
import {
  Building2, TrendingUp, CreditCard, Wrench,
  ArrowUpRight, ArrowDownRight,
} from 'lucide-react';

const rentData = [
  { month: 'Jan', amount: 180000 },
  { month: 'Feb', amount: 195000 },
  { month: 'Mar', amount: 210000 },
  { month: 'Apr', amount: 205000 },
  { month: 'May', amount: 230000 },
  { month: 'Jun', amount: 240000 },
];

const occupancyData = [
  { name: 'Occupied', value: 8, color: '#6366F1' },
  { name: 'Vacant', value: 3, color: '#E2E8F0' },
  { name: 'Maintenance', value: 1, color: '#F59E0B' },
];

const maintenanceData = [
  { day: 'Mon', count: 4 },
  { day: 'Tue', count: 7 },
  { day: 'Wed', count: 3 },
  { day: 'Thu', count: 8 },
  { day: 'Fri', count: 5 },
  { day: 'Sat', count: 2 },
  { day: 'Sun', count: 1 },
];

const STATS = [
  { label: 'Total Properties', value: '12', change: '+2', positive: true, icon: Building2, color: 'from-indigo-500 to-blue-500' },
  { label: 'Rent Collected (This Month)', value: '₹2.4L', change: '+12%', positive: true, icon: TrendingUp, color: 'from-emerald-500 to-green-500' },
  { label: 'Pending Payments', value: '3', change: '-1', positive: true, icon: CreditCard, color: 'from-amber-500 to-orange-500' },
  { label: 'Open Maintenance', value: '5', change: '+2', positive: false, icon: Wrench, color: 'from-pink-500 to-rose-500' },
];

export default function DashboardPage() {
  return (
    <div className="space-y-6">
      {/* Page header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-extrabold text-slate-900">Dashboard</h1>
          <p className="text-sm text-slate-500">Welcome back, John! Here's what's happening.</p>
        </div>
      </div>

      {/* Stat cards */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {STATS.map((stat, i) => {
          const Icon = stat.icon;
          return (
            <motion.div
              key={stat.label}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: i * 0.1 }}
              whileHover={{ y: -4 }}
              className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition-shadow hover:shadow-lg"
            >
              <div className="flex items-start justify-between">
                <div className={`flex h-11 w-11 items-center justify-center rounded-xl bg-gradient-to-br ${stat.color} shadow-lg`}>
                  <Icon className="h-5 w-5 text-white" />
                </div>
                <div className={`flex items-center gap-1 text-xs font-semibold ${stat.positive ? 'text-emerald-600' : 'text-red-500'}`}>
                  {stat.positive ? <ArrowUpRight className="h-3.5 w-3.5" /> : <ArrowDownRight className="h-3.5 w-3.5" />}
                  {stat.change}
                </div>
              </div>
              <p className="mt-4 text-2xl font-extrabold text-slate-900">{stat.value}</p>
              <p className="mt-1 text-sm text-slate-500">{stat.label}</p>
            </motion.div>
          );
        })}
      </div>

      {/* Charts row */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        {/* Rent collection chart */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
          className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm lg:col-span-2"
        >
          <div className="mb-4 flex items-center justify-between">
            <div>
              <h3 className="font-bold text-slate-900">Rent Collection</h3>
              <p className="text-sm text-slate-500">Monthly collection trend</p>
            </div>
            <div className="flex items-center gap-2 rounded-lg bg-indigo-50 px-3 py-1.5 text-xs font-semibold text-indigo-600">
              <TrendingUp className="h-3.5 w-3.5" />
              +18% vs last period
            </div>
          </div>
          <ResponsiveContainer width="100%" height={260}>
            <AreaChart data={rentData}>
              <defs>
                <linearGradient id="rentGradient" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stopColor="#6366F1" stopOpacity={0.3} />
                  <stop offset="100%" stopColor="#6366F1" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#E2E8F0" vertical={false} />
              <XAxis dataKey="month" tick={{ fontSize: 12, fill: '#64748B' }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fontSize: 12, fill: '#64748B' }} axisLine={false} tickLine={false}
                tickFormatter={(v) => `₹${(v / 1000).toFixed(0)}K`} />
              <Tooltip
                contentStyle={{ borderRadius: '12px', border: '1px solid #E2E8F0', fontSize: '13px' }}
                formatter={(v) => [`₹${v.toLocaleString()}`, 'Collected']}
              />
              <Area type="monotone" dataKey="amount" stroke="#6366F1" strokeWidth={2} fill="url(#rentGradient)" />
            </AreaChart>
          </ResponsiveContainer>
        </motion.div>

        {/* Occupancy pie */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.4 }}
          className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm"
        >
          <h3 className="mb-4 font-bold text-slate-900">Property Occupancy</h3>
          <ResponsiveContainer width="100%" height={200}>
            <PieChart>
              <Pie data={occupancyData} dataKey="value" nameKey="name" cx="50%" cy="50%"
                innerRadius={50} outerRadius={80} paddingAngle={4}>
                {occupancyData.map((entry, index) => (
                  <Cell key={index} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip contentStyle={{ borderRadius: '12px', border: '1px solid #E2E8F0', fontSize: '13px' }} />
            </PieChart>
          </ResponsiveContainer>
          <div className="mt-2 space-y-2">
            {occupancyData.map((item) => (
              <div key={item.name} className="flex items-center justify-between text-sm">
                <div className="flex items-center gap-2">
                  <span className="h-3 w-3 rounded-full" style={{ backgroundColor: item.color }} />
                  <span className="text-slate-600">{item.name}</span>
                </div>
                <span className="font-semibold text-slate-900">{item.value}</span>
              </div>
            ))}
          </div>
        </motion.div>
      </div>

      {/* Bottom row */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Maintenance chart */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.5 }}
          className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm"
        >
          <h3 className="mb-4 font-bold text-slate-900">Maintenance Requests (Weekly)</h3>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={maintenanceData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#E2E8F0" vertical={false} />
              <XAxis dataKey="day" tick={{ fontSize: 12, fill: '#64748B' }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fontSize: 12, fill: '#64748B' }} axisLine={false} tickLine={false} />
              <Tooltip contentStyle={{ borderRadius: '12px', border: '1px solid #E2E8F0', fontSize: '13px' }} />
              <Bar dataKey="count" radius={[8, 8, 0, 0]} fill="#8B5CF6" />
            </BarChart>
          </ResponsiveContainer>
        </motion.div>

        {/* Recent activity */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.6 }}
          className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm"
        >
          <h3 className="mb-4 font-bold text-slate-900">Recent Activity</h3>
          <div className="space-y-4">
            {[
              { title: 'Rent received from Priya Sharma', time: '2 hours ago', amount: '₹25,000', color: 'bg-emerald-500' },
              { title: 'New maintenance request — AC repair', time: '5 hours ago', amount: 'High priority', color: 'bg-amber-500' },
              { title: 'New tenant registered — Amit Patel', time: '1 day ago', amount: 'Tenant', color: 'bg-indigo-500' },
              { title: 'Lease expired — Flat 302', time: '2 days ago', amount: 'Action needed', color: 'bg-red-500' },
            ].map((item, i) => (
              <div key={i} className="flex items-center gap-3">
                <div className={`h-2 w-2 shrink-0 rounded-full ${item.color}`} />
                <div className="flex-1">
                  <p className="text-sm font-medium text-slate-700">{item.title}</p>
                  <p className="text-xs text-slate-400">{item.time}</p>
                </div>
                <span className="text-sm font-semibold text-slate-900">{item.amount}</span>
              </div>
            ))}
          </div>
        </motion.div>
      </div>
    </div>
  );
}
