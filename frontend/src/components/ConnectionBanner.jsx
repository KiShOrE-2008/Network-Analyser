import React from 'react';
import { WifiOff, RefreshCw } from 'lucide-react';

export default function ConnectionBanner({ onRetry, lastSuccessTime }) {
  return (
    <div className="connection-banner">
      <div className="banner-content">
        <div className="banner-icon">
          <WifiOff size={24} />
        </div>
        <div className="banner-text">
          <h4 className="banner-title">BACKEND CONNECTION LOST</h4>
          <p className="banner-desc">
            Unable to reach Spring Boot backend engine. 
            {lastSuccessTime ? ` Last successful sync: ${lastSuccessTime}.` : ''}
          </p>
        </div>
      </div>
      <button className="btn btn-secondary btn-sm" onClick={onRetry}>
        <RefreshCw size={14} /> Retry Connection
      </button>
    </div>
  );
}
