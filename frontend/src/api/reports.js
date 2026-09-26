import { api } from './client';

export const reportsApi = {
  // GET /api/reports/summary
  getSummaryReport: () => api.get('/api/reports/summary'),

  // Export CSV URL helper
  exportCsvUrl: '/api/reports/export/csv',

  // GET /api/reports/export/csv
  downloadCsv: () => api.get('/api/reports/export/csv')
};
