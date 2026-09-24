import React, { useState } from 'react';
import { Search, Plus, Zap } from 'lucide-react';

export default function DevicesView({
  devices,
  onScanClick,
  onOpenRegisterModal,
  onDeviceClick
}) {
  const [search, setSearch] = useState('');
  const [typeFilter, setTypeFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('ALL');

  const filteredDevices = devices.filter(d => {
    const q = search.toLowerCase().trim();
    const matchQuery = !q || (
      (d.name && d.name.toLowerCase().includes(q)) ||
      (d.ipAddress && d.ipAddress.toLowerCase().includes(q)) ||
      (d.hostname && d.hostname.toLowerCase().includes(q)) ||
      (d.macAddress && d.macAddress.toLowerCase().includes(q)) ||
      (d.vendor && d.vendor.toLowerCase().includes(q))
    );

    const matchType = typeFilter === 'ALL' || d.deviceType === typeFilter;
    const matchStatus = statusFilter === 'ALL' || d.status === statusFilter;

    return matchQuery && matchType && matchStatus;
  });

  return (
    <section className="view-panel active">
      <div className="section-hero">
        <div>
          <h2 className="section-title">DEVICES</h2>
          <p className="section-desc">Registered endpoints, IP addresses, vendors, and latency monitoring.</p>
        </div>
        <div className="hero-actions">
          <button className="btn btn-secondary btn-sm" onClick={onScanClick}>⚡ Scan Network</button>
          <button className="btn btn-primary btn-sm" onClick={onOpenRegisterModal}>+ Register Device</button>
        </div>
      </div>

      <div className="toolbar">
        <div className="search-box">
          <Search className="search-icon" size={16} />
          <input
            type="text"
            className="search-input"
            placeholder="Search devices by IP, name, hostname, MAC, vendor..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>

        <div className="filter-group">
          <select
            className="form-select"
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value)}
          >
            <option value="ALL">All Types</option>
            <option value="ROUTER">Router</option>
            <option value="SERVER">Server</option>
            <option value="WORKSTATION">Workstation</option>
            <option value="SWITCH">Switch</option>
            <option value="MOBILE">Mobile</option>
            <option value="UNKNOWN">Unknown</option>
          </select>

          <select
            className="form-select"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="ALL">All Status</option>
            <option value="ONLINE">Online</option>
            <option value="OFFLINE">Offline</option>
            <option value="UNKNOWN">Unknown</option>
          </select>
        </div>
      </div>

      <div className="card table-card">
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>STATUS</th>
                <th>DEVICE</th>
                <th>IP ADDRESS</th>
                <th>HOSTNAME</th>
                <th>MAC ADDRESS</th>
                <th>VENDOR</th>
                <th>TYPE</th>
                <th>LATENCY</th>
                <th>LAST SEEN</th>
                <th style={{ textAlign: 'right' }}>ACTIONS</th>
              </tr>
            </thead>
            <tbody>
              {filteredDevices.length === 0 ? (
                <tr>
                  <td colSpan="10" className="empty-state">No matching devices in inventory.</td>
                </tr>
              ) : (
                filteredDevices.map(d => {
                  const isOnline = d.status === 'ONLINE';
                  const latStr = typeof d._latency === 'number' ? `${d._latency.toFixed(1)} ms` : 'N/A';
                  const lastSeen = d.lastSeen ? new Date(d.lastSeen).toLocaleTimeString() : 'N/A';

                  return (
                    <tr key={d.id} onClick={() => onDeviceClick(d.id)} style={{ cursor: 'pointer' }}>
                      <td>
                        <span className={`badge ${isOnline ? 'badge-online' : 'badge-offline'}`}>
                          <span className={`status-dot ${isOnline ? 'online' : 'offline'}`}></span>
                          {d.status || 'UNKNOWN'}
                        </span>
                      </td>
                      <td style={{ fontWeight: 600, color: '#fff' }}>{d.name}</td>
                      <td className="mono">{d.ipAddress}</td>
                      <td className="mono">{d.hostname || 'N/A'}</td>
                      <td className="mono">{d.macAddress || 'N/A'}</td>
                      <td>{d.vendor || 'N/A'}</td>
                      <td><span className="badge badge-new">{d.deviceType || 'UNKNOWN'}</span></td>
                      <td className="mono">{latStr}</td>
                      <td style={{ color: 'var(--text-subtle)' }}>{lastSeen}</td>
                      <td style={{ textAlign: 'right' }} onClick={(e) => e.stopPropagation()}>
                        <button className="btn btn-secondary btn-sm" onClick={() => onDeviceClick(d.id)}>
                          Inspect
                        </button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </section>
  );
}
