import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * ProtectedRoute — checks authentication + optional role requirement.
 * @param {ReactNode} children — page to render
 * @param {string} role — 'owner' or 'tenant' (optional)
 */
export default function ProtectedRoute({ children, role }) {
  const { isAuthenticated, isOwner, isTenant, loading } = useAuth();

  if (loading) return null;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if ((role === 'owner' && !isOwner) || (role === 'tenant' && !isTenant))
    return <Navigate to="/login" replace />;

  return children;
}
