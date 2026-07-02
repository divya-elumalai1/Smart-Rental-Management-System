/**
 * Merge class names conditionally.
 * Filters out falsy values and joins the rest with spaces.
 * @param  {...any} classes
 * @returns {string}
 */
export function cn(...classes) {
  return classes.filter(Boolean).join(' ');
}
