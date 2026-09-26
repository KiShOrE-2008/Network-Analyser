import React, { useState } from 'react';
import {
  Globe,
  Zap,
  RefreshCw,
  Plus,
  CheckCircle,
  Radio,
  Terminal,
  Shield,
  Eye
} from 'lucide-react';
import EmptyState from '../components/EmptyState';

export default function DiscoveryView({
  localNetworks = [],
  scanResults = null,
  scanning = false,
  autoDiscoveryStatus = null,
  onStartAutoDiscovery,
  onScanSubnet,
  onImportDevices,
  onInspectDevice,
  nmapAvailable = false
}) {
  const [selectedSubnet, setSelectedSubnet] = useState('');
  const [scanStrategy, setScanStrategy] = useState('PING');
  const [selectedImportIps, setSelectedImportIps] = useState(new Set());

  const activeSubnet = selectedSubnet || (localNetworks.length > 0 ? localNetworks[0].cidr : '192.168.1.0/24');

  const handleSubnetScan = () => {
    onScanSubnet(activeSubnet, scanStrategy);
  };

  const handleToggleSelectAll = (e) => {
    if (e.target.checked && scanResults?.discoveredDevices) {
      const allIps = scanResults.discoveredDevices
        .filter(d => !d.alreadyMonitored)
        .map(d => d.ipAddress);
      setSelectedImportIps(new Set(allIps));
    } else {
      setSelectedImportIps(new Set());
    }
  };

  const handleToggleSelectIp = (ip) => {
    const next = new Set(selectedImportIps);
    if (next.has(ip)) next.delete(ip);
    else next.add(ip);
    setSelectedImportIps(next);
  };

  const handleImportSelected = () => {
    if (!scanResults?.discoveredDevices || selectedImportIps.size === 0) return;
    const candidatesToImport = scanResults.discoveredDevices
      .filter(d => selectedImportIps.has(d.ipAddress))
      .map(d => ({
        name: d.suggestedName || d.hostname || d.ipAddress,
        ipAddress: d.ipAddress,
        hostname: d.hostname,
        deviceType: d.suggestedType || 'UNKNOWN',
        vendor: d.vendor,
        macAddress: d.macAddress,
        osClue: d.osClue
      }));
    onImportDevices(candidatesToImport);
    setSelectedImportIps(new Set());
  };

  return (
    <section className="view-panel active">
      <div className="section-hero">
        <div>
          <h2 className="section-title">NETWORK DISCOVERY</h2>
          <p className="section-desc">Automated CIDR range scanning, attached network detection, and selective host inventory import.</p>
        </div>
        <div style={{ display: 'flex', gap: '12px' }}>
          <button
            className="btn btn-primary btn-sm"
            onClick={onStartAutoDiscovery}
            disabled={scanning || autoDiscoveryStatus?.running}
          >
            <Zap size={14} /> {scanning || autoDiscoveryStatus?.running ? 'Scanning Subnets...' : 'START AUTOMATIC DISCOVERY'}
          </button>
        </div>
      </div>

      {/* DETECTED INTERFACES & CIDR CARD */}
      <div className="card" style={{ marginBottom: '24px' }}>
        <div className="card-header">
          <div>
            <h3 className="card-title">ATTACHED LOCAL NETWORK INTERFACES</h3>
            <div className="card-sub">IPv4 adapters detected on backend server</div>
          </div>
        </div>

        <div className="interface-cards-grid">
          {localNetworks.length === 0 ? (
            <div className="interface-card mono">
              <span className="info-label">DEFAULT LOCAL SUBNET</span>
              <span className="net-cidr">192.168.1.0/24</span>
              <span className="net-sub font-mono">254 possible host addresses</span>
            </div>
          ) : (
            localNetworks.map((net) => {
              const totalHosts = Math.pow(2, 32 - (net.prefixLength || 24)) - 2;
              return (
                <div
                  key={net.cidr || net.address}
                  className={`interface-card ${activeSubnet === net.cidr ? 'active' : ''}`}
                  onClick={() => setSelectedSubnet(net.cidr)}
                >
                  <div className="net-card-top">
                    <Shield size={16} className="text-primary" />
                    <span className="net-iface mono">{net.interfaceName || 'wlan0'}</span>
                  </div>
                  <div className="net-cidr mono">{net.cidr}</div>
                  <div className="net-sub text-muted mono">
                    IP: {net.address} • /{net.prefixLength || 24} ({totalHosts > 0 ? totalHosts : 254} hosts)
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>

      {/* MANUAL SCAN CONTROL BAR */}
      <div className="card toolbar-card" style={{ marginBottom: '24px' }}>
        <div className="scan-control-grid">
          <div className="form-group mb-0">
            <label className="form-label">TARGET SUBNET CIDR</label>
            <input
              type="text"
              className="form-input mono"
              value={activeSubnet}
              onChange={(e) => setSelectedSubnet(e.target.value)}
              placeholder="192.168.1.0/24"
            />
          </div>

          <div className="form-group mb-0">
            <label className="form-label">SCAN STRATEGY</label>
            <select
              className="form-select"
              value={scanStrategy}
              onChange={(e) => setScanStrategy(e.target.value)}
            >
              <option value="PING">ICMP Ping Scan (Fast)</option>
              <option value="TCP">TCP Port Probe Scan</option>
              {nmapAvailable && <option value="NMAP">Nmap Engine Scan</option>}
            </select>
          </div>

          <div style={{ display: 'flex', alignItems: 'flex-end' }}>
            <button
              className="btn btn-secondary w-full"
              onClick={handleSubnetScan}
              disabled={scanning}
            >
              <Radio size={14} /> {scanning ? 'Scanning...' : 'SCAN CIDR RANGE'}
            </button>
          </div>
        </div>
      </div>

      {/* SCANNING PROGRESS BANNER */}
      {scanning && (
        <div className="card scanning-banner" style={{ marginBottom: '24px' }}>
          <div className="card-header">
            <div>
              <h3 className="card-title text-primary">SCANNING NETWORK RANGE</h3>
              <div className="card-sub mono">{activeSubnet}</div>
            </div>
            <RefreshCw className="spin" size={20} color="var(--primary)" />
          </div>
          <div className="scan-progress-wrapper">
            <div className="scan-progress-bar indeterminate" />
          </div>
          <div className="scan-metrics-row mono text-muted">
            <span>Probing IP range...</span>
            <span>Concurrent Thread Pool Workers Active</span>
          </div>
        </div>
      )}

      {/* DISCOVERY RESULTS SUMMARY & HOSTS TABLE */}
      {scanResults && (
        <div className="card">
          <div className="card-header">
            <div>
              <h3 className="card-title">DISCOVERY RESULTS</h3>
              <div className="card-sub mono">
                CIDR: {scanResults.subnetCidr} • Scanned {scanResults.totalScanned || 254} hosts in {scanResults.scanDurationMs || 0}ms
              </div>
            </div>
            {selectedImportIps.size > 0 && (
              <button className="btn btn-primary btn-sm" onClick={handleImportSelected}>
                <Plus size={14} /> Import Selected ({selectedImportIps.size})
              </button>
            )}
          </div>

          {/* SUMMARY CHIPS */}
          <div className="kpi-grid" style={{ marginBottom: '20px' }}>
            <div className="kpi-card">
              <span className="kpi-label">TOTAL DISCOVERED</span>
              <div className="kpi-value mono">{scanResults.devicesDiscoveredCount || 0}</div>
            </div>
            <div className="kpi-card green-accent">
              <span className="kpi-label">NEW ENDPOINTS</span>
              <div className="kpi-value green-text mono">{scanResults.newDevicesCount || 0}</div>
            </div>
            <div className="kpi-card blue-accent">
              <span className="kpi-label">ALREADY MONITORED</span>
              <div className="kpi-value blue-text mono">{scanResults.existingDevicesCount || 0}</div>
            </div>
          </div>

          {/* DISCOVERED CANDIDATES TABLE */}
          {!scanResults.discoveredDevices || scanResults.discoveredDevices.length === 0 ? (
            <EmptyState
              title="No Hosts Found on Subnet"
              description="No active IPv4 endpoints responded to the discovery probe."
            />
          ) : (
            <div className="table-responsive">
              <table className="data-table">
                <thead>
                  <tr>
                    <th style={{ width: '40px' }}>
                      <input
                        type="checkbox"
                        onChange={handleToggleSelectAll}
                        checked={
                          selectedImportIps.size > 0 &&
                          scanResults.discoveredDevices.filter(d => !d.alreadyMonitored).length === selectedImportIps.size
                        }
                      />
                    </th>
                    <th>IP ADDRESS</th>
                    <th>HOSTNAME</th>
                    <th>MAC ADDRESS</th>
                    <th>VENDOR (OUI)</th>
                    <th>OS CLUE</th>
                    <th>TYPE</th>
                    <th>STATUS</th>
                    <th style={{ textAlign: 'right' }}>ACTIONS</th>
                  </tr>
                </thead>
                <tbody>
                  {scanResults.discoveredDevices.map((host) => {
                    const isMonitored = host.alreadyMonitored;
                    const isSelected = selectedImportIps.has(host.ipAddress);

                    return (
                      <tr key={host.ipAddress} className={isMonitored ? 'row-monitored' : ''}>
                        <td>
                          {!isMonitored && (
                            <input
                              type="checkbox"
                              checked={isSelected}
                              onChange={() => handleToggleSelectIp(host.ipAddress)}
                            />
                          )}
                        </td>
                        <td className="mono font-semibold">{host.ipAddress}</td>
                        <td className="mono text-muted">{host.hostname || '—'}</td>
                        <td className="mono">{host.macAddress || 'N/A'}</td>
                        <td className="text-muted">{host.vendor || 'Generic'}</td>
                        <td className="text-muted">{host.osClue || 'N/A'}</td>
                        <td>
                          <span className="badge badge-subtle">{host.suggestedType || 'UNKNOWN'}</span>
                        </td>
                        <td>
                          <span className={`status-badge ${host.reachable ? 'open' : 'closed'}`}>
                            {host.reachable ? 'REACHABLE' : 'UNREACHABLE'}
                          </span>
                        </td>
                        <td style={{ textAlign: 'right' }}>
                          {isMonitored ? (
                            <span className="badge badge-success">
                              <CheckCircle size={12} /> Monitored
                            </span>
                          ) : (
                            <button
                              className="btn btn-secondary btn-xs"
                              onClick={() => onImportDevices([{
                                name: host.suggestedName || host.hostname || host.ipAddress,
                                ipAddress: host.ipAddress,
                                hostname: host.hostname,
                                deviceType: host.suggestedType || 'UNKNOWN',
                                vendor: host.vendor,
                                macAddress: host.macAddress,
                                osClue: host.osClue
                              }])}
                            >
                              <Plus size={12} /> Import
                            </button>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </section>
  );
}
