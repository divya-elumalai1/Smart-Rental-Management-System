import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Eye, EyeOff } from 'lucide-react';
import { cn } from '../../utils/cn';

/**
 * FloatingLabelInput — premium input with floating label animation.
 * Supports password toggle, error display, and validation styling.
 */
export default function FloatingLabelInput({
  label,
  name,
  type = 'text',
  register,
  error,
  icon: Icon,
  ...props
}) {
  const [showPassword, setShowPassword] = useState(false);
  const inputType = type === 'password' && showPassword ? 'text' : type;

  return (
    <div className="relative">
      <div className="relative">
        {Icon && (
          <Icon className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-slate-400" />
        )}

        <input
          id={name}
          name={name}
          type={inputType}
          placeholder=" "
          className={cn(
            'peer w-full rounded-xl border bg-white px-4 py-3.5 pt-6 text-slate-900 transition-all duration-200',
            'focus:outline-none focus:ring-2 focus:ring-indigo-500/30',
            Icon && 'pl-12',
            type === 'password' && 'pr-12',
            error
              ? 'border-red-400 focus:border-red-500'
              : 'border-slate-200 focus:border-indigo-500'
          )}
          {...(register ? register(name) : {})}
          {...props}
        />

        <label
          htmlFor={name}
          className={cn(
            'pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 transition-all duration-200',
            'peer-focus:left-4 peer-focus:top-2.5 peer-focus:text-xs peer-focus:font-medium peer-focus:text-indigo-500',
            'peer-[:not(:placeholder-shown)]:left-4 peer-[:not(:placeholder-shown)]:top-2.5 peer-[:not(:placeholder-shown)]:text-xs peer-[:not(:placeholder-shown)]:font-medium',
            Icon && 'left-12 peer-focus:left-12 peer-[:not(:placeholder-shown)]:left-12'
          )}
        >
          {label}
        </label>

        {type === 'password' && (
          <button
            type="button"
            onClick={() => setShowPassword((s) => !s)}
            className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
          >
            {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
          </button>
        )}
      </div>

      <AnimatePresence>
        {error && (
          <motion.p
            initial={{ opacity: 0, y: -4 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -4 }}
            className="mt-1.5 text-xs text-red-500"
          >
            {error.message}
          </motion.p>
        )}
      </AnimatePresence>
    </div>
  );
}
