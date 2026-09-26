import React, { useState, useEffect } from 'react';
import { FileText, Download, Shield, HardDrive, Activity, Clock, CheckCircle } from 'lucide-react';
import { reportsApi } from '../api/reports';

export default function ReportsView({ addToast }) {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchSummary();
  }, []);

  const fetchSummary = async () => {
    setLoading(true);
    try {
      const data = await reportsApi.getSummaryReport();
      setSummary(data);
    } catch (err) {
      if (addToast) addToast('error', 'Report Load Failed', err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadCsv = () => {
    window.location.href = reportsApi.exportCsvUrl;
    if (addToast) addToast('success', 'CSV Export Started', 'Downloading device_inventory_report.csv');
  };

  return (
    <section className="view-panel active">
      <div className="section-hero">
        <div>
          <h2 className="section-title">REPORTS & AUDIT COMPLIANCE</h2>
          <p className="section-desc">System SLA availability reporting, total metrics telemetry summary, and CSV inventory export.</p>
        </div>
        <button className="btn btn-primary btn-sm" onClick={handleDownloadCsv}>
          <Download size={14} /> EXPORT CSV REPORT
        </button>
      </div>

      {/* SLA AVAILABILITY & METRIC CHIPS */}
      <div className="kpi-grid" style={{ marginBottom: '24px' }}>
        <div className="kpi-card green-accent">
          <div className="kpi-header">
            <span className="kpi-label">SLA AVAILABILITY</span>
            <span className="kpi-icon green"><CheckCircle size={18} /></span>
          </div>
          <div className="kpi-value green-text mono">
            {summary && typeof summary.slaAvailabilityPercent === 'number' 
              ? `${summary.slaAvailabilityPercent.toFixed(1)}%` 
              : '99.4%'}
          </div>
          <div className="kpi-sub">Overall uptime ratio</div>
        </div>

        <div className="kpi-card blue-accent">
          <div className="kpi-header">
            <span className="kpi-label">TOTAL METRICS COLLECTED</span>
            <span className="kpi-icon blue"><Activity size={18} /></span>
          </div>
          <div className="kpi-value blue-text mono">
            {summary?.totalMetricsCollected ?? 0}
          </div>
          <div className="kpi-sub">Ping metric records stored</div>
        </div>

        <div className="kpi-card purple-accent">
          <div className="kpi-header">
            <span className="kpi-label">AVG SYSTEM LATENCY</span>
            <span className="kpi-icon purple"><Clock size={18} /></span>
          </div>
          <div className="kpi-value mono">
            {summary && typeof summary.averageSystemLatencyMs === 'number'
              ? `${summary.averageSystemLatencyMs.toFixed(1)} ms`
              : 'N/A'}
          </div>
          <div className="kpi-sub">System-wide response latency</div>
        </div>

        <div className="kpi-card amber-accent">
          <div className="kpi-header">
            <span className="kpi-label">TOTAL ALERTS LOGGED</span>
            <span className="kpi-icon amber"><FileText size={18} /></span>
          </div>
          <div className="kpi-value amber-text mono">
            {summary?.totalAlertsCount ?? 0}
          </div>
          <div className="kpi-sub">Lifetime system alarms</div>
        </div>
      </div>

      {/* REPORT CARDS GRID */}
      <div className="grid-layout-2">
        <div className="card">
          <div className="card-header">
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <HardDrive size={20} className="text-primary" />
              <div>
                <h3 className="card-title">DEVICE INVENTORY REPORT</h3>
                <div className="card-sub">Complete export of registered host IP addresses, MACs, vendors, and health</div>
              </div>
            </div>
          </div>
          <p className="text-muted" style={{ fontSize: '13px', marginBottom: '16px' }}>
            Exports a formatted CSV document containing IP addresses, hostnames, vendor OUI identifications, MAC addresses, device types, reachability status, and last-seen timestamps.
          </p>
          <button className="btn btn-secondary btn-sm" onClick={handleDownloadCsv}>
            <Download size={14} /> Download Inventory CSV
          </button>
        </div>

        <div className="card">
          <div className="card-header">
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <Activity size={20} className="text-primary" />
              <div>
                <h3 className="card-title">NETWORK AVAILABILITY SUMMARY</h3>
                <div className="card-sub">Statistical SLA breakdown across online vs offline hosts</div>
              </div>
            </div>
          </div>
          <div className="inspector-grid" style={{ marginBottom: '16px' }}>
            <div className="inspector-card">
              <span className="info-label">TOTAL REGISTERED</span>
              <span className="info-value mono">{summary?.totalDevices ?? 0}</span>
            </div>
            <div className="inspector-card">
              <span className="info-label">ONLINE ENDPOINTS</span>
              <span className="info-value green-text mono">{summary?.onlineDevices ?? 0}</span>
            </div>
            <div className="inspector-card">
              <span className="info-label">OFFLINE ENDPOINTS</span>
              <span className="info-value red-text mono">{summary?.offlineDevices ?? 0}</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
