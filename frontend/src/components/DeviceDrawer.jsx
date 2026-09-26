import React, { useState, useEffect } from 'react';
import {
  X,
  Zap,
  Activity,
  Radio,
  Terminal,
  ShieldAlert,
  Server,
  Cpu,
  Clock,
  HardDrive,
  CheckCircle,
  AlertOctagon,
  RefreshCw
} from 'lucide-react';
import { devicesApi } from '../api/devices';
import { diagnosticsApi } from '../api/diagnostics';

export default function DeviceDrawer({
  deviceId,
  isOpen,
  onClose,
  onDeviceUpdated,
  addToast
}) {
  const [activeTab, setActiveTab] = useState('overview');
  const [device, setDevice] = useState(null);
  const [ports, setPorts] = useState([]);
  const [snmp, setSnmp] = useState(null);
  const [events, setEvents] = useState([]);
  const [interfaces, setInterfaces] = useState([]);
  const [diagData, setDiagData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(null);

  useEffect(() => {
    if (!isOpen || !deviceId) {
      setDevice(null);
      setPorts([]);
      setSnmp(null);
      setEvents([]);
      return;
    }

    fetchDeviceDetails();
  }, [isOpen, deviceId]);

  const fetchDeviceDetails = async () => {
    setLoading(true);
    try {
      const dev = await devicesApi.getDeviceById(deviceId);
      setDevice(dev);

      // Fetch supplementary port, snmp, event, interface, and diagnostics info in parallel
      const [portsRes, snmpRes, eventsRes, ifacesRes, diagRes] = await Promise.allSettled([
        devicesApi.getPorts(deviceId),
        devicesApi.getSnmp(deviceId),
        devicesApi.getEvents(deviceId),
        diagnosticsApi.getDeviceInterfaces(deviceId),
        diagnosticsApi.getDeviceDiagnostics(deviceId)
      ]);

      if (portsRes.status === 'fulfilled') setPorts(portsRes.value || []);
      if (snmpRes.status === 'fulfilled') setSnmp(snmpRes.value || null);
      if (eventsRes.status === 'fulfilled') setEvents(eventsRes.value || []);
      if (ifacesRes.status === 'fulfilled') setInterfaces(ifacesRes.value || []);
      if (diagRes.status === 'fulfilled') setDiagData(diagRes.value || null);
    } catch (err) {
      if (addToast) addToast('error', 'Device Load Failed', err.message);
    } finally {
      setLoading(false);
    }
  };

  const handlePingCheck = async () => {
    setActionLoading('ping');
    try {
      const res = await devicesApi.pingCheck(deviceId);
      if (addToast) {
        addToast(
          res.reachable ? 'success' : 'error',
          'Ping Probe Result',
          `${res.ipAddress} is ${res.reachable ? 'REACHABLE' : 'UNREACHABLE'} (${res.latencyMs !== null ? `${res.latencyMs}ms` : 'No response'})`
        );
      }
      fetchDeviceDetails();
      if (onDeviceUpdated) onDeviceUpdated();
    } catch (err) {
      if (addToast) addToast('error', 'Ping Check Failed', err.message);
    } finally {
      setActionLoading(null);
    }
  };

  const handlePortScan = async () => {
    setActionLoading('ports');
    try {
      const res = await devicesApi.scanPorts(deviceId);
      setPorts(res.openPorts || []);
      if (addToast) {
        addToast('success', 'Port Scan Complete', `Found ${res.openPortsCount} open ports on target.`);
      }
      fetchDeviceDetails();
    } catch (err) {
      if (addToast) addToast('error', 'Port Scan Failed', err.message);
    } finally {
      setActionLoading(null);
    }
  };

  const handleSnmpCheck = async () => {
    setActionLoading('snmp');
    try {
      const res = await devicesApi.snmpCheck(deviceId, 'public');
      setSnmp(res);
      if (addToast) {
        addToast('success', 'SNMP Check Complete', `Queried SNMP metrics for ${res.deviceIp}`);
      }
    } catch (err) {
      if (addToast) addToast('warning', 'SNMP Telemetry Unavailable', 'Host did not respond to SNMP v2c/v3 request.');
    } finally {
      setActionLoading(null);
    }
  };

  const handleNmapScan = async () => {
    setActionLoading('nmap');
    try {
      const res = await devicesApi.nmapScan(deviceId, 'FAST_PORT');
      if (addToast) {
        addToast('success', 'Nmap Engine Scan Complete', `Scanned host in ${res.scanDurationMs}ms.`);
      }
      fetchDeviceDetails();
    } catch (err) {
      if (addToast) addToast('error', 'Nmap Scan Failed', err.message);
    } finally {
      setActionLoading(null);
    }
  };

  if (!isOpen) return null;

  const isOnline = device?.status === 'ONLINE';
  const openPortsCount = ports.filter(p => p.status === 'OPEN').length;

  // Filter port change events from device event history
  const portChangeEvents = events.filter(e => e.eventType === 'PORT_CHANGE' || (e.message && e.message.toLowerCase().includes('port')));

  return (
    <div className="drawer-backdrop" onClick={onClose}>
      <div className="drawer-panel" onClick={e => e.stopPropagation()}>
        {/* DRAWER HEADER */}
        <div className="drawer-header">
          <div>
            <div className="drawer-status-line">
              <span className={`status-dot-sm ${isOnline ? 'green' : 'red'}`} />
              <span className={`drawer-status-text ${isOnline ? 'online-text' : 'offline-text'}`}>
                {device?.status || 'UNKNOWN'}
              </span>
            </div>
            <h2 className="drawer-title">
              {device?.name && device.name !== device.ipAddress
                ? device.name
                : device?.hostname
                ? device.hostname
                : `Discovered Host ${device?.ipAddress || ''}`}
            </h2>
            <div className="drawer-ip mono">{device?.ipAddress || '—'}</div>
          </div>
          <button className="icon-btn" onClick={onClose} aria-label="Close Inspector">
            <X size={20} />
          </button>
        </div>

        {/* DRAWER TABS */}
        <div className="drawer-tabs">
          <button
            className={`tab-btn ${activeTab === 'overview' ? 'active' : ''}`}
            onClick={() => setActiveTab('overview')}
          >
            OVERVIEW
          </button>
          <button
            className={`tab-btn ${activeTab === 'ports' ? 'active' : ''}`}
            onClick={() => setActiveTab('ports')}
          >
            PORTS {openPortsCount > 0 && <span className="tab-badge">{openPortsCount}</span>}
          </button>
          <button
            className={`tab-btn ${activeTab === 'telemetry' ? 'active' : ''}`}
            onClick={() => setActiveTab('telemetry')}
          >
            TELEMETRY
          </button>
          <button
            className={`tab-btn ${activeTab === 'events' ? 'active' : ''}`}
            onClick={() => setActiveTab('events')}
          >
            EVENTS {events.length > 0 && <span className="tab-badge">{events.length}</span>}
          </button>
          <button
            className={`tab-btn ${activeTab === 'network' ? 'active' : ''}`}
            onClick={() => setActiveTab('network')}
          >
            NETWORK {interfaces.length > 0 && <span className="tab-badge">{interfaces.length}</span>}
          </button>
        </div>

        {/* DRAWER BODY CONTENT */}
        <div className="drawer-content">
          {loading ? (
            <div className="drawer-loading">
              <RefreshCw className="spin" size={24} />
              <span>Loading telemetry details...</span>
            </div>
          ) : !device ? (
            <div className="drawer-loading">Device unavailable.</div>
          ) : (
            <>
              {/* TAB 1: OVERVIEW */}
              {activeTab === 'overview' && (
                <div className="tab-pane">
                  {/* QUICK ACTION BUTTONS */}
                  <div className="action-button-grid">
                    <button
                      className="btn btn-secondary btn-sm"
                      onClick={handlePingCheck}
                      disabled={actionLoading === 'ping'}
                    >
                      <Zap size={14} /> {actionLoading === 'ping' ? 'Probing...' : 'RUN CHECK'}
                    </button>
                    <button
                      className="btn btn-secondary btn-sm"
                      onClick={handlePortScan}
                      disabled={actionLoading === 'ports'}
                    >
                      <Radio size={14} /> {actionLoading === 'ports' ? 'Scanning...' : 'SCAN PORTS'}
                    </button>
                    <button
                      className="btn btn-secondary btn-sm"
                      onClick={handleSnmpCheck}
                      disabled={actionLoading === 'snmp'}
                    >
                      <Activity size={14} /> {actionLoading === 'snmp' ? 'Querying...' : 'SNMP CHECK'}
                    </button>
                    <button
                      className="btn btn-secondary btn-sm"
                      onClick={handleNmapScan}
                      disabled={actionLoading === 'nmap'}
                    >
                      <Terminal size={14} /> {actionLoading === 'nmap' ? 'Executing...' : 'NMAP SCAN'}
                    </button>
                  </div>

                  {/* METADATA GRID */}
                  <div className="inspector-grid">
                    <div className="inspector-card">
                      <span className="info-label">IP ADDRESS</span>
                      <span className="info-value mono">{device.ipAddress || 'N/A'}</span>
                    </div>

                    <div className="inspector-card">
                      <span className="info-label">HOSTNAME</span>
                      <span className="info-value mono">{device.hostname || 'N/A'}</span>
                    </div>

                    <div className="inspector-card">
                      <span className="info-label">MAC ADDRESS</span>
                      <span className="info-value mono">{device.macAddress || 'N/A'}</span>
                    </div>

                    <div className="inspector-card">
                      <span className="info-label">VENDOR (OUI)</span>
                      <span className="info-value">{device.vendor || 'N/A'}</span>
                    </div>

                    <div className="inspector-card">
                      <span className="info-label">MODEL</span>
                      <span className="info-value">{device.model || 'N/A'}</span>
                    </div>

                    <div className="inspector-card">
                      <span className="info-label">OS CLUE</span>
                      <span className="info-value">{device.osClue || 'N/A'}</span>
                    </div>

                    <div className="inspector-card">
                      <span className="info-label">DEVICE TYPE</span>
                      <span className="info-value badge badge-subtle">{device.deviceType || 'UNKNOWN'}</span>
                    </div>

                    <div className="inspector-card">
                      <span className="info-label">HEALTH STATUS</span>
                      <span className={`info-value status-text-${(device.healthStatus || '').toLowerCase()}`}>
                        {device.healthStatus || 'UNKNOWN'}
                      </span>
                    </div>

                    <div className="inspector-card">
                      <span className="info-label">MONITORING</span>
                      <span className="info-value">
                        {device.monitoringEnabled ? 'Enabled' : 'Disabled'}
                      </span>
                    </div>

                    <div className="inspector-card">
                      <span className="info-label">SCAN INTERVAL</span>
                      <span className="info-value mono">{device.scanInterval ? `${device.scanInterval}s` : '10s'}</span>
                    </div>

                    <div className="inspector-card">
                      <span className="info-label">LAST SEEN</span>
                      <span className="info-value mono">
                        {device.lastSeenAt ? new Date(device.lastSeenAt).toLocaleString() : 'N/A'}
                      </span>
                    </div>
                  </div>
                </div>
              )}

              {/* TAB 2: PORTS & EXPOSURE */}
              {activeTab === 'ports' && (
                <div className="tab-pane">
                  {/* PORT EXPOSURE DETECTED BANNER */}
                  {portChangeEvents.length > 0 && (
                    <div className="exposure-alert-banner">
                      <div className="banner-icon"><ShieldAlert size={20} color="var(--warning)" /></div>
                      <div>
                        <div className="banner-title">PORT EXPOSURE CHANGE DETECTED</div>
                        <div className="banner-sub">{portChangeEvents[0].message}</div>
                      </div>
                    </div>
                  )}

                  <div className="ports-summary-bar">
                    <span className="summary-title">OPEN PORTS EXPOSED</span>
                    <span className="badge badge-primary mono">{openPortsCount} OPEN</span>
                  </div>

                  {ports.length === 0 ? (
                    <div className="empty-state-box">
                      <p className="empty-state-desc">No scanned ports available. Click "SCAN PORTS" to probe exposed TCP services.</p>
                      <button className="btn btn-secondary btn-sm" onClick={handlePortScan}>
                        <Radio size={14} /> Run Port Probe
                      </button>
                    </div>
                  ) : (
                    <table className="data-table">
                      <thead>
                        <tr>
                          <th>PORT</th>
                          <th>PROTO</th>
                          <th>SERVICE</th>
                          <th>STATUS</th>
                        </tr>
                      </thead>
                      <tbody>
                        {ports.map((p) => (
                          <tr key={p.id || p.port}>
                            <td className="mono font-semibold">{p.port}</td>
                            <td className="mono text-muted">{p.protocol || 'TCP'}</td>
                            <td>{p.serviceName || 'Unknown'}</td>
                            <td>
                              <span className={`status-badge ${p.status === 'OPEN' ? 'open' : 'closed'}`}>
                                {p.status}
                              </span>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  )}
                </div>
              )}

              {/* TAB 3: TELEMETRY (SNMP) */}
              {activeTab === 'telemetry' && (
                <div className="tab-pane">
                  <div className="telemetry-grid">
                    <div className="telemetry-card">
                      <div className="t-icon blue"><Cpu size={20} /></div>
                      <div className="t-content">
                        <span className="info-label">CPU USAGE</span>
                        <span className="t-val mono">
                          {snmp && typeof snmp.cpuUsagePercent === 'number' && snmp.cpuUsagePercent >= 0 
                            ? `${snmp.cpuUsagePercent.toFixed(1)}%` 
                            : 'N/A'}
                        </span>
                      </div>
                    </div>

                    <div className="telemetry-card">
                      <div className="t-icon purple"><HardDrive size={20} /></div>
                      <div className="t-content">
                        <span className="info-label">MEMORY USAGE</span>
                        <span className="t-val mono">
                          {snmp && typeof snmp.memoryUsagePercent === 'number' && snmp.memoryUsagePercent >= 0 
                            ? `${snmp.memoryUsagePercent.toFixed(1)}%` 
                            : 'N/A'}
                        </span>
                      </div>
                    </div>

                    <div className="telemetry-card">
                      <div className="t-icon green"><Clock size={20} /></div>
                      <div className="t-content">
                        <span className="info-label">SYSTEM UPTIME</span>
                        <span className="t-val mono">
                          {snmp && snmp.sysUptimeSeconds > 0 
                            ? `${Math.floor(snmp.sysUptimeSeconds / 3600)}h ${Math.floor((snmp.sysUptimeSeconds % 3600) / 60)}m` 
                            : 'N/A'}
                        </span>
                      </div>
                    </div>

                    <div className="telemetry-card">
                      <div className="t-icon cyan"><Server size={20} /></div>
                      <div className="t-content">
                        <span className="info-label">INTERFACES</span>
                        <span className="t-val mono">
                          {snmp && snmp.networkInterfacesCount > 0 
                            ? snmp.networkInterfacesCount 
                            : 'N/A'}
                        </span>
                      </div>
                    </div>
                  </div>

                  {!snmp && (
                    <div className="telemetry-notice">
                      <span>SNMP hardware telemetry unavailable for this endpoint.</span>
                    </div>
                  )}
                </div>
              )}

              {/* TAB 4: EVENTS */}
              {activeTab === 'events' && (
                <div className="tab-pane">
                  {events.length === 0 ? (
                    <div className="empty-state-box">
                      <p className="empty-state-desc">No events recorded for this device.</p>
                    </div>
                  ) : (
                    <div className="timeline-container">
                      {events.map((e) => (
                        <div key={e.id} className="timeline-item">
                          <div className="timeline-marker" />
                          <div className="timeline-content">
                            <div className="timeline-header">
                              <span className="timeline-title">{e.eventType || 'EVENT'}</span>
                              <span className="timeline-time mono">
                                {e.eventTime ? new Date(e.eventTime).toLocaleTimeString() : ''}
                              </span>
                            </div>
                            <div className="timeline-msg">{e.message}</div>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {/* TAB 5: NETWORK DIAGNOSTICS & INTERFACES */}
              {activeTab === 'network' && (
                <div className="tab-pane">
                  <div className="telemetry-grid">
                    <div className="telemetry-card">
                      <div className="t-icon cyan"><Activity size={20} /></div>
                      <div className="t-content">
                        <span className="info-label">LATENCY</span>
                        <span className="t-val mono">
                          {diagData?.latencyMs != null ? `${diagData.latencyMs} ms` : 'N/A'}
                        </span>
                      </div>
                    </div>

                    <div className="telemetry-card">
                      <div className="t-icon purple"><Zap size={20} /></div>
                      <div className="t-content">
                        <span className="info-label">JITTER</span>
                        <span className="t-val mono">
                          {diagData?.jitterMs != null ? `${diagData.jitterMs} ms` : 'N/A'}
                        </span>
                      </div>
                    </div>

                    <div className="telemetry-card">
                      <div className="t-icon red"><AlertOctagon size={20} /></div>
                      <div className="t-content">
                        <span className="info-label">PACKET LOSS</span>
                        <span className="t-val mono">
                          {diagData?.packetLossPercent != null ? `${diagData.packetLossPercent}%` : 'N/A'}
                        </span>
                      </div>
                    </div>

                    <div className="telemetry-card">
                      <div className="t-icon green"><Radio size={20} /></div>
                      <div className="t-content">
                        <span className="info-label">LAST SPEED TEST</span>
                        <span className="t-val mono" style={{ fontSize: '0.85rem' }}>
                          {diagData?.lastSpeedTest
                            ? `${diagData.lastSpeedTest.downloadMbps} ↓ / ${diagData.lastSpeedTest.uploadMbps} ↑ Mbps`
                            : 'N/A'}
                        </span>
                      </div>
                    </div>
                  </div>

                  <div className="ports-summary-bar" style={{ marginTop: '1rem' }}>
                    <span className="summary-title">SNMP INTERFACES</span>
                    <span className="badge badge-primary mono">{interfaces.length} INTERFACES</span>
                  </div>

                  {interfaces.length === 0 ? (
                    <div className="empty-state-box">
                      <p className="empty-state-desc">No SNMP interfaces reported for this host. SNMP monitoring may be disabled or unsupported.</p>
                    </div>
                  ) : (
                    <table className="data-table">
                      <thead>
                        <tr>
                          <th>NAME</th>
                          <th>STATUS</th>
                          <th>RX RATE</th>
                          <th>TX RATE</th>
                          <th>ERRORS</th>
                          <th>DROPS</th>
                        </tr>
                      </thead>
                      <tbody>
                        {interfaces.map((iface) => (
                          <tr key={iface.id || iface.interfaceName}>
                            <td className="mono font-semibold">{iface.interfaceName}</td>
                            <td>
                              <span className={`status-badge ${iface.status === 'UP' ? 'open' : 'closed'}`}>
                                {iface.status}
                              </span>
                            </td>
                            <td className="mono green">{iface.rxMbps != null ? `${iface.rxMbps} Mbps` : 'N/A'}</td>
                            <td className="mono blue">{iface.txMbps != null ? `${iface.txMbps} Mbps` : 'N/A'}</td>
                            <td className="mono">{iface.rxErrors + iface.txErrors}</td>
                            <td className="mono">{iface.rxDrops + iface.txDrops}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  )}
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}
