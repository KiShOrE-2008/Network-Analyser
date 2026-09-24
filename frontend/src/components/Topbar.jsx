import React from 'react';
import { RefreshCw, Plus } from 'lucide-react';

export default function Topbar({
  activeView,
  backendConnected,
  lastUpdated,
  onRefresh,
  onOpenRegisterModal
}) {
  const titleMap = {
    landing: 'Project Information',
    overview: 'Overview',
    devices: 'Device Inventory',
    discovery: 'Network Discovery',
    monitoring: 'Monitoring Controls',
    alerts: 'Alert Center',
    reports: 'Reports & Export',
    settings: 'Settings & Diagnostics',
    help: 'Documentation & Help',
    about: 'About NetScope'
  };

  return (
    <header className="app-topbar">
      <div className="topbar-left">
        <h1 className="topbar-title">{titleMap[activeView] || 'NetScope'}</h1>
      </div>

      <div className="topbar-right">
        <div className={`sys-badge ${!backendConnected ? 'offline' : ''}`}>
          <span className={`status-dot ${backendConnected ? 'green' : 'red'}`}></span>
          <span>{backendConnected ? 'SYSTEM OPERATIONAL' : 'CONNECTION LOST'}</span>
        </div>
        <span className="last-updated">Last updated: {lastUpdated || '--:--:--'}</span>
        <button className="btn btn-secondary btn-sm" onClick={onRefresh} title="Refresh Telemetry">
          <RefreshCw size={14} /> Refresh
        </button>
        <button className="btn btn-primary btn-sm" onClick={onOpenRegisterModal}>
          <Plus size={14} /> Register Device
        </button>
      </div>
    </header>
  );
}
