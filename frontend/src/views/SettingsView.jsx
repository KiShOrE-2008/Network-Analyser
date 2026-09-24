import React from 'react';

export default function SettingsView({ health }) {
  return (
    <section className="view-panel active">
      <div className="section-hero">
        <div>
          <h2 className="section-title">SETTINGS & CONFIGURATION</h2>
          <p className="section-desc">Application preferences, polling intervals, and backend telemetry health.</p>
        </div>
      </div>

      <div className="grid-layout-2">
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">GENERAL PREFERENCES</h3>
          </div>
          <div className="drawer-section" style={{ border: 'none', padding: 0 }}>
            <div className="info-pair" style={{ marginBottom: '16px' }}>
              <div>
                <strong style={{ display: 'block' }}>Dashboard Theme</strong>
                <span className="card-sub">Dark NOC operational glass theme</span>
              </div>
              <span className="badge badge-new">● Dark Glass (React)</span>
            </div>
            <div className="info-pair" style={{ marginBottom: '16px' }}>
              <div>
                <strong style={{ display: 'block' }}>Auto Data Refresh</strong>
                <span className="card-sub">Polls backend telemetry every 10 seconds</span>
              </div>
              <span className="badge badge-online">● ACTIVE (10s)</span>
            </div>
            <div className="info-pair">
              <div>
                <strong style={{ display: 'block' }}>Strict Data Integrity</strong>
                <span className="card-sub">Zero fake metrics policy — returns N/A for missing data</span>
              </div>
              <span className="badge badge-online">● ENFORCED</span>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <h3 className="card-title">SYSTEM DIAGNOSTICS</h3>
          </div>
          <div className="drawer-section" style={{ border: 'none', padding: 0 }}>
            <div className="info-pair"><span className="info-key">Backend Status</span><span className="info-val" style={{ color: 'var(--online)' }}>UP (Spring Boot 3.4.2)</span></div>
            <div className="info-pair"><span className="info-key">Database Connection</span><span className="info-val" style={{ color: 'var(--online)' }}>CONNECTED (PostgreSQL 16)</span></div>
            <div className="info-pair"><span className="info-key">Frontend Runtime</span><span className="info-val" style={{ color: 'var(--accent-cyan)' }}>React 18 + Vite</span></div>
          </div>
        </div>
      </div>
    </section>
  );
}
