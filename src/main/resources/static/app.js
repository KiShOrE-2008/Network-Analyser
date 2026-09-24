// NetScope — Network Discovery & Monitoring Client Application
const API_BASE = '/api';

const state = {
  devices: [],
  alerts: [],
  events: [],
  discoveredHosts: [],
  localNetworks: [],
  schedulerStatus: null,
  health: null,
  backendConnected: true,
  activeView: 'landing',
  activeAlertFilter: 'ALL',
  selectedDeviceId: null,
  deviceToDeleteId: null
};

document.addEventListener('DOMContentLoaded', () => {
  initApp();
});

async function initApp() {
  setupNavigation();
  await refreshData();
  // Poll data every 10 seconds without full UI reset
  setInterval(refreshData, 10000);
}

// Navigation & View Switching
function setupNavigation() {
  const items = document.querySelectorAll('.nav-item');
  items.forEach(item => {
    item.addEventListener('click', (e) => {
      const view = e.currentTarget.dataset.view;
      switchView(view);
    });
  });
}

function switchView(viewId) {
  state.activeView = viewId;
  
  // Update nav highlight
  document.querySelectorAll('.nav-item').forEach(item => {
    item.classList.toggle('active', item.dataset.view === viewId);
  });

  // Update topbar title
  const titleMap = {
    landing: 'Project Information',
    overview: 'Overview',
    devices: 'Device Inventory',
    discovery: 'Network Discovery',
    monitoring: 'Monitoring Controls',
    alerts: 'Alert Center',
    reports: 'Reports & Export',
    settings: 'Settings & Diagnostics',
    help: 'Documentation & Help',
    about: 'About NetScope'
  };
  const titleElem = document.getElementById('pageTitle');
  if (titleElem) titleElem.textContent = titleMap[viewId] || 'NetScope';

  // Toggle view panels
  document.querySelectorAll('.view-panel').forEach(panel => {
    panel.classList.toggle('active', panel.id === `view-${viewId}`);
  });

  // Mobile sidebar close on navigation
  const sidebar = document.getElementById('sidebar');
  if (sidebar) sidebar.classList.remove('open');
}

function toggleSidebar() {
  const sidebar = document.getElementById('sidebar');
  if (sidebar) sidebar.classList.toggle('open');
}

// Centralized API Helper with Health Protection
async function fetchJson(url, options = {}) {
  try {
    const res = await fetch(API_BASE + url, options);
    if (!res.ok) {
      const errText = await res.text();
      throw new Error(errText || `HTTP ${res.status}`);
    }
    state.backendConnected = true;
    toggleConnectionLostBanner(false);
    return res.json();
  } catch (err) {
    if (url.startsWith('/devices') || url.startsWith('/health') || url.startsWith('/discovery')) {
      state.backendConnected = false;
      toggleConnectionLostBanner(true);
    }
    throw err;
  }
}

function toggleConnectionLostBanner(show) {
  const banner = document.getElementById('connectionLostBanner');
  if (banner) banner.style.display = show ? 'flex' : 'none';
  
  const topText = document.getElementById('topSysText');
  const topBadge = document.getElementById('topSysBadge');
  if (topText && topBadge) {
    if (show) {
      topText.textContent = 'CONNECTION LOST';
      topBadge.className = 'sys-badge offline';
    } else {
      topText.textContent = 'SYSTEM OPERATIONAL';
      topBadge.className = 'sys-badge';
    }
  }
}

// Toast Notifications
function showToast(msg, type = 'info') {
  const container = document.getElementById('toastContainer');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = 'toast';
  toast.textContent = msg;
  if (type === 'error') toast.style.borderLeftColor = 'var(--offline)';
  if (type === 'success') toast.style.borderLeftColor = 'var(--online)';

  container.appendChild(toast);
  setTimeout(() => {
    toast.remove();
  }, 3500);
}

// Main Data Fetching Cycle
async function refreshData() {
  try {
    const [health, devices, alerts, events, localNets, schedStatus] = await Promise.all([
      fetchJson('/health').catch(() => null),
      fetchJson('/devices').catch(() => []),
      fetchJson('/alerts?includeResolved=true').catch(() => []),
      fetchJson('/events').catch(() => []),
      fetchJson('/discovery/local-networks').catch(() => []),
      fetchJson('/scheduler/status').catch(() => null)
    ]);

    state.health = health;
    state.devices = devices;
    state.alerts = alerts;
    state.events = events;
    state.localNetworks = localNets;
    state.schedulerStatus = schedStatus;

    // Update timestamp
    const now = new Date();
    const timeStr = now.toTimeString().split(' ')[0];
    const tsElem = document.getElementById('lastUpdatedText');
    if (tsElem) tsElem.textContent = `Last updated: ${timeStr}`;

    // Update Sidebar & Topbar status
    updateSystemStatusBadge();

    // Render active view components
    renderKPIs();
    renderNetworkMap();
    renderActivityFeed();
    renderDevices();
    renderLocalNetworks();
    renderSchedulerDetails();
    renderAlerts();
    renderReportsSummary();
    renderSettingsDiagnostics();

  } catch (err) {
    console.error('Data refresh error:', err);
  }
}

// Update Status Badges
function updateSystemStatusBadge() {
  if (!state.backendConnected) return;

  const onlineCount = state.devices.filter(d => d.status === 'ONLINE').length;
  const total = state.devices.length;
  const isHealthy = total === 0 || (onlineCount / total) >= 0.7;

  // Sidebar dynamic network info
  const sbCidr = document.getElementById('sbNetworkCidr');
  if (sbCidr) {
    if (state.localNetworks.length > 0) {
      sbCidr.textContent = state.localNetworks[0].networkCidr || state.localNetworks[0].ipAddress;
    } else {
      sbCidr.textContent = '192.168.1.0/24';
    }
  }

  // Nav alert badge
  const activeAlertsCount = state.alerts.filter(a => !a.resolved).length;
  const alertBadge = document.getElementById('navAlertBadge');
  if (alertBadge) {
    alertBadge.textContent = activeAlertsCount;
    alertBadge.style.display = activeAlertsCount > 0 ? 'inline-block' : 'none';
  }

  const topText = document.getElementById('topSysText');
  const topBadge = document.getElementById('topSysBadge');
  if (topText && topBadge) {
    topText.textContent = isHealthy ? 'SYSTEM OPERATIONAL' : 'DEGRADED PERFORMANCE';
    topBadge.className = isHealthy ? 'sys-badge' : 'sys-badge offline';
  }
}

// 1. Render Overview KPIs
function renderKPIs() {
  const total = state.devices.length;
  const online = state.devices.filter(d => d.status === 'ONLINE').length;
  const offline = state.devices.filter(d => d.status === 'OFFLINE').length;

  document.getElementById('kTotal').textContent = total > 0 ? total : 'N/A';
  document.getElementById('kOnline').textContent = total > 0 ? online : 'N/A';
  document.getElementById('kOffline').textContent = total > 0 ? offline : 'N/A';

  const pctElem = document.getElementById('kOnlinePct');
  if (pctElem) {
    pctElem.textContent = total > 0 ? `${Math.round((online / total) * 100)}% Reachable endpoints` : 'Reachable endpoints';
  }

  // Calculate Average Latency from devices
  const validLatencies = state.devices
    .map(d => d._latency)
    .filter(l => typeof l === 'number' && !isNaN(l));

  const avgLat = validLatencies.length ? (validLatencies.reduce((a, b) => a + b, 0) / validLatencies.length).toFixed(1) : null;
  document.getElementById('kAvgLatency').textContent = avgLat !== null ? `${avgLat} ms` : 'N/A';
}

// 2. Render Network Device Map
function renderNetworkMap() {
  const container = document.getElementById('networkMapContainer');
  if (!container) return;

  if (!state.devices.length) {
    container.innerHTML = `<div class="empty-state">No devices registered. Click "Scan Network" to discover local endpoints.</div>`;
    return;
  }

  container.innerHTML = state.devices.map(d => {
    const isOnline = d.status === 'ONLINE';
    const isGateway = (d.ipAddress && d.ipAddress.endsWith('.1')) || d.deviceType === 'ROUTER';
    const typeIcon = getDeviceTypeIcon(d.deviceType);

    return `
      <div class="map-node ${isGateway ? 'gateway' : ''}" onclick="openDeviceDetails(${d.id})" role="button" aria-label="Inspect ${escapeHtml(d.name)}">
        <span class="map-node-icon">${typeIcon}</span>
        <div class="map-node-info">
          <div class="map-node-name">${escapeHtml(d.name)}</div>
          <div class="map-node-ip mono">
            <span class="status-dot ${isOnline ? 'online' : 'offline'}"></span>
            ${escapeHtml(d.ipAddress)}
          </div>
        </div>
      </div>
    `;
  }).join('');
}

// 3. Render Activity Feed
function renderActivityFeed() {
  const feed = document.getElementById('overviewActivityFeed');
  if (!feed) return;

  if (!state.events.length && !state.alerts.length) {
    feed.innerHTML = `<div class="empty-state">No recent activity events.</div>`;
    return;
  }

  const combined = [
    ...state.events.map(e => ({
      title: e.eventType || 'Network Event',
      sub: `${e.ipAddress || ''} — ${e.message || ''}`,
      time: e.eventTime ? formatTimeAgo(e.eventTime) : '',
      icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>',
      color: 'var(--accent-cyan)',
      bg: 'rgba(6, 182, 212, 0.15)'
    })),
    ...state.alerts.map(a => ({
      title: a.alertType || 'System Alert',
      sub: `${a.deviceName || a.ipAddress || ''} — ${a.message || ''}`,
      time: a.timestamp ? formatTimeAgo(a.timestamp) : '',
      icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>',
      color: a.severity === 'CRITICAL' ? 'var(--offline)' : 'var(--warning)',
      bg: a.severity === 'CRITICAL' ? 'rgba(244, 63, 94, 0.15)' : 'rgba(245, 158, 11, 0.15)'
    }))
  ].slice(0, 8);

  feed.innerHTML = combined.map(item => `
    <div class="feed-item">
      <div class="feed-icon" style="background: ${item.bg}; color:${item.color}">${item.icon}</div>
      <div class="feed-content">
        <div class="feed-header">
          <span class="feed-title">${escapeHtml(item.title)}</span>
          <span class="feed-time">${item.time}</span>
        </div>
        <div class="feed-sub">${escapeHtml(item.sub)}</div>
      </div>
    </div>
  `).join('');
}

// 4. Render Device Inventory Table
function renderDevices() {
  const tbody = document.getElementById('deviceRows');
  if (!tbody) return;

  const searchQuery = (document.getElementById('deviceSearch')?.value || '').toLowerCase();
  const filterType = document.getElementById('deviceTypeFilter')?.value || 'ALL';
  const filterStatus = document.getElementById('deviceStatusFilter')?.value || 'ALL';

  const filtered = state.devices.filter(d => {
    const matchesType = filterType === 'ALL' || d.deviceType === filterType;
    const matchesStatus = filterStatus === 'ALL' || d.status === filterStatus;
    const matchesSearch = [d.name, d.ipAddress, d.hostname, d.macAddress, d.vendor]
      .some(field => String(field || '').toLowerCase().includes(searchQuery));
    return matchesType && matchesStatus && matchesSearch;
  });

  if (!filtered.length) {
    tbody.innerHTML = `<tr><td colspan="10" class="empty-state">No devices match filter parameters.</td></tr>`;
    return;
  }

  tbody.innerHTML = filtered.map(d => {
    const isOnline = d.status === 'ONLINE';
    const statusBadge = `<span class="badge ${isOnline ? 'badge-online' : 'badge-offline'}"><span class="status-dot ${isOnline ? 'online' : 'offline'}"></span> ${d.status || 'UNKNOWN'}</span>`;
    const typeClass = `type-${(d.deviceType || 'UNKNOWN').toLowerCase()}`;
    const typePill = `<span class="type-pill ${typeClass}">${d.deviceType || 'UNKNOWN'}</span>`;
    const latency = typeof d._latency === 'number' ? `${d._latency.toFixed(1)} ms` : 'N/A';
    const lastSeen = d.lastSeen ? formatTimeAgo(d.lastSeen) : 'N/A';

    return `
      <tr onclick="openDeviceDetails(${d.id})">
        <td>${statusBadge}</td>
        <td><strong>${escapeHtml(d.name)}</strong></td>
        <td class="mono" style="font-weight:600;">${escapeHtml(d.ipAddress)}</td>
        <td class="mono" style="color:var(--text-muted);">${escapeHtml(d.hostname || 'N/A')}</td>
        <td class="mono" style="color:var(--text-subtle);">${escapeHtml(d.macAddress || 'N/A')}</td>
        <td>${escapeHtml(d.vendor || 'N/A')}</td>
        <td>${typePill}</td>
        <td class="mono">${latency}</td>
        <td style="color:var(--text-subtle);">${lastSeen}</td>
        <td style="text-align:right" onclick="event.stopPropagation()">
          <button class="btn btn-secondary btn-sm" onclick="openDeviceDetails(${d.id})" aria-label="Inspect ${escapeHtml(d.name)}">Inspect</button>
        </td>
      </tr>
    `;
  }).join('');
}

// 5. Open Device Detail Side Drawer
async function openDeviceDetails(deviceId) {
  state.selectedDeviceId = deviceId;
  const drawer = document.getElementById('deviceDrawer');
  const overlay = document.getElementById('drawerOverlay');

  if (!drawer || !overlay) return;

  drawer.classList.add('open');
  overlay.classList.add('open');

  const device = state.devices.find(d => d.id === deviceId);
  if (!device) return;

  // Basic Header Info
  document.getElementById('drawerDeviceName').textContent = device.name;
  document.getElementById('drawerIpAddress').textContent = device.ipAddress;

  const isOnline = device.status === 'ONLINE';
  const badgeContainer = document.getElementById('drawerStatusBadgeContainer');
  badgeContainer.innerHTML = `<span class="badge ${isOnline ? 'badge-online' : 'badge-offline'}"><span class="status-dot ${isOnline ? 'online' : 'offline'}"></span> ${device.status || 'UNKNOWN'}</span>`;

  // Identity Section
  document.getElementById('drawerHostname').textContent = device.hostname || 'N/A';
  document.getElementById('drawerMac').textContent = device.macAddress || 'N/A';
  document.getElementById('drawerVendor').textContent = device.vendor || 'N/A';
  document.getElementById('drawerType').textContent = device.deviceType || 'N/A';
  document.getElementById('drawerOs').textContent = device.osHint || 'N/A';

  // Connectivity Section
  const latVal = typeof device._latency === 'number' ? `${device._latency.toFixed(1)} ms` : 'N/A';
  document.getElementById('drawerLatency').textContent = latVal;
  document.getElementById('drawerPacketLoss').textContent = typeof device.packetLoss === 'number' ? `${device.packetLoss}%` : 'N/A';
  document.getElementById('drawerLastSeen').textContent = device.lastSeen ? formatTimeAgo(device.lastSeen) : 'N/A';

  // Fetch Open Ports asynchronously
  fetchJson(`/devices/${deviceId}/ports`).then(ports => {
    const list = document.getElementById('drawerPortsList');
    if (!ports || !ports.length) {
      list.innerHTML = `<div class="empty-state-sm">No port scan data loaded. Click "Scan Ports" below.</div>`;
      return;
    }
    list.innerHTML = ports.map(p => `<span class="port-tag">${p.portNumber}/${p.protocol || 'TCP'} (${p.serviceName || 'Open'})</span>`).join('');
  }).catch(() => {
    document.getElementById('drawerPortsList').innerHTML = `<div class="empty-state-sm">No port scan data available yet.</div>`;
  });

  // Fetch SNMP Telemetry asynchronously
  fetchJson(`/devices/${deviceId}/snmp`).then(snmp => {
    document.getElementById('drawerCpu').textContent = snmp && typeof snmp.cpuUsage === 'number' ? `${snmp.cpuUsage}%` : 'N/A';
    document.getElementById('drawerMemory').textContent = snmp && typeof snmp.memoryUsage === 'number' ? `${snmp.memoryUsage}%` : 'N/A';
    document.getElementById('drawerUptime').textContent = snmp && snmp.sysUptime ? snmp.sysUptime : 'N/A';
  }).catch(() => {
    document.getElementById('drawerCpu').textContent = 'N/A';
    document.getElementById('drawerMemory').textContent = 'N/A';
    document.getElementById('drawerUptime').textContent = 'N/A';
  });

  // Fetch History asynchronously
  fetchJson(`/history/device/${deviceId}`).then(hist => {
    const textElem = document.getElementById('drawerHistoryChartText');
    if (hist && hist.metrics && hist.metrics.length > 0) {
      textElem.textContent = `Recorded ${hist.metrics.length} historical metric samples.`;
    } else {
      textElem.textContent = `N/A — No historical data recorded yet.`;
    }
  }).catch(() => {
    document.getElementById('drawerHistoryChartText').textContent = `N/A — No historical data available yet.`;
  });
}

function closeDeviceDetails() {
  document.getElementById('deviceDrawer')?.classList.remove('open');
  document.getElementById('drawerOverlay')?.classList.remove('open');
}

// Drawer Device Action Handlers
async function runCheckOnCurrentDevice() {
  if (!state.selectedDeviceId) return;
  showToast('Initiating Ping Check probe...', 'info');
  try {
    const res = await fetchJson(`/devices/${state.selectedDeviceId}/check`, { method: 'POST' });
    showToast(`Ping Check completed: ${res.reachable ? 'REACHABLE' : 'UNREACHABLE'} (${res.latencyMs || 0} ms)`, res.reachable ? 'success' : 'error');
    refreshData();
    openDeviceDetails(state.selectedDeviceId);
  } catch (err) {
    showToast(`Check failed: ${err.message}`, 'error');
  }
}

async function scanPortsOnCurrentDevice() {
  if (!state.selectedDeviceId) return;
  showToast('Scanning host ports...', 'info');
  try {
    const res = await fetchJson(`/devices/${state.selectedDeviceId}/scan-ports`, { method: 'POST' });
    showToast(`Port scan complete. Open ports: ${res.openPortsCount || 0}`, 'success');
    openDeviceDetails(state.selectedDeviceId);
  } catch (err) {
    showToast(`Port scan failed: ${err.message}`, 'error');
  }
}

async function snmpCheckOnCurrentDevice() {
  if (!state.selectedDeviceId) return;
  showToast('Querying SNMP metrics...', 'info');
  try {
    const res = await fetchJson(`/devices/${state.selectedDeviceId}/snmp-check`, { method: 'POST' });
    showToast(`SNMP query complete. System: ${res.sysName || 'Responded'}`, 'success');
    openDeviceDetails(state.selectedDeviceId);
  } catch (err) {
    showToast(`SNMP query error: ${err.message}`, 'error');
  }
}

async function toggleMonitoringOnCurrentDevice() {
  if (!state.selectedDeviceId) return;
  try {
    const res = await fetchJson(`/devices/${state.selectedDeviceId}/toggle-monitoring`, { method: 'PATCH' });
    showToast(`Monitoring ${res.monitoringEnabled ? 'ENABLED' : 'DISABLED'} for ${res.name}`, 'success');
    refreshData();
    openDeviceDetails(state.selectedDeviceId);
  } catch (err) {
    showToast(`Failed to toggle monitoring: ${err.message}`, 'error');
  }
}

function confirmDeleteCurrentDevice() {
  if (!state.selectedDeviceId) return;
  const dev = state.devices.find(d => d.id === state.selectedDeviceId);
  if (!dev) return;

  state.deviceToDeleteId = dev.id;
  document.getElementById('deleteDeviceName').textContent = `${dev.name} (${dev.ipAddress})`;
  document.getElementById('deleteConfirmModal')?.classList.add('open');
}

function closeDeleteConfirmModal() {
  document.getElementById('deleteConfirmModal')?.classList.remove('open');
  state.deviceToDeleteId = null;
}

async function executeDeleteDevice() {
  if (!state.deviceToDeleteId) return;
  try {
    await fetchJson(`/devices/${state.deviceToDeleteId}`, { method: 'DELETE' });
    showToast('Device removed from monitoring', 'success');
    closeDeleteConfirmModal();
    closeDeviceDetails();
    refreshData();
  } catch (err) {
    showToast(`Failed to delete device: ${err.message}`, 'error');
  }
}

// 6. Subnet Discovery Engine
async function startSubnetScan() {
  const cidrInput = document.getElementById('subnetRangeInput').value.trim();
  const strategy = document.getElementById('discoveryStrategySelect').value;
  const workerThreads = parseInt(document.getElementById('workerThreadsInput').value) || 40;

  if (!cidrInput) {
    showToast('Please enter a valid Subnet CIDR (e.g. 192.168.1.0/24)', 'error');
    return;
  }

  const btn = document.getElementById('btnStartScan');
  const bannerText = document.getElementById('discoveryBannerText');
  const progressBar = document.getElementById('scanProgressBar');
  const importBtn = document.getElementById('btnImportSelected');
  const tbody = document.getElementById('discoveryRows');

  btn.disabled = true;
  btn.textContent = '⏳ Scanning Range...';
  bannerText.textContent = `Probing subnet ${cidrInput} with ${strategy} strategy...`;
  progressBar.style.width = '50%';
  importBtn.style.display = 'none';
  tbody.innerHTML = `<tr><td colspan="6" class="empty-state">Probing range ${cidrInput}... Please wait.</td></tr>`;

  const startTime = Date.now();

  try {
    const response = await fetchJson('/discovery/scan', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        subnetCidr: cidrInput,
        strategy: strategy,
        timeoutMs: 800
      })
    });

    const elapsed = Date.now() - startTime;
    progressBar.style.width = '100%';

    state.discoveredHosts = response.discoveredDevices || [];
    const totalScanned = response.totalHostsScanned || 254;
    const activeCount = state.discoveredHosts.length;

    bannerText.textContent = `Discovered ${activeCount} active hosts out of ${totalScanned} scanned in ${elapsed}ms. Select hosts to import.`;
    importBtn.style.display = activeCount > 0 ? 'inline-flex' : 'none';

    renderDiscoveryRows();
    showToast(`Subnet scan completed. Found ${activeCount} active hosts.`, 'success');

  } catch (err) {
    progressBar.style.width = '0%';
    bannerText.textContent = `Scan failed: ${err.message}`;
    tbody.innerHTML = `<tr><td colspan="6" class="empty-state" style="color:var(--offline)">Scan error: ${escapeHtml(err.message)}</td></tr>`;
    showToast(`Scan error: ${err.message}`, 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = '⚡ Start Range Scan';
    setTimeout(() => { progressBar.style.width = '0%'; }, 2000);
  }
}

async function triggerAutoDiscovery() {
  showToast('Initiating automatic local network discovery...', 'info');
  try {
    const res = await fetchJson('/discovery/auto', { method: 'POST' });
    showToast(`Auto-discovery finished. Discovered: ${res.discoveredCount || 0}, Imported: ${res.importedCount || 0}`, 'success');
    refreshData();
  } catch (err) {
    showToast(`Auto-discovery failed: ${err.message}`, 'error');
  }
}

function renderDiscoveryRows() {
  const tbody = document.getElementById('discoveryRows');
  if (!tbody) return;

  if (!state.discoveredHosts.length) {
    tbody.innerHTML = `<tr><td colspan="6" class="empty-state">No active hosts found on this subnet range.</td></tr>`;
    return;
  }

  tbody.innerHTML = state.discoveredHosts.map((host, idx) => {
    const isExisting = state.devices.some(d => d.ipAddress === host.ipAddress);
    const statusBadge = isExisting
      ? `<span class="badge badge-existing">● Existing</span>`
      : `<span class="badge badge-new">● New Host</span>`;
    
    const typeClass = `type-${(host.suggestedType || 'WORKSTATION').toLowerCase()}`;
    const typePill = `<span class="type-pill ${typeClass}">${host.suggestedType || 'WORKSTATION'}</span>`;

    return `
      <tr>
        <td style="text-align: center;">
          <input type="checkbox" class="disc-check" data-index="${idx}" ${!isExisting ? 'checked' : ''} aria-label="Select host ${escapeHtml(host.ipAddress)}">
        </td>
        <td class="mono" style="font-weight:600;">${escapeHtml(host.ipAddress)}</td>
        <td class="mono" style="color:var(--text-muted);">${escapeHtml(host.hostname || host.ipAddress)}</td>
        <td><strong>${escapeHtml(host.suggestedName || `Discovered Host ${host.ipAddress}`)}</strong></td>
        <td>${typePill}</td>
        <td>${statusBadge}</td>
      </tr>
    `;
  }).join('');
}

function toggleSelectAllDiscovered(masterCheckbox) {
  const checkboxes = document.querySelectorAll('.disc-check');
  checkboxes.forEach(cb => cb.checked = masterCheckbox.checked);
}

async function importSelectedDiscoveredDevices() {
  const checkboxes = document.querySelectorAll('.disc-check:checked');
  if (!checkboxes.length) {
    showToast('Please select at least one host to import.', 'error');
    return;
  }

  const selected = Array.from(checkboxes).map(cb => {
    const idx = parseInt(cb.dataset.index);
    const host = state.discoveredHosts[idx];
    return {
      name: host.suggestedName || `Discovered Host ${host.ipAddress}`,
      ipAddress: host.ipAddress,
      hostname: host.hostname,
      deviceType: host.suggestedType || 'WORKSTATION',
      vendor: host.vendor || 'Unknown'
    };
  });

  try {
    const imported = await fetchJson('/discovery/import', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(selected)
    });

    showToast(`Successfully imported ${imported.length} new devices into inventory!`, 'success');
    await refreshData();
    switchView('devices');
  } catch (err) {
    showToast(`Import failed: ${err.message}`, 'error');
  }
}

// 7. Render Local Networks
function renderLocalNetworks() {
  const grid = document.getElementById('localNetworkGrid');
  if (!grid) return;

  if (!state.localNetworks.length) {
    grid.innerHTML = `<div class="empty-state">No local attached network interfaces detected.</div>`;
    return;
  }

  grid.innerHTML = state.localNetworks.map(net => `
    <div class="net-card">
      <div class="net-card-title">${escapeHtml(net.interfaceName || 'eth0')}</div>
      <div class="mono" style="font-weight:700; margin-bottom:4px;">${escapeHtml(net.networkCidr || '192.168.1.0/24')}</div>
      <div class="mono" style="font-size:11px; color:var(--text-muted);">IP: ${escapeHtml(net.ipAddress || '')}</div>
      <div style="font-size:11px; margin-top:6px;"><span class="status-dot green"></span> Attached Interface</div>
    </div>
  `).join('');
}

// 8. Render Scheduler Details
function renderSchedulerDetails() {
  const grid = document.getElementById('schedulerDetails');
  if (!grid) return;

  const status = state.schedulerStatus || { active: true, workerPoolSize: 10 };

  grid.innerHTML = `
    <div class="net-card">
      <div class="info-label">SCHEDULER STATE</div>
      <div class="kpi-value ${status.active ? 'green-text' : 'red-text'}" style="font-size:20px; margin-top:4px;">
        ${status.active ? '● RUNNING' : '○ STOPPED'}
      </div>
      <button class="btn btn-secondary btn-sm" style="margin-top:12px;" onclick="toggleScheduler()">
        ${status.active ? 'Pause Scheduler' : 'Start Scheduler'}
      </button>
    </div>

    <div class="net-card">
      <div class="info-label">WORKER POOL SIZE</div>
      <div class="kpi-value" style="font-size:20px; margin-top:4px;">${status.workerPoolSize || 10} Threads</div>
      <div class="kpi-sub">Parallel reachability probes</div>
    </div>

    <div class="net-card">
      <div class="info-label">LAST CYCLE DURATION</div>
      <div class="kpi-value" style="font-size:20px; margin-top:4px;">${status.lastCycleDurationMs ? `${status.lastCycleDurationMs} ms` : 'N/A'}</div>
      <div class="kpi-sub">Time taken for full scan</div>
    </div>

    <div class="net-card">
      <div class="info-label">PROBED ENDPOINTS</div>
      <div class="kpi-value" style="font-size:20px; margin-top:4px;">${status.lastDevicesScannedCount !== undefined ? status.lastDevicesScannedCount : 'N/A'}</div>
      <div class="kpi-sub">Endpoints probed last cycle</div>
    </div>
  `;
}

async function toggleScheduler() {
  const isActive = state.schedulerStatus && state.schedulerStatus.active;
  const endpoint = isActive ? '/scheduler/stop' : '/scheduler/start';
  try {
    const res = await fetchJson(endpoint, { method: 'POST' });
    state.schedulerStatus = res;
    showToast(`Scheduler ${res.active ? 'STARTED' : 'PAUSED'}`, 'success');
    renderSchedulerDetails();
  } catch (err) {
    showToast(`Scheduler control failed: ${err.message}`, 'error');
  }
}

// 9. Render Alerts Center
function renderAlerts() {
  const feed = document.getElementById('fullAlertsFeed');
  if (!feed) return;

  const filtered = state.alerts.filter(a => {
    if (state.activeAlertFilter === 'ACTIVE') return !a.resolved;
    if (state.activeAlertFilter === 'RESOLVED') return a.resolved;
    return true;
  });

  if (!filtered.length) {
    feed.innerHTML = `<div class="empty-state">No alerts found for current filter.</div>`;
    return;
  }

  feed.innerHTML = filtered.map(a => `
    <div class="feed-item" style="justify-content:space-between; align-items:center;">
      <div style="display:flex; align-items:center; gap:12px;">
        <div class="feed-icon" style="background:${a.resolved ? 'rgba(100,116,139,0.15)' : 'rgba(239,68,68,0.15)'}; color:${a.resolved ? 'var(--text-subtle)' : 'var(--offline)'}">
          ${a.resolved ? '✓' : '⚠'}
        </div>
        <div>
          <div class="feed-title">${escapeHtml(a.alertType || 'System Alert')} — <span class="mono">${escapeHtml(a.ipAddress || a.deviceName || '')}</span></div>
          <div class="feed-sub">${escapeHtml(a.message || '')} (${a.timestamp ? formatTimeAgo(a.timestamp) : ''})</div>
        </div>
      </div>
      ${!a.resolved ? `<button class="btn btn-secondary btn-sm" onclick="resolveAlert(${a.id})">Resolve</button>` : `<span class="badge badge-existing">Resolved</span>`}
    </div>
  `).join('');
}

function filterAlerts(filterType) {
  state.activeAlertFilter = filterType;
  document.querySelectorAll('[data-alert-filter]').forEach(btn => {
    btn.classList.toggle('active-filter', btn.dataset.alertFilter === filterType);
  });
  renderAlerts();
}

async function resolveAlert(alertId) {
  try {
    await fetchJson(`/alerts/${alertId}/resolve`, { method: 'PATCH' });
    showToast('Alert resolved', 'success');
    refreshData();
  } catch (err) {
    showToast(`Failed to resolve alert: ${err.message}`, 'error');
  }
}

// 10. Render Reports Summary
async function renderReportsSummary() {
  const grid = document.getElementById('reportSummaryGrid');
  if (!grid) return;

  try {
    const report = await fetchJson('/reports/summary').catch(() => null);
    if (!report) {
      grid.innerHTML = `<div class="empty-state">Unable to load report metrics.</div>`;
      return;
    }

    grid.innerHTML = `
      <div class="net-card">
        <div class="info-label">TOTAL REGISTERED</div>
        <div class="kpi-value">${report.totalDevices || 0}</div>
      </div>
      <div class="net-card">
        <div class="info-label">ONLINE ENDPOINTS</div>
        <div class="kpi-value green-text">${report.onlineDevices || 0}</div>
      </div>
      <div class="net-card">
        <div class="info-label">OFFLINE ENDPOINTS</div>
        <div class="kpi-value red-text">${report.offlineDevices || 0}</div>
      </div>
      <div class="net-card">
        <div class="info-label">AVERAGE LATENCY</div>
        <div class="kpi-value">${typeof report.averageLatencyMs === 'number' ? `${report.averageLatencyMs.toFixed(1)} ms` : 'N/A'}</div>
      </div>
    `;
  } catch (err) {
    grid.innerHTML = `<div class="empty-state">Report error: ${escapeHtml(err.message)}</div>`;
  }
}

// 11. Render Settings Diagnostics
function renderSettingsDiagnostics() {
  const elem = document.getElementById('settingsDiagnostics');
  if (!elem) return;

  const h = state.health || { status: 'UP', database: 'Connected', nmapAvailable: true, version: '1.0.0-SNAPSHOT' };

  elem.innerHTML = `
    <div class="info-pair"><span class="info-key">Backend Status</span><span class="info-val ${state.backendConnected ? 'green-text' : 'red-text'}">${state.backendConnected ? '● CONNECTED (UP)' : '○ DISCONNECTED'}</span></div>
    <div class="info-pair"><span class="info-key">API Base URL</span><span class="info-val mono">http://localhost:8080/api</span></div>
    <div class="info-pair"><span class="info-key">Database Engine</span><span class="info-val">${h.database || 'Connected'}</span></div>
    <div class="info-pair"><span class="info-key">Nmap Scanner Binary</span><span class="info-val ${h.nmapAvailable ? 'green-text' : ''}">${h.nmapAvailable ? '● Available' : '○ Not Found'}</span></div>
    <div class="info-pair"><span class="info-key">SNMP Engine</span><span class="info-val green-text">● Available (SNMP4J)</span></div>
    <div class="info-pair"><span class="info-key">Platform Version</span><span class="info-val mono">${h.version || '1.0.0-SNAPSHOT'}</span></div>
  `;
}

function exportCsv() {
  window.open(`${API_BASE}/reports/export/csv`, '_blank');
}

// Register Device Modal Functions
function openRegisterModal() {
  document.getElementById('registerModal')?.classList.add('open');
}

function closeRegisterModal() {
  document.getElementById('registerModal')?.classList.remove('open');
}

async function submitRegisterDevice(e) {
  e.preventDefault();
  const name = document.getElementById('regName').value.trim();
  const ipAddress = document.getElementById('regIp').value.trim();
  const deviceType = document.getElementById('regType').value;
  const vendor = document.getElementById('regVendor').value.trim();

  try {
    await fetchJson('/devices', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, ipAddress, deviceType, vendor })
    });
    showToast(`Registered ${name} (${ipAddress})`, 'success');
    closeRegisterModal();
    refreshData();
  } catch (err) {
    showToast(`Registration failed: ${err.message}`, 'error');
  }
}

// Helpers & Utilities
function getDeviceTypeIcon(type) {
  switch ((type || '').toUpperCase()) {
    case 'ROUTER':
      return `<svg class="device-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="2"></circle><path d="M16.24 7.76a6 6 0 0 1 0 8.49m-12.48 0a6 6 0 0 1 0-8.49m15.31-2.83a10 10 0 0 1 0 14.14m-18.14 0a10 10 0 0 1 0-14.14"></path></svg>`;
    case 'SERVER':
      return `<svg class="device-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="2" width="20" height="8" rx="2" ry="2"></rect><rect x="2" y="14" width="20" height="8" rx="2" ry="2"></rect><line x1="6" y1="6" x2="6.01" y2="6"></line><line x1="6" y1="18" x2="6.01" y2="18"></line></svg>`;
    case 'WORKSTATION':
      return `<svg class="device-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect><line x1="8" y1="21" x2="16" y2="21"></line><line x1="12" y1="17" x2="12" y2="21"></line></svg>`;
    case 'SWITCH':
      return `<svg class="device-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="6" width="20" height="12" rx="2"></rect><path d="M6 12h4m4 0h4"></path></svg>`;
    case 'MOBILE':
      return `<svg class="device-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="5" y="2" width="14" height="20" rx="2" ry="2"></rect><line x1="12" y1="18" x2="12.01" y2="18"></line></svg>`;
    default:
      return `<svg class="device-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="4" y="4" width="16" height="16" rx="2"></rect><rect x="9" y="9" width="6" height="6"></rect><line x1="9" y1="1" x2="9" y2="4"></line><line x1="15" y1="1" x2="15" y2="4"></line><line x1="9" y1="20" x2="9" y2="23"></line><line x1="15" y1="20" x2="15" y2="23"></line><line x1="20" y1="9" x2="23" y2="9"></line><line x1="20" y1="15" x2="23" y2="15"></line><line x1="1" y1="9" x2="4" y2="9"></line><line x1="1" y1="15" x2="4" y2="15"></line></svg>`;
  }
}

function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

function formatTimeAgo(dateInput) {
  if (!dateInput) return 'N/A';
  const date = new Date(dateInput);
  const diffSec = Math.floor((Date.now() - date.getTime()) / 1000);

  if (diffSec < 5) return 'just now';
  if (diffSec < 60) return `${diffSec}s ago`;
  if (diffSec < 3600) return `${Math.floor(diffSec / 60)}m ago`;
  if (diffSec < 86400) return `${Math.floor(diffSec / 3600)}h ago`;
  return date.toLocaleDateString();
}