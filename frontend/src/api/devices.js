import { api } from './client';

export const devicesApi = {
  // GET /api/devices?search=...&type=...&status=...
  getAllDevices: (params = {}) => {
    const query = new URLSearchParams();
    if (params.search) query.append('search', params.search);
    if (params.type) query.append('type', params.type);
    if (params.status) query.append('status', params.status);
    const queryString = query.toString();
    return api.get(`/api/devices${queryString ? `?${queryString}` : ''}`);
  },

  // GET /api/devices/{id}
  getDeviceById: (id) => api.get(`/api/devices/${id}`),

  // POST /api/devices
  createDevice: (data) => api.post('/api/devices', data),

  // PUT /api/devices/{id}
  updateDevice: (id, data) => api.put(`/api/devices/${id}`, data),

  // PATCH /api/devices/{id}/toggle-monitoring
  toggleMonitoring: (id) => api.patch(`/api/devices/${id}/toggle-monitoring`),

  // DELETE /api/devices/{id}
  deleteDevice: (id) => api.delete(`/api/devices/${id}`),

  // POST /api/devices/{id}/check
  pingCheck: (id) => api.post(`/api/devices/${id}/check`),

  // GET /api/devices/metrics/recent
  getRecentMetrics: () => api.get('/api/devices/metrics/recent'),

  // GET /api/devices/{id}/metrics
  getMetrics: (id) => api.get(`/api/devices/${id}/metrics`),

  // GET /api/devices/{id}/events
  getEvents: (id) => api.get(`/api/devices/${id}/events`),

  // POST /api/devices/{id}/scan-ports
  scanPorts: (id) => api.post(`/api/devices/${id}/scan-ports`),

  // GET /api/devices/{id}/ports
  getPorts: (id) => api.get(`/api/devices/${id}/ports`),

  // POST /api/devices/{id}/nmap-scan?profile=...
  nmapScan: (id, profile = 'FAST_PORT') => api.post(`/api/devices/${id}/nmap-scan?profile=${encodeURIComponent(profile)}`),

  // POST /api/devices/{id}/snmp-check?community=...
  snmpCheck: (id, community = 'public') => api.post(`/api/devices/${id}/snmp-check?community=${encodeURIComponent(community)}`),

  // GET /api/devices/{id}/snmp
  getSnmp: (id) => api.get(`/api/devices/${id}/snmp`),

  // GET /api/history/device/{id}?limit=...
  getHistory: (id, limit = 100) => api.get(`/api/history/device/${id}?limit=${limit}`)
};
