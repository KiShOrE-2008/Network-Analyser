import React from 'react';
import {
  LayoutDashboard,
  HardDrive,
  Globe,
  Activity,
  AlertTriangle,
  FileText,
  Settings,
  ChevronLeft,
  ChevronRight,
  Database,
  Terminal,
  Server,
  Gauge
} from 'lucide-react';

export default function Sidebar({
  activeView,
  setActiveView,
  collapsed,
  setCollapsed,
  backendConnected,
  dbConnected,
  nmapAvailable,
  activeAlertsCount,
  mobileOpen,
  setMobileOpen
}) {
  const mainNav = [
    { id: 'overview', label: 'Overview', icon: LayoutDashboard },
    { id: 'devices', label: 'Devices', icon: HardDrive },
    { id: 'discovery', label: 'Discovery', icon: Globe },
    { id: 'monitoring', label: 'Monitoring', icon: Activity },
    { id: 'diagnostics', label: 'Diagnostics', icon: Gauge },
    { id: 'alerts', label: 'Alerts', icon: AlertTriangle, badge: activeAlertsCount }
  ];

  const secNav = [
    { id: 'reports', label: 'Reports', icon: FileText },
    { id: 'settings', label: 'Settings', icon: Settings }
  ];

  const handleNavClick = (id) => {
    setActiveView(id);
    if (mobileOpen) setMobileOpen(false);
  };

  return (
    <>
      {/* Mobile Drawer Overlay */}
      {mobileOpen && (
        <div 
          className="mobile-overlay"
          onClick={() => setMobileOpen(false)}
        />
      )}

      <aside className={`app-sidebar ${collapsed ? 'collapsed' : ''} ${mobileOpen ? 'mobile-open' : ''}`}>
        <div className="sidebar-brand">
          <div className="brand-icon">N</div>
          {!collapsed && (
            <div className="brand-text">
              <div className="brand-title">NETSCOPE</div>
              <div className="brand-subtitle">Discovery & Monitoring</div>
            </div>
          )}
          <button 
            className="collapse-toggle-btn"
            onClick={() => setCollapsed(!collapsed)}
            aria-label="Toggle Navigation Sidebar"
          >
            {collapsed ? <ChevronRight size={16} /> : <ChevronLeft size={16} />}
          </button>
        </div>

        <nav className="sidebar-nav">
          <div className="nav-group-label">{!collapsed && 'CORE MONITORS'}</div>
          {mainNav.map(item => {
            const IconComp = item.icon;
            const isActive = activeView === item.id;
            return (
              <button
                key={item.id}
                className={`nav-item ${isActive ? 'active' : ''}`}
                onClick={() => handleNavClick(item.id)}
                title={collapsed ? item.label : undefined}
              >
                <IconComp className="nav-icon" size={18} />
                {!collapsed && <span>{item.label}</span>}
                {!collapsed && item.badge > 0 && (
                  <span className="nav-badge">{item.badge}</span>
                )}
              </button>
            );
          })}

          <div className="nav-divider" />

          <div className="nav-group-label">{!collapsed && 'UTILITIES & SYSTEM'}</div>
          {secNav.map(item => {
            const IconComp = item.icon;
            const isActive = activeView === item.id;
            return (
              <button
                key={item.id}
                className={`nav-item ${isActive ? 'active' : ''}`}
                onClick={() => handleNavClick(item.id)}
                title={collapsed ? item.label : undefined}
              >
                <IconComp className="nav-icon" size={18} />
                {!collapsed && <span>{item.label}</span>}
              </button>
            );
          })}
        </nav>

        {!collapsed && (
          <div className="sidebar-footer">
            <div className="footer-status-item">
              <span className={`status-dot-sm ${backendConnected ? 'green' : 'red'}`} />
              <Server size={12} className="footer-icon" />
              <span>Backend {backendConnected ? 'Connected' : 'Offline'}</span>
            </div>

            <div className="footer-status-item">
              <span className={`status-dot-sm ${dbConnected ? 'green' : 'gray'}`} />
              <Database size={12} className="footer-icon" />
              <span>PostgreSQL {dbConnected ? 'Connected' : 'Unknown'}</span>
            </div>

            <div className="footer-status-item">
              <span className={`status-dot-sm ${nmapAvailable ? 'green' : 'gray'}`} />
              <Terminal size={12} className="footer-icon" />
              <span>Nmap Engine {nmapAvailable ? 'Available' : 'Unavailable'}</span>
            </div>
          </div>
        )}
      </aside>
    </>
  );
}
