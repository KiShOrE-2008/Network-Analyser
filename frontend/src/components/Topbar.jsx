import React from 'react';
import { Search, Settings, Menu, Shield, RefreshCw } from 'lucide-react';

export default function Topbar({
  localNetworks,
  selectedCidr,
  setSelectedCidr,
  onlineCount,
  alertCount,
  backendConnected,
  onOpenSearch,
  onOpenSettings,
  onToggleMobile,
  onManualRefresh
}) {
  const isHealthy = backendConnected && alertCount === 0;

  return (
    <header className="app-topbar">
      {/* LEFT: BRAND & MOBILE TRIGGER */}
      <div className="topbar-left">
        <button 
          className="mobile-menu-btn"
          onClick={onToggleMobile}
          aria-label="Open navigation menu"
        >
          <Menu size={20} />
        </button>
        <div className="topbar-title-block">
          <h1 className="topbar-title">NETSCOPE</h1>
          <span className="topbar-subtitle">Network Discovery & Monitoring</span>
        </div>
      </div>

      {/* CENTER: DETECTED NETWORK SUBNET SELECTOR */}
      <div className="topbar-center">
        <div className="network-selector">
          <span className="net-selector-icon"><Shield size={14} /></span>
          <span className="net-label">Subnet:</span>
          <select 
            value={selectedCidr || ''} 
            onChange={(e) => setSelectedCidr(e.target.value)}
            className="net-select mono"
          >
            {localNetworks && localNetworks.length > 0 ? (
              localNetworks.map((net) => (
                <option key={net.cidr || net.address} value={net.cidr}>
                  {net.interfaceName ? `${net.interfaceName} • ` : ''}{net.cidr}
                </option>
              ))
            ) : (
              <option value="192.168.1.0/24">Local Network • 192.168.1.0/24</option>
            )}
          </select>
        </div>
      </div>

      {/* RIGHT: SYSTEM STATUS, COUNTERS, SEARCH & SETTINGS */}
      <div className="topbar-right">
        {/* SYSTEM STATUS BADGE */}
        <div className={`topbar-status-badge ${!backendConnected ? 'offline' : alertCount > 0 ? 'warning' : 'healthy'}`}>
          <span className="status-dot-sm" />
          <span>
            {!backendConnected 
              ? 'BACKEND OFFLINE' 
              : alertCount > 0 
                ? 'ATTENTION REQUIRED' 
                : 'SYSTEM HEALTHY'}
          </span>
        </div>

        {/* ONLINE DEVICE COUNTER */}
        <div className="topbar-metric-chip online">
          <span className="chip-dot green" />
          <span className="chip-val mono">{onlineCount ?? 0}</span>
          <span className="chip-lbl">ONLINE</span>
        </div>

        {/* ACTIVE ALERTS COUNTER */}
        <div className={`topbar-metric-chip ${alertCount > 0 ? 'alert' : 'muted'}`}>
          <span className={`chip-dot ${alertCount > 0 ? 'red' : 'gray'}`} />
          <span className="chip-val mono">{alertCount ?? 0}</span>
          <span className="chip-lbl">ALERTS</span>
        </div>

        {/* REFRESH BUTTON */}
        <button 
          className="topbar-action-btn"
          onClick={onManualRefresh}
          title="Refresh Data"
        >
          <RefreshCw size={16} />
        </button>

        {/* SEARCH TRIGGER BUTTON */}
        <button 
          className="topbar-action-btn"
          onClick={onOpenSearch}
          title="Search Inventory (Ctrl + K)"
        >
          <Search size={16} />
        </button>

        {/* SETTINGS TRIGGER BUTTON */}
        <button 
          className="topbar-action-btn"
          onClick={onOpenSettings}
          title="System Settings"
        >
          <Settings size={16} />
        </button>
      </div>
    </header>
  );
}
