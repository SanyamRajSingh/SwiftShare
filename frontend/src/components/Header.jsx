import React from 'react';
import { useApp } from '../context/AppContext';

export const Header = () => {
  const { activeTab, setActiveTab } = useApp();

  return (
    <header className="app-header">
      <div className="header-container">
        <div className="brand-section">
          <svg className="brand-icon" viewBox="0 0 24 24" aria-hidden="true">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" />
          </svg>
          <h1 className="brand-title">SwiftShare</h1>
          <span className="brand-badge">Syllabus v2.0</span>
          <span style={{ fontSize: '0.8rem', color: '#94a3b8', display: 'flex', alignItems: 'center', marginLeft: '0.5rem' }}>
            <span className="pulse-dot"></span>
            Live Core Sync
          </span>
        </div>

        <nav className="main-nav" aria-label="Main Navigation">
          <ul>
            <li>
              <a
                href="#/dashboard"
                className={activeTab === 'dashboard' ? 'active' : ''}
              >
                Dashboard & History
              </a>
            </li>
            <li>
              <a
                href="#/console"
                className={activeTab === 'console' ? 'active' : ''}
              >
                Direct Metadata Console
              </a>
            </li>
            <li>
              <a
                href="#/audit"
                className={activeTab === 'audit' ? 'active' : ''}
              >
                Audit Trail Telemetry
              </a>
            </li>
            <li>
              <a
                href="#/syllabus"
                className={activeTab === 'syllabus' ? 'active' : ''}
              >
                Syllabus Verification
              </a>
            </li>
          </ul>
        </nav>
      </div>
    </header>
  );
};
