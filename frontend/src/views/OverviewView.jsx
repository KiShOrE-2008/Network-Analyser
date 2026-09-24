import React from 'react';
import { Monitor, Zap, AlertTriangle, Clock, RefreshCw, Shield, AlertOctagon } from 'lucide-react';
import { AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';

export default function OverviewView({
  devices,
  events,
  alerts,
  onScanClick,
  onDeviceClick,
  onRefresh
}) {
  const total = devices.length;
  const online = devices.filter(d => d.status === 'ONLINE').length;
  const offline = devices.filter(d => d.status === 'OFFLINE').length;
  const reachablePct = total > 0 ? Math.round((online / total) * 100) : 0;

  const validLatencies = devices
    .map(d => d._latency)
    .filter(l => typeof l === 'number' && !isNaN(l));

  const avgLatency = validLatencies.length
    ? (validLatencies.reduce((a, b) => a + b, 0) / validLatencies.length).toFixed(1)
    : null;

  // Mock sample series for Recharts sparkline graph
  const chartData = [
    { time: '10:00', latency: 12 },
    { time: '10:05', latency: 15 },
    { time: '10:10', latency: 9 },
    { time: '10:15', latency: 22 },
    { time: '10:20', latency: 14 },
    { time: '10:25', latency: avgLatency ? Number(avgLatency) : 11 },
  ];

  // Combined Activity items
  const combinedActivity = [
    ...(events || []).map(e => ({
      id: `event-${e.id}`,
      title: e.eventType || 'Network Event',
      sub: `${e.ipAddress || ''} — ${e.message || ''}`,
      time: e.eventTime ? new Date(e.eventTime).toLocaleTimeString() : 'Just now',
      icon: Shield,
      color: 'var(--accent-cyan)',
      bg: 'rgba(6, 182, 212, 0.15)'
    })),
    ...(alerts || []).map(a => ({
      id: `alert-${a.id}`,
      title: a.alertType || 'System Alert',
      sub: `${a.deviceName || a.ipAddress || ''} — ${a.message || ''}`,
      time: a.timestamp ? new Date(a.timestamp).toLocaleTimeString() : 'Just now',
      icon: AlertOctagon,
      color: a.severity === 'CRITICAL' ? 'var(--offline)' : 'var(--warning)',
      bg: a.severity === 'CRITICAL' ? 'rgba(244, 63, 94, 0.15)' : 'rgba(245, 158, 11, 0.15)'
    }))
  ].slice(0, 8);

  return (
    <section className="view-panel active">
      <div className="section-hero">
        <div>
          <h2 className="section-title">NETWORK OVERVIEW</h2>
          <p className="section-desc">Real-time visibility into discovered devices and network health.</p>
        </div>
        <button className="btn btn-primary btn-sm" onClick={onScanClick}>
          ⚡ Scan Network
        </button>
      </div>

      {/* KPI CARDS GRID */}
      <div className="kpi-grid">
        <div className="kpi-card">
          <div className="kpi-header">
            <span className="kpi-label">TOTAL DEVICES</span>
            <span className="kpi-icon blue"><Monitor size={20} /></span>
          </div>
          <div className="kpi-value">{total > 0 ? total : 'N/A'}</div>
          <div className="kpi-sub">Discovered on local network</div>
        </div>

        <div className="kpi-card green-accent">
          <div className="kpi-header">
            <span className="kpi-label">ONLINE</span>
            <span class="kpi-icon green"><Zap size={20} /></span>
          </div>
          <div className="kpi-value green-text">{total > 0 ? online : 'N/A'}</div>
          <div className="kpi-sub">{total > 0 ? `${reachablePct}% Reachable endpoints` : 'Reachable endpoints'}</div>
        </div>

        <div className="kpi-card red-accent">
          <div className="kpi-header">
            <span className="kpi-label">OFFLINE</span>
            <span className="kpi-icon red"><AlertTriangle size={20} /></span>
          </div>
          <div className="kpi-value red-text">{total > 0 ? offline : 'N/A'}</div>
          <div className="kpi-sub">Requires attention</div>
        </div>

        <div className="kpi-card purple-accent">
          <div className="kpi-header">
            <span className="kpi-label">AVERAGE LATENCY</span>
            <span className="kpi-icon purple"><Clock size={20} /></span>
          </div>
          <div className="kpi-value">{avgLatency !== null ? `${avgLatency} ms` : 'N/A'}</div>
          <div className="kpi-sub">Across reachable devices</div>
        </div>
      </div>

      {/* LIVE LATENCY SPARKLINE GRAPH CARD */}
      <div className="card" style={{ marginBottom: '24px' }}>
        <div className="card-header">
          <div>
            <h3 className="card-title">REAL-TIME LATENCY TELEMETRY</h3>
            <div className="card-sub">Aggregated network ping response times (ms)</div>
          </div>
        </div>
        <div style={{ width: '100%', height: 180 }}>
          <ResponsiveContainer>
            <AreaChart data={chartData}>
              <defs>
                <linearGradient id="latencyGradient" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#6366f1" stopOpacity={0.6}/>
                  <stop offset="95%" stopColor="#6366f1" stopOpacity={0}/>
                </linearGradient>
              </defs>
              <XAxis dataKey="time" stroke="#64748b" fontSize={11} />
              <YAxis stroke="#64748b" fontSize={11} />
              <Tooltip contentStyle={{ background: '#0f172a', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '8px', color: '#fff' }} />
              <Area type="monotone" dataKey="latency" stroke="#6366f1" strokeWidth={2} fillOpacity={1} fill="url(#latencyGradient)" />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* SPLIT LAYOUT: DEVICE MAP + RECENT ACTIVITY */}
      <div className="grid-layout-2">
        {/* NETWORK TOPOLOGY MAP */}
        <div className="card">
          <div className="card-header">
            <div>
              <h3 className="card-title">DISCOVERED DEVICES MAP</h3>
              <div className="card-sub">Visual layout of active endpoints on subnet</div>
            </div>
            <button className="btn btn-secondary btn-sm" onClick={onRefresh}>
              <RefreshCw size={14} /> Refresh Map
            </button>
          </div>

          <div className="network-map-container">
            {devices.length === 0 ? (
              <div className="empty-state">No devices registered. Click "Scan Network" to discover endpoints.</div>
            ) : (
              devices.map(d => {
                const isOnline = d.status === 'ONLINE';
                const isGateway = (d.ipAddress && d.ipAddress.endsWith('.1')) || d.deviceType === 'ROUTER';

                return (
                  <div
                    key={d.id}
                    className={`map-node ${isGateway ? 'gateway' : ''}`}
                    onClick={() => onDeviceClick(d.id)}
                  >
                    <span className="map-node-icon"><Monitor size={20} /></span>
                    <div className="map-node-info">
                      <div className="map-node-name">{d.name}</div>
                      <div className="map-node-ip mono">
                        <span className={`status-dot ${isOnline ? 'online' : 'offline'}`}></span>
                        {d.ipAddress}
                      </div>
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </div>

        {/* RECENT ACTIVITY FEED */}
        <div className="card">
          <div className="card-header">
            <div>
              <h3 className="card-title">RECENT ACTIVITY</h3>
              <div className="card-sub">Latest reachability state events & alerts</div>
            </div>
          </div>

          <div className="activity-feed">
            {combinedActivity.length === 0 ? (
              <div className="empty-state">No recent activity events recorded.</div>
            ) : (
              combinedActivity.map(item => {
                const IconComp = item.icon;
                return (
                  <div key={item.id} className="feed-item">
                    <div className="feed-icon" style={{ background: item.bg, color: item.color }}>
                      <IconComp size={16} />
                    </div>
                    <div className="feed-content">
                      <div className="feed-header">
                        <span className="feed-title">{item.title}</span>
                        <span className="feed-time">{item.time}</span>
                      </div>
                      <div className="feed-sub">{item.sub}</div>
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </div>
      </div>
    </section>
  );
}
