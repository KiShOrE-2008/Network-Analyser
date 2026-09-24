import React, { useState } from 'react';
import { X } from 'lucide-react';

export default function RegisterModal({ onClose, onRegister }) {
  const [name, setName] = useState('');
  const [ipAddress, setIpAddress] = useState('');
  const [deviceType, setDeviceType] = useState('ROUTER');
  const [vendor, setVendor] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!name || !ipAddress) return;
    onRegister({ name, ipAddress, deviceType, vendor });
  };

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h3 style={{ color: '#fff' }}>Register New Device</h3>
          <button className="close-btn" onClick={onClose}><X size={20} /></button>
        </div>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Device Name</label>
            <input
              type="text"
              className="form-input"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. Core Switch"
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">IP Address</label>
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
            <label className="form-label">Device Type</label>
            <select
              className="form-select"
              value={deviceType}
              onChange={(e) => setDeviceType(e.target.value)}
            >
              <option value="ROUTER">Router</option>
              <option value="SERVER">Server</option>
              <option value="WORKSTATION">Workstation</option>
              <option value="SWITCH">Switch</option>
              <option value="MOBILE">Mobile</option>
              <option value="UNKNOWN">Unknown</option>
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Vendor (Optional)</label>
            <input
              type="text"
              className="form-input"
              value={vendor}
              onChange={(e) => setVendor(e.target.value)}
              placeholder="e.g. Cisco"
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '24px' }}>
            <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary">Save & Register</button>
          </div>
        </form>
      </div>
    </div>
  );
}
