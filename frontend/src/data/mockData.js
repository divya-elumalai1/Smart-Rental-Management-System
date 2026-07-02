/**
 * Real data for Sapthagiri Residency — 10 rental units in Bengaluru.
 * Owner: Elumalai
 * In production, this will come from the Spring Boot API.
 */

export const BUILDING = {
  name: 'Sapthagiri Residency',
  address: 'Bengaluru, Karnataka',
  ownerName: 'Elumalai',
};

/**
 * Units grouped by floor.
 * OCCUPIED: G1, 101, 102, 301
 * UNDER_CONSTRUCTION: 103, 104, 202, 203, 302, 303
 */
export const FLOORS = [
  {
    floor: 'Ground Floor',
    units: [
      { id: 1, name: 'G1', type: 'House', area: 'Ground Floor',
        tenant: { id: 1, name: 'Isthan', phone: '', since: '' },
        rentAmount: 9000, deposit: 0, status: 'PAID' },
    ],
  },
  {
    floor: '1st Floor',
    units: [
      { id: 2, name: '101', type: 'House', area: '1st Floor',
        tenant: { id: 2, name: 'Nagaraj', phone: '', since: '' },
        rentAmount: 9500, deposit: 0, status: 'PAID' },
      { id: 3, name: '102', type: 'House', area: '1st Floor',
        tenant: { id: 3, name: 'Rahul', phone: '', since: '' },
        rentAmount: 10500, deposit: 0, status: 'PAID' },
      { id: 4, name: '103', type: 'House', area: '1st Floor',
        tenant: null, rentAmount: 0, deposit: 0, status: 'UNDER_CONSTRUCTION' },
      { id: 5, name: '104', type: 'House', area: '1st Floor',
        tenant: null, rentAmount: 0, deposit: 0, status: 'UNDER_CONSTRUCTION' },
    ],
  },
  {
    floor: '2nd Floor',
    units: [
      { id: 6, name: '202', type: 'House', area: '2nd Floor',
        tenant: null, rentAmount: 0, deposit: 0, status: 'UNDER_CONSTRUCTION' },
      { id: 7, name: '203', type: 'House', area: '2nd Floor',
        tenant: null, rentAmount: 0, deposit: 0, status: 'UNDER_CONSTRUCTION' },
    ],
  },
  {
    floor: '3rd Floor',
    units: [
      { id: 8, name: '301', type: 'House', area: '3rd Floor',
        tenant: { id: 4, name: 'Harish', phone: '', since: '' },
        rentAmount: 11500, deposit: 0, status: 'PAID' },
      { id: 9, name: '302', type: 'House', area: '3rd Floor',
        tenant: null, rentAmount: 0, deposit: 0, status: 'UNDER_CONSTRUCTION' },
      { id: 10, name: '303', type: 'House', area: '3rd Floor',
        tenant: null, rentAmount: 0, deposit: 0, status: 'UNDER_CONSTRUCTION' },
    ],
  },
];

/** Flat list of all units (derived from floors). */
export const UNITS = FLOORS.flatMap((f) => f.units);

export const STATUS_CONFIG = {
  PAID:               { label: 'Paid',              dot: 'bg-emerald-500', text: 'text-emerald-300',     bg: 'bg-emerald-500/10',     ring: 'ring-emerald-500/30',     pulse: false },
  PENDING:            { label: 'Pending',            dot: 'bg-amber-500',   text: 'text-amber-300',       bg: 'bg-amber-500/10',       ring: 'ring-amber-500/30',       pulse: false },
  OVERDUE:            { label: 'Overdue',            dot: 'bg-red-500',     text: 'text-red-300',         bg: 'bg-red-500/10',         ring: 'ring-red-500/30',         pulse: true  },
  VACANT:             { label: 'Vacant',             dot: 'bg-slate-400',   text: 'text-slate-400',       bg: 'bg-slate-500/10',       ring: 'ring-slate-500/30',       pulse: false },
  UNDER_CONSTRUCTION: { label: 'Under Construction', dot: 'bg-cyan-500',   text: 'text-cyan-300',        bg: 'bg-cyan-500/10',        ring: 'ring-cyan-500/30',        pulse: false },
};

/** Computed dashboard summary. */
export function getDashboardSummary() {
  const occupied = UNITS.filter((u) => u.tenant !== null).length;
  const underConstruction = UNITS.filter((u) => u.status === 'UNDER_CONSTRUCTION').length;
  const vacant = UNITS.filter((u) => u.status === 'VACANT').length;
  const paid = UNITS.filter((u) => u.status === 'PAID');
  const pending = UNITS.filter((u) => u.status === 'PENDING' || u.status === 'OVERDUE');
  const collected = paid.reduce((sum, u) => sum + u.rentAmount, 0);
  const pendingAmount = pending.reduce((sum, u) => sum + u.rentAmount, 0);
  const totalExpected = UNITS.filter((u) => u.tenant !== null)
    .reduce((sum, u) => sum + u.rentAmount, 0);

  return {
    total: UNITS.length,
    occupied,
    underConstruction,
    vacant,
    paid: paid.length,
    pending: pending.length,
    collected,
    pendingAmount,
    totalExpected,
  };
}

/* WhatsApp reminder template */
export const WHATSAPP_TEMPLATE = (name, amount, date) =>
`Hello ${name} 👋
This is a reminder from *Sapthagiri Residency*.
Your rent of ₹${amount} is due on ${date}.
Please pay on time.
Thank you! 🙏
- Elumalai`;

/* Email reminder template */
export const EMAIL_REMINDER_TEMPLATE = (name, amount, date) =>
`Dear ${name},

This is a reminder from Sapthagiri Residency regarding your rent payment of ₹${amount} due on ${date}.

Please ensure timely payment to avoid any inconvenience.

Thank you,
Elumalai
Sapthagiri Residency`;
