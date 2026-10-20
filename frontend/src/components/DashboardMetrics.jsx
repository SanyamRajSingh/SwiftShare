import React, { useMemo } from 'react';

export const DashboardMetrics = ({ transfers }) => {
  // Unit 2 Syllabus: useMemo for performance optimization and aggregate computation
  const stats = useMemo(() => {
    const total = transfers.length;
    const successCount = transfers.filter((t) => t.status?.toLowerCase().includes('success')).length;
    const failedCount = transfers.filter((t) => t.status?.toLowerCase().includes('fail') || t.status?.toLowerCase().includes('cancel')).length;
    const totalBytes = transfers.reduce((acc, curr) => acc + (Number(curr.sizeBytes) || 0), 0);
    const avgBytes = total > 0 ? Math.round(totalBytes / total) : 0;
    const successRate = total > 0 ? Math.round((successCount / total) * 100) : 100;

    const formatBytes = (bytes) => {
      if (!bytes || bytes === 0) return '0 B';
      const k = 1024;
      const sizes = ['B', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return `${parseFloat((bytes / Math.pow(k, i)).toFixed(1))} ${sizes[i]}`;
    };

    return {
      total,
      successCount,
      failedCount,
      totalVolume: formatBytes(totalBytes),
      avgSize: formatBytes(avgBytes),
      successRate: `${successRate}%`,
    };
  }, [transfers]);

  return (
    <section className="metrics-grid" aria-label="Transfer Metrics Summary">
      <div className="metric-card">
        <span className="metric-label">Total Records</span>
        <span className="metric-value">{stats.total}</span>
        <span className="metric-subtext">Persisted across Core JPA DB</span>
      </div>

      <div className="metric-card">
        <span className="metric-label">Successful Transfers</span>
        <span className="metric-value" style={{ color: '#059669' }}>{stats.successCount}</span>
        <span className="metric-subtext">Success Rate: {stats.successRate}</span>
      </div>

      <div className="metric-card">
        <span className="metric-label">Total Data Volume</span>
        <span className="metric-value" style={{ color: '#4f46e5' }}>{stats.totalVolume}</span>
        <span className="metric-subtext">Average File: {stats.avgSize}</span>
      </div>

      <div className="metric-card">
        <span className="metric-label">Failed / Cancelled</span>
        <span className="metric-value" style={{ color: stats.failedCount > 0 ? '#dc2626' : '#64748b' }}>
          {stats.failedCount}
        </span>
        <span className="metric-subtext">Security & Integrity Errors</span>
      </div>
    </section>
  );
};
