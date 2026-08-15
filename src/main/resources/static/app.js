// Network Device Monitoring Dashboard SPA Client
const API_BASE = '/api';

// Global Application State
const state = {
    devices: [],
    alerts: [],
    scheduler: { active: true, workerPoolSize: 10, lastRunTime: null, lastCycleDurationMs: 0 },
    nmapStatus: { available: false, binary: 'Checking...', version: 'None' },
    activeTab: 'overview',
    searchQuery: '',
    typeFilter: 'ALL',
    selectedDevice: null,
    selectedDeviceMetrics: [],
    selectedDevicePorts: [],
    selectedDeviceNmap: null,
    discoveredDevices: [],
    isScanning: false,
    stompConnected: false
};

// DOM Content Loaded Handler
document.addEventListener('DOMContentLoaded', () => {
    initApp();
});

async function initApp() {
    setupNavigation();
    setupEventListeners();
    await checkNmapStatus();
    await refreshAllData();
    initWebSockets();
    setInterval(refreshAllData, 12000); // Background polling fallback
}

// Navigation & Tab Switching
function setupNavigation() {
    const navBtns = document.querySelectorAll('.nav-btn');
    navBtns.forEach(btn => {
        btn.addEventListener('click', (e) => {
            const targetTab = e.currentTarget.dataset.tab;
            switchTab(targetTab);
        });
    });
}

function switchTab(tabId) {
    state.activeTab = tabId;
    document.querySelectorAll('.nav-btn').forEach(btn => {
        const isActive = btn.dataset.tab === tabId;
        btn.classList.toggle('active', isActive);
        if (isActive) {
            btn.className = "nav-btn active w-full flex items-center gap-3 px-4 py-3 rounded-lg text-primary bg-primary/10 border-r-4 border-primary transition-all text-xs font-bold uppercase tracking-wider";
        } else {
            btn.className = "nav-btn w-full flex items-center gap-3 px-4 py-3 rounded-lg text-on-surface-variant hover:text-primary hover:bg-white/5 transition-all text-xs font-bold uppercase tracking-wider";
        }
    });
    document.querySelectorAll('.tab-content').forEach(content => {
        const isTarget = content.id === `tab-${tabId}`;
        content.classList.toggle('hidden', !isTarget);
        content.style.display = isTarget ? 'block' : 'none';
    });
    renderActiveTab();
}

// Data Fetchers & API Calls
async function refreshAllData() {
    await Promise.all([
        fetchDevices(),
        fetchAlerts(),
        fetchSchedulerStatus()
    ]);
    renderKpis();
    renderActiveTab();
}

async function checkNmapStatus() {
    try {
        const res = await fetch(`${API_BASE}/discovery/nmap/status`);
        if (res.ok) {
            state.nmapStatus = await res.json();
        }
    } catch (err) {
        console.error('Nmap status check failed:', err);
    }
}

async function fetchDevices() {
    try {
        const res = await fetch(`${API_BASE}/devices`);
        if (res.ok) {
            state.devices = await res.json();
        }
    } catch (err) {
        console.error('Fetch devices failed:', err);
    }
}

async function fetchAlerts() {
    try {
        const res = await fetch(`${API_BASE}/alerts?includeResolved=true`);
        if (res.ok) {
            state.alerts = await res.json();
        }
    } catch (err) {
        console.error('Fetch alerts failed:', err);
    }
}

async function fetchSchedulerStatus() {
    try {
        const res = await fetch(`${API_BASE}/scheduler/status`);
        if (res.ok) {
            state.scheduler = await res.json();
        }
    } catch (err) {
        console.error('Fetch scheduler status failed:', err);
    }
}

// Rendering Logic
function renderKpis() {
    const totalDevices = state.devices.length;
    const onlineDevices = state.devices.filter(d => d.status === 'ONLINE').length;
    const offlineDevices = state.devices.filter(d => d.status === 'OFFLINE').length;
    const activeAlerts = state.alerts.filter(a => !a.resolved).length;

    document.getElementById('kpi-total-devices').innerText = totalDevices;
    document.getElementById('kpi-online-devices').innerText = `${onlineDevices} / ${totalDevices}`;
    document.getElementById('kpi-active-alerts').innerText = activeAlerts;
    document.getElementById('kpi-scheduler-status').innerText = state.scheduler.active ? 'ACTIVE' : 'PAUSED';

    const schedulerBadge = document.getElementById('scheduler-badge-dot');
    if (schedulerBadge) {
        schedulerBadge.style.backgroundColor = state.scheduler.active ? '#10b981' : '#f59e0b';
    }
}

function renderActiveTab() {
    if (state.activeTab === 'overview') {
        renderOverviewTab();
    } else if (state.activeTab === 'devices') {
        renderDevicesTab();
    } else if (state.activeTab === 'discovery') {
        renderDiscoveryTab();
    } else if (state.activeTab === 'alerts') {
        renderAlertsTab();
    } else if (state.activeTab === 'scheduler') {
        renderSchedulerTab();
    }
}

async function renderOverviewTab() {
    const activeAlertsContainer = document.getElementById('overview-active-alerts');
    const openAlerts = state.alerts.filter(a => !a.resolved).slice(0, 5);

    if (openAlerts.length === 0) {
        activeAlertsContainer.innerHTML = `<div class="text-center text-on-surface-variant py-8">🎉 All network segments operational. No critical alerts active!</div>`;
    } else {
        activeAlertsContainer.innerHTML = openAlerts.map(alert => `
            <div class="p-4 bg-white/5 border border-white/5 rounded-xl flex justify-between items-center border-l-4" style="border-left-color: ${getSeverityColor(alert.severity)};">
                <div>
                    <div class="font-semibold text-on-surface text-sm flex items-center gap-2">
                        <span>${escapeHtml(alert.deviceName)}</span>
                        <span class="font-mono text-xs text-on-surface-variant">(${escapeHtml(alert.deviceIp)})</span>
                    </div>
                    <div class="text-xs text-on-surface-variant mt-1">${escapeHtml(alert.message)}</div>
                </div>
                <button class="px-3 py-1.5 bg-primary/10 text-primary border border-primary/20 text-xs font-bold uppercase rounded-lg hover:bg-primary hover:text-background transition-colors" onclick="resolveAlert(${alert.id})">Acknowledge</button>
            </div>
        `).join('');
    }

    try {
        const res = await fetch(`${API_BASE}/reports/summary`);
        if (res.ok) {
            const report = await res.json();
            const slaEl = document.getElementById('overview-sla-percent');
            const latEl = document.getElementById('overview-avg-latency');
            if (slaEl) slaEl.innerText = `${report.slaAvailabilityPercent}%`;
            if (latEl) latEl.innerText = `${report.averageSystemLatencyMs} ms`;
        }
    } catch (err) {
        console.error('Fetch report summary failed:', err);
    }
}

function renderDevicesTab() {
    const tableBody = document.getElementById('devices-table-body');
    const filtered = state.devices.filter(d => {
        const matchesQuery = !state.searchQuery || 
            d.name.toLowerCase().includes(state.searchQuery.toLowerCase()) ||
            d.ipAddress.includes(state.searchQuery) ||
            (d.hostname && d.hostname.toLowerCase().includes(state.searchQuery.toLowerCase()));
        
        const matchesType = state.typeFilter === 'ALL' || d.deviceType === state.typeFilter;
        return matchesQuery && matchesType;
    });

    if (filtered.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="7" class="text-center text-on-surface-variant py-12">No devices found. Click "+ Register Device" to add your first device.</td></tr>`;
        return;
    }

    tableBody.innerHTML = filtered.map(d => `
        <tr class="hover:bg-white/5 transition-colors">
            <td class="py-4 px-6">
                <div class="font-medium text-on-surface font-sans">${escapeHtml(d.name)}</div>
                <div class="text-xs text-on-surface-variant font-sans">${escapeHtml(d.vendor || '')} ${escapeHtml(d.model || '')}</div>
            </td>
            <td class="py-4 px-6 text-on-surface-variant font-mono"><code>${escapeHtml(d.ipAddress)}</code></td>
            <td class="py-4 px-6 font-sans"><span class="px-2 py-0.5 rounded text-xs font-bold uppercase bg-primary/10 text-primary border border-primary/20">${escapeHtml(d.deviceType)}</span></td>
            <td class="py-4 px-6 font-sans">
                <span class="inline-flex items-center gap-1.5 px-2 py-0.5 rounded text-xs font-bold ${d.status === 'ONLINE' ? 'bg-secondary/10 text-secondary border border-secondary/20' : 'bg-error/10 text-error border border-error/20'}">
                    <span class="w-1.5 h-1.5 rounded-full ${d.status === 'ONLINE' ? 'bg-secondary pulse-emerald' : 'bg-error pulse-rose'}"></span> ${d.status}
                </span>
            </td>
            <td class="py-4 px-6 font-sans">
                <span class="px-2 py-0.5 rounded text-xs font-bold ${getHealthBadgeClass(d.healthStatus)}">
                    ${d.healthStatus || 'HEALTHY'}
                </span>
            </td>
            <td class="py-4 px-6 font-sans">
                <label class="relative inline-flex items-center cursor-pointer">
                    <input type="checkbox" ${d.monitoringEnabled ? 'checked' : ''} onchange="toggleDeviceMonitoring(${d.id})" class="sr-only peer">
                    <div class="w-9 h-5 bg-white/10 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-secondary"></div>
                </label>
            </td>
            <td class="py-4 px-6 text-right font-sans">
                <div class="flex items-center justify-end gap-2">
                    <button class="px-2.5 py-1 glass-panel text-xs text-on-surface hover:text-primary rounded-lg" onclick="openDeviceDetail(${d.id})" title="Inspect Device">📊 Inspect</button>
                    <button class="px-2.5 py-1 bg-primary/10 text-primary border border-primary/20 text-xs rounded-lg hover:bg-primary hover:text-background transition-colors" onclick="triggerPingCheck(${d.id})" title="Ping Check">⚡ Ping</button>
                    <button class="px-2 py-1 text-on-surface-variant hover:text-error transition-colors" onclick="deleteDevice(${d.id})" title="Delete Device">🗑️</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function getHealthBadgeClass(status) {
    if (status === 'CRITICAL') return 'bg-error/10 text-error border border-error/20';
    if (status === 'WARNING') return 'bg-tertiary/10 text-tertiary border border-tertiary/20';
    return 'bg-secondary/10 text-secondary border border-secondary/20';
}

function renderAlertsTab() {
    const alertsContainer = document.getElementById('alerts-list-container');
    if (state.alerts.length === 0) {
        alertsContainer.innerHTML = `<div class="text-center text-on-surface-variant py-12">No alerts recorded in system history.</div>`;
        return;
    }

    alertsContainer.innerHTML = state.alerts.map(a => `
        <div class="p-4 bg-white/5 border border-white/5 rounded-xl flex justify-between items-center border-l-4" style="border-left-color: ${getSeverityColor(a.severity)};">
            <div>
                <div class="flex items-center gap-3 mb-1">
                    <span class="px-2 py-0.5 rounded text-xs font-bold uppercase" style="background: ${getSeverityColor(a.severity)}22; color: ${getSeverityColor(a.severity)}; border: 1px solid ${getSeverityColor(a.severity)}44;">${a.severity}</span>
                    <span class="font-bold text-on-surface text-sm">${escapeHtml(a.deviceName)} (${escapeHtml(a.deviceIp)})</span>
                    <span class="text-xs text-on-surface-variant">• ${new Date(a.createdAt).toLocaleString()}</span>
                </div>
                <div class="text-xs text-on-surface mt-1">${escapeHtml(a.message)}</div>
            </div>
            <div>
                ${a.resolved 
                    ? `<span class="px-3 py-1 rounded-full text-xs font-semibold bg-secondary/10 text-secondary border border-secondary/20">✓ Resolved ${a.resolvedAt ? new Date(a.resolvedAt).toLocaleTimeString() : ''}</span>`
                    : `<button class="px-3 py-1.5 bg-primary/10 text-primary border border-primary/20 text-xs font-bold uppercase rounded-lg hover:bg-primary hover:text-background transition-colors" onclick="resolveAlert(${a.id})">Acknowledge</button>`
                }
            </div>
        </div>
    `).join('');
}

function renderSchedulerTab() {
    document.getElementById('sched-active-state').innerText = state.scheduler.active ? 'ACTIVE' : 'PAUSED';
    document.getElementById('sched-pool-size').innerText = `${state.scheduler.workerPoolSize || 10} Worker Threads`;
    document.getElementById('sched-last-run').innerText = state.scheduler.lastRunTime ? new Date(state.scheduler.lastRunTime).toLocaleString() : 'Never';
    document.getElementById('sched-last-duration').innerText = `${state.scheduler.lastCycleDurationMs || 0} ms`;
    document.getElementById('sched-devices-scanned').innerText = `${state.scheduler.lastDevicesScannedCount || 0} Devices`;
}

// Device Detail Modal & Inspection
async function openDeviceDetail(deviceId) {
    const device = state.devices.find(d => d.id === deviceId);
    if (!device) return;

    state.selectedDevice = device;
    document.getElementById('modal-device-name').innerText = `${device.name} (${device.ipAddress})`;
    document.getElementById('device-modal').style.display = 'flex';

    // Fetch Metrics, Ports, Nmap
    await Promise.all([
        fetchDeviceMetrics(deviceId),
        fetchDevicePorts(deviceId)
    ]);

    renderDeviceMetricsChart();
    renderDevicePorts();
}

async function fetchDeviceMetrics(deviceId) {
    try {
        const res = await fetch(`${API_BASE}/devices/${deviceId}/metrics`);
        if (res.ok) {
            state.selectedDeviceMetrics = await res.json();
        }
    } catch (err) {
        console.error('Fetch metrics failed:', err);
    }
}

async function fetchDevicePorts(deviceId) {
    try {
        const res = await fetch(`${API_BASE}/devices/${deviceId}/ports`);
        if (res.ok) {
            state.selectedDevicePorts = await res.json();
        }
    } catch (err) {
        console.error('Fetch ports failed:', err);
    }
}

function renderDeviceMetricsChart() {
    const container = document.getElementById('metrics-chart-container');
    const metrics = state.selectedDeviceMetrics;

    if (!metrics || metrics.length === 0) {
        container.innerHTML = `<div style="text-align: center; color: var(--text-muted); padding-top: 80px;">No historical ping metrics recorded yet for this device. Click "Check Ping" to trigger scan.</div>`;
        return;
    }

    // Render SVG Line Chart
    const width = 650;
    const height = 180;
    const padding = 30;

    const latencies = metrics.map(m => m.latencyMs || 0);
    const maxLat = Math.max(...latencies, 10);
    const minLat = 0;

    const points = metrics.map((m, idx) => {
        const x = padding + (idx / Math.max(metrics.length - 1, 1)) * (width - 2 * padding);
        const y = height - padding - ((m.latencyMs || 0) / maxLat) * (height - 2 * padding);
        return `${x},${y}`;
    }).join(' ');

    container.innerHTML = `
        <svg width="100%" height="100%" viewBox="0 0 ${width} ${height}">
            <polyline fill="none" stroke="var(--primary)" stroke-width="3" points="${points}" />
            ${metrics.map((m, idx) => {
                const x = padding + (idx / Math.max(metrics.length - 1, 1)) * (width - 2 * padding);
                const y = height - padding - ((m.latencyMs || 0) / maxLat) * (height - 2 * padding);
                return `<circle cx="${x}" cy="${y}" r="4" fill="var(--primary)" />`;
            }).join('')}
        </svg>
        <div style="position: absolute; top: 10px; right: 16px; font-size: 12px; color: var(--text-muted);">Max: ${maxLat.toFixed(1)} ms</div>
    `;
}

function renderDevicePorts() {
    const container = document.getElementById('device-ports-container');
    const ports = state.selectedDevicePorts;

    if (!ports || ports.length === 0) {
        container.innerHTML = `<div style="color: var(--text-muted); text-align: center; padding: 20px;">No TCP port scan results recorded yet. Click "Scan TCP Ports" below.</div>`;
        return;
    }

    container.innerHTML = ports.map(p => `
        <div class="port-item">
            <div class="port-number">${p.port}</div>
            <div class="port-service">${escapeHtml(p.serviceName)}</div>
            <div class="${p.status === 'OPEN' ? 'port-state-open' : 'port-state-closed'}">${p.status}</div>
        </div>
    `).join('');
}

// Action Triggers
async function triggerPingCheck(deviceId) {
    try {
        const res = await fetch(`${API_BASE}/devices/${deviceId}/check`, { method: 'POST' });
        if (res.ok) {
            const result = await res.json();
            showToast(`Ping check for ${result.deviceName}: ${result.reachable ? 'ONLINE (' + result.latencyMs + 'ms)' : 'OFFLINE'}`);
            await refreshAllData();
            if (state.selectedDevice && state.selectedDevice.id === deviceId) {
                openDeviceDetail(deviceId);
            }
        }
    } catch (err) {
        showToast('Ping check failed', true);
    }
}

async function triggerPortScan(deviceId) {
    try {
        showToast('Executing TCP port scan on common ports...');
        const res = await fetch(`${API_BASE}/devices/${deviceId}/scan-ports`, { method: 'POST' });
        if (res.ok) {
            const result = await res.json();
            showToast(`TCP Port scan completed: ${result.openPortsCount} open ports found.`);
            await fetchDevicePorts(deviceId);
            renderDevicePorts();
        }
    } catch (err) {
        showToast('Port scan failed', true);
    }
}

async function triggerNmapScan(deviceId) {
    try {
        showToast('Running Nmap audit process...');
        const res = await fetch(`${API_BASE}/devices/${deviceId}/nmap-scan`, { method: 'POST' });
        if (res.ok) {
            const result = await res.json();
            state.selectedDeviceNmap = result;
            renderNmapResult(result);
            showToast(`Nmap audit completed in ${result.executionTimeMs}ms.`);
        }
    } catch (err) {
        showToast('Nmap audit failed', true);
    }
}

function renderNmapResult(result) {
    const container = document.getElementById('nmap-result-container');
    if (!result || !result.hosts || result.hosts.length === 0) {
        container.innerHTML = `<div style="color: var(--text-muted);">No Nmap host findings.</div>`;
        return;
    }

    const host = result.hosts[0];
    container.innerHTML = `
        <div style="background: rgba(139, 92, 246, 0.1); border: 1px solid rgba(139, 92, 246, 0.3); padding: 14px; border-radius: var(--radius-md);">
            <div style="font-weight: 700; color: var(--purple);">Host: ${escapeHtml(host.ipAddress)} (${escapeHtml(host.hostname)})</div>
            <div style="font-size: 13px; color: var(--text-muted); margin-top: 4px;">OS Match: <strong>${escapeHtml(host.osMatch || 'Unknown')}</strong></div>
            <div style="margin-top: 10px; font-weight: 600; font-size: 13px;">Open Services:</div>
            <ul style="margin-top: 4px; padding-left: 20px; font-size: 13px;">
                ${host.openPorts.map(p => `<li>Port ${p.port}/${p.protocol} - <strong>${escapeHtml(p.serviceName)}</strong> (${escapeHtml(p.serviceProduct || '')} ${escapeHtml(p.serviceVersion || '')})</li>`).join('')}
            </ul>
        </div>
    `;
}

async function toggleDeviceMonitoring(deviceId) {
    try {
        const res = await fetch(`${API_BASE}/devices/${deviceId}/toggle-monitoring`, { method: 'PATCH' });
        if (res.ok) {
            await fetchDevices();
            renderDevicesTab();
        }
    } catch (err) {
        showToast('Toggle monitoring failed', true);
    }
}

async function deleteDevice(deviceId) {
    if (!confirm('Are you sure you want to delete this device?')) return;
    try {
        const res = await fetch(`${API_BASE}/devices/${deviceId}`, { method: 'DELETE' });
        if (res.ok) {
            showToast('Device deleted');
            await refreshAllData();
        }
    } catch (err) {
        showToast('Delete device failed', true);
    }
}

async function resolveAlert(alertId) {
    try {
        const res = await fetch(`${API_BASE}/alerts/${alertId}/resolve`, { method: 'PATCH' });
        if (res.ok) {
            showToast('Alert acknowledged');
            await fetchAlerts();
            renderKpis();
            renderActiveTab();
        }
    } catch (err) {
        showToast('Resolve alert failed', true);
    }
}

// Subnet Discovery Scanner
async function startSubnetScan() {
    const cidr = document.getElementById('subnet-cidr-input').value.trim();
    const strategy = document.getElementById('subnet-strategy-select').value;
    const threads = parseInt(document.getElementById('subnet-threads-input').value) || 20;

    if (!cidr) {
        showToast('Please enter a valid CIDR range (e.g. 192.168.1.0/24)', true);
        return;
    }

    state.isScanning = true;
    document.getElementById('discovery-results-container').innerHTML = `<div style="text-align: center; padding: 40px; color: var(--primary);">Scanning subnet range ${cidr} with ${threads} worker threads...</div>`;

    try {
        const res = await fetch(`${API_BASE}/discovery/scan`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ subnetCidr: cidr, strategy: strategy, threads: threads, timeoutMs: 800 })
        });

        if (res.ok) {
            const data = await res.json();
            state.discoveredDevices = data.discoveredDevices || [];
            renderDiscoveryResults(data);
        } else {
            showToast('Subnet scan failed', true);
        }
    } catch (err) {
        showToast('Subnet scan error', true);
    } finally {
        state.isScanning = false;
    }
}

function renderDiscoveryResults(data) {
    const container = document.getElementById('discovery-results-container');
    const list = data.discoveredDevices || [];

    if (list.length === 0) {
        container.innerHTML = `<div style="text-align: center; color: var(--text-muted); padding: 32px;">No active hosts responded in range ${escapeHtml(data.subnetCidr)}.</div>`;
        return;
    }

    container.innerHTML = `
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
            <div>Discovered <strong>${data.devicesDiscoveredCount}</strong> active hosts out of ${data.totalScanned} scanned in ${data.scanDurationMs}ms (${data.newDevicesCount} new).</div>
            <button class="btn btn-primary btn-sm" onclick="importDiscoveredDevices()">Import Selected New Devices</button>
        </div>
        <table class="data-table">
            <thead>
                <tr>
                    <th><input type="checkbox" id="select-all-discovery" onchange="toggleSelectAllDiscovery(this.checked)"></th>
                    <th>IP Address</th>
                    <th>Hostname</th>
                    <th>Suggested Name</th>
                    <th>Device Type</th>
                    <th>Status</th>
                </tr>
            </thead>
            <tbody>
                ${list.map((item, idx) => `
                    <tr>
                        <td><input type="checkbox" class="discovery-checkbox" data-idx="${idx}" ${item.alreadyMonitored ? 'disabled' : 'checked'}></td>
                        <td><code>${escapeHtml(item.ipAddress)}</code></td>
                        <td>${escapeHtml(item.hostname)}</td>
                        <td>${escapeHtml(item.suggestedName)}</td>
                        <td><span class="badge" style="background: rgba(59,130,246,0.15); color: var(--primary);">${escapeHtml(item.suggestedType)}</span></td>
                        <td>${item.alreadyMonitored ? '<span class="badge badge-warning">Already Monitored</span>' : '<span class="badge badge-online">New Host</span>'}</td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;
}

function toggleSelectAllDiscovery(checked) {
    document.querySelectorAll('.discovery-checkbox:not([disabled])').forEach(cb => cb.checked = checked);
}

async function importDiscoveredDevices() {
    const checkboxes = document.querySelectorAll('.discovery-checkbox:checked');
    const toImport = [];

    checkboxes.forEach(cb => {
        const idx = parseInt(cb.dataset.idx);
        const item = state.discoveredDevices[idx];
        if (item && !item.alreadyMonitored) {
            toImport.push({
                name: item.suggestedName,
                ipAddress: item.ipAddress,
                hostname: item.hostname !== item.ipAddress ? item.hostname : null,
                deviceType: item.suggestedType,
                monitoringEnabled: true,
                scanInterval: 10
            });
        }
    });

    if (toImport.length === 0) {
        showToast('No new devices selected for import', true);
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/discovery/import`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(toImport)
        });

        if (res.ok) {
            const imported = await res.json();
            showToast(`Successfully imported ${imported.length} new devices into monitoring engine!`);
            await refreshAllData();
            switchTab('devices');
        }
    } catch (err) {
        showToast('Import devices failed', true);
    }
}

// Scheduler Start/Stop
async function toggleScheduler(start) {
    const endpoint = start ? `${API_BASE}/scheduler/start` : `${API_BASE}/scheduler/stop`;
    try {
        const res = await fetch(endpoint, { method: 'POST' });
        if (res.ok) {
            state.scheduler = await res.json();
            showToast(`Scheduler ${start ? 'Started' : 'Paused'}`);
            renderKpis();
            renderSchedulerTab();
        }
    } catch (err) {
        showToast('Scheduler toggle failed', true);
    }
}

// Device Form Submit
async function submitDeviceForm(e) {
    e.preventDefault();
    const dto = {
        name: document.getElementById('dev-name').value.trim(),
        ipAddress: document.getElementById('dev-ip').value.trim(),
        hostname: document.getElementById('dev-hostname').value.trim() || null,
        deviceType: document.getElementById('dev-type').value,
        vendor: document.getElementById('dev-vendor').value.trim() || null,
        model: document.getElementById('dev-model').value.trim() || null,
        monitoringEnabled: true,
        scanInterval: 10
    };

    try {
        const res = await fetch(`${API_BASE}/devices`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto)
        });

        if (res.ok) {
            showToast('Device registered successfully!');
            closeModal('register-modal');
            await refreshAllData();
        } else {
            const err = await res.json();
            showToast(err.message || 'Validation failed', true);
        }
    } catch (err) {
        showToast('Register device failed', true);
    }
}

// WebSockets Initializer (SockJS/STOMP client)
function initWebSockets() {
    try {
        const socket = new SockJS('/ws-monitoring');
        const stompClient = Stomp.over(socket);
        stompClient.debug = null; // Suppress verbose console logs

        stompClient.connect({}, () => {
            state.stompConnected = true;
            updateWsBadge(true);

            stompClient.subscribe('/topic/metrics', (msg) => {
                const metric = JSON.parse(msg.body);
                updateLiveMetric(metric);
            });

            stompClient.subscribe('/topic/alerts', (msg) => {
                const alert = JSON.parse(msg.body);
                state.alerts.unshift(alert);
                renderKpis();
                if (state.activeTab === 'alerts' || state.activeTab === 'overview') renderActiveTab();
            });
        }, () => {
            state.stompConnected = false;
            updateWsBadge(false);
        });
    } catch (err) {
        updateWsBadge(false);
    }
}

async function triggerSnmpCheck(deviceId) {
    try {
        showToast('Querying SNMP hardware metrics...');
        const res = await fetch(`${API_BASE}/devices/${deviceId}/snmp-check`, { method: 'POST' });
        if (res.ok) {
            const result = await res.json();
            renderSnmpResult(result);
            showToast(`SNMP query completed: CPU ${result.cpuUsagePercent}%, Memory ${result.memoryUsagePercent}%.`);
        }
    } catch (err) {
        showToast('SNMP query failed', true);
    }
}

function renderSnmpResult(result) {
    const container = document.getElementById('nmap-result-container');
    if (!result) return;

    container.innerHTML = `
        <div class="bg-primary/10 border border-primary/20 p-3 rounded-lg text-xs space-y-1">
            <div class="font-bold text-primary">SNMP Hardware Query Result (Community: ${escapeHtml(result.community)})</div>
            <div>System Uptime: <strong>${Math.floor(result.sysUptimeSeconds / 86400)} days, ${Math.floor((result.sysUptimeSeconds % 86400) / 3600)} hours</strong></div>
            <div>CPU Load: <strong class="text-secondary">${result.cpuUsagePercent}%</strong></div>
            <div>Memory Usage: <strong class="text-tertiary">${result.memoryUsagePercent}%</strong></div>
            <div>Network Interfaces: <strong>${result.networkInterfacesCount} active interfaces</strong></div>
        </div>
    `;
}

function closeModal(modalId) {
    document.getElementById(modalId).classList.add('hidden');
}

function updateWsBadge(connected) {
    const badge = document.getElementById('ws-connection-badge');
    if (badge) {
        badge.className = `flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold ${connected ? 'bg-emerald-500/10 text-secondary border border-secondary/20' : 'bg-rose-500/10 text-error border border-error/20'}`;
        badge.innerHTML = `<span class="w-2 h-2 rounded-full ${connected ? 'bg-secondary pulse-emerald' : 'bg-error pulse-rose'}"></span> ${connected ? 'LIVE WS CONNECTED' : 'REST POLLING ACTIVE'}`;
    }
}

function updateLiveMetric(metric) {
    const device = state.devices.find(d => d.id === metric.deviceId);
    if (device) {
        device.status = metric.deviceStatus;
        renderKpis();
        if (state.activeTab === 'devices') renderDevicesTab();
    }
}

// Helpers
function setupEventListeners() {
    document.getElementById('search-devices-input')?.addEventListener('input', (e) => {
        state.searchQuery = e.target.value;
        renderDevicesTab();
    });

    document.getElementById('filter-device-type')?.addEventListener('change', (e) => {
        state.typeFilter = e.target.value;
        renderDevicesTab();
    });
}

function closeModal(modalId) {
    document.getElementById(modalId).style.display = 'none';
}

function getSeverityColor(sev) {
    if (sev === 'CRITICAL') return '#ef4444';
    if (sev === 'WARNING') return '#f59e0b';
    return '#3b82f6';
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str).replace(/[&<>"']/g, m => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    })[m]);
}

function showToast(msg, isError = false) {
    const toast = document.createElement('div');
    toast.style.cssText = `
        position: fixed; bottom: 24px; right: 24px; z-index: 2000;
        background: ${isError ? 'var(--offline)' : 'var(--primary)'};
        color: #fff; padding: 12px 20px; border-radius: var(--radius-md);
        font-weight: 600; font-size: 14px; box-shadow: 0 10px 25px rgba(0,0,0,0.5);
    `;
    toast.innerText = msg;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3500);
}
