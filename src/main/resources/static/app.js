// NetOps Command — Dashboard Client JS
const API_BASE = '/api';

const state = {
  devices: [],
  alerts: [],
  discoveredHosts: [],
  scheduler: { active: true, workerPoolSize: 10 },
  activeView: 'overview'
};

document.addEventListener('DOMContentLoaded', () => {
  initApp();
});

async function initApp() {
  setupNavigation();
  await refreshData();
  setInterval(refreshData, 12000);
}

function setupNavigation() {
  const btns = document.querySelectorAll('.nav-btn');
  btns.forEach(btn => {
    btn.addEventListener('click', (e) => {
      const view = e.currentTarget.dataset.view;
      switchView(view);
    });
  });
}

function switchView(viewId) {
  state.activeView = viewId;
  document.querySelectorAll('.nav-btn').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.view === viewId);
  });

  document.querySelectorAll('.view-content').forEach(view => {
    const isTarget = view.id === `view-${viewId}`;
    view.classList.toggle('active', isTarget);
  });
}

// API Utilities
async function fetchJson(url, options = {}) {
  const res = await fetch(API_BASE + url, options);
  if (!res.ok) {
    const errText = await res.text();
    throw new Error(errText || `HTTP ${res.status}`);
  }
  return res.json();
}

function showToast(msg) {
  const toast = document.getElementById('toast');
  if (!toast) return;
  toast.textContent = msg;
  toast.classList.add('show');
  setTimeout(() => toast.classList.remove('show'), 3500);
}

// Data Refreshing
async function refreshData() {
  try {
    const [devices, alerts] = await Promise.all([
      fetchJson('/devices').catch(() => []),
      fetchJson('/alerts?includeResolved=true').catch(() => [])
    ]);

    state.devices = devices;
    state.alerts = alerts;

    renderKPIs();
    renderDeviceInventory();
    renderActivityFeed();
    renderAlertCenter();
  } catch (err) {
    console.error('Data refresh error:', err);
  }
}

// Render KPI Cards
function renderKPIs() {
  const total = state.devices.length;
  const online = state.devices.filter(d => d.status === 'ONLINE').length;
  const offline = state.devices.filter(d => d.status === 'OFFLINE').length;
  const activeAlerts = state.alerts.filter(a => !a.resolved).length;

  document.getElementById('kTotal').textContent = total;
  document.getElementById('kOnline').textContent = `${online} / ${total}`;
  document.getElementById('kOnlineSub').textContent = total > 0 ? `${Math.round((online / total) * 100)}% Reachable` : '0% Reachable';
  document.getElementById('kAlerts').textContent = activeAlerts;

  const validLatencies = state.devices
    .map(d => d._latency)
    .filter(l => typeof l === 'number' && !isNaN(l));
  
  const avgLat = validLatencies.length ? (validLatencies.reduce((a, b) => a + b, 0) / validLatencies.length).toFixed(1) : 0;
  
  const avgText = document.getElementById('avgLatencyText');
  if (avgText) avgText.textContent = `${avgLat} ms`;

  const slaPct = document.getElementById('slaPct');
  if (slaPct) slaPct.textContent = total > 0 ? `${Math.round((online / total) * 100)}%` : '100%';
}

// Render Device Inventory Table
function renderDeviceInventory() {
  const tbody = document.getElementById('deviceRows');
  if (!tbody) return;

  const searchQuery = (document.getElementById('deviceSearch')?.value || '').toLowerCase();
  const filterType = document.getElementById('deviceFilter')?.value || 'ALL';

  const filtered = state.devices.filter(d => {
    const matchesFilter = filterType === 'ALL' || d.deviceType === filterType;
    const matchesSearch = [d.name, d.ipAddress, d.hostname, d.macAddress, d.vendor]
      .some(field => String(field || '').toLowerCase().includes(searchQuery));
    return matchesFilter && matchesSearch;
  });

  if (!filtered.length) {
    tbody.innerHTML = `<tr><td colspan="7" class="empty-state">No matching devices found in inventory.</td></tr>`;
    return;
  }

  tbody.innerHTML = filtered.map(d => {
    const isOnline = d.status === 'ONLINE';
    const statusBadge = `<span class="badge ${isOnline ? 'badge-online' : 'badge-offline'}"><span class="status-dot"></span> ${d.status || 'UNKNOWN'}</span>`;
    const typeClass = `type-${(d.deviceType || 'UNKNOWN').toLowerCase()}`;
    const typePill = `<span class="type-pill ${typeClass}">${d.deviceType || 'UNKNOWN'}</span>`;
    const latency = typeof d._latency === 'number' ? `${d._latency.toFixed(1)} ms` : '—';

    return `
      <tr>
        <td><strong>${escapeHtml(d.name)}</strong><br><small style="color:var(--text-muted);">${escapeHtml(d.vendor || d.hostname || '')}</small></td>
        <td class="mono">${escapeHtml(d.ipAddress)}</td>
        <td class="mono">${escapeHtml(d.macAddress || '—')}</td>
        <td>${typePill}</td>
        <td>${statusBadge}</td>
        <td class="mono">${latency}</td>
        <td style="text-align:right">
          <button class="btn btn-secondary btn-sm" onclick="pingCheckDevice(${d.id})">⚡ Ping</button>
          <button class="btn btn-primary btn-sm" onclick="openInspectModal(${d.id})">📊 Inspect</button>
          <button class="btn btn-danger btn-sm" onclick="deleteDevice(${d.id})">🗑</button>
        </td>
      </tr>
    `;
  }).join('');
}

// Subnet Discovery Range Scan Engine
async function startSubnetScan() {
  const cidrInput = document.getElementById('subnetRangeInput').value.trim();
  const strategy = document.getElementById('discoveryStrategySelect').value;
  const workerThreads = parseInt(document.getElementById('workerThreadsInput').value) || 40;

  if (!cidrInput) {
    showToast('Please enter a valid Subnet CIDR (e.g. 192.168.1.0/24)');
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
  progressBar.style.width = '45%';
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

    bannerText.textContent = `Discovered ${activeCount} active hosts out of ${totalScanned} scanned in ${elapsed}ms. Select hosts below to import.`;
    importBtn.style.display = activeCount > 0 ? 'inline-flex' : 'none';

    renderDiscoveryRows();
    showToast(`Subnet scan finished. Found ${activeCount} active hosts.`);

  } catch (err) {
    progressBar.style.width = '0%';
    bannerText.textContent = `Scan failed: ${err.message}`;
    tbody.innerHTML = `<tr><td colspan="6" class="empty-state" style="color:var(--offline)">Scan error: ${escapeHtml(err.message)}</td></tr>`;
    showToast(`Scan error: ${err.message}`);
  } finally {
    btn.disabled = false;
    btn.textContent = '⚡ Start Range Scan';
    setTimeout(() => { progressBar.style.width = '0%'; }, 2000);
  }
}

// Render Discovery Rows Table
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
          <input type="checkbox" class="disc-check" data-index="${idx}" ${!isExisting ? 'checked' : ''}>
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

// Toggle Select All Checkboxes
function toggleSelectAllDiscovered(headerCheckbox) {
  const checkboxes = document.querySelectorAll('.disc-check');
  checkboxes.forEach(cb => cb.checked = headerCheckbox.checked);
}

// Import Selected Devices
async function importSelectedDiscoveredDevices() {
  const checkedBoxes = document.querySelectorAll('.disc-check:checked');
  if (!checkedBoxes.length) {
    showToast('Please select at least one discovered host to import.');
    return;
  }

  const selectedDevices = Array.from(checkedBoxes).map(cb => {
    const idx = parseInt(cb.dataset.index);
    const host = state.discoveredHosts[idx];
    return {
      name: host.suggestedName || `Host ${host.ipAddress}`,
      ipAddress: host.ipAddress,
      hostname: host.hostname,
      deviceType: host.suggestedType || 'WORKSTATION',
      vendor: host.vendor || 'Unknown',
      macAddress: host.macAddress || null,
      monitoringEnabled: true,
      scanInterval: 10
    };
  });

  try {
    const imported = await fetchJson('/discovery/import', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(selectedDevices)
    });

    showToast(`Successfully imported ${imported.length} new devices to inventory!`);
    await refreshData();
    switchView('inventory');
  } catch (err) {
    showToast(`Import failed: ${err.message}`);
  }
}

// Device Actions
async function pingCheckDevice(id) {
  try {
    showToast(`Pinging device #${id}...`);
    const res = await fetchJson(`/devices/${id}/check`, { method: 'POST' });
    showToast(`Ping ${res.reachable ? 'SUCCESS' : 'FAILED'} — Latency: ${res.latencyMs != null ? res.latencyMs.toFixed(1) + 'ms' : 'N/A'}`);
    await refreshData();
  } catch (err) {
    showToast(`Ping check failed: ${err.message}`);
  }
}

async function deleteDevice(id) {
  if (!confirm('Are you sure you want to delete this device from inventory?')) return;
  try {
    await fetchJson(`/devices/${id}`, { method: 'DELETE' });
    showToast('Device deleted from inventory.');
    await refreshData();
  } catch (err) {
    showToast(`Delete failed: ${err.message}`);
  }
}

// Device Inspection Modal
async function openInspectModal(id) {
  const device = state.devices.find(d => d.id === id);
  if (!device) return;

  const modal = document.getElementById('inspectModal');
  document.getElementById('inspectTitle').textContent = device.name;
  document.getElementById('inspectSub').textContent = `${device.ipAddress} • ${device.deviceType || 'UNKNOWN'} • MAC: ${device.macAddress || 'N/A'}`;
  
  const body = document.getElementById('inspectBody');
  body.innerHTML = `<div class="empty-state">Loading telemetry for ${device.ipAddress}...</div>`;
  modal.classList.add('open');

  try {
    const [ports, metrics, events] = await Promise.all([
      fetchJson(`/devices/${id}/ports`).catch(() => []),
      fetchJson(`/devices/${id}/metrics`).catch(() => []),
      fetchJson(`/devices/${id}/events`).catch(() => [])
    ]);

    body.innerHTML = `
      <div style="display:grid; grid-template-columns: repeat(4, 1fr); gap:12px; margin-bottom:20px;">
        <div class="kpi-card" style="padding:14px;">
          <div class="kpi-info">
            <div class="kpi-label">Status</div>
            <div class="kpi-value" style="font-size:18px;">${device.status}</div>
          </div>
        </div>
        <div class="kpi-card" style="padding:14px;">
          <div class="kpi-info">
            <div class="kpi-label">Health</div>
            <div class="kpi-value" style="font-size:18px;">${device.healthStatus || 'NORMAL'}</div>
          </div>
        </div>
        <div class="kpi-card" style="padding:14px;">
          <div class="kpi-info">
            <div class="kpi-label">Vendor</div>
            <div class="kpi-value" style="font-size:16px;">${escapeHtml(device.vendor || 'N/A')}</div>
          </div>
        </div>
        <div class="kpi-card" style="padding:14px;">
          <div class="kpi-info">
            <div class="kpi-label">Scan Interval</div>
            <div class="kpi-value" style="font-size:18px;">${device.scanInterval || 10}s</div>
          </div>
        </div>
      </div>

      <h4 style="margin-bottom:10px; font-size:14px; font-weight:700;">Open Port Matrix</h4>
      <div class="port-grid">
        ${ports.length ? ports.map(p => `
          <div class="port-item">
            <div class="port-number">${p.portNumber}</div>
            <div class="port-service">${escapeHtml(p.serviceName || 'TCP')}</div>
            <div class="${p.state === 'OPEN' ? 'port-state-open' : 'port-state-closed'}">${p.state}</div>
          </div>
        `).join('') : '<div class="empty-state" style="grid-column: 1/-1;">No port scan performed yet. Click "Scan Ports" below.</div>'}
      </div>

      <div style="margin-top:20px; text-align:right;">
        <button class="btn btn-secondary" onclick="scanDevicePorts(${device.id})">🔌 Scan Common TCP Ports</button>
      </div>
    `;

  } catch (err) {
    body.innerHTML = `<div class="empty-state" style="color:var(--offline)">Failed to load telemetry: ${escapeHtml(err.message)}</div>`;
  }
}

async function scanDevicePorts(id) {
  showToast('Scanning ports on target...');
  try {
    await fetchJson(`/devices/${id}/scan-ports`, { method: 'POST' });
    showToast('Port scan completed!');
    openInspectModal(id);
  } catch (err) {
    showToast(`Port scan error: ${err.message}`);
  }
}

function closeInspectModal() {
  document.getElementById('inspectModal').classList.remove('open');
}

// Register Modal
function openRegisterModal() {
  document.getElementById('registerModal').classList.add('open');
}

function closeRegisterModal() {
  document.getElementById('registerModal').classList.remove('open');
}

async function submitRegisterDevice(e) {
  e.preventDefault();
  const dto = {
    name: document.getElementById('regName').value.trim(),
    ipAddress: document.getElementById('regIp').value.trim(),
    deviceType: document.getElementById('regType').value,
    vendor: document.getElementById('regVendor').value.trim() || null,
    monitoringEnabled: true,
    scanInterval: 10
  };

  try {
    await fetchJson('/devices', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(dto)
    });
    showToast(`Device "${dto.name}" registered successfully!`);
    closeRegisterModal();
    await refreshData();
  } catch (err) {
    showToast(`Registration failed: ${err.message}`);
  }
}

// Render Feeds
function renderActivityFeed() {
  const feed = document.getElementById('activityFeed');
  if (!feed) return;

  const alerts = state.alerts.slice(0, 6);
  if (!alerts.length) {
    feed.innerHTML = `<div class="empty-state">🎉 All network segments operational. No critical alerts active!</div>`;
    return;
  }

  feed.innerHTML = alerts.map(a => `
    <div class="feed-item">
      <div class="feed-content">
        <span class="status-dot" style="color:${a.severity === 'CRITICAL' ? 'var(--offline)' : 'var(--accent-amber)'}"></span>
        <div>
          <div class="feed-title">${escapeHtml(a.deviceName || 'Device')} <small style="color:var(--text-subtle);">(${escapeHtml(a.deviceIp || '')})</small></div>
          <div class="feed-sub">${escapeHtml(a.message || '')}</div>
        </div>
      </div>
      <button class="btn btn-secondary btn-sm" onclick="acknowledgeAlert(${a.id})">Acknowledge</button>
    </div>
  `).join('');
}

function renderAlertCenter() {
  const feed = document.getElementById('fullAlertsFeed');
  if (!feed) return;

  if (!state.alerts.length) {
    feed.innerHTML = `<div class="empty-state">No recorded alerts in system log.</div>`;
    return;
  }

  feed.innerHTML = state.alerts.map(a => `
    <div class="feed-item">
      <div class="feed-content">
        <span class="status-dot" style="color:${a.resolved ? 'var(--online)' : 'var(--offline)'}"></span>
        <div>
          <div class="feed-title">${escapeHtml(a.severity || 'ALERT')} • ${escapeHtml(a.deviceName || 'Device')}</div>
          <div class="feed-sub">${escapeHtml(a.message || '')} • ${new Date(a.createdAt).toLocaleString()}</div>
        </div>
      </div>
      ${!a.resolved ? `<button class="btn btn-secondary btn-sm" onclick="acknowledgeAlert(${a.id})">Acknowledge</button>` : '<span class="badge badge-online">Resolved</span>'}
    </div>
  `).join('');
}

async function acknowledgeAlert(id) {
  try {
    await fetchJson(`/alerts/${id}/acknowledge`, { method: 'POST' });
    showToast('Alert acknowledged.');
    await refreshData();
  } catch (err) {
    showToast(`Action failed: ${err.message}`);
  }
}

async function exportCsv() {
  window.open(`${API_BASE}/reports/export/csv`, '_blank');
}

function escapeHtml(str) {
  return String(str || '').replace(/[&<>"']/g, c => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;'
  }[c]));
}