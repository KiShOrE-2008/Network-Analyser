import React from 'react';
import { ArrowRight, Radio, Activity, AlertTriangle, ShieldCheck, Cpu, Database, Server, Terminal } from 'lucide-react';

export default function LandingView({ onContinue }) {
  return (
    <div className="landing-container">
      {/* HERO HEADER */}
      <div className="landing-hero">
        <div className="landing-badge">
          <span className="status-dot green"></span>
          <span>NetScope Telemetry Platform</span>
        </div>
        <h1 className="landing-title">Enterprise Network Discovery & Real-Time Telemetry Console</h1>
        <p className="landing-subtitle">
          Comprehensive IPv4 subnet scanning, ICMP/TCP reachability monitoring, multi-threaded worker pools, and automated alert telemetry designed for network administrators.
        </p>

        <div style={{ display: 'flex', gap: '16px', marginTop: '12px', flexWrap: 'wrap', justifyContent: 'center' }}>
          <button className="btn-continue-lg" onClick={onContinue}>
            Continue to Monitoring Console <ArrowRight size={18} />
          </button>
          <button
            className="btn btn-secondary"
            onClick={() => {
              const elem = document.getElementById('techStackSection');
              if (elem) elem.scrollIntoView({ behavior: 'smooth' });
            }}
          >
            Explore Architecture
          </button>
        </div>
      </div>

      {/* CORE CAPABILITIES GRID */}
      <div>
        <h2 className="section-title" style={{ textAlign: 'center', marginBottom: '24px' }}>
          CORE SYSTEM CAPABILITIES
        </h2>
        <div className="kpi-grid" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))' }}>
          <div className="kpi-card">
            <div className="kpi-header">
              <span className="kpi-label">AUTOMATED DISCOVERY</span>
              <span className="kpi-icon blue"><Radio size={20} /></span>
            </div>
            <h3 style={{ fontSize: '16px', marginBottom: '8px', color: '#fff' }}>Subnet Range Scanner</h3>
            <p style={{ fontSize: '13px', color: 'var(--text-muted)', lineHeight: '1.5' }}>
              Automatically detects local IPv4 interfaces (`wlan0`, `eth0`), computes CIDR ranges, and executes multi-threaded ICMP Ping & TCP port probes.
            </p>
          </div>

          <div className="kpi-card green-accent">
            <div className="kpi-header">
              <span className="kpi-label">TELEMETRY POLLING</span>
              <span className="kpi-icon green"><Activity size={20} /></span>
            </div>
            <h3 style={{ fontSize: '16px', marginBottom: '8px', color: '#fff' }}>10s Polling Scheduler</h3>
            <p style={{ fontSize: '13px', color: 'var(--text-muted)', lineHeight: '1.5' }}>
              Background scheduled threads continuously check endpoint latency, reachability status, and record high-precision metric telemetry.
            </p>
          </div>

          <div className="kpi-card red-accent">
            <div className="kpi-header">
              <span className="kpi-label">ALERT ENGINE</span>
              <span className="kpi-icon red"><AlertTriangle size={20} /></span>
            </div>
            <h3 style={{ fontSize: '16px', marginBottom: '8px', color: '#fff' }}>State Alarm Dispatcher</h3>
            <p style={{ fontSize: '13px', color: 'var(--text-muted)', lineHeight: '1.5' }}>
              Instantly detects host transitions from Healthy to Critical/Offline, raising system alarms and tracking SLA availability metrics.
            </p>
          </div>

          <div className="kpi-card purple-accent">
            <div className="kpi-header">
              <span className="kpi-label">SNMP & PORTS</span>
              <span className="kpi-icon purple"><ShieldCheck size={20} /></span>
            </div>
            <h3 style={{ fontSize: '16px', marginBottom: '8px', color: '#fff' }}>Deep Host Inspector</h3>
            <p style={{ fontSize: '13px', color: 'var(--text-muted)', lineHeight: '1.5' }}>
              Queries target endpoints for active TCP ports, OS hints, MAC addresses, vendor identification, and SNMP system uptime metrics.
            </p>
          </div>
        </div>
      </div>

      {/* TECH STACK & SYSTEM SPECS */}
      <div className="card" id="techStackSection">
        <div className="card-header">
          <div>
            <h3 className="card-title">PROJECT ARCHITECTURE & TECH STACK</h3>
            <div className="card-sub">Enterprise technology stack powering backend runtime and React telemetry frontend</div>
          </div>
        </div>

        <div className="scheduler-details-grid">
          <div className="stat-box">
            <div className="stat-box-title">BACKEND RUNTIME</div>
            <div className="stat-box-val" style={{ fontSize: '18px', color: 'var(--accent-cyan)' }}>Java 21 LTS</div>
            <div className="card-sub" style={{ marginTop: '4px' }}>Spring Boot 3.4.2, Virtual Threads</div>
          </div>

          <div className="stat-box">
            <div className="stat-box-title">PERSISTENCE LAYER</div>
            <div className="stat-box-val" style={{ fontSize: '18px', color: 'var(--accent-emerald)' }}>PostgreSQL 16</div>
            <div className="card-sub" style={{ marginTop: '4px' }}>Spring Data JPA & Flyway Migrations</div>
          </div>

          <div className="stat-box">
            <div className="stat-box-title">DISCOVERY ENGINES</div>
            <div className="stat-box-val" style={{ fontSize: '18px', color: 'var(--accent-purple)' }}>ICMP / Nmap / SNMP</div>
            <div className="card-sub" style={{ marginTop: '4px' }}>Multi-threaded Executor Service</div>
          </div>

          <div className="stat-box">
            <div className="stat-box-title">CONSOLE FRONTEND</div>
            <div className="stat-box-val" style={{ fontSize: '18px', color: 'var(--primary)' }}>React 18 + Vite</div>
            <div className="card-sub" style={{ marginTop: '4px' }}>Lucide Icons, Recharts & Glassmorphism</div>
          </div>
        </div>
      </div>

      {/* BOTTOM CALL TO ACTION */}
      <div className="card" style={{ textAlign: 'center', padding: '40px', background: 'linear-gradient(135deg, rgba(15,23,42,0.9), rgba(30,41,59,0.7))' }}>
        <h2 style={{ fontSize: '24px', fontWeight: '800', marginBottom: '8px' }}>Ready to inspect local network telemetry?</h2>
        <p style={{ color: 'var(--text-muted)', fontSize: '14px', marginBottom: '24px' }}>
          Launch the interactive React NetScope NOC monitoring console to scan your subnet and view live devices.
        </p>
        <button className="btn-continue-lg" onClick={onContinue}>
          Continue to Monitoring Console <ArrowRight size={18} />
        </button>
      </div>
    </div>
  );
}
