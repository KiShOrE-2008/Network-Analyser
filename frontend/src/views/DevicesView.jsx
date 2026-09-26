import React, { useState } from 'react';
import {
  Search,
  Plus,
  Zap,
  Radio,
  Trash2,
  Eye,
  ToggleLeft,
  ToggleRight,
  Filter,
  Monitor
} from 'lucide-react';
import EmptyState from '../components/EmptyState';

export default function DevicesView({
  devices = [],
  onRegisterClick,
  onInspectClick,
  onPingClick,
  onPortScanClick,
  onToggleMonitoring,
  onDeleteClick
}) {
  const [search, setSearch] = useState('');
  const [typeFilter, setTypeFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [monitoringFilter, setMonitoringFilter] = useState('');

  const filteredDevices = devices.filter((d) => {
    if (search.trim()) {
      const term = search.toLowerCase();
      const match =
        (d.name && d.name.toLowerCase().includes(term)) ||
        (d.ipAddress && d.ipAddress.toLowerCase().includes(term)) ||
        (d.hostname && d.hostname.toLowerCase().includes(term)) ||
        (d.macAddress && d.macAddress.toLowerCase().includes(term)) ||
        (d.vendor && d.vendor.toLowerCase().includes(term));
      if (!match) return false;
    }

    if (typeFilter && d.deviceType !== typeFilter) return false;
    if (statusFilter && d.status !== statusFilter) return false;
    if (monitoringFilter === 'ENABLED' && !d.monitoringEnabled) return false;
    if (monitoringFilter === 'DISABLED' && d.monitoringEnabled) return false;

    return true;
  });

  return (
    <section className="view-panel active">
      <div className="section-hero">
        <div>
          <h2 className="section-title">DEVICE INVENTORY</h2>
          <p className="section-desc">Registered network endpoints, MAC identification, and reachability monitoring controls.</p>
        </div>
        <button className="btn btn-primary btn-sm" onClick={onRegisterClick}>
          <Plus size={16} /> Add Device
        </button>
      </div>

      {/* FILTER TOOLBAR */}
      <div className="card toolbar-card">
        <div className="toolbar-grid">
          <div className="search-input-wrapper">
            <Search size={16} className="search-icon" />
            <input
              type="text"
              className="form-input mono"
              placeholder="Search IP, hostname, MAC address, vendor..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>

          <div className="filter-group">
            <Filter size={14} className="text-muted" />

            <select
              className="form-select"
              value={typeFilter}
              onChange={(e) => setTypeFilter(e.target.value)}
            >
              <option value="">All Types</option>
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

            <select
              className="form-select"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="">All Statuses</option>
              <option value="ONLINE">Online</option>
              <option value="OFFLINE">Offline</option>
              <option value="UNKNOWN">Unknown</option>
            </select>

            <select
              className="form-select"
              value={monitoringFilter}
              onChange={(e) => setMonitoringFilter(e.target.value)}
            >
              <option value="">Monitoring: All</option>
              <option value="ENABLED">Enabled</option>
              <option value="DISABLED">Disabled</option>
            </select>
          </div>
        </div>
      </div>

      {/* DEVICE DATA TABLE */}
      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <div className="table-responsive">
          {filteredDevices.length === 0 ? (
            <EmptyState
              title="No Devices Found"
              description="No registered devices match your search or filter parameters."
              actionLabel="Add New Device"
              onAction={onRegisterClick}
            />
          ) : (
            <table className="data-table">
              <thead>
                <tr>
                  <th>STATUS</th>
                  <th>DEVICE NAME</th>
                  <th>IP ADDRESS</th>
                  <th>HOSTNAME</th>
                  <th>TYPE</th>
                  <th>VENDOR</th>
                  <th>LATENCY</th>
                  <th>PACKET LOSS</th>
                  <th>LAST SEEN</th>
                  <th>MONITORING</th>
                  <th style={{ textAlign: 'right' }}>ACTIONS</th>
                </tr>
              </thead>
              <tbody>
                {filteredDevices.map((d) => {
                  const isOnline = d.status === 'ONLINE';
                  const isOffline = d.status === 'OFFLINE';
                  const latencyVal = d._latency !== undefined && d._latency !== null ? `${d._latency} ms` : 'N/A';
                  const lossVal = d._packetLoss !== undefined && d._packetLoss !== null ? `${d._packetLoss}%` : 'N/A';

                  return (
                    <tr key={d.id}>
                      <td>
                        <div className="status-cell">
                          <span className={`status-dot-sm ${isOnline ? 'green' : isOffline ? 'red' : 'gray'}`} />
                          <span className={`mono ${isOnline ? 'green-text' : isOffline ? 'red-text' : 'text-muted'}`}>
                            {d.status || 'UNKNOWN'}
                          </span>
                        </div>
                      </td>
                      <td className="font-semibold">{d.name || d.ipAddress}</td>
                      <td className="mono">{d.ipAddress}</td>
                      <td className="mono text-muted">{d.hostname || '—'}</td>
                      <td>
                        <span className="badge badge-subtle">{d.deviceType || 'UNKNOWN'}</span>
                      </td>
                      <td className="text-muted">{d.vendor || '—'}</td>
                      <td className="mono">{latencyVal}</td>
                      <td className="mono">{lossVal}</td>
                      <td className="mono text-muted">
                        {d.lastSeenAt ? new Date(d.lastSeenAt).toLocaleTimeString() : 'N/A'}
                      </td>
                      <td>
                        <button
                          className={`toggle-btn ${d.monitoringEnabled ? 'active' : ''}`}
                          onClick={() => onToggleMonitoring(d.id)}
                          title={d.monitoringEnabled ? 'Disable Monitoring' : 'Enable Monitoring'}
                        >
                          {d.monitoringEnabled ? <ToggleRight size={22} color="var(--online)" /> : <ToggleLeft size={22} color="var(--text-subtle)" />}
                        </button>
                      </td>
                      <td>
                        <div className="table-actions">
                          <button
                            className="btn-icon"
                            onClick={() => onInspectClick(d.id)}
                            title="Inspect Device"
                          >
                            <Eye size={16} />
                          </button>
                          <button
                            className="btn-icon"
                            onClick={() => onPingClick(d.id)}
                            title="Run Ping Probe"
                          >
                            <Zap size={16} />
                          </button>
                          <button
                            className="btn-icon"
                            onClick={() => onPortScanClick(d.id)}
                            title="Scan Ports"
                          >
                            <Radio size={16} />
                          </button>
                          <button
                            className="btn-icon danger"
                            onClick={() => onDeleteClick(d.id)}
                            title="Delete Device"
                          >
                            <Trash2 size={16} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </section>
  );
}
