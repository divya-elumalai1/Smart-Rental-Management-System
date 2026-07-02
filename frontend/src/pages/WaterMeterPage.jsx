import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Camera, Send, RefreshCw, Droplet } from 'lucide-react';
import { waterMeterAPI } from '../utils/api';
import toast from 'react-hot-toast';

export default function WaterMeterPage() {
  const [units, setUnits] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState({});
  const [calculating, setCalculating] = useState({});
  const [bills, setBills] = useState({});

  useEffect(() => {
    fetchOccupiedUnits();
  }, []);

  const fetchOccupiedUnits = async () => {
    try {
      const res = await waterMeterAPI.getOccupied();
      setUnits(res.data || []);
    } catch (err) {
      console.error('Failed to fetch units', err);
      toast.error('Failed to load units');
    } finally {
      setLoading(false);
    }
  };

  const handlePhotoUpload = async (unitNumber, file) => {
    if (!file) return;
    
    setUploading(prev => ({ ...prev, [unitNumber]: true }));
    try {
      const formData = new FormData();
      formData.append('photo', file);
      formData.append('unitNumber', unitNumber);
      
      const res = await waterMeterAPI.readPhoto(formData);
      const reading = res.data;
      
      toast.success(`Meter reading: ${reading}`);
      
      // Auto-calculate bill
      await calculateBill(unitNumber, reading);
    } catch (err) {
      toast.error('Failed to read meter');
      console.error(err);
    } finally {
      setUploading(prev => ({ ...prev, [unitNumber]: false }));
    }
  };

  const calculateBill = async (unitNumber, reading) => {
    setCalculating(prev => ({ ...prev, [unitNumber]: true }));
    try {
      const res = await waterMeterAPI.calculate(unitNumber, reading);
      setBills(prev => ({ ...prev, [unitNumber]: res.data }));
      toast.success('Bill calculated successfully');
    } catch (err) {
      toast.error('Failed to calculate bill');
      console.error(err);
    } finally {
      setCalculating(prev => ({ ...prev, [unitNumber]: false }));
    }
  };

  const handleSendBill = async (unitNumber) => {
    try {
      const bill = bills[unitNumber];
      await waterMeterAPI.save({
        unitNumber,
        currentReading: bill.currentReading,
        photoUrl: '',
        notes: 'Bill sent from water meter page',
      });
      toast.success('Bill saved successfully');
    } catch (err) {
      toast.error('Failed to save bill');
      console.error(err);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <RefreshCw className="h-8 w-8 animate-spin text-indigo-400" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <motion.div
        initial={{ opacity: 0, y: -10 }}
        animate={{ opacity: 1, y: 0 }}
        className="flex items-center justify-between"
      >
        <div>
          <h1 className="text-2xl font-extrabold text-white" style={{ fontFamily: 'Plus Jakarta Sans' }}>
            Water Meter Readings
          </h1>
          <p className="mt-1 text-sm text-slate-500">
            Upload meter photos, calculate bills, and send to tenants
          </p>
        </div>
      </motion.div>

      {/* Units Grid */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {units.map((unit, index) => {
          const bill = bills[unit.unitNumber];
          return (
            <motion.div
              key={unit.unitNumber}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.1 }}
              className="glass-card p-6"
            >
              {/* Unit Header */}
              <div className="mb-4 flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-cyan-500 to-blue-500 text-sm font-bold text-white">
                    {unit.unitNumber}
                  </div>
                  <div>
                    <h3 className="font-semibold text-white">Unit {unit.unitNumber}</h3>
                    <p className="text-xs text-slate-500">{unit.tenantName || 'Occupied'}</p>
                  </div>
                </div>
                <Droplet className="h-5 w-5 text-cyan-400" />
              </div>

              {/* Upload Section */}
              <div className="mb-4">
                <label className="mb-2 block text-xs font-medium text-slate-400">
                  Upload Meter Photo
                </label>
                <div className="relative">
                  <input
                    type="file"
                    accept="image/*"
                    onChange={(e) => handlePhotoUpload(unit.unitNumber, e.target.files[0])}
                    className="absolute inset-0 cursor-pointer opacity-0"
                    disabled={uploading[unit.unitNumber]}
                  />
                  <div className={`flex items-center justify-center gap-2 rounded-xl border-2 border-dashed border-white/[0.1] bg-white/[0.02] py-4 text-center transition-colors hover:border-indigo-500/50 hover:bg-white/[0.05] ${uploading[unit.unitNumber] ? 'opacity-50' : ''}`}>
                    {uploading[unit.unitNumber] ? (
                      <RefreshCw className="h-5 w-5 animate-spin text-indigo-400" />
                    ) : (
                      <>
                        <Camera className="h-5 w-5 text-slate-400" />
                        <span className="text-sm text-slate-400">Click to upload photo</span>
                      </>
                    )}
                  </div>
                </div>
              </div>

              {/* Bill Details */}
              {bill && (
                <motion.div
                  initial={{ opacity: 0, height: 0 }}
                  animate={{ opacity: 1, height: 'auto' }}
                  className="space-y-3 border-t border-white/[0.06] pt-4"
                >
                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <p className="text-[10px] uppercase tracking-wider text-slate-500">Previous</p>
                      <p className="text-sm font-semibold text-white">{bill.previousReading}</p>
                    </div>
                    <div>
                      <p className="text-[10px] uppercase tracking-wider text-slate-500">Current</p>
                      <p className="text-sm font-semibold text-white">{bill.currentReading}</p>
                    </div>
                    <div>
                      <p className="text-[10px] uppercase tracking-wider text-slate-500">Units</p>
                      <p className="text-sm font-semibold text-cyan-400">{bill.unitsConsumed}</p>
                    </div>
                    <div>
                      <p className="text-[10px] uppercase tracking-wider text-slate-500">Rate</p>
                      <p className="text-sm font-semibold text-white">₹{bill.waterRate}/unit</p>
                    </div>
                  </div>

                  <div className="flex items-center justify-between rounded-lg bg-emerald-500/10 px-4 py-3">
                    <div>
                      <p className="text-xs text-emerald-400">Water Bill</p>
                      <p className="text-lg font-bold text-emerald-400">₹{bill.waterBill?.toLocaleString('en-IN')}</p>
                    </div>
                    <div className="text-right">
                      <p className="text-xs text-slate-400">Total</p>
                      <p className="text-lg font-bold text-white">₹{bill.totalBill?.toLocaleString('en-IN')}</p>
                    </div>
                  </div>

                  <button
                    onClick={() => handleSendBill(unit.unitNumber)}
                    className="flex w-full items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-indigo-500 to-violet-500 py-2.5 text-sm font-semibold text-white shadow-lg shadow-indigo-500/30 transition-shadow hover:shadow-indigo-500/50"
                  >
                    <Send className="h-4 w-4" />
                    Send Bill
                  </button>
                </motion.div>
              )}

              {!bill && !uploading[unit.unitNumber] && (
                <div className="mt-4 text-center">
                  <p className="text-xs text-slate-500">
                    Upload a photo to calculate the water bill
                  </p>
                </div>
              )}
            </motion.div>
          );
        })}
      </div>

      {units.length === 0 && (
        <div className="glass-card flex flex-col items-center justify-center py-12 text-center">
          <Droplet className="mb-3 h-12 w-12 text-slate-600" />
          <p className="text-slate-500">No occupied units found</p>
        </div>
      )}
    </div>
  );
}
