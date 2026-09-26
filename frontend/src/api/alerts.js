import { api } from './client';

export const alertsApi = {
  // GET /api/alerts?includeResolved=true/false
  getAlerts: (includeResolved = false) => 
    api.get(`/api/alerts?includeResolved=${includeResolved}`),

  // GET /api/alerts/device/{deviceId}
  getDeviceAlerts: (deviceId) => 
    api.get(`/api/alerts/device/${deviceId}`),

  // PATCH /api/alerts/{id}/resolve
  resolveAlert: (id) => 
    api.patch(`/api/alerts/${id}/resolve`)
};
