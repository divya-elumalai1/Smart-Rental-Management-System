import { useState, useEffect, useRef } from 'react';

/**
 * Custom count-up animation hook.
 * Replaces react-countup (which has CJS/ESM interop issues with Vite).
 *
 * @param {number} end - target value
 * @param {number} duration - seconds
 * @param {boolean} active - start condition
 * @returns {{ ref: object, value: number }}
 */
export function useCountUp(end, duration = 2, active = true) {
  const ref = useRef(null);
  const [value, setValue] = useState(0);

  useEffect(() => {
    if (!active) return;

    let start = 0;
    const startTime = performance.now();
    const steps = 60;
    const increment = end / steps;
    const interval = (duration * 1000) / steps;

    const timer = setInterval(() => {
      start += increment;
      if (start >= end) {
        setValue(end);
        clearInterval(timer);
      } else {
        setValue(Math.floor(start));
      }
    }, interval);

    return () => clearInterval(timer);
  }, [end, duration, active]);

  return { ref, value };
}
