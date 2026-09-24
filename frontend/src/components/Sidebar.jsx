import React from 'react';
import {
  Info,
  LayoutDashboard,
  HardDrive,
  Globe,
  Activity,
  AlertTriangle,
  FileText,
  Settings,
  HelpCircle,
  FileCode
} from 'lucide-react';

export default function Sidebar({ activeView, setActiveView, localNetworks, alerts }) {
  const activeAlertsCount = (alerts || []).filter(a => !a.resolved).length;
  const networkCidr = localNetworks && localNetworks.length > 0 
    ? (localNetworks[0].networkCidr || localNetworks[0].ipAddress)
    : '192.168.1.0/24';

  const navItems = [
    { id: 'landing', label: 'Project Info', icon: Info },
    { id: 'overview', label: 'Overview', icon: LayoutDashboard },
    { id: 'devices', label: 'Devices', icon: HardDrive },
    { id: 'discovery', label: 'Discovery', icon: Globe },
    { id: 'monitoring', label: 'Monitoring', icon: Activity },
    { id: 'alerts', label: 'Alerts', icon: AlertTriangle, badge: activeAlertsCount },
    { id: 'reports', label: 'Reports', icon: FileText },
    { id: 'settings', label: 'Settings', icon: Settings },
    { id: 'help', label: 'Documentation', icon: HelpCircle },
    { id: 'about', label: 'About', icon: FileCode },
  ];

  return (
    <aside className="app-sidebar" id="sidebar">
      <div className="sidebar-brand">
        <div className="brand-logo">N</div>
        <div className="brand-text">
          <div className="brand-title">NETSCOPE</div>
          <div className="brand-subtitle">NETWORK MONITORING</div>
        </div>
      </div>

      <nav className="sidebar-nav">
        {navItems.map(item => {
          const IconComp = item.icon;
          const isActive = activeView === item.id;
          return (
            <button
              key={item.id}
              className={`nav-item ${isActive ? 'active' : ''}`}
              onClick={() => setActiveView(item.id)}
            >
              <IconComp className="nav-icon" />
              <span>{item.label}</span>
              {item.badge > 0 && (
                <span className="nav-badge">{item.badge}</span>
              )}
            </button>
          );
        })}
      </nav>

      <div className="sidebar-footer">
        <div className="footer-info-row">
          <span className="info-label">NETWORK</span>
          <span className="info-val mono">{networkCidr}</span>
        </div>
        <div className="footer-info-row">
          <span className="info-label">SYSTEM</span>
          <span className="info-status">
            <span className="status-dot green"></span> Operational
          </span>
        </div>
      </div>
    </aside>
  );
}
