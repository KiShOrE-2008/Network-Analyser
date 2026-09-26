import React, { useState } from 'react';
import {
  Monitor,
  Zap,
  AlertTriangle,
  Clock,
  RefreshCw,
  Shield,
  AlertOctagon,
  Radio,
  ArrowRight,
  ShieldAlert,
  Server,
  Activity,
  HardDrive
} from 'lucide-react';
import { AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';
import EmptyState from '../components/EmptyState';

export default function OverviewView({
  devices = [],
  events = [],
  alerts = [],
  metrics = [],
  onScanClick,
  onDeviceClick,
  onRefresh,
  onViewAllChanges
}) {
  const [timeRange, setTimeRange] = useState('1H');

  const total = devices.length;
  const online = devices.filter(d => d.status === 'ONLINE').length;
  const offline = devices.filter(d => d.status === 'OFFLINE').length;
  const activeAlertsCount = (alerts || []).filter(a => !a.isResolved && !a.resolved).length;

  const validLatencies = devices
    .map(d => d._latency !== undefined ? d._latency : null)
    .filter(l => typeof l === 'number' && !isNaN(l) && l >= 0);

  const avgLatency = validLatencies.length > 0
    ? (validLatencies.reduce((a, b) => a + b, 0) / validLatencies.length).toFixed(1)
    : null;

  // Filter events for "WHAT'S CHANGED" card
  const newDevicesCount = events.filter(e => e.eventType === 'DEVICE_DISCOVERED').length;
  const portChangesCount = events.filter(e => e.eventType === 'PORT_CHANGE' || (e.message && e.message.toLowerCase().includes('port'))).length;
  const onlineEventsCount = events.filter(e => e.eventType === 'STATUS_CHANGE' && e.currentStatus === 'ONLINE').length;
  const offlineEventsCount = events.filter(e => e.eventType === 'STATUS_CHANGE' && e.currentStatus === 'OFFLINE').length;
  const identityChangesCount = events.filter(e => e.eventType === 'NETWORK_CHANGE' || (e.message && e.message.toLowerCase().includes('mac'))).length;

  // Real metric series formatting
  const chartData = (metrics || []).map(m => ({
    time: m.timestamp ? new Date(m.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '',
    latency: m.latencyMs !== null && m.latencyMs !== undefined ? m.latencyMs : null,
    packetLoss: m.packetLossPercent !== null && m.packetLossPercent !== undefined ? m.packetLossPercent : null
  })).filter(item => item.latency !== null);

  return (
    <section className="view-panel active">
      <div className="section-hero">
        <div>
          <h2 className="section-title">NETWORK OVERVIEW</h2>
          <p className="section-desc">Real-time operational visibility into discovered hosts, network events, and ping telemetry.</p>
        </div>
        <button className="btn btn-primary btn-sm" onClick={onScanClick}>
          <Zap size={14} /> Start Discovery Scan
        </button>
      </div>

      {/* A. KPI CARDS GRID */}
      <div className="kpi-grid">
        <div className="kpi-card">
          <div className="kpi-header">
            <span className="kpi-label">TOTAL DEVICES</span>
            <span className="kpi-icon blue"><Monitor size={18} /></span>
          </div>
          <div className="kpi-value mono">{total > 0 ? total : 0}</div>
          <div className="kpi-sub">Discovered on subnet</div>
        </div>

        <div className="kpi-card green-accent">
          <div className="kpi-header">
            <span className="kpi-label">ONLINE</span>
            <span className="kpi-icon green"><Zap size={18} /></span>
          </div>
          <div className="kpi-value green-text mono">{online}</div>
          <div className="kpi-sub">{total > 0 ? `${Math.round((online / total) * 100)}% Reachable` : 'Reachable endpoints'}</div>
        </div>

        <div className="kpi-card red-accent">
          <div className="kpi-header">
            <span className="kpi-label">OFFLINE</span>
            <span className="kpi-icon red"><AlertTriangle size={18} /></span>
          </div>
          <div className="kpi-value red-text mono">{offline}</div>
          <div className="kpi-sub">Unreachable endpoints</div>
        </div>

        <div className="kpi-card purple-accent">
          <div className="kpi-header">
            <span className="kpi-label">AVG LATENCY</span>
            <span className="kpi-icon purple"><Clock size={18} /></span>
          </div>
          <div className="kpi-value mono">{avgLatency !== null ? `${avgLatency} ms` : 'N/A'}</div>
          <div className="kpi-sub">Across reachable hosts</div>
        </div>

        <div className="kpi-card amber-accent">
          <div className="kpi-header">
            <span className="kpi-label">ACTIVE ALERTS</span>
            <span className="kpi-icon amber"><AlertOctagon size={18} /></span>
          </div>
          <div className="kpi-value amber-text mono">{activeAlertsCount}</div>
          <div className="kpi-sub">Unresolved system alarms</div>
        </div>
      </div>

      {/* B. WHAT'S CHANGED CARD & RECENT EVENTS */}
      <div className="grid-layout-2" style={{ marginBottom: '24px' }}>
        {/* WHAT'S CHANGED CARD */}
        <div className="card">
          <div className="card-header">
            <div>
              <h3 className="card-title">WHAT'S CHANGED</h3>
              <div className="card-sub">Recorded network events & port exposure detections</div>
            </div>
            <button className="btn btn-secondary btn-sm" onClick={onViewAllChanges}>
              VIEW ALL CHANGES <ArrowRight size={14} />
            </button>
          </div>

          <div className="changes-list">
            <div className="change-item">
              <span className="change-icon blue">+</span>
              <div className="change-details">
                <span className="change-val mono">{newDevicesCount}</span>
                <span className="change-lbl">New Devices Discovered</span>
              </div>
            </div>

            <div className="change-item">
              <span className="change-icon amber">⚡</span>
              <div className="change-details">
                <span className="change-val mono">{portChangesCount}</span>
                <span className="change-lbl">Port Exposure Changes</span>
              </div>
            </div>

            <div className="change-item">
              <span className="change-icon green">●</span>
              <div className="change-details">
                <span className="change-val mono">{onlineEventsCount}</span>
                <span className="change-lbl">Devices Came Online</span>
              </div>
            </div>

            <div className="change-item">
              <span className="change-icon red">●</span>
              <div className="change-details">
                <span className="change-val mono">{offlineEventsCount}</span>
                <span className="change-lbl">Devices Went Offline</span>
              </div>
            </div>

            <div className="change-item">
              <span className="change-icon cyan">↻</span>
              <div className="change-details">
                <span className="change-val mono">{identityChangesCount}</span>
                <span className="change-lbl">Device Identity Changes</span>
              </div>
            </div>
          </div>
        </div>

        {/* RECENT EVENTS FEED */}
        <div className="card">
          <div className="card-header">
            <div>
              <h3 className="card-title">RECENT EVENTS</h3>
              <div className="card-sub">Latest reachability & state log stream</div>
            </div>
          </div>

          <div className="activity-feed">
            {events.length === 0 ? (
              <EmptyState 
                title="No Recent Events" 
                description="No network status or discovery events recorded yet."
              />
            ) : (
              events.slice(0, 6).map((item) => (
                <div key={item.id} className="feed-item">
                  <div className="feed-icon blue">
                    <Shield size={14} />
                  </div>
                  <div className="feed-content">
                    <div className="feed-header">
                      <span className="feed-title">{item.eventType || 'NETWORK_EVENT'}</span>
                      <span className="feed-time mono">
                        {item.eventTime ? new Date(item.eventTime).toLocaleTimeString() : ''}
                      </span>
                    </div>
                    <div className="feed-sub mono">
                      {item.deviceIp ? `${item.deviceIp} — ` : ''}{item.message}
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      {/* C. REAL LATENCY CHART */}
      <div className="card" style={{ marginBottom: '24px' }}>
        <div className="card-header">
          <div>
            <h3 className="card-title">LATENCY & PACKET LOSS TELEMETRY</h3>
            <div className="card-sub">Real-time ping response time (ms) history</div>
          </div>
          <div className="btn-group">
            {['1H', '6H', '24H', '7D'].map((range) => (
              <button
                key={range}
                className={`btn btn-xs ${timeRange === range ? 'btn-primary' : 'btn-secondary'}`}
                onClick={() => setTimeRange(range)}
              >
                {range}
              </button>
            ))}
          </div>
        </div>

        {chartData.length === 0 ? (
          <div className="empty-chart-notice">
            <span>No historical telemetry available. Run ping checks or enable background monitoring.</span>
          </div>
        ) : (
          <div style={{ width: '100%', height: 200 }}>
            <ResponsiveContainer>
              <AreaChart data={chartData}>
                <defs>
                  <linearGradient id="latencyGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#3B82F6" stopOpacity={0.5} />
                    <stop offset="95%" stopColor="#3B82F6" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <XAxis dataKey="time" stroke="#64748b" fontSize={11} />
                <YAxis stroke="#64748b" fontSize={11} unit="ms" />
                <Tooltip 
                  contentStyle={{ background: '#101722', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '8px', color: '#F1F5F9' }} 
                />
                <Area type="monotone" dataKey="latency" stroke="#3B82F6" strokeWidth={2} fillOpacity={1} fill="url(#latencyGradient)" name="Latency (ms)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>

      {/* D. DISCOVERED NETWORK MAP */}
      <div className="card">
        <div className="card-header">
          <div>
            <h3 className="card-title">DISCOVERED NETWORK MAP</h3>
            <div className="card-sub">Visual node layout of active endpoints on subnet</div>
          </div>
          <button className="btn btn-secondary btn-sm" onClick={onRefresh}>
            <RefreshCw size={14} /> Refresh Nodes
          </button>
        </div>

        {devices.length === 0 ? (
          <EmptyState 
            title="No Devices Discovered Yet"
            description="Start network discovery scan to populate endpoints."
            actionLabel="Start Discovery"
            onAction={onScanClick}
          />
        ) : (
          <div className="network-map-grid">
            {devices.map((d) => {
              const isOnline = d.status === 'ONLINE';
              const isGateway = (d.ipAddress && d.ipAddress.endsWith('.1')) || d.deviceType === 'ROUTER';

              return (
                <div
                  key={d.id}
                  className={`map-node-card ${isGateway ? 'gateway' : ''}`}
                  onClick={() => onDeviceClick(d.id)}
                >
                  <div className="node-card-top">
                    <span className={`status-dot-sm ${isOnline ? 'green' : 'red'}`} />
                    <span className="node-type-badge mono">{d.deviceType || 'UNKNOWN'}</span>
                  </div>
                  <div className="node-card-title">{d.name || d.hostname || d.ipAddress}</div>
                  <div className="node-card-ip mono">{d.ipAddress}</div>
                  <div className="node-card-meta">
                    <span className="mono">{d._latency !== undefined && d._latency !== null ? `${d._latency}ms` : 'N/A'}</span>
                    <span>{d.vendor || 'Generic'}</span>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </section>
  );
}
