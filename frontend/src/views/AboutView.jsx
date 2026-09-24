import React from 'react';

export default function AboutView() {
  return (
    <section className="view-panel active">
      <div className="section-hero">
        <div>
          <h2 className="section-title">ABOUT NETSCOPE</h2>
          <p className="section-desc">Enterprise Network Discovery & Telemetry Platform.</p>
        </div>
      </div>

      <div className="card" style={{ maxWidth: '640px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '20px' }}>
          <div className="brand-logo" style={{ width: '54px', height: '54px', fontSize: '26px' }}>N</div>
          <div>
            <h3 style={{ fontSize: '22px', fontWeight: '800' }}>NetScope Platform</h3>
            <div className="card-sub">Version 2.0.0-REACT (Enterprise React Edition)</div>
          </div>
        </div>
        <div className="drawer-section" style={{ border: 'none', padding: 0 }}>
          <div className="info-pair"><span className="info-key">Frontend Framework</span><span className="info-val">React 18 / Vite / Lucide / Recharts</span></div>
          <div className="info-pair"><span className="info-key">Backend Runtime</span><span className="info-val">Java 21 / Spring Boot 3.4.2</span></div>
          <div className="info-pair"><span className="info-key">Database Engine</span><span className="info-val">PostgreSQL 16</span></div>
          <div className="info-pair"><span className="info-key">Discovery Engine</span><span className="info-val">ICMP, TCP, Nmap, SNMP4J</span></div>
          <div className="info-pair"><span className="info-key">License</span><span className="info-val">MIT Open Source License</span></div>
        </div>
      </div>
    </section>
  );
}
