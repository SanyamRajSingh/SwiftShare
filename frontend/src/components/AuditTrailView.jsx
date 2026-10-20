import React from 'react';

export const AuditTrailView = ({ auditEvents }) => {
  return (
    <section className="section-card" aria-label="Microservice Audit Trail">
      <div className="section-header">
        <div>
          <h2 className="section-title">Audit Microservice Telemetry</h2>
          <p className="section-subtitle">
            Persisted independently by <code>SwiftShareAuditApplication</code> (Port 8081) via service-to-service HTTP calls.
          </p>
        </div>
      </div>

      <div className="alert-banner info" style={{ marginBottom: '1.25rem' }}>
        <span>
          🌐 <strong>Service-to-Service Verification:</strong> When a transfer is registered or updated in the 
          <strong> Core Service (:8080)</strong>, it executes an HTTP POST to <code>http://localhost:8081/api/audit/events</code>. 
          The Audit Microservice validates and independently persists the event to its own database.
        </span>
      </div>

      <div className="table-wrapper">
        <table className="data-table">
          <thead>
            <tr>
              <th scope="col">Audit ID</th>
              <th scope="col">Event Type</th>
              <th scope="col">Target Transfer ID</th>
              <th scope="col">Details & Payload Summary</th>
              <th scope="col">Caller IP / Origin</th>
              <th scope="col">Audit Timestamp</th>
            </tr>
          </thead>
          <tbody>
            {!auditEvents || auditEvents.length === 0 ? (
              <tr>
                <td colSpan="6" style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
                  📭 No audit events registered yet. Create a transfer record or trigger an API call to observe live service-to-service telemetry.
                </td>
              </tr>
            ) : (
              auditEvents.map((evt) => (
                <tr key={evt.id}>
                  <td><strong>#{evt.id}</strong></td>
                  <td>
                    <span
                      style={{
                        padding: '0.2rem 0.5rem',
                        borderRadius: '4px',
                        fontSize: '0.75rem',
                        fontWeight: 600,
                        backgroundColor: '#e0e7ff',
                        color: '#3730a3',
                      }}
                    >
                      {evt.eventType}
                    </span>
                  </td>
                  <td>{evt.transferId ? `#${evt.transferId}` : 'N/A'}</td>
                  <td>{evt.details || 'Operational log entry'}</td>
                  <td><code>{evt.originService || 'Core-Service'}</code></td>
                  <td>{evt.timestamp || 'Recorded'}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
};
