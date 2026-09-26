import { api } from './client';

export const systemApi = {
  // GET /api/health
  getHealth: () => api.get('/api/health'),

  // GET /api/scheduler/status
  getSchedulerStatus: () => api.get('/api/scheduler/status'),

  // POST /api/scheduler/start
  startScheduler: () => api.post('/api/scheduler/start'),

  // POST /api/scheduler/stop
  stopScheduler: () => api.post('/api/scheduler/stop')
};
