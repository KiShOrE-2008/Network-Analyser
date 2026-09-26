import React, { useState, useEffect } from 'react';
import { Settings, Server, Database, Terminal, Shield, RefreshCw, CheckCircle, XCircle } from 'lucide-react';
import { systemApi } from '../api/system';

export default function SettingsView({
  healthStatus = null,
  schedulerStatus = null,
  nmapStatus = null,
  onRefreshDiagnostics,
  onToggleScheduler,
  addToast
}) {
  const [scanInterval, setScanInterval] = useState('10');
  const [discoveryInterval, setDiscoveryInterval] = useState('60');
  const [snmpCommunity, setSnmpCommunity] = useState('public');
  const [testing, setTesting] = useState(false);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    fetchSettings();
  }, []);

  const fetchSettings = async () => {
    try {
      const data = await systemApi.getSettings();
      if (data) {
        if (data.scanInterval) setScanInterval(String(data.scanInterval));
        if (data.discoveryInterval) setDiscoveryInterval(String(data.discoveryInterval));
        if (data.snmpCommunity) setSnmpCommunity(data.snmpCommunity);
      }
    } catch (err) {
      console.error('Failed to load system settings', err);
    }
  };

  const handleSaveSettings = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const updated = {
        scanInterval: parseInt(scanInterval, 10) || 10,
        discoveryInterval: parseInt(discoveryInterval, 10) || 60,
        snmpCommunity: snmpCommunity || 'public',
        schedulerEnabled: schedulerStatus?.active ?? true
      };
      await systemApi.saveSettings(updated);
      if (addToast) addToast('success', 'Settings Saved', 'Monitoring configuration persisted successfully.');
    } catch (err) {
      if (addToast) addToast('error', 'Save Failed', err.message);
    } finally {
      setSaving(false);
    }
  };

  const handleTestBackend = async () => {
    setTesting(true);
    try {
      await onRefreshDiagnostics();
      if (addToast) addToast('success', 'Diagnostics Complete', 'Spring Boot backend and database operational.');
    } catch (err) {
      if (addToast) addToast('error', 'Diagnostics Failed', err.message);
    } finally {
      setTesting(false);
    }
  };

  return (
    <section className="view-panel active">
      <div className="section-hero">
        <div>
          <h2 className="section-title">SYSTEM CONFIGURATION & DIAGNOSTICS</h2>
          <p className="section-desc">Monitoring intervals, discovery parameters, SNMP community configurations, and engine health checks.</p>
        </div>
      </div>

      <div className="grid-layout-2">
        {/* CONFIGURATION FORM */}
        <div className="card">
          <div className="card-header">
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Settings size={18} className="text-primary" />
              <h3 className="card-title">MONITORING & DISCOVERY PARAMETERS</h3>
            </div>
          </div>

          <form onSubmit={handleSaveSettings}>
            <div className="form-group">
              <label className="form-label">DEFAULT MONITORING CYCLE INTERVAL (SECONDS)</label>
              <input
                type="number"
                className="form-input mono"
                value={scanInterval}
                onChange={(e) => setScanInterval(e.target.value)}
                min="5"
                max="300"
              />
              <span className="text-muted" style={{ fontSize: '11px', marginTop: '4px', display: 'block' }}>
                Interval between background worker ICMP reachability checks.
              </span>
            </div>

            <div className="form-group">
              <label className="form-label">AUTO-DISCOVERY SUBNET SCAN INTERVAL (SECONDS)</label>
              <input
                type="number"
                className="form-input mono"
                value={discoveryInterval}
                onChange={(e) => setDiscoveryInterval(e.target.value)}
                min="30"
                max="3600"
              />
            </div>

            <div className="form-group">
              <label className="form-label">DEFAULT SNMP COMMUNITY STRING</label>
              <input
                type="text"
                className="form-input mono"
                value={snmpCommunity}
                onChange={(e) => setSnmpCommunity(e.target.value)}
                placeholder="public"
              />
            </div>

            <div className="form-group">
              <label className="checkbox-label mono">
                <input
                  type="checkbox"
                  checked={schedulerStatus?.active ?? true}
                  onChange={onToggleScheduler}
                />
                Enable Background Monitoring Worker Scheduler
              </label>
            </div>

            <button type="submit" className="btn btn-primary btn-sm" style={{ marginTop: '12px' }}>
              Save Configuration
            </button>
          </form>
        </div>

        {/* SYSTEM DIAGNOSTICS */}
        <div className="card">
          <div className="card-header">
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Server size={18} className="text-primary" />
              <h3 className="card-title">SYSTEM ENGINE DIAGNOSTICS</h3>
            </div>
            <button className="btn btn-secondary btn-sm" onClick={handleTestBackend} disabled={testing}>
              <RefreshCw className={testing ? 'spin' : ''} size={14} /> Run Diagnostics
            </button>
          </div>

          <div className="inspector-grid" style={{ marginBottom: '20px' }}>
            <div className="inspector-card">
              <span className="info-label">BACKEND ENGINE</span>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginTop: '4px' }}>
                <span className={`status-dot-sm ${healthStatus?.status === 'UP' ? 'green' : 'red'}`} />
                <span className="info-value font-semibold">
                  {healthStatus?.service || 'NetScope Backend'} ({healthStatus?.status || 'DOWN'})
                </span>
              </div>
            </div>

            <div className="inspector-card">
              <span className="info-label">POSTGRESQL REPOSITORY</span>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginTop: '4px' }}>
                <span className={`status-dot-sm ${healthStatus?.database === 'Connected' ? 'green' : 'gray'}`} />
                <span className="info-value font-semibold">{healthStatus?.database || 'Connected'}</span>
              </div>
            </div>

            <div className="inspector-card">
              <span className="info-label">NMAP ENGINE BINARY</span>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginTop: '4px' }}>
                <span className={`status-dot-sm ${healthStatus?.nmapAvailable ? 'green' : 'gray'}`} />
                <span className="info-value font-semibold">
                  {healthStatus?.nmapAvailable ? 'Available (nmap)' : 'Not Found'}
                </span>
              </div>
            </div>

            <div className="inspector-card">
              <span className="info-label">APPLICATION VERSION</span>
              <span className="info-value mono">{healthStatus?.version || '1.0.0-SNAPSHOT'}</span>
            </div>
          </div>

          <div className="diagnostic-actions" style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
            <button className="btn btn-secondary btn-xs" onClick={handleTestBackend}>
              <Server size={12} /> Test Backend
            </button>
            <button className="btn btn-secondary btn-xs" onClick={handleTestBackend}>
              <Database size={12} /> Test Database
            </button>
            <button className="btn btn-secondary btn-xs" onClick={handleTestBackend}>
              <Terminal size={12} /> Check Nmap
            </button>
          </div>
        </div>
      </div>
    </section>
  );
}
