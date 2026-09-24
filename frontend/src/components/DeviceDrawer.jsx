import React, { useState, useEffect } from 'react';
import { X, Zap, Search, Activity, ToggleLeft, Trash2 } from 'lucide-react';

export default function DeviceDrawer({
  deviceId,
  devices,
  onClose,
  onCheck,
  onScanPorts,
  onToggleMonitoring,
  onDelete
}) {
  const [ports, setPorts] = useState([]);
  const [snmp, setSnmp] = useState(null);

  const device = (devices || []).find(d => d.id === deviceId);

  useEffect(() => {
    if (!deviceId) return;
    fetch(`/api/devices/${deviceId}/ports`)
      .then(r => r.ok ? r.json() : [])
      .then(p => setPorts(p || []))
      .catch(() => setPorts([]));

    fetch(`/api/devices/${deviceId}/snmp`)
      .then(r => r.ok ? r.json() : null)
      .then(s => setSnmp(s))
      .catch(() => setSnmp(null));
  }, [deviceId]);

  if (!device) return null;

  const isOnline = device.status === 'ONLINE';

  return (
    <>
      <div className="drawer-overlay" onClick={onClose}></div>
      <aside className="device-drawer">
        <div className="drawer-header">
          <div>
            <div className="drawer-title">{device.name}</div>
            <div className="drawer-sub mono">{device.ipAddress}</div>
          </div>
          <button className="close-btn" onClick={onClose}><X size={20} /></button>
        </div>

        <div className="drawer-body">
          <div style={{ marginBottom: '20px' }}>
            <span className={`badge ${isOnline ? 'badge-online' : 'badge-offline'}`}>
              <span className={`status-dot ${isOnline ? 'online' : 'offline'}`}></span>
              {device.status || 'UNKNOWN'}
            </span>
          </div>

          <div className="drawer-section">
            <div className="drawer-section-title">IDENTITY</div>
            <div className="info-pair"><span className="info-key">Hostname</span><span className="info-val mono">{device.hostname || 'N/A'}</span></div>
            <div className="info-pair"><span className="info-key">MAC Address</span><span className="info-val mono">{device.macAddress || 'N/A'}</span></div>
            <div className="info-pair"><span className="info-key">Vendor</span><span className="info-val">{device.vendor || 'N/A'}</span></div>
            <div className="info-pair"><span className="info-key">Device Type</span><span className="info-val">{device.deviceType || 'N/A'}</span></div>
          </div>

          <div className="drawer-section">
            <div className="drawer-section-title">CONNECTIVITY</div>
            <div className="info-pair"><span className="info-key">Latency</span><span className="info-val mono">{typeof device._latency === 'number' ? `${device._latency.toFixed(1)} ms` : 'N/A'}</span></div>
            <div className="info-pair"><span className="info-key">Last Seen</span><span className="info-val">{device.lastSeen ? new Date(device.lastSeen).toLocaleTimeString() : 'N/A'}</span></div>
          </div>

          <div className="drawer-section">
            <div className="drawer-section-title">DISCOVERED OPEN PORTS</div>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
              {ports.length === 0 ? (
                <div style={{ fontSize: '12px', color: 'var(--text-subtle)' }}>No port scan data loaded.</div>
              ) : (
                ports.map((p, i) => (
                  <span key={i} className="badge badge-new">
                    {p.portNumber}/{p.protocol || 'TCP'} ({p.serviceName || 'Open'})
                  </span>
                ))
              )}
            </div>
          </div>

          <div className="drawer-actions">
            <button className="btn btn-secondary btn-sm" onClick={() => onCheck(device.id)}>
              <Zap size={14} /> Check
            </button>
            <button className="btn btn-secondary btn-sm" onClick={() => onScanPorts(device.id)}>
              <Search size={14} /> Scan Ports
            </button>
            <button className="btn btn-secondary btn-sm" onClick={() => onToggleMonitoring(device.id)}>
              <ToggleLeft size={14} /> Toggle Monitoring
            </button>
            <button className="btn btn-danger btn-sm" onClick={() => onDelete(device.id)}>
              <Trash2 size={14} /> Delete
            </button>
          </div>
        </div>
      </aside>
    </>
  );
}
