import React, { useState, useMemo } from 'react';

export const TransferTable = ({ transfers, loading, error, onRefresh, onDelete }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');

  // Unit 2 Syllabus: Filter and map array operations with useMemo
  const filteredTransfers = useMemo(() => {
    return transfers.filter((t) => {
      const matchesSearch =
        t.fileName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        t.sender?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        t.receiver?.toLowerCase().includes(searchTerm.toLowerCase());
      
      const matchesStatus =
        statusFilter === 'ALL' ||
        (statusFilter === 'SUCCESS' && t.status?.toLowerCase().includes('success')) ||
        (statusFilter === 'FAILED' && !t.status?.toLowerCase().includes('success'));

      return matchesSearch && matchesStatus;
    });
  }, [transfers, searchTerm, statusFilter]);

  const getStatusClass = (statusStr) => {
    if (!statusStr) return 'pending';
    const s = statusStr.toLowerCase();
    if (s.includes('success')) return 'success';
    if (s.includes('fail') || s.includes('tag') || s.includes('timeout')) return 'failed';
    return 'pending';
  };

  const formatFileSize = (bytes) => {
    if (!bytes) return '0 B';
    if (bytes >= 1048576) return `${(bytes / 1048576).toFixed(2)} MB`;
    if (bytes >= 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${bytes} B`;
  };

  return (
    <section className="section-card" aria-label="Transfer Records Table">
      <div className="section-header">
        <div>
          <h2 className="section-title">Transfer History Registry</h2>
          <p className="section-subtitle">
            Synchronized with the Spring Boot Core Service (JPA Entity: <code>TransferEntity</code>)
          </p>
        </div>
        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          <button
            type="button"
            className="btn btn-secondary"
            onClick={onRefresh}
            disabled={loading}
          >
            {loading ? 'Refreshing...' : '🔄 Refresh'}
          </button>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', marginBottom: '1rem' }}>
        <input
          type="text"
          className="form-input"
          style={{ maxWidth: '320px' }}
          placeholder="Filter by filename, sender, or receiver..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          aria-label="Filter records"
        />

        <select
          className="form-select"
          style={{ maxWidth: '180px' }}
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          aria-label="Filter by status"
        >
          <option value="ALL">All Outcomes</option>
          <option value="SUCCESS">Success Only</option>
          <option value="FAILED">Failed / Cancelled</option>
        </select>

        <span style={{ fontSize: '0.875rem', color: 'var(--text-muted)', alignSelf: 'center', marginLeft: 'auto' }}>
          Showing <strong>{filteredTransfers.length}</strong> of {transfers.length} records
        </span>
      </div>

      {error && (
        <div className="alert-banner error" style={{ marginBottom: '1rem' }}>
          <span>⚠️ Connection Warning: {error}. Start the Spring Boot backend to interact with persistent data.</span>
        </div>
      )}

      {/* Semantic Table Presentation */}
      <div className="table-wrapper">
        <table className="data-table">
          <thead>
            <tr>
              <th scope="col">ID</th>
              <th scope="col">File Name</th>
              <th scope="col">File Size</th>
              <th scope="col">Sender Node</th>
              <th scope="col">Receiver Node</th>
              <th scope="col">Transfer Timestamp</th>
              <th scope="col">Status</th>
              <th scope="col" style={{ textAlign: 'right' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading && transfers.length === 0 ? (
              <tr>
                <td colSpan="8" style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                  ⏳ Loading transfer records from Core JPA persistence...
                </td>
              </tr>
            ) : filteredTransfers.length === 0 ? (
              <tr>
                <td colSpan="8" style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                  📭 No transfer records match your current filter criteria.
                </td>
              </tr>
            ) : (
              filteredTransfers.map((item) => (
                <tr key={item.id}>
                  <td><strong>#{item.id}</strong></td>
                  <td>
                    <span style={{ fontWeight: 600 }}>{item.fileName}</span>
                  </td>
                  <td>{formatFileSize(item.sizeBytes)}</td>
                  <td><code>{item.sender || 'Local Peer'}</code></td>
                  <td><code>{item.receiver || 'Remote Peer'}</code></td>
                  <td>{item.transferDate || 'Just now'}</td>
                  <td>
                    <span className={`status-badge ${getStatusClass(item.status)}`}>
                      {item.status}
                    </span>
                    {item.auditStatus && (
                      <span
                        style={{
                          display: 'block',
                          fontSize: '0.7rem',
                          marginTop: '0.25rem',
                          color: item.auditStatus === 'AUDITED_DELIVERED' ? '#059669' : '#d97706',
                          fontWeight: 500,
                        }}
                      >
                        {item.auditStatus === 'AUDITED_DELIVERED' ? '✓ Audited' : '⚠️ Audit Offline'}
                      </span>
                    )}
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <button
                      type="button"
                      className="btn btn-danger"
                      style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem' }}
                      onClick={() => onDelete(item.id)}
                      title="Delete record from Core JPA DB"
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
};
