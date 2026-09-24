import React from 'react';
import { Download } from 'lucide-react';

export default function ReportsView({ devices, alerts }) {
  const handleExportCsv = () => {
    window.open('/api/reports/export/devices/csv', '_blank');
  };

  const total = devices.length;
  const online = devices.filter(d => d.status === 'ONLINE').length;
  const offline = devices.filter(d => d.status === 'OFFLINE').length;
  const slaPct = total > 0 ? Math.round((online / total) * 1000) / 10 : 100;

  return (
    <section className="view-panel active">
      <div className="section-hero">
        <div>
          <h2 className="section-title">REPORTS & EXPORT</h2>
          <p className="section-desc">Network telemetry summary metrics and device inventory export.</p>
        </div>
        <button className="btn btn-primary btn-sm" onClick={handleExportCsv}>
          <Download size={14} /> Export CSV
        </button>
      </div>

      <div className="card" style={{ marginBottom: '24px' }}>
        <div className="card-header">
          <h3 className="card-title">SYSTEM SUMMARY REPORT</h3>
        </div>
        <div className="report-summary-grid">
          <div className="stat-box">
            <div className="stat-box-title">TOTAL REGISTERED HOSTS</div>
            <div className="stat-box-val">{total}</div>
          </div>
          <div className="stat-box">
            <div className="stat-box-title">ONLINE ENDPOINTS</div>
            <div className="stat-box-val" style={{ color: 'var(--online)' }}>{online}</div>
          </div>
          <div className="stat-box">
            <div className="stat-box-title">OFFLINE / UNREACHABLE</div>
            <div className="stat-box-val" style={{ color: 'var(--offline)' }}>{offline}</div>
          </div>
          <div className="stat-box">
            <div className="stat-box-title">SLA AVAILABILITY %</div>
            <div className="stat-box-val" style={{ color: 'var(--accent-cyan)' }}>{slaPct}%</div>
          </div>
        </div>
      </div>
    </section>
  );
}
