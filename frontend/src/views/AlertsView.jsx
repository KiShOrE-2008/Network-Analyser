import React, { useState } from 'react';
import { AlertTriangle } from 'lucide-react';

export default function AlertsView({ alerts }) {
  const [filter, setFilter] = useState('ALL');

  const filteredAlerts = (alerts || []).filter(a => {
    if (filter === 'ACTIVE') return !a.resolved;
    if (filter === 'RESOLVED') return a.resolved;
    return true;
  });

  return (
    <section className="view-panel active">
      <div className="section-hero">
        <div>
          <h2 className="section-title">ALERT CENTER</h2>
          <p className="section-desc">System notifications, host state transitions, and reachability alarms.</p>
        </div>
      </div>

      <div className="toolbar">
        <div className="filter-group">
          <button
            className={`btn btn-secondary btn-sm ${filter === 'ALL' ? 'active-filter' : ''}`}
            onClick={() => setFilter('ALL')}
          >
            All Alerts ({alerts?.length || 0})
          </button>
          <button
            className={`btn btn-secondary btn-sm ${filter === 'ACTIVE' ? 'active-filter' : ''}`}
            onClick={() => setFilter('ACTIVE')}
          >
            Active Alarms ({(alerts || []).filter(a => !a.resolved).length})
          </button>
          <button
            className={`btn btn-secondary btn-sm ${filter === 'RESOLVED' ? 'active-filter' : ''}`}
            onClick={() => setFilter('RESOLVED')}
          >
            Resolved ({(alerts || []).filter(a => a.resolved).length})
          </button>
        </div>
      </div>

      <div className="card">
        <div className="activity-feed">
          {filteredAlerts.length === 0 ? (
            <div className="empty-state">No alerts match the selected filter.</div>
          ) : (
            filteredAlerts.map((a, idx) => {
              const isCritical = a.severity === 'CRITICAL';
              return (
                <div key={a.id || idx} className="feed-item">
                  <div
                    className="feed-icon"
                    style={{
                      background: isCritical ? 'rgba(244, 63, 94, 0.15)' : 'rgba(245, 158, 11, 0.15)',
                      color: isCritical ? 'var(--offline)' : 'var(--warning)'
                    }}
                  >
                    <AlertTriangle size={16} />
                  </div>
                  <div className="feed-content">
                    <div className="feed-header">
                      <span className="feed-title">{a.alertType || 'System Alarm'}</span>
                      <span className="feed-time">
                        {a.timestamp ? new Date(a.timestamp).toLocaleString() : 'Recent'}
                      </span>
                    </div>
                    <div className="feed-sub">
                      <strong>{a.deviceName || a.ipAddress || 'Device'}</strong>: {a.message || 'State change detected'}
                    </div>
                  </div>
                  <div>
                    <span className={`badge ${a.resolved ? 'badge-online' : (isCritical ? 'badge-offline' : 'badge-warning')}`}>
                      {a.resolved ? 'RESOLVED' : (a.severity || 'CRITICAL')}
                    </span>
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>
    </section>
  );
}
