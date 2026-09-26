import React, { useState } from 'react';
import {
  AlertTriangle,
  AlertOctagon,
  Info,
  CheckCircle,
  Radio,
  Clock,
  Shield,
  ShieldAlert,
  Monitor,
  Check
} from 'lucide-react';
import EmptyState from '../components/EmptyState';

export default function AlertsView({
  alerts = [],
  events = [],
  onResolveAlert
}) {
  const [activeTab, setActiveTab] = useState('alerts');
  const [includeResolved, setIncludeResolved] = useState(false);

  const criticalCount = alerts.filter(a => a.severity === 'CRITICAL' && !a.isResolved && !a.resolved).length;
  const warningCount = alerts.filter(a => a.severity === 'WARNING' && !a.isResolved && !a.resolved).length;
  const infoCount = alerts.filter(a => a.severity === 'INFO' && !a.isResolved && !a.resolved).length;
  const unresolvedCount = alerts.filter(a => !a.isResolved && !a.resolved).length;

  const displayAlerts = alerts.filter(a => includeResolved || (!a.isResolved && !a.resolved));

  return (
    <section className="view-panel active">
      <div className="section-hero">
        <div>
          <h2 className="section-title">ALERT CENTER & NETWORK CHANGE TIMELINE</h2>
          <p className="section-desc">Real-time state alarms, severity tracking, port exposure notifications, and event timeline history.</p>
        </div>
      </div>

      {/* KPI SUMMARY CARDS */}
      <div className="kpi-grid" style={{ marginBottom: '24px' }}>
        <div className="kpi-card red-accent">
          <div className="kpi-header">
            <span className="kpi-label">CRITICAL ALARMS</span>
            <span className="kpi-icon red"><AlertOctagon size={18} /></span>
          </div>
          <div className="kpi-value red-text mono">{criticalCount}</div>
          <div className="kpi-sub">Immediate action required</div>
        </div>

        <div className="kpi-card amber-accent">
          <div className="kpi-header">
            <span className="kpi-label">WARNING ALERTS</span>
            <span className="kpi-icon amber"><AlertTriangle size={18} /></span>
          </div>
          <div className="kpi-value amber-text mono">{warningCount}</div>
          <div className="kpi-sub">Threshold warnings</div>
        </div>

        <div className="kpi-card blue-accent">
          <div className="kpi-header">
            <span className="kpi-label">INFO NOTIFICATIONS</span>
            <span className="kpi-icon blue"><Info size={18} /></span>
          </div>
          <div className="kpi-value blue-text mono">{infoCount}</div>
          <div className="kpi-sub">Informational events</div>
        </div>

        <div className="kpi-card purple-accent">
          <div className="kpi-header">
            <span className="kpi-label">UNRESOLVED ALERTS</span>
            <span className="kpi-icon purple"><Clock size={18} /></span>
          </div>
          <div className="kpi-value mono">{unresolvedCount}</div>
          <div className="kpi-sub">Active in queue</div>
        </div>
      </div>

      {/* TAB NAVIGATION */}
      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <div className="card-header" style={{ padding: '16px 20px', borderBottom: '1px solid var(--border-color)' }}>
          <div className="btn-group">
            <button
              className={`btn btn-sm ${activeTab === 'alerts' ? 'btn-primary' : 'btn-secondary'}`}
              onClick={() => setActiveTab('alerts')}
            >
              ACTIVE & HISTORICAL ALERTS
            </button>
            <button
              className={`btn btn-sm ${activeTab === 'timeline' ? 'btn-primary' : 'btn-secondary'}`}
              onClick={() => setActiveTab('timeline')}
            >
              NETWORK CHANGE TIMELINE
            </button>
          </div>

          {activeTab === 'alerts' && (
            <label className="checkbox-label mono text-muted" style={{ fontSize: '12px', cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={includeResolved}
                onChange={(e) => setIncludeResolved(e.target.checked)}
              />
              Show Resolved Alarms
            </label>
          )}
        </div>

        {/* TAB 1: ALERTS TABLE */}
        {activeTab === 'alerts' && (
          <div className="table-responsive">
            {displayAlerts.length === 0 ? (
              <EmptyState
                title="No Active Alerts"
                description="System network status is operating cleanly with zero unresolved alarms."
                icon={CheckCircle}
              />
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>SEVERITY</th>
                    <th>DEVICE IP / NAME</th>
                    <th>ALERT TYPE</th>
                    <th>MESSAGE</th>
                    <th>CREATED AT</th>
                    <th>STATUS</th>
                    <th style={{ textAlign: 'right' }}>ACTION</th>
                  </tr>
                </thead>
                <tbody>
                  {displayAlerts.map((alert) => {
                    const isResolved = alert.isResolved || alert.resolved;
                    const isCritical = alert.severity === 'CRITICAL';
                    const isWarning = alert.severity === 'WARNING';

                    return (
                      <tr key={alert.id}>
                        <td>
                          <span className={`badge ${isCritical ? 'badge-danger' : isWarning ? 'badge-warning' : 'badge-subtle'}`}>
                            {alert.severity || 'INFO'}
                          </span>
                        </td>
                        <td className="mono font-semibold">
                          {alert.deviceIp || alert.deviceName || `Device #${alert.deviceId}`}
                        </td>
                        <td>
                          <span className="badge badge-subtle">{alert.alertType || 'GENERAL'}</span>
                        </td>
                        <td className="text-muted">{alert.message}</td>
                        <td className="mono text-muted">
                          {alert.createdAt ? new Date(alert.createdAt).toLocaleString() : 'N/A'}
                        </td>
                        <td>
                          <span className={`status-badge ${isResolved ? 'open' : 'closed'}`}>
                            {isResolved ? 'RESOLVED' : 'ACTIVE'}
                          </span>
                        </td>
                        <td style={{ textAlign: 'right' }}>
                          {!isResolved ? (
                            <button
                              className="btn btn-secondary btn-xs"
                              onClick={() => onResolveAlert(alert.id)}
                            >
                              <Check size={12} /> Resolve
                            </button>
                          ) : (
                            <span className="mono text-muted" style={{ fontSize: '11px' }}>
                              {alert.resolvedAt ? new Date(alert.resolvedAt).toLocaleTimeString() : 'Done'}
                            </span>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            )}
          </div>
        )}

        {/* TAB 2: NETWORK CHANGE TIMELINE */}
        {activeTab === 'timeline' && (
          <div style={{ padding: '24px' }}>
            {events.length === 0 ? (
              <EmptyState
                title="No Network Changes Recorded"
                description="Network discovery and reachability monitor have not logged state shifts yet."
              />
            ) : (
              <div className="timeline-container">
                {events.map((event) => {
                  const isNewDevice = event.eventType === 'DEVICE_DISCOVERED';
                  const isPortChange = event.eventType === 'PORT_CHANGE' || (event.message && event.message.toLowerCase().includes('port'));
                  const isStatusChange = event.eventType === 'STATUS_CHANGE';

                  return (
                    <div key={event.id} className="timeline-item">
                      <div className={`timeline-marker ${isNewDevice ? 'blue' : isPortChange ? 'amber' : 'green'}`} />
                      <div className="timeline-content">
                        <div className="timeline-header">
                          <span className="timeline-title font-semibold">
                            {isNewDevice ? '● NEW DEVICE DISCOVERED' : isPortChange ? '⚠ PORT EXPOSURE CHANGE' : isStatusChange ? '↻ STATUS CHANGE' : event.eventType}
                          </span>
                          <span className="timeline-time mono text-muted">
                            {event.eventTime ? new Date(event.eventTime).toLocaleString() : ''}
                          </span>
                        </div>
                        <div className="timeline-device mono font-semibold text-primary">
                          {event.deviceIp} {event.deviceName ? `(${event.deviceName})` : ''}
                        </div>
                        <div className="timeline-msg text-muted">{event.message}</div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        )}
      </div>
    </section>
  );
}
