import React, { useEffect } from 'react';
import { useApp } from './context/AppContext';
import { useTransferApi } from './hooks/useTransferApi';
import { Header } from './components/Header';
import { DashboardMetrics } from './components/DashboardMetrics';
import { TransferConsole } from './components/TransferConsole';
import { TransferTable } from './components/TransferTable';
import { AuditTrailView } from './components/AuditTrailView';
import { SyllabusShowcase } from './components/SyllabusShowcase';
import { Footer } from './components/Footer';

function AppContent() {
  const { activeTab, notification, setNotification } = useApp();
  const {
    transfers,
    auditEvents,
    loading,
    error,
    refresh,
    createTransfer,
    deleteTransfer,
  } = useTransferApi();

  // Unit 2 Syllabus: useEffect hook for synchronizing document title
  useEffect(() => {
    const titles = {
      dashboard: 'SwiftShare — Dashboard & History',
      console: 'SwiftShare — Metadata Console',
      audit: 'SwiftShare — Microservice Audit Telemetry',
      syllabus: 'SwiftShare — Syllabus Verification',
    };
    document.title = titles[activeTab] || 'SwiftShare Web Console';
  }, [activeTab]);

  return (
    <>
      <Header />

      <main className="app-main">
        {/* Floating Notification Banner */}
        {notification && (
          <aside className={`alert-banner ${notification.type}`} role="status">
            <span>{notification.message}</span>
            <button
              type="button"
              className="btn btn-secondary"
              style={{ padding: '0.15rem 0.5rem', fontSize: '0.75rem' }}
              onClick={() => setNotification(null)}
            >
              ✕
            </button>
          </aside>
        )}

        {/* View Switching (Client-Side Navigation) */}
        {activeTab === 'dashboard' && (
          <>
            <DashboardMetrics transfers={transfers} />
            <TransferTable
              transfers={transfers}
              loading={loading}
              error={error}
              onRefresh={refresh}
              onDelete={deleteTransfer}
            />
          </>
        )}

        {activeTab === 'console' && (
          <>
            <TransferConsole onTransferCreated={createTransfer} />
            <TransferTable
              transfers={transfers}
              loading={loading}
              error={error}
              onRefresh={refresh}
              onDelete={deleteTransfer}
            />
          </>
        )}

        {activeTab === 'audit' && (
          <AuditTrailView auditEvents={auditEvents} />
        )}

        {activeTab === 'syllabus' && (
          <SyllabusShowcase />
        )}
      </main>

      <Footer />
    </>
  );
}

export default function App() {
  return <AppContent />;
}
