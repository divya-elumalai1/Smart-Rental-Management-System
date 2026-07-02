import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { motion, useInView } from 'framer-motion';
import {
  Building2, ShieldCheck, CreditCard, Wrench, MessageSquare, ArrowRight,
  Star, TrendingUp, Users, Home, Sparkles, CheckCircle2,
} from 'lucide-react';
import Navbar from '../components/layout/Navbar';
import GradientButton from '../components/ui/GradientButton';
import GlassCard from '../components/ui/GlassCard';

/* ===========================================
   Animated counter sub-component
   =========================================== */
function AnimatedCounter({ target, suffix = '', duration = 2 }) {
  const ref = useRef(null);
  const inView = useInView(ref, { once: true });
  const [count, setCount] = useState(0);

  useEffect(() => {
    if (!inView) return;
    let start = 0;
    const steps = 60;
    const increment = target / steps;
    const timer = setInterval(() => {
      start += increment;
      if (start >= target) {
        setCount(target);
        clearInterval(timer);
      } else {
        setCount(Math.floor(start));
      }
    }, duration * 1000 / steps);
    return () => clearInterval(timer);
  }, [inView, target, duration]);

  return <span ref={ref}>{count.toLocaleString()}{suffix}</span>;
}

/* ===========================================
   Feature card data
   =========================================== */
const FEATURES = [
  { icon: Home, title: 'Property Management', desc: 'Add, edit, and manage all your rental properties in one place with detailed tracking.', gradient: 'from-indigo-500 to-blue-500' },
  { icon: CreditCard, title: 'Rent Collection', desc: 'Accept rent payments via Razorpay with automatic receipts and overdue tracking.', gradient: 'from-violet-500 to-purple-500' },
  { icon: Wrench, title: 'Maintenance Requests', desc: 'Tenants raise requests with priority levels. Landlords track and resolve efficiently.', gradient: 'from-cyan-500 to-teal-500' },
  { icon: MessageSquare, title: 'AI Chatbot Assistant', desc: 'Powered by OpenAI — tenants get instant answers about rent, dues, and lease details.', gradient: 'from-amber-500 to-orange-500' },
  { icon: ShieldCheck, title: 'Secure & Verified', desc: 'JWT authentication, BCrypt encryption, and role-based access for every user.', gradient: 'from-emerald-500 to-green-500' },
  { icon: TrendingUp, title: 'Analytics Dashboard', desc: 'Real-time insights into occupancy, rent collection, and maintenance metrics.', gradient: 'from-pink-500 to-rose-500' },
];

const STATS = [
  { value: 500, suffix: '+', label: 'Properties Managed', icon: Building2 },
  { value: 2000, suffix: '+', label: 'Happy Tenants', icon: Users },
  { value: 15, suffix: 'L+', label: 'Rent Collected', icon: TrendingUp },
  { value: 99, suffix: '%', label: 'Uptime Guarantee', icon: ShieldCheck },
];

const TESTIMONIALS = [
  { name: 'Rajesh Kumar', role: 'Landlord, Mumbai', text: 'SmartRental transformed how I manage my 12 properties. Rent collection is effortless now.', avatar: 'RK', rating: 5 },
  { name: 'Priya Sharma', role: 'Tenant, Bangalore', text: 'The AI chatbot answers all my questions instantly. I never miss a payment reminder.', avatar: 'PS', rating: 5 },
  { name: 'Amit Patel', role: 'Landlord, Pune', text: 'Maintenance requests are handled so smoothly. My tenants are happier than ever.', avatar: 'AP', rating: 5 },
  { name: 'Sneha Reddy', role: 'Tenant, Hyderabad', text: 'Beautiful interface, easy to use. The document upload feature saved me so many trips.', avatar: 'SR', rating: 5 },
];

/* ===========================================
   Landing Page
   =========================================== */
export default function LandingPage() {
  const [activeTestimonial, setActiveTestimonial] = useState(0);

  useEffect(() => {
    const timer = setInterval(() => {
      setActiveTestimonial((t) => (t + 1) % TESTIMONIALS.length);
    }, 4000);
    return () => clearInterval(timer);
  }, []);

  return (
    <div className="min-h-screen bg-slate-100">
      <Navbar />

      {/* ===========================================
          Hero Section
          =========================================== */}
      <section className="relative flex min-h-screen items-center justify-center overflow-hidden pt-20">
        {/* Animated gradient background */}
        <div className="absolute inset-0 animated-gradient-bg opacity-90" />
        <div className="absolute inset-0 bg-gradient-to-b from-transparent via-transparent to-slate-100" />

        {/* Floating orbs */}
        <motion.div
          animate={{ y: [0, -30, 0], x: [0, 20, 0] }}
          transition={{ duration: 6, repeat: Infinity, ease: 'easeInOut' }}
          className="absolute left-10 top-20 h-72 w-72 rounded-full bg-indigo-400/30 blur-3xl"
        />
        <motion.div
          animate={{ y: [0, 40, 0], x: [0, -20, 0] }}
          transition={{ duration: 8, repeat: Infinity, ease: 'easeInOut' }}
          className="absolute right-10 top-40 h-96 w-96 rounded-full bg-violet-400/30 blur-3xl"
        />

        {/* Hero content */}
        <div className="relative z-10 mx-auto max-w-7xl px-6 text-center">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
            className="mb-6 inline-flex items-center gap-2 rounded-full bg-white/20 px-4 py-1.5 text-sm font-medium text-white backdrop-blur-md"
          >
            <Sparkles className="h-4 w-4" />
            India's #1 Rental Management Platform
          </motion.div>

          <motion.h1
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
            className="text-5xl font-extrabold leading-tight tracking-tight text-white md:text-7xl"
          >
            Manage Rentals
            <br />
            <span className="bg-gradient-to-r from-amber-300 to-white bg-clip-text text-transparent">
              The Smart Way
            </span>
          </motion.h1>

          <motion.p
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3 }}
            className="mx-auto mt-6 max-w-2xl text-lg text-white/80 md:text-xl"
          >
            From property listings to rent collection, maintenance requests to AI-powered support —
            everything you need to run your rental business effortlessly.
          </motion.p>

          <motion.div
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.4 }}
            className="mt-10 flex flex-col items-center justify-center gap-4 sm:flex-row"
          >
            <Link to="/register">
              <GradientButton size="lg" className="bg-white text-indigo-600 shadow-2xl shadow-indigo-900/30 hover:shadow-xl">
                Start Free Trial
                <ArrowRight className="h-5 w-5" />
              </GradientButton>
            </Link>
            <a href="#features">
              <GradientButton size="lg" variant="ghost" className="text-white border border-white/30 hover:bg-white/10">
                Explore Features
              </GradientButton>
            </a>
          </motion.div>

          {/* Floating property cards */}
          <motion.div
            initial={{ opacity: 0, y: 50 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.5 }}
            className="mt-16 grid grid-cols-1 gap-4 sm:grid-cols-3"
          >
            {[
              { title: '2BHK Apartment', price: '₹25,000/mo', city: 'Mumbai', status: 'Available' },
              { title: '3BHK Villa', price: '₹55,000/mo', city: 'Bangalore', status: 'Available' },
              { title: '1BHK Studio', price: '₹15,000/mo', city: 'Pune', status: 'Available' },
            ].map((card, i) => (
              <motion.div
                key={card.title}
                animate={{ y: [0, -10, 0] }}
                transition={{ duration: 3, repeat: Infinity, delay: i * 0.3, ease: 'easeInOut' }}
                className="rounded-2xl bg-white/15 p-5 text-left backdrop-blur-md border border-white/20"
              >
                <div className="mb-3 h-24 rounded-lg bg-gradient-to-br from-white/30 to-white/5" />
                <p className="text-sm font-semibold text-white">{card.title}</p>
                <p className="text-xs text-white/70">{card.city}</p>
                <div className="mt-2 flex items-center justify-between">
                  <span className="text-lg font-bold text-white">{card.price}</span>
                  <span className="rounded-full bg-emerald-400/30 px-2 py-0.5 text-[10px] font-semibold text-emerald-100">
                    {card.status}
                  </span>
                </div>
              </motion.div>
            ))}
          </motion.div>
        </div>
      </section>

      {/* ===========================================
          Stats Section
          =========================================== */}
      <section className="bg-white py-20">
        <div className="mx-auto max-w-7xl px-6">
          <div className="grid grid-cols-2 gap-8 md:grid-cols-4">
            {STATS.map((stat, i) => {
              const Icon = stat.icon;
              return (
                <motion.div
                  key={stat.label}
                  initial={{ opacity: 0, y: 20 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  viewport={{ once: true }}
                  transition={{ delay: i * 0.1 }}
                  className="text-center"
                >
                  <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-indigo-100 to-violet-100">
                    <Icon className="h-6 w-6 text-indigo-600" />
                  </div>
                  <p className="text-3xl font-extrabold text-slate-900 md:text-4xl">
                    <AnimatedCounter target={stat.value} suffix={stat.suffix} />
                  </p>
                  <p className="mt-1 text-sm text-slate-500">{stat.label}</p>
                </motion.div>
              );
            })}
          </div>
        </div>
      </section>

      {/* ===========================================
          Features Section
          =========================================== */}
      <section id="features" className="bg-slate-50 py-24">
        <div className="mx-auto max-w-7xl px-6">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            className="mb-16 text-center"
          >
            <h2 className="text-4xl font-extrabold text-slate-900 md:text-5xl">
              Everything you need to <span className="gradient-text">scale</span>
            </h2>
            <p className="mx-auto mt-4 max-w-2xl text-lg text-slate-500">
              A complete toolkit for landlords and tenants — powerful, intuitive, and beautiful.
            </p>
          </motion.div>

          <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
            {FEATURES.map((feature, i) => {
              const Icon = feature.icon;
              return (
                <motion.div
                  key={feature.title}
                  initial={{ opacity: 0, y: 30 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  viewport={{ once: true }}
                  transition={{ delay: i * 0.1 }}
                >
                  <GlassCard className="h-full p-7">
                    <div className={`mb-4 flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br ${feature.gradient} shadow-lg`}>
                      <Icon className="h-6 w-6 text-white" />
                    </div>
                    <h3 className="mb-2 text-lg font-bold text-slate-900">{feature.title}</h3>
                    <p className="text-sm leading-relaxed text-slate-500">{feature.desc}</p>
                  </GlassCard>
                </motion.div>
              );
            })}
          </div>
        </div>
      </section>

      {/* ===========================================
          Testimonials Section
          =========================================== */}
      <section id="testimonials" className="bg-white py-24">
        <div className="mx-auto max-w-7xl px-6">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            className="mb-12 text-center"
          >
            <h2 className="text-4xl font-extrabold text-slate-900 md:text-5xl">
              Loved by <span className="gradient-text">thousands</span>
            </h2>
            <p className="mt-4 text-lg text-slate-500">Don't just take our word for it.</p>
          </motion.div>

          <div className="relative mx-auto max-w-2xl">
            <motion.div
              key={activeTestimonial}
              initial={{ opacity: 0, x: 30 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -30 }}
              className="glass rounded-3xl p-10 text-center shadow-xl"
            >
              <div className="mb-4 flex justify-center gap-1">
                {Array.from({ length: TESTIMONIALS[activeTestimonial].rating }).map((_, i) => (
                  <Star key={i} className="h-5 w-5 fill-amber-400 text-amber-400" />
                ))}
              </div>
              <p className="mb-6 text-lg leading-relaxed text-slate-700">
                "{TESTIMONIALS[activeTestimonial].text}"
              </p>
              <div className="flex items-center justify-center gap-3">
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-gradient-to-br from-indigo-500 to-violet-500 text-sm font-bold text-white">
                  {TESTIMONIALS[activeTestimonial].avatar}
                </div>
                <div className="text-left">
                  <p className="font-semibold text-slate-900">{TESTIMONIALS[activeTestimonial].name}</p>
                  <p className="text-sm text-slate-500">{TESTIMONIALS[activeTestimonial].role}</p>
                </div>
              </div>
            </motion.div>

            {/* Dots */}
            <div className="mt-6 flex justify-center gap-2">
              {TESTIMONIALS.map((_, i) => (
                <button
                  key={i}
                  onClick={() => setActiveTestimonial(i)}
                  className={`h-2 rounded-full transition-all ${
                    i === activeTestimonial ? 'w-8 bg-indigo-500' : 'w-2 bg-slate-300'
                  }`}
                />
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* ===========================================
          CTA Section
          =========================================== */}
      <section className="relative overflow-hidden py-24">
        <div className="absolute inset-0 animated-gradient-bg" />
        <div className="relative mx-auto max-w-4xl px-6 text-center">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
          >
            <h2 className="text-4xl font-extrabold text-white md:text-5xl">
              Ready to transform your rental business?
            </h2>
            <p className="mx-auto mt-4 max-w-xl text-lg text-white/80">
              Join thousands of landlords and tenants who manage their rentals the smart way.
            </p>
            <div className="mt-8 flex flex-col items-center justify-center gap-4 sm:flex-row">
              <Link to="/register">
                <GradientButton size="lg" className="bg-white text-indigo-600 shadow-2xl">
                  Get Started Free
                  <ArrowRight className="h-5 w-5" />
                </GradientButton>
              </Link>
              <div className="flex items-center gap-2 text-white/80">
                <CheckCircle2 className="h-5 w-5" />
                <span className="text-sm">No credit card required</span>
              </div>
            </div>
          </motion.div>
        </div>
      </section>

      {/* ===========================================
          Footer
          =========================================== */}
      <footer className="bg-slate-900 py-12 text-slate-400">
        <div className="mx-auto max-w-7xl px-6">
          <div className="grid grid-cols-2 gap-8 md:grid-cols-4">
            <div className="col-span-2 md:col-span-1">
              <div className="mb-4 flex items-center gap-2.5">
                <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-gradient-to-br from-indigo-500 to-violet-500">
                  <Building2 className="h-5 w-5 text-white" />
                </div>
                <span className="text-lg font-bold text-white">SmartRental</span>
              </div>
              <p className="text-sm">India's smartest rental management platform.</p>
            </div>
            <div>
              <h4 className="mb-3 text-sm font-semibold uppercase tracking-wider text-white">Product</h4>
              <ul className="space-y-2 text-sm">
                <li><a href="#features" className="hover:text-white transition-colors">Features</a></li>
                <li><a href="#pricing" className="hover:text-white transition-colors">Pricing</a></li>
                <li><a href="#testimonials" className="hover:text-white transition-colors">Reviews</a></li>
              </ul>
            </div>
            <div>
              <h4 className="mb-3 text-sm font-semibold uppercase tracking-wider text-white">Company</h4>
              <ul className="space-y-2 text-sm">
                <li><a href="#" className="hover:text-white transition-colors">About</a></li>
                <li><a href="#" className="hover:text-white transition-colors">Contact</a></li>
                <li><a href="#" className="hover:text-white transition-colors">Careers</a></li>
              </ul>
            </div>
            <div>
              <h4 className="mb-3 text-sm font-semibold uppercase tracking-wider text-white">Legal</h4>
              <ul className="space-y-2 text-sm">
                <li><a href="#" className="hover:text-white transition-colors">Privacy Policy</a></li>
                <li><a href="#" className="hover:text-white transition-colors">Terms of Service</a></li>
                <li><a href="#" className="hover:text-white transition-colors">Cookie Policy</a></li>
              </ul>
            </div>
          </div>
          <div className="mt-10 border-t border-slate-800 pt-6 text-center text-sm">
            <p>© 2026 SmartRental. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  );
}
