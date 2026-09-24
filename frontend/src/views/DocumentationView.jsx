import React from 'react';

export default function DocumentationView() {
  return (
    <section className="view-panel active">
      <div className="section-hero">
        <div>
          <h2 className="section-title">DOCUMENTATION & HELP</h2>
          <p className="section-desc">Feature guide, discovery mechanics, and technical specifications.</p>
        </div>
      </div>

      <div className="card" style={{ marginBottom: '24px' }}>
        <h3 className="card-title" style={{ marginBottom: '12px' }}>🚀 Getting Started Workflow</h3>
        <ol style={{ marginLeft: '20px', lineHeight: '1.8', color: 'var(--text-muted)', fontSize: '14px' }}>
          <li><strong>Interface Detection:</strong> Upon launch, NetScope queries attached IPv4 network adapters (`wlan0`, `eth0`).</li>
          <li><strong>CIDR Calculation:</strong> The system automatically derives local subnet ranges (e.g. `192.168.1.0/24`).</li>
          <li><strong>Subnet Range Scanning:</strong> Active hosts are discovered using multi-threaded ICMP Ping or Nmap probes.</li>
          <li><strong>Device Onboarding:</strong> Discovered endpoints are imported into PostgreSQL inventory.</li>
          <li><strong>Continuous Telemetry:</strong> Background worker pool polls endpoints every 10 seconds for latency and reachability state changes.</li>
        </ol>
      </div>

      <div className="card">
        <h3 className="card-title" style={{ marginBottom: '12px' }}>🛡️ Technical Honesty & Limitations</h3>
        <p style={{ lineHeight: '1.6', color: 'var(--text-muted)', fontSize: '14px', marginBottom: '12px' }}>
          NetScope adheres strictly to real network data integrity. Remote endpoints on an IPv4 network do not reveal CPU usage, RAM consumption, or OS details unless explicitly configured to expose them via supported protocols like SNMP or Nmap.
        </p>
        <p style={{ lineHeight: '1.6', color: 'var(--text-subtle)', fontSize: '13px' }}>
          When telemetry is unexposed by a target host, NetScope reports the value as <strong>N/A</strong> rather than fabricating fake percentages or sample data.
        </p>
      </div>
    </section>
  );
}
