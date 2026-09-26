import React, { useState, useEffect } from 'react';
import { Search, X, Monitor, ChevronRight } from 'lucide-react';

export default function SearchModal({ devices = [], isOpen, onClose, onSelectDevice }) {
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    const handleKeyDown = (e) => {
      if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
        e.preventDefault();
        if (isOpen) onClose();
        else setSearchTerm('');
      } else if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const filtered = (devices || []).filter(d => {
    if (!searchTerm.trim()) return true;
    const term = searchTerm.toLowerCase();
    return (
      (d.name && d.name.toLowerCase().includes(term)) ||
      (d.ipAddress && d.ipAddress.toLowerCase().includes(term)) ||
      (d.hostname && d.hostname.toLowerCase().includes(term)) ||
      (d.macAddress && d.macAddress.toLowerCase().includes(term)) ||
      (d.vendor && d.vendor.toLowerCase().includes(term)) ||
      (d.deviceType && d.deviceType.toLowerCase().includes(term))
    );
  }).slice(0, 10);

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-card search-modal" onClick={e => e.stopPropagation()}>
        <div className="search-input-header">
          <Search size={20} className="search-modal-icon" />
          <input
            type="text"
            className="search-modal-input mono"
            placeholder="Search IP address, hostname, MAC address, vendor..."
            value={searchTerm}
            onChange={e => setSearchTerm(e.target.value)}
            autoFocus
          />
          <button className="icon-btn" onClick={onClose}>
            <X size={18} />
          </button>
        </div>

        <div className="search-results-list">
          {filtered.length === 0 ? (
            <div className="empty-search-state">
              No matching devices found for "{searchTerm}".
            </div>
          ) : (
            filtered.map(device => (
              <div 
                key={device.id} 
                className="search-result-row"
                onClick={() => {
                  onSelectDevice(device.id);
                  onClose();
                }}
              >
                <div className="result-main">
                  <span className={`status-dot-sm ${device.status === 'ONLINE' ? 'green' : 'red'}`} />
                  <Monitor size={16} className="result-type-icon" />
                  <div>
                    <div className="result-title">{device.name || device.ipAddress}</div>
                    <div className="result-sub mono">
                      {device.ipAddress} {device.hostname ? `• ${device.hostname}` : ''} {device.macAddress ? `• ${device.macAddress}` : ''}
                    </div>
                  </div>
                </div>
                <div className="result-action">
                  <span className="badge badge-subtle">{device.deviceType || 'UNKNOWN'}</span>
                  <ChevronRight size={16} />
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
