import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Building2, MapPin, IndianRupee, Shield, Calendar, Home, User, Ruler, Bath, BedDouble } from 'lucide-react';
import { tenantPortalAPI } from '../utils/api';
import toast from 'react-hot-toast';

export default function TenantMyUnit() {
  const [unit, setUnit] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const res = await tenantPortalAPI.getMyUnit();
        setUnit(res.data);
      } catch (err) {
        toast.error('Failed to load unit details');
        console.error(err);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-2 border-indigo-500 border-t-transparent" />
      </div>
    );
  }

  if (!unit) {
    return (
      <div className="flex h-64 items-center justify-center">
        <p className="text-slate-500">No unit assigned</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }}>
        <h1 className="text-2xl font-extrabold text-white" style={{ fontFamily: 'Plus Jakarta Sans' }}>My Unit</h1>
        <p className="mt-1 text-sm text-slate-500">Your assigned unit details</p>
      </motion.div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.1 }}
          className="glass-card p-6">
          <div className="mb-4 flex items-center gap-2">
            <Building2 className="h-5 w-5 text-indigo-400" />
            <h3 className="font-bold text-white">Unit Information</h3>
          </div>
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <Home className="h-4 w-4 text-slate-500" />
              <div>
                <p className="text-xs text-slate-500">Unit Number</p>
                <p className="text-sm font-semibold text-white">{unit.unit_number || '-'}</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <MapPin className="h-4 w-4 text-slate-500" />
              <div>
                <p className="text-xs text-slate-500">Floor</p>
                <p className="text-sm font-semibold text-white">{unit.floor_label || '-'}</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <MapPin className="h-4 w-4 text-slate-500" />
              <div>
                <p className="text-xs text-slate-500">Address</p>
                <p className="text-sm font-semibold text-white">{unit.address || '-'}</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <MapPin className="h-4 w-4 text-slate-500" />
              <div>
                <p className="text-xs text-slate-500">City</p>
                <p className="text-sm font-semibold text-white">{unit.city || '-'}{unit.state ? `, ${unit.state}` : ''}</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Ruler className="h-4 w-4 text-slate-500" />
              <div>
                <p className="text-xs text-slate-500">Area</p>
                <p className="text-sm font-semibold text-white">{unit.area_sqft ? `${unit.area_sqft} sq.ft` : '-'}</p>
              </div>
            </div>
            {unit.bedrooms && (
              <div className="flex items-center gap-3">
                <BedDouble className="h-4 w-4 text-slate-500" />
                <div>
                  <p className="text-xs text-slate-500">Bedrooms</p>
                  <p className="text-sm font-semibold text-white">{unit.bedrooms}</p>
                </div>
              </div>
            )}
            {unit.bathrooms && (
              <div className="flex items-center gap-3">
                <Bath className="h-4 w-4 text-slate-500" />
                <div>
                  <p className="text-xs text-slate-500">Bathrooms</p>
                  <p className="text-sm font-semibold text-white">{unit.bathrooms}</p>
                </div>
              </div>
            )}
          </div>
        </motion.div>

        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }}
          className="glass-card p-6">
          <div className="mb-4 flex items-center gap-2">
            <IndianRupee className="h-5 w-5 text-emerald-400" />
            <h3 className="font-bold text-white">Rent & Deposit</h3>
          </div>
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <IndianRupee className="h-4 w-4 text-slate-500" />
              <div>
                <p className="text-xs text-slate-500">Monthly Rent</p>
                <p className="text-lg font-bold text-emerald-400">₹{Number(unit.rent_amount || 0).toLocaleString('en-IN')}</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Shield className="h-4 w-4 text-slate-500" />
              <div>
                <p className="text-xs text-slate-500">Deposit</p>
                <p className="text-sm font-semibold text-white">₹{Number(unit.deposit || 0).toLocaleString('en-IN')}</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Calendar className="h-4 w-4 text-slate-500" />
              <div>
                <p className="text-xs text-slate-500">Status</p>
                <span className={`mt-1 inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold ring-1 ${
                  unit.status === 'OCCUPIED' ? 'bg-emerald-500/10 text-emerald-300 ring-emerald-500/30' : 'bg-amber-500/10 text-amber-300 ring-amber-500/30'
                }`}>
                  <span className={`h-1.5 w-1.5 rounded-full ${unit.status === 'OCCUPIED' ? 'bg-emerald-500' : 'bg-amber-500'}`} />
                  {unit.status || '-'}
                </span>
              </div>
            </div>
          </div>
        </motion.div>

        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.3 }}
          className="glass-card p-6">
          <div className="mb-4 flex items-center gap-2">
            <User className="h-5 w-5 text-amber-400" />
            <h3 className="font-bold text-white">Landlord</h3>
          </div>
          <div className="flex items-center gap-3">
            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-gradient-to-br from-amber-400 to-orange-500 text-lg font-bold text-white shadow-lg">
              {(unit.landlord_name || 'O').charAt(0)}
            </div>
            <div>
              <p className="text-sm font-semibold text-white">{unit.landlord_name || 'Owner'}</p>
              <p className="text-xs text-slate-500">Property Owner</p>
            </div>
          </div>
        </motion.div>

        {unit.amenities && (
          <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.4 }}
            className="glass-card p-6">
            <div className="mb-4 flex items-center gap-2">
              <Home className="h-5 w-5 text-cyan-400" />
              <h3 className="font-bold text-white">Amenities</h3>
            </div>
            <p className="text-sm text-slate-300">{unit.amenities}</p>
          </motion.div>
        )}
      </div>
    </div>
  );
}
