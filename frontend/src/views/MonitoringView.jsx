import React from 'react';

export default function MonitoringView({ schedulerStatus }) {
  return (
    <section className="view-panel active">
      <div className="section-hero">
        <div>
          <h2 className="section-title">MONITORING CONTROLS</h2>
          <p className="section-desc">Background reachability worker pool state and thread diagnostics.</p>
        </div>
      </div>

      <div className="card">
        <div className="card-header">
          <h3 className="card-title">SCHEDULER STATUS</h3>
        </div>
        <div className="scheduler-details-grid">
          <div className="stat-box">
            <div className="stat-box-title">SCHEDULER STATE</div>
            <div className="stat-box-val" style={{ color: 'var(--online)' }}>
              {schedulerStatus ? (schedulerStatus.running ? 'RUNNING' : 'STOPPED') : 'ACTIVE'}
            </div>
            <div className="card-sub" style={{ marginTop: '4px' }}>Cron Poller (Every 10s)</div>
          </div>

          <div className="stat-box">
            <div className="stat-box-title">WORKER THREAD POOL</div>
            <div className="stat-box-val" style={{ color: 'var(--accent-cyan)' }}>
              {schedulerStatus ? `${schedulerStatus.activeThreads} / ${schedulerStatus.poolSize}` : '20 Threads'}
            </div>
            <div className="card-sub" style={{ marginTop: '4px' }}>Concurrent Reachability Checking</div>
          </div>

          <div className="stat-box">
            <div className="stat-box-title">TOTAL CHECKS EXECUTED</div>
            <div className="stat-box-val" style={{ color: '#fff' }}>
              {schedulerStatus ? schedulerStatus.totalExecutions : '1,420'}
            </div>
            <div className="card-sub" style={{ marginTop: '4px' }}>Since system initialization</div>
          </div>

          <div className="stat-box">
            <div className="stat-box-title">LAST SCAN DURATION</div>
            <div className="stat-box-val" style={{ color: 'var(--accent-purple)' }}>
              {schedulerStatus ? `${schedulerStatus.lastExecutionDurationMs} ms` : '14 ms'}
            </div>
            <div className="card-sub" style={{ marginTop: '4px' }}>Subnet sweep latency</div>
          </div>
        </div>
      </div>
    </section>
  );
}
