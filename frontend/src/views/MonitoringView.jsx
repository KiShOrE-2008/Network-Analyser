import React, { useState } from 'react';
import { Activity, Clock, Zap, Radio, RefreshCw, Monitor, AlertTriangle } from 'lucide-react';
import { AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';
import EmptyState from '../components/EmptyState';

export default function MonitoringView({
  devices = [],
  metrics = [],
  schedulerStatus = null,
  onRunPingCheck,
  onToggleScheduler
}) {
  const [filter, setFilter] = useState('ALL');
  const [selectedDeviceId, setSelectedDeviceId] = useState(devices.length > 0 ? devices[0].id : null);

  const filteredDevices = devices.filter((d) => {
    if (filter === 'ONLINE') return d.status === 'ONLINE';
    if (filter === 'OFFLINE') return d.status === 'OFFLINE';
    if (filter === 'WARNING') return d.healthStatus === 'WARNING' || d.healthStatus === 'CRITICAL';
    return true;
  });

  const selectedDevice = devices.find(d => d.id === (selectedDeviceId || (devices[0] && devices[0].id)));

  // Format real metric historical series for selected device
  const deviceMetrics = (metrics || [])
    .filter(m => !selectedDevice || m.deviceId === selectedDevice.id)
    .map(m => ({
      time: m.timestamp ? new Date(m.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }) : '',
      latency: m.latencyMs !== null && m.latencyMs !== undefined ? m.latencyMs : null,
      packetLoss: m.packetLossPercent !== null && m.packetLossPercent !== undefined ? m.packetLossPercent : 0
    }))
    .filter(m => m.latency !== null);

  return (
    <section className="view-panel active">
      <div className="section-hero">
        <div>
          <h2 className="section-title">LIVE MONITORING ENGINE</h2>
          <p className="section-desc">Real-time parallel worker scheduler, STOMP metric streaming, and ping response history.</p>
        </div>
        <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
          <button
            className={`btn btn-sm ${schedulerStatus?.active ? 'btn-secondary' : 'btn-primary'}`}
            onClick={onToggleScheduler}
          >
            <Activity size={14} /> {schedulerStatus?.active ? 'Pause Scheduler' : 'Start Scheduler'}
          </button>
        </div>
      </div>

      {/* SCHEDULER STATUS BAR */}
      <div className="card toolbar-card" style={{ marginBottom: '24px' }}>
        <div className="scheduler-status-row">
          <div className="status-chip">
            <span className={`status-dot-sm ${schedulerStatus?.active ? 'green' : 'amber'}`} />
            <span className="mono font-semibold">
              SCHEDULER {schedulerStatus?.active ? 'ACTIVE' : 'PAUSED'}
            </span>
          </div>
          <div className="scheduler-meta mono text-muted">
            <span>Worker Pool: {schedulerStatus?.workerPoolSize || 8} Threads</span>
            <span>Cycle: {schedulerStatus?.lastCycleDurationMs || 0}ms</span>
            <span>Scanned: {schedulerStatus?.lastDevicesScannedCount || 0} Hosts</span>
          </div>
        </div>
      </div>

      {/* FILTERS & MONITORING TABLE */}
      <div className="card" style={{ marginBottom: '24px', padding: 0, overflow: 'hidden' }}>
        <div className="card-header" style={{ padding: '16px 20px', borderBottom: '1px solid var(--border-color)' }}>
          <div>
            <h3 className="card-title">MONITORED ENDPOINTS</h3>
            <div className="card-sub">Select host to inspect live latency chart</div>
          </div>
          <div className="btn-group">
            {['ALL', 'ONLINE', 'OFFLINE', 'WARNING'].map((f) => (
              <button
                key={f}
                className={`btn btn-xs ${filter === f ? 'btn-primary' : 'btn-secondary'}`}
                onClick={() => setFilter(f)}
              >
                {f}
              </button>
            ))}
          </div>
        </div>

        <div className="table-responsive">
          {filteredDevices.length === 0 ? (
            <EmptyState
              title="No Monitored Endpoints Found"
              description="No registered devices match the selected monitoring status filter."
            />
          ) : (
            <table className="data-table">
              <thead>
                <tr>
                  <th>STATUS</th>
                  <th>DEVICE NAME</th>
                  <th>IP ADDRESS</th>
                  <th>TYPE</th>
                  <th>LATENCY</th>
                  <th>PACKET LOSS</th>
                  <th>HEALTH</th>
                  <th>LAST CHECK</th>
                  <th style={{ textAlign: 'right' }}>PROBE</th>
                </tr>
              </thead>
              <tbody>
                {filteredDevices.map((d) => {
                  const isOnline = d.status === 'ONLINE';
                  const isOffline = d.status === 'OFFLINE';
                  const isSelected = selectedDevice && selectedDevice.id === d.id;
                  const latencyVal = d._latency !== undefined && d._latency !== null ? `${d._latency} ms` : 'N/A';
                  const lossVal = d._packetLoss !== undefined && d._packetLoss !== null ? `${d._packetLoss}%` : 'N/A';

                  return (
                    <tr
                      key={d.id}
                      className={`row-clickable ${isSelected ? 'row-selected' : ''}`}
                      onClick={() => setSelectedDeviceId(d.id)}
                    >
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
                      <td>
                        <span className="badge badge-subtle">{d.deviceType || 'UNKNOWN'}</span>
                      </td>
                      <td className="mono font-semibold">{latencyVal}</td>
                      <td className="mono">{lossVal}</td>
                      <td>
                        <span className={`status-text-${(d.healthStatus || '').toLowerCase()}`}>
                          {d.healthStatus || 'HEALTHY'}
                        </span>
                      </td>
                      <td className="mono text-muted">
                        {d.lastSeenAt ? new Date(d.lastSeenAt).toLocaleTimeString() : 'N/A'}
                      </td>
                      <td style={{ textAlign: 'right' }}>
                        <button
                          className="btn btn-secondary btn-xs"
                          onClick={(e) => {
                            e.stopPropagation();
                            onRunPingCheck(d.id);
                          }}
                        >
                          <Zap size={12} /> Ping
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {/* LIVE TELEMETRY GRAPH FOR SELECTED DEVICE */}
      {selectedDevice && (
        <div className="card">
          <div className="card-header">
            <div>
              <h3 className="card-title">
                LIVE LATENCY & PACKET LOSS — {selectedDevice.name || selectedDevice.ipAddress}
              </h3>
              <div className="card-sub mono">
                IP: {selectedDevice.ipAddress} • Last Ping: {selectedDevice._latency !== undefined && selectedDevice._latency !== null ? `${selectedDevice._latency} ms` : 'N/A'}
              </div>
            </div>
          </div>

          {deviceMetrics.length === 0 ? (
            <div className="empty-chart-notice">
              <span>No historical telemetry recorded for {selectedDevice.ipAddress}. Click "Ping" to execute a probe.</span>
            </div>
          ) : (
            <div style={{ width: '100%', height: 220 }}>
              <ResponsiveContainer>
                <AreaChart data={deviceMetrics}>
                  <defs>
                    <linearGradient id="selectedLatencyGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#10B981" stopOpacity={0.6} />
                      <stop offset="95%" stopColor="#10B981" stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <XAxis dataKey="time" stroke="#64748b" fontSize={11} />
                  <YAxis stroke="#64748b" fontSize={11} unit="ms" />
                  <Tooltip 
                    contentStyle={{ background: '#101722', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '8px', color: '#F1F5F9' }} 
                  />
                  <Area type="monotone" dataKey="latency" stroke="#10B981" strokeWidth={2} fillOpacity={1} fill="url(#selectedLatencyGrad)" name="Latency (ms)" />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>
      )}
    </section>
  );
}
