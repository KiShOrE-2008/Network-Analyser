import React, { useState } from 'react';
import { X, Plus } from 'lucide-react';

export default function RegisterModal({ onClose, onRegister }) {
  const [name, setName] = useState('');
  const [ipAddress, setIpAddress] = useState('');
  const [hostname, setHostname] = useState('');
  const [deviceType, setDeviceType] = useState('ROUTER');
  const [vendor, setVendor] = useState('');
  const [model, setModel] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!name || !ipAddress) return;

    setSubmitting(true);
    try {
      await onRegister({
        name,
        ipAddress,
        hostname: hostname || null,
        deviceType,
        vendor: vendor || null,
        model: model || null
      });
      onClose();
    } catch (_) {
      // Error handled by parent toast
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-card" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h3>Register Device Inventory</h3>
          <button className="icon-btn" onClick={onClose}><X size={18} /></button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Device Name *</label>
            <input
              type="text"
              className="form-input"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. Core Router Gateway"
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">IP Address *</label>
            <input
              type="text"
              className="form-input mono"
              value={ipAddress}
              onChange={(e) => setIpAddress(e.target.value)}
              placeholder="192.168.1.1"
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Hostname (Optional)</label>
            <input
              type="text"
              className="form-input mono"
              value={hostname}
              onChange={(e) => setHostname(e.target.value)}
              placeholder="router.local"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Device Type</label>
            <select
              className="form-select"
              value={deviceType}
              onChange={(e) => setDeviceType(e.target.value)}
            >
              <option value="ROUTER">Router</option>
              <option value="SWITCH">Switch</option>
              <option value="SERVER">Server</option>
              <option value="WORKSTATION">Workstation</option>
              <option value="PRINTER">Printer</option>
              <option value="FIREWALL">Firewall</option>
              <option value="ACCESS_POINT">Access Point</option>
              <option value="IOT">IoT Endpoint</option>
              <option value="UNKNOWN">Unknown</option>
            </select>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Vendor (OUI)</label>
              <input
                type="text"
                className="form-input"
                value={vendor}
                onChange={(e) => setVendor(e.target.value)}
                placeholder="e.g. Cisco / TP-Link"
              />
            </div>

            <div className="form-group">
              <label className="form-label">Model (Optional)</label>
              <input
                type="text"
                className="form-input"
                value={model}
                onChange={(e) => setModel(e.target.value)}
                placeholder="e.g. Catalyst 3850"
              />
            </div>
          </div>

          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              <Plus size={16} /> {submitting ? 'Registering...' : 'Save & Register'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
