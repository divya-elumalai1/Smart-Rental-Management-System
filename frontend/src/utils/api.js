import api from './axios';

/**
 * API service layer — all backend API calls.
 * Base paths use /v1/ prefix (auth uses /auth/).
 */

// ===========================================
// AUTH
// ===========================================
export const authAPI = {
  login: (email, password) => api.post('/auth/login', { email, password }),
  register: (data) => api.post('/auth/register', data),
  me: () => api.get('/auth/me'),
  logout: () => api.post('/auth/logout'),
};

// ===========================================
// DASHBOARD
// ===========================================
export const dashboardAPI = {
  getSummary: () => api.get('/v1/dashboard/summary'),
  getOwnerUnits: () => api.get('/v1/dashboard/units'),
  getTenantDashboard: () => api.get('/v1/dashboard/tenant'),
};

// ===========================================
// PROPERTIES / UNITS
// ===========================================
export const propertyAPI = {
  getAll: () => api.get('/v1/properties'),
  getById: (id) => api.get(`/v1/properties/${id}`),
  getMine: () => api.get('/v1/properties/landlord/me'),
  create: (data) => api.post('/v1/properties', data),
  update: (id, data) => api.put(`/v1/properties/${id}`, data),
  delete: (id) => api.delete(`/v1/properties/${id}`),
};

// ===========================================
// LEASES / TENANTS
// ===========================================
export const leaseAPI = {
  getAll: () => api.get('/v1/leases'),
  getById: (id) => api.get(`/v1/leases/${id}`),
  getActive: () => api.get('/v1/leases/active'),
  getMine: () => api.get('/v1/leases/tenant/me'),
  create: (data) => api.post('/v1/leases', data),
  update: (id, data) => api.put(`/v1/leases/${id}`, data),
  delete: (id) => api.delete(`/v1/leases/${id}`),
};

// ===========================================
// TENANTS (owner CRUD)
// ===========================================
export const tenantAPI = {
  getAll: () => api.get('/v1/tenants'),
  assign: (data) => api.post('/v1/tenants', data),
  update: (leaseId, data) => api.put(`/v1/tenants/${leaseId}`, data),
  remove: (leaseId) => api.delete(`/v1/tenants/${leaseId}`),
};

// ===========================================
// PAYMENTS
// ===========================================
export const paymentAPI = {
  getAll: () => api.get('/v1/payments'),
  getMine: () => api.get('/v1/payments/tenant/me'),
  getByProperty: (propertyId) => api.get(`/v1/payments/property/${propertyId}`),
  create: (data) => api.post('/v1/payments', data),
  markPaid: (id, data) => api.put(`/v1/payments/${id}/mark-paid`, data),
  getReceipt: (id) => api.get(`/v1/payments/${id}/receipt`),
  deletePayment: (id) => api.delete(`/v1/payments/${id}`),
};

// ===========================================
// MAINTENANCE
// ===========================================
export const maintenanceAPI = {
  getAll: () => api.get('/v1/maintenance'),
  getById: (id) => api.get(`/v1/maintenance/${id}`),
  create: (data) => api.post('/v1/maintenance', data),
  updateStatus: (id, status) => api.put(`/v1/maintenance/${id}/status`, { status }),
  addComment: (id, comment) => api.post(`/v1/maintenance/${id}/comments`, { comment }),
};

// ===========================================
// DOCUMENTS
// ===========================================
export const documentAPI = {
  getAll: () => api.get('/v1/documents'),
  getByProperty: (propertyId) => api.get(`/v1/documents/property/${propertyId}`),
  upload: (formData) => api.post('/v1/documents', formData),
  delete: (id) => api.delete(`/v1/documents/${id}`),
};

// ===========================================
// REMINDERS
// ===========================================
export const reminderAPI = {
  getAll: () => api.get('/v1/reminders'),
  send: (id) => api.post(`/v1/reminders/${id}/send`),
  getLogs: () => api.get('/v1/reminders/logs'),
};

// ===========================================
// WATER METER
// ===========================================
export const waterMeterAPI = {
  readPhoto: (formData) => api.post('/v1/water-meter/read-photo', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }),
  calculate: (unitNumber, currentReading) => api.post('/v1/water-meter/calculate', null, {
    params: { unitNumber, currentReading },
  }),
  save: (data) => api.post('/v1/water-meter/save', null, {
    params: data,
  }),
  getByUnit: (unitNumber) => api.get(`/v1/water-meter/unit/${unitNumber}`),
  getOccupied: () => api.get('/v1/water-meter/occupied'),
};

// ===========================================
// OWNER ENDPOINTS
// ===========================================
export const ownerAPI = {
  getUnits: () => api.get('/v1/owner/units'),
  addTenant: (data) => api.post('/v1/owner/tenants', data),
  editTenant: (unitNumber, data) => api.put(`/v1/owner/tenants/${unitNumber}`, data),
  removeTenant: (unitNumber) => api.delete(`/v1/owner/tenants/${unitNumber}`),
  markPaid: (data) => api.post('/v1/owner/payments/mark-paid', data),
  getAllPayments: (params) => api.get('/v1/owner/payments/all', { params }),
  getDashboardStats: () => api.get('/v1/owner/dashboard/stats'),
};

// ===========================================
// TENANT PORTAL ENDPOINTS
// ===========================================
export const tenantPortalAPI = {
  getMyUnit: () => api.get('/v1/tenant/my-unit'),
  getMyPayments: () => api.get('/v1/tenant/my-payments'),
  getMyBill: (month) => api.get(`/v1/tenant/my-bill/${month}`),
  raiseMaintenance: (data) => api.post('/v1/tenant/maintenance', data),
};
