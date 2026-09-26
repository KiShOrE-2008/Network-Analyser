import { api } from './client';

export const diagnosticsApi = {
  // GET /api/diagnostics/gateway
  getGatewayStatus: () => api.get('/api/diagnostics/gateway'),

  // POST /api/diagnostics/dns-test
  testDns: (hostname = 'example.com') => api.post('/api/diagnostics/dns-test', { hostname }),

  // GET /api/diagnostics/speed-test/download?sizeMb=...
  downloadUrl: (sizeMb = 25) => `/api/diagnostics/speed-test/download?sizeMb=${sizeMb}`,

  // POST /api/diagnostics/speed-test/upload
  uploadUrl: '/api/diagnostics/speed-test/upload',

  // POST /api/diagnostics/speed-test
  recordSpeedTest: (data) => api.post('/api/diagnostics/speed-test', data),

  // GET /api/diagnostics/speed-test/history
  getSpeedTestHistory: () => api.get('/api/diagnostics/speed-test/history'),

  // GET /api/devices/{id}/interfaces
  getDeviceInterfaces: (id) => api.get(`/api/devices/${id}/interfaces`),

  // POST /api/devices/{id}/interface-check
  checkDeviceInterfaces: (id) => api.post(`/api/devices/${id}/interface-check`),

  // GET /api/diagnostics/devices/{id} (Device diagnostics)
  getDeviceDiagnostics: (id) => api.get(`/api/diagnostics/devices/${id}`)
};
