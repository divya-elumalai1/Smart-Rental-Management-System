import { motion } from 'framer-motion';
import { cn } from '../../utils/cn';

const VARIANTS = {
  primary: 'bg-gradient-to-r from-indigo-500 to-violet-500 text-white shadow-lg shadow-indigo-500/30 hover:shadow-indigo-500/50',
  secondary: 'bg-white text-slate-900 border border-slate-200 hover:bg-slate-50',
  ghost: 'bg-transparent text-slate-700 hover:bg-slate-100',
  danger: 'bg-red-500 text-white shadow-lg shadow-red-500/30 hover:shadow-red-500/50',
};

const SIZES = {
  sm: 'px-4 py-2 text-sm',
  md: 'px-6 py-3 text-sm',
  lg: 'px-8 py-4 text-base',
};

/**
 * GradientButton — premium animated button with micro-interactions.
 */
export default function GradientButton({
  children,
  variant = 'primary',
  size = 'md',
  className,
  type = 'button',
  disabled = false,
  ...props
}) {
  return (
    <motion.button
      type={type}
      disabled={disabled}
      whileHover={{ scale: disabled ? 1 : 1.03 }}
      whileTap={{ scale: disabled ? 1 : 0.97 }}
      transition={{ type: 'spring', stiffness: 400, damping: 17 }}
      className={cn(
        'inline-flex items-center justify-center gap-2 rounded-xl font-semibold transition-all duration-200',
        'focus:outline-none focus:ring-2 focus:ring-indigo-500/40',
        disabled && 'opacity-50 cursor-not-allowed',
        VARIANTS[variant],
        SIZES[size],
        className
      )}
      {...props}
    >
      {children}
    </motion.button>
  );
}
