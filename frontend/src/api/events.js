import { api } from './client';

export const eventsApi = {
  // GET /api/events
  getRecentEvents: () => api.get('/api/events'),

  // GET /api/events/device/{deviceId}
  getDeviceEvents: (deviceId) => api.get(`/api/events/device/${deviceId}`)
};
