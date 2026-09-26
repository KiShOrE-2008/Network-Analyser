import { api } from './client';

export const discoveryApi = {
  // GET /api/discovery/local-networks
  getLocalNetworks: () => api.get('/api/discovery/local-networks'),

  // POST /api/discovery/auto
  autoDiscover: () => api.post('/api/discovery/auto'),

  // GET /api/discovery/auto/status
  getAutoDiscoveryStatus: () => api.get('/api/discovery/auto/status'),

  // POST /api/discovery/scan
  scanSubnet: (subnetCidr, strategy = 'PING_ONLY') => 
    api.post('/api/discovery/scan', { subnetCidr, strategy }),

  // POST /api/discovery/import
  importDevices: (devicesToImport) => 
    api.post('/api/discovery/import', devicesToImport),

  // GET /api/discovery/nmap/status
  getNmapStatus: () => api.get('/api/discovery/nmap/status'),

  // POST /api/discovery/nmap
  scanNmapSubnet: (subnetCidr, strategy = 'FAST_PORT') => 
    api.post('/api/discovery/nmap', { subnetCidr, strategy })
};
