import React, { useState, useEffect, useCallback } from 'react';
import Sidebar from './components/Sidebar';
import Topbar from './components/Topbar';
import DeviceDrawer from './components/DeviceDrawer';
import RegisterModal from './components/RegisterModal';

import LandingView from './views/LandingView';
import OverviewView from './views/OverviewView';
import DevicesView from './views/DevicesView';
import DiscoveryView from './views/DiscoveryView';
import MonitoringView from './views/MonitoringView';
import AlertsView from './views/AlertsView';
import ReportsView from './views/ReportsView';
import SettingsView from './views/SettingsView';
import DocumentationView from './views/DocumentationView';
import AboutView from './views/AboutView';

export default function App() {
  const [activeView, setActiveView] = useState('landing');
  const [devices, setDevices] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [events, setEvents] = useState([]);
  const [localNetworks, setLocalNetworks] = useState([]);
  const [schedulerStatus, setSchedulerStatus] = useState(null);
  const [health, setHealth] = useState(null);
  const [backendConnected, setBackendConnected] = useState(true);
  const [lastUpdated, setLastUpdated] = useState('--:--:--');

  const [selectedDeviceId, setSelectedDeviceId] = useState(null);
  const [isRegisterModalOpen, setIsRegisterModalOpen] = useState(false);

  const refreshData = useCallback(async () => {
    try {
      const [hRes, devRes, altRes, evtRes, netRes, schRes] = await Promise.all([
        fetch('/api/health').then(r => r.ok ? r.json() : null).catch(() => null),
        fetch('/api/devices').then(r => r.ok ? r.json() : []).catch(() => []),
        fetch('/api/alerts?includeResolved=true').then(r => r.ok ? r.json() : []).catch(() => []),
        fetch('/api/events').then(r => r.ok ? r.json() : []).catch(() => []),
        fetch('/api/discovery/local-networks').then(r => r.ok ? r.json() : []).catch(() => []),
        fetch('/api/scheduler/status').then(r => r.ok ? r.json() : null).catch(() => null)
      ]);

      setHealth(hRes);
      setDevices(devRes || []);
      setAlerts(altRes || []);
      setEvents(evtRes || []);
      setLocalNetworks(netRes || []);
      setSchedulerStatus(schRes);
      setBackendConnected(true);

      const now = new Date();
      setLastUpdated(now.toTimeString().split(' ')[0]);
    } catch (err) {
      console.error('Data refresh error:', err);
      setBackendConnected(false);
    }
  }, []);

  useEffect(() => {
    refreshData();
    const interval = setInterval(refreshData, 10000);
    return () => clearInterval(interval);
  }, [refreshData]);

  const handleRegisterDevice = async (deviceData) => {
    try {
      const res = await fetch('/api/devices', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(deviceData)
      });
      if (res.ok) {
        setIsRegisterModalOpen(false);
        refreshData();
      }
    } catch (err) {
      console.error('Register failed:', err);
    }
  };

  const handleCheckDevice = async (id) => {
    try {
      await fetch(`/api/devices/${id}/check`, { method: 'POST' });
      refreshData();
    } catch (err) {
      console.error(err);
    }
  };

  const handleScanPorts = async (id) => {
    try {
      await fetch(`/api/devices/${id}/scan-ports`, { method: 'POST' });
      refreshData();
    } catch (err) {
      console.error(err);
    }
  };

  const handleToggleMonitoring = async (id) => {
    try {
      await fetch(`/api/devices/${id}/toggle-monitoring`, { method: 'PUT' });
      refreshData();
    } catch (err) {
      console.error(err);
    }
  };

  const handleDeleteDevice = async (id) => {
    try {
      await fetch(`/api/devices/${id}`, { method: 'DELETE' });
      setSelectedDeviceId(null);
      refreshData();
    } catch (err) {
      console.error(err);
    }
  };

  const handleImportDiscovered = async (discoveredHosts) => {
    try {
      for (const host of discoveredHosts) {
        await fetch('/api/devices/upsert-discovered', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(host)
        });
      }
      refreshData();
      setActiveView('devices');
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="app-layout">
      {/* AMBIENT GLOW ORBS */}
      <div className="ambient-orb orb-1"></div>
      <div className="ambient-orb orb-2"></div>
      <div className="ambient-orb orb-3"></div>

      <Sidebar
        activeView={activeView}
        setActiveView={setActiveView}
        localNetworks={localNetworks}
        alerts={alerts}
      />

      <div className="app-main">
        <Topbar
          activeView={activeView}
          backendConnected={backendConnected}
          lastUpdated={lastUpdated}
          onRefresh={refreshData}
          onOpenRegisterModal={() => setIsRegisterModalOpen(true)}
        />

        <main className="content-body">
          {!backendConnected && (
            <div className="connection-lost-banner">
              <span className="status-dot red"></span>
              <span><strong>CONNECTION LOST:</strong> Unable to reach backend API. Check if Spring Boot server is running on port 8080.</span>
              <button className="btn btn-secondary btn-sm" onClick={refreshData}>Retry</button>
            </div>
          )}

          {activeView === 'landing' && (
            <LandingView onContinue={() => setActiveView('overview')} />
          )}

          {activeView === 'overview' && (
            <OverviewView
              devices={devices}
              events={events}
              alerts={alerts}
              onScanClick={() => setActiveView('discovery')}
              onDeviceClick={(id) => setSelectedDeviceId(id)}
              onRefresh={refreshData}
            />
          )}

          {activeView === 'devices' && (
            <DevicesView
              devices={devices}
              onScanClick={() => setActiveView('discovery')}
              onOpenRegisterModal={() => setIsRegisterModalOpen(true)}
              onDeviceClick={(id) => setSelectedDeviceId(id)}
            />
          )}

          {activeView === 'discovery' && (
            <DiscoveryView
              localNetworks={localNetworks}
              onImportSelected={handleImportDiscovered}
              onAutoDiscovery={() => refreshData()}
            />
          )}

          {activeView === 'monitoring' && (
            <MonitoringView schedulerStatus={schedulerStatus} />
          )}

          {activeView === 'alerts' && (
            <AlertsView alerts={alerts} />
          )}

          {activeView === 'reports' && (
            <ReportsView devices={devices} alerts={alerts} />
          )}

          {activeView === 'settings' && (
            <SettingsView health={health} />
          )}

          {activeView === 'help' && (
            <DocumentationView />
          )}

          {activeView === 'about' && (
            <AboutView />
          )}
        </main>
      </div>

      {selectedDeviceId && (
        <DeviceDrawer
          deviceId={selectedDeviceId}
          devices={devices}
          onClose={() => setSelectedDeviceId(null)}
          onCheck={handleCheckDevice}
          onScanPorts={handleScanPorts}
          onToggleMonitoring={handleToggleMonitoring}
          onDelete={handleDeleteDevice}
        />
      )}

      {isRegisterModalOpen && (
        <RegisterModal
          onClose={() => setIsRegisterModalOpen(false)}
          onRegister={handleRegisterDevice}
        />
      )}
    </div>
  );
}
