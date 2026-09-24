import React, { useState } from 'react';
import { Radio, Play, Download } from 'lucide-react';

export default function DiscoveryView({
  localNetworks,
  onStartSubnetScan,
  onImportSelected,
  onAutoDiscovery
}) {
  const [subnetInput, setSubnetInput] = useState('192.168.1.0/24');
  const [strategy, setStrategy] = useState('PING');
  const [workers, setWorkers] = useState(40);
  const [isScanning, setIsScanning] = useState(false);
  const [discoveredHosts, setDiscoveredHosts] = useState([]);
  const [selectedHosts, setSelectedHosts] = useState({});

  const handleScan = async () => {
    setIsScanning(true);
    try {
      const res = await fetch(`/api/discovery/scan?subnet=${encodeURIComponent(subnetInput)}&strategy=${strategy}&threads=${workers}`, {
        method: 'POST'
      });
      if (res.ok) {
        const data = await res.json();
        setDiscoveredHosts(data || []);
      }
    } catch (err) {
      console.error('Scan error:', err);
    } finally {
      setIsScanning(false);
    }
  };

  const handleToggleHost = (ip) => {
    setSelectedHosts(prev => ({
      ...prev,
      [ip]: !prev[ip]
    }));
  };

  const handleImport = () => {
    const toImport = discoveredHosts.filter(h => selectedHosts[h.ipAddress]);
    if (onImportSelected) onImportSelected(toImport);
  };

  return (
    <section className="view-panel active">
      <div className="section-hero">
        <div>
          <h2 className="section-title">NETWORK DISCOVERY</h2>
          <p className="section-desc">Probing attached interfaces, subnet scanning, and host onboarding.</p>
        </div>
        <button className="btn btn-gradient-purple btn-sm" onClick={onAutoDiscovery}>
          ⚡ Run Auto-Discovery
        </button>
      </div>

      {/* LOCAL NETWORK INTERFACES */}
      <div className="card" style={{ marginBottom: '24px' }}>
        <div className="card-header">
          <h3 className="card-title">LOCAL NETWORK INTERFACES</h3>
        </div>
        <div className="local-net-grid">
          {(!localNetworks || localNetworks.length === 0) ? (
            <div className="empty-state">Detecting local interfaces...</div>
          ) : (
            localNetworks.map((net, idx) => (
              <div key={idx} className="local-net-card">
                <div className="local-net-icon"><Radio size={20} /></div>
                <div>
                  <div style={{ fontWeight: 700, color: '#fff', fontSize: '14px' }}>{net.interfaceName || 'wlan0'}</div>
                  <div className="mono" style={{ fontSize: '12px', color: 'var(--accent-cyan)' }}>{net.ipAddress}</div>
                  <div className="mono" style={{ fontSize: '11px', color: 'var(--text-subtle)' }}>{net.networkCidr}</div>
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      {/* SUBNET SCANNER */}
      <div className="card">
        <div className="card-header">
          <h3 className="card-title">SUBNET RANGE SCANNER</h3>
        </div>

        <div className="scan-form-grid">
          <div className="form-group">
            <label className="form-label">Subnet Range (CIDR)</label>
            <input
              type="text"
              className="form-input mono"
              value={subnetInput}
              onChange={(e) => setSubnetInput(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Strategy</label>
            <select className="form-select" value={strategy} onChange={(e) => setStrategy(e.target.value)}>
              <option value="PING">ICMP Ping Scan</option>
              <option value="TCP">TCP Port Scan</option>
              <option value="NMAP">Nmap Engine</option>
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Worker Threads</label>
            <input
              type="number"
              className="form-input"
              value={workers}
              onChange={(e) => setWorkers(Number(e.target.value))}
              min="1"
              max="100"
            />
          </div>

          <button className="btn btn-primary" onClick={handleScan} disabled={isScanning}>
            {isScanning ? 'Scanning...' : '⚡ Start Range Scan'}
          </button>
        </div>

        {isScanning && (
          <div className="progress-container">
            <div className="progress-bar" style={{ width: '60%' }}></div>
          </div>
        )}

        <div className="discovery-status-banner">
          <div>
            {isScanning ? 'Scanning subnet range...' : `Ready to scan. Found ${discoveredHosts.length} hosts.`}
          </div>
          {discoveredHosts.length > 0 && (
            <button className="btn btn-gradient-purple btn-sm" onClick={handleImport}>
              <Download size={14} /> Import Selected
            </button>
          )}
        </div>

        <div className="table-container" style={{ marginTop: '16px' }}>
          <table className="data-table">
            <thead>
              <tr>
                <th style={{ width: '40px', textAlign: 'center' }}>SELECT</th>
                <th>IP ADDRESS</th>
                <th>HOSTNAME</th>
                <th>SUGGESTED NAME</th>
                <th>DEVICE TYPE</th>
                <th>STATUS</th>
              </tr>
            </thead>
            <tbody>
              {discoveredHosts.length === 0 ? (
                <tr>
                  <td colSpan="6" className="empty-state">
                    No scan performed yet. Click "Start Range Scan" to discover active endpoints on your subnet.
                  </td>
                </tr>
              ) : (
                discoveredHosts.map((h, i) => (
                  <tr key={i}>
                    <td style={{ textAlign: 'center' }}>
                      <input
                        type="checkbox"
                        checked={!!selectedHosts[h.ipAddress]}
                        onChange={() => handleToggleHost(h.ipAddress)}
                      />
                    </td>
                    <td className="mono" style={{ fontWeight: 600, color: '#fff' }}>{h.ipAddress}</td>
                    <td className="mono">{h.hostname || 'N/A'}</td>
                    <td>{h.suggestedName || h.ipAddress}</td>
                    <td><span className="badge badge-new">{h.suggestedType || 'WORKSTATION'}</span></td>
                    <td>
                      <span className="badge badge-online">
                        <span className="status-dot online"></span> ONLINE
                      </span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </section>
  );
}
