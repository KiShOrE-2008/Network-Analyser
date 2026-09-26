import React, { useState, useEffect, useCallback } from 'react';
import Sidebar from './components/Sidebar';
import Topbar from './components/Topbar';
import DeviceDrawer from './components/DeviceDrawer';
import RegisterModal from './components/RegisterModal';
import SearchModal from './components/SearchModal';
import ToastContainer from './components/ToastContainer';
import ConnectionBanner from './components/ConnectionBanner';

import OverviewView from './views/OverviewView';
import DevicesView from './views/DevicesView';
import DiscoveryView from './views/DiscoveryView';
import MonitoringView from './views/MonitoringView';
import DiagnosticsView from './views/DiagnosticsView';
import AlertsView from './views/AlertsView';
import ReportsView from './views/ReportsView';
import SettingsView from './views/SettingsView';

import { devicesApi } from './api/devices';
import { discoveryApi } from './api/discovery';
import { alertsApi } from './api/alerts';
import { eventsApi } from './api/events';
import { systemApi } from './api/system';
import { wsService } from './api/websocket';
import './App.css';

export default function App() {
  const [activeView, setActiveView] = useState('overview');
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);

  // Core backend state
  const [devices, setDevices] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [events, setEvents] = useState([]);
  const [localNetworks, setLocalNetworks] = useState([]);
  const [selectedCidr, setSelectedCidr] = useState('');
  const [healthStatus, setHealthStatus] = useState(null);
  const [schedulerStatus, setSchedulerStatus] = useState(null);
  const [scanResults, setScanResults] = useState(null);
  const [scanning, setScanning] = useState(false);

  // Connection & Error State
  const [backendConnected, setBackendConnected] = useState(true);
  const [lastSuccessTime, setLastSuccessTime] = useState(null);
  const [toasts, setToasts] = useState([]);

  // Modal & Drawer State
  const [selectedDeviceId, setSelectedDeviceId] = useState(null);
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [isRegisterOpen, setIsRegisterOpen] = useState(false);
  const [isSearchOpen, setIsSearchOpen] = useState(false);

  // Toast Helper
  const addToast = useCallback((type, title, message) => {
    const id = Date.now() + Math.random();
    setToasts((prev) => [...prev.slice(-4), { id, type, title, message }]);
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 4500);
  }, []);

  const dismissToast = (id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  };

  // Primary Data Fetcher
  const fetchAllData = useCallback(async () => {
    try {
      const [devRes, alertRes, eventRes, netRes, healthRes, schedRes] = await Promise.allSettled([
        devicesApi.getAllDevices(),
        alertsApi.getAlerts(true),
        eventsApi.getRecentEvents(),
        discoveryApi.getLocalNetworks(),
        systemApi.getHealth(),
        systemApi.getSchedulerStatus()
      ]);

      let hasSuccess = false;

      if (devRes.status === 'fulfilled') {
        setDevices(devRes.value || []);
        hasSuccess = true;
      }
      if (alertRes.status === 'fulfilled') {
        setAlerts(alertRes.value || []);
        hasSuccess = true;
      }
      if (eventRes.status === 'fulfilled') {
        setEvents(eventRes.value || []);
        hasSuccess = true;
      }
      if (netRes.status === 'fulfilled') {
        setLocalNetworks(netRes.value || []);
        if (netRes.value && netRes.value.length > 0 && !selectedCidr) {
          setSelectedCidr(netRes.value[0].cidr);
        }
        hasSuccess = true;
      }
      if (healthRes.status === 'fulfilled') {
        setHealthStatus(healthRes.value || null);
        hasSuccess = true;
      }
      if (schedRes.status === 'fulfilled') {
        setSchedulerStatus(schedRes.value || null);
        hasSuccess = true;
      }

      if (hasSuccess) {
        setBackendConnected(true);
        setLastSuccessTime(new Date().toLocaleTimeString());
      } else {
        setBackendConnected(false);
      }
    } catch (err) {
      setBackendConnected(false);
    }
  }, [selectedCidr]);

  // Initial Load & Polling Interval
  useEffect(() => {
    fetchAllData();

    // Setup STOMP WebSocket subscriber for real-time metric/alert stream
    wsService.connect(
      () => {
        wsService.subscribe('/topic/metrics', (pingMetric) => {
          setDevices((prev) =>
            prev.map((d) =>
              d.id === pingMetric.deviceId
                ? {
                    ...d,
                    status: pingMetric.reachable ? 'ONLINE' : 'OFFLINE',
                    _latency: pingMetric.latencyMs,
                    _packetLoss: pingMetric.packetLossPercent
                  }
                : d
            )
          );
        });

        wsService.subscribe('/topic/alerts', (newAlert) => {
          setAlerts((prev) => [newAlert, ...prev.filter((a) => a.id !== newAlert.id)]);
        });

        wsService.subscribe('/topic/devices', () => {
          fetchAllData();
        });
      },
      () => {
        // Fallback to polling if WebSocket disconnects
      }
    );

    const interval = setInterval(fetchAllData, 10000);
    return () => {
      clearInterval(interval);
      wsService.disconnect();
    };
  }, [fetchAllData]);

  // Device Action Handlers
  const handleOpenInspector = (id) => {
    setSelectedDeviceId(id);
    setIsDrawerOpen(true);
  };

  const handleRegisterDevice = async (dto) => {
    try {
      const created = await devicesApi.createDevice(dto);
      addToast('success', 'Device Registered', `Registered ${created.name} (${created.ipAddress})`);
      fetchAllData();
    } catch (err) {
      addToast('error', 'Registration Failed', err.message);
      throw err;
    }
  };

  const handleToggleMonitoring = async (id) => {
    try {
      const updated = await devicesApi.toggleMonitoring(id);
      addToast(
        'info',
        'Monitoring Toggled',
        `Monitoring ${updated.monitoringEnabled ? 'enabled' : 'disabled'} for ${updated.name}`
      );
      setDevices((prev) => prev.map((d) => (d.id === id ? updated : d)));
    } catch (err) {
      addToast('error', 'Toggle Failed', err.message);
    }
  };

  const handleDeleteDevice = async (id) => {
    if (!window.confirm('Remove this device from monitoring inventory?')) return;
    try {
      await devicesApi.deleteDevice(id);
      addToast('success', 'Device Removed', `Removed device #${id} from inventory.`);
      setDevices((prev) => prev.filter((d) => d.id !== id));
      if (selectedDeviceId === id) setIsDrawerOpen(false);
    } catch (err) {
      addToast('error', 'Deletion Failed', err.message);
    }
  };

  const handlePingCheck = async (id) => {
    try {
      const res = await devicesApi.pingCheck(id);
      addToast(
        res.reachable ? 'success' : 'error',
        'Ping Check Complete',
        `${res.ipAddress} is ${res.reachable ? 'REACHABLE' : 'UNREACHABLE'} (${res.latencyMs !== null ? `${res.latencyMs}ms` : 'No response'})`
      );
      fetchAllData();
    } catch (err) {
      addToast('error', 'Ping Failed', err.message);
    }
  };

  const handlePortScan = async (id) => {
    try {
      const res = await devicesApi.scanPorts(id);
      addToast('success', 'Port Scan Complete', `Found ${res.openPortsCount} open ports.`);
      handleOpenInspector(id);
    } catch (err) {
      addToast('error', 'Port Scan Failed', err.message);
    }
  };

  // Discovery Handlers
  const handleStartAutoDiscovery = async () => {
    setScanning(true);
    try {
      const res = await discoveryApi.autoDiscover();
      addToast(
        'success',
        'Auto-Discovery Complete',
        `Discovered ${res.devicesDiscoveredCount} hosts on local network.`
      );
      fetchAllData();
    } catch (err) {
      addToast('error', 'Auto-Discovery Failed', err.message);
    } finally {
      setScanning(false);
    }
  };

  const handleScanSubnet = async (cidr, strategy) => {
    setScanning(true);
    try {
      let res;
      if (strategy === 'NMAP') {
        const nmapRes = await discoveryApi.scanNmapSubnet(cidr, 'FAST_PORT');
        res = {
          subnetCidr: nmapRes.target || cidr,
          totalScanned: nmapRes.totalHostsScanned || 254,
          devicesDiscoveredCount: nmapRes.hostsUpCount || 0,
          newDevicesCount: nmapRes.hostsUpCount || 0,
          existingDevicesCount: 0,
          scanDurationMs: nmapRes.executionTimeMs || 0,
          discoveredDevices: (nmapRes.hosts || []).map((h) => ({
            ipAddress: h.ipAddress,
            hostname: h.hostname,
            macAddress: h.macAddress,
            vendor: h.vendor,
            osClue: h.osMatch,
            suggestedName: h.hostname || h.ipAddress,
            reachable: h.status === 'up' || true,
            alreadyMonitored: devices.some((d) => d.ipAddress === h.ipAddress),
            suggestedType: 'UNKNOWN'
          }))
        };
      } else {
        res = await discoveryApi.scanSubnet(cidr, strategy);
      }
      setScanResults(res);
      addToast(
        'success',
        'Subnet Scan Complete',
        `Scanned ${res.totalScanned || 254} hosts on ${res.subnetCidr}. Discovered ${res.devicesDiscoveredCount || 0} active endpoints.`
      );
    } catch (err) {
      addToast('error', 'Subnet Scan Failed', err.message);
    } finally {
      setScanning(false);
    }
  };

  const handleImportDevices = async (devicesToImport) => {
    try {
      const imported = await discoveryApi.importDevices(devicesToImport);
      addToast('success', 'Hosts Imported', `Successfully imported ${imported.length} hosts into inventory.`);
      fetchAllData();
    } catch (err) {
      addToast('error', 'Import Failed', err.message);
    }
  };

  // Alert Handlers
  const handleResolveAlert = async (id) => {
    try {
      const resolved = await alertsApi.resolveAlert(id);
      addToast('success', 'Alert Resolved', `Marked alert #${id} as resolved.`);
      setAlerts((prev) => prev.map((a) => (a.id === id ? resolved : a)));
    } catch (err) {
      addToast('error', 'Resolve Failed', err.message);
    }
  };

  // Scheduler Handler
  const handleToggleScheduler = async () => {
    try {
      if (schedulerStatus?.active) {
        const res = await systemApi.stopScheduler();
        setSchedulerStatus(res);
        addToast('info', 'Scheduler Paused', 'Background monitoring thread pool paused.');
      } else {
        const res = await systemApi.startScheduler();
        setSchedulerStatus(res);
        addToast('success', 'Scheduler Active', 'Background worker thread pool running.');
      }
    } catch (err) {
      addToast('error', 'Scheduler Toggle Failed', err.message);
    }
  };

  const onlineCount = devices.filter((d) => d.status === 'ONLINE').length;
  const activeAlertsCount = alerts.filter((a) => !a.isResolved && !a.resolved).length;

  return (
    <div className="app-shell">
      {/* SIDEBAR NAVIGATION */}
      <Sidebar
        activeView={activeView}
        setActiveView={setActiveView}
        collapsed={sidebarCollapsed}
        setCollapsed={setSidebarCollapsed}
        backendConnected={backendConnected}
        dbConnected={healthStatus?.database === 'Connected'}
        nmapAvailable={healthStatus?.nmapAvailable ?? false}
        activeAlertsCount={activeAlertsCount}
        mobileOpen={mobileOpen}
        setMobileOpen={setMobileOpen}
      />

      {/* MAIN CONTAINER */}
      <div className="app-container">
        {/* TOPBAR */}
        <Topbar
          localNetworks={localNetworks}
          selectedCidr={selectedCidr}
          setSelectedCidr={setSelectedCidr}
          onlineCount={onlineCount}
          alertCount={activeAlertsCount}
          backendConnected={backendConnected}
          onOpenSearch={() => setIsSearchOpen(true)}
          onOpenSettings={() => setActiveView('settings')}
          onToggleMobile={() => setMobileOpen(!mobileOpen)}
          onManualRefresh={fetchAllData}
        />

        {/* CONNECTION LOSS BANNER */}
        {!backendConnected && (
          <ConnectionBanner
            onRetry={fetchAllData}
            lastSuccessTime={lastSuccessTime}
          />
        )}

        {/* DYNAMIC VIEW CONTENT AREA */}
        <main className="main-content">
          {activeView === 'overview' && (
            <OverviewView
              devices={devices}
              events={events}
              alerts={alerts}
              onScanClick={() => setActiveView('discovery')}
              onDeviceClick={handleOpenInspector}
              onRefresh={fetchAllData}
              onViewAllChanges={() => setActiveView('alerts')}
            />
          )}

          {activeView === 'devices' && (
            <DevicesView
              devices={devices}
              onRegisterClick={() => setIsRegisterOpen(true)}
              onInspectClick={handleOpenInspector}
              onPingClick={handlePingCheck}
              onPortScanClick={handlePortScan}
              onToggleMonitoring={handleToggleMonitoring}
              onDeleteClick={handleDeleteDevice}
            />
          )}

          {activeView === 'discovery' && (
            <DiscoveryView
              localNetworks={localNetworks}
              scanResults={scanResults}
              scanning={scanning}
              autoDiscoveryStatus={{ running: scanning }}
              onStartAutoDiscovery={handleStartAutoDiscovery}
              onScanSubnet={handleScanSubnet}
              onImportDevices={handleImportDevices}
              onInspectDevice={handleOpenInspector}
              nmapAvailable={healthStatus?.nmapAvailable ?? false}
            />
          )}

          {activeView === 'monitoring' && (
            <MonitoringView
              devices={devices}
              schedulerStatus={schedulerStatus}
              onRunPingCheck={handlePingCheck}
              onToggleScheduler={handleToggleScheduler}
            />
          )}

          {activeView === 'diagnostics' && (
            <DiagnosticsView addToast={addToast} />
          )}

          {activeView === 'alerts' && (
            <AlertsView
              alerts={alerts}
              events={events}
              onResolveAlert={handleResolveAlert}
            />
          )}

          {activeView === 'reports' && (
            <ReportsView addToast={addToast} />
          )}

          {activeView === 'settings' && (
            <SettingsView
              healthStatus={healthStatus}
              schedulerStatus={schedulerStatus}
              nmapStatus={{ available: healthStatus?.nmapAvailable }}
              onRefreshDiagnostics={fetchAllData}
              onToggleScheduler={handleToggleScheduler}
              addToast={addToast}
            />
          )}
        </main>
      </div>

      {/* DEVICE INSPECTOR DRAWER */}
      <DeviceDrawer
        deviceId={selectedDeviceId}
        isOpen={isDrawerOpen}
        onClose={() => setIsDrawerOpen(false)}
        onDeviceUpdated={fetchAllData}
        addToast={addToast}
      />

      {/* REGISTER DEVICE MODAL */}
      {isRegisterOpen && (
        <RegisterModal
          onClose={() => setIsRegisterOpen(false)}
          onRegister={handleRegisterDevice}
        />
      )}

      {/* QUICK SEARCH OVERLAY MODAL */}
      <SearchModal
        devices={devices}
        isOpen={isSearchOpen}
        onClose={() => setIsSearchOpen(false)}
        onSelectDevice={handleOpenInspector}
      />

      {/* TOAST NOTIFICATION CONTAINER */}
      <ToastContainer toasts={toasts} onDismiss={dismissToast} />
    </div>
  );
}
