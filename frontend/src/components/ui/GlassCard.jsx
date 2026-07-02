import { motion } from 'framer-motion';
import { cn } from '../../utils/cn';

/**
 * GlassCard — premium glassmorphism card with hover lift.
 * Used across landing, auth, and dashboard pages.
 */
export default function GlassCard({ children, className, hover = true, ...props }) {
  return (
    <motion.div
      whileHover={hover ? { y: -4, scale: 1.01 } : undefined}
      transition={{ type: 'spring', stiffness: 300, damping: 20 }}
      className={cn(
        'glass rounded-2xl shadow-[0_8px_32px_rgba(99,102,241,0.08)]',
        className
      )}
      {...props}
    >
      {children}
    </motion.div>
  );
}
