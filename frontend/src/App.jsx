import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider, useAuth } from './context/AuthContext';
import ErrorBoundary from './components/ErrorBoundary';
import ProtectedRoute from './components/ProtectedRoute';

// Owner layout + pages
import DashboardLayout from './components/layout/DashboardLayout';
import OwnerDashboard from './pages/OwnerDashboard';
import TenantsPage from './pages/TenantsPage';
import RentTrackerPage from './pages/RentTrackerPage';
import PaymentsPage from './pages/PaymentsPage';
import MaintenancePage from './pages/MaintenancePage';
import DocumentsPage from './pages/DocumentsPage';
import RemindersPage from './pages/RemindersPage';
import ReportsPage from './pages/ReportsPage';
import SettingsPage from './pages/SettingsPage';
import WaterMeterPage from './pages/WaterMeterPage';

// Tenant layout + pages
import TenantLayout from './components/layout/TenantLayout';
import TenantDashboard from './pages/TenantDashboard';
import TenantMyUnit from './pages/TenantMyUnit';
import TenantMyBills from './pages/TenantMyBills';
import TenantPayments from './pages/TenantPayments';
import TenantMaintenance from './pages/TenantMaintenance';
import TenantDocuments from './pages/TenantDocuments';
import TenantProfile from './pages/TenantProfile';
import LoginPage from './pages/LoginPage';

function AppRoutes() {
  const { isAuthenticated, isOwner, isTenant, loading } = useAuth();

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-[#020617]">
        <div className="h-8 w-8 animate-spin rounded-full border-2 border-indigo-500 border-t-transparent" />
      </div>
    );
  }

  const getDashboard = () => {
    if (isOwner) return '/owner/dashboard';
    if (isTenant) return '/tenant/dashboard';
    return '/login';
  };

  return (
    <Routes>
      {/* Login */}
      <Route path="/login" element={
        isAuthenticated && (isOwner || isTenant)
          ? <Navigate to={getDashboard()} replace />
          : <LoginPage />
      } />

      {/* Owner routes (role: owner) */}
      <Route path="/owner" element={
        <ProtectedRoute role="owner"><DashboardLayout /></ProtectedRoute>
      }>
        <Route index element={<Navigate to="dashboard" replace />} />
        <Route path="dashboard" element={<OwnerDashboard />} />
        <Route path="tenants" element={<TenantsPage />} />
        <Route path="rent" element={<RentTrackerPage />} />
        <Route path="payments" element={<PaymentsPage />} />
        <Route path="maintenance" element={<MaintenancePage />} />
        <Route path="documents" element={<DocumentsPage />} />
        <Route path="reminders" element={<RemindersPage />} />
        <Route path="reports" element={<ReportsPage />} />
        <Route path="settings" element={<SettingsPage />} />
        <Route path="water-meter" element={<WaterMeterPage />} />
      </Route>

      {/* Tenant routes (role: tenant) */}
      <Route path="/tenant" element={
        <ProtectedRoute role="tenant"><TenantLayout /></ProtectedRoute>
      }>
        <Route index element={<Navigate to="dashboard" replace />} />
        <Route path="dashboard" element={<TenantDashboard />} />
        <Route path="unit" element={<TenantMyUnit />} />
        <Route path="bills" element={<TenantMyBills />} />
        <Route path="payments" element={<TenantPayments />} />
        <Route path="maintenance" element={<TenantMaintenance />} />
        <Route path="documents" element={<TenantDocuments />} />
        <Route path="profile" element={<TenantProfile />} />
      </Route>

      {/* Default redirect */}
      <Route path="/" element={
        isAuthenticated
          ? <Navigate to={getDashboard()} replace />
          : <Navigate to="/login" replace />
      } />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <ErrorBoundary>
      <BrowserRouter>
        <AuthProvider>
          <Toaster position="top-right" toastOptions={{
            duration: 4000,
            style: { borderRadius: '12px', background: 'rgba(15,23,42,0.9)', backdropFilter: 'blur(20px)', border: '1px solid rgba(255,255,255,0.08)', color: '#F8FAFC', fontSize: '14px', fontWeight: 500 },
            success: { iconTheme: { primary: '#10B981', secondary: '#fff' } },
            error: { iconTheme: { primary: '#EF4444', secondary: '#fff' } },
          }} />
          <AppRoutes />
        </AuthProvider>
      </BrowserRouter>
    </ErrorBoundary>
  );
}
