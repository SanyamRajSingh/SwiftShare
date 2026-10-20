import React, { createContext, useContext, useState, useEffect } from 'react';

const AppContext = createContext(null);

const VALID_ROUTES = ['dashboard', 'console', 'audit', 'syllabus'];

const parseCurrentRoute = () => {
  const hash = window.location.hash.replace(/^#\/?/, '').trim();
  return VALID_ROUTES.includes(hash) ? hash : 'dashboard';
};

export const AppProvider = ({ children }) => {
  // Unit 2 Syllabus: Client-side routing with hashchange event and browser history
  const [activeTab, setActiveTabState] = useState(parseCurrentRoute);
  const [notification, setNotification] = useState(null);
  const [apiBaseUrl, setApiBaseUrl] = useState('/api');

  useEffect(() => {
    const handleHashChange = () => {
      const route = parseCurrentRoute();
      setActiveTabState(route);
    };

    // Ensure initial hash matches state
    if (!window.location.hash) {
      window.location.hash = '#/dashboard';
    }

    window.addEventListener('hashchange', handleHashChange);
    return () => window.removeEventListener('hashchange', handleHashChange);
  }, []);

  const setActiveTab = (tab) => {
    if (VALID_ROUTES.includes(tab)) {
      window.location.hash = `#/${tab}`;
      setActiveTabState(tab);
    }
  };

  const showNotification = (message, type = 'info') => {
    setNotification({ message, type });
    setTimeout(() => {
      setNotification((curr) => (curr?.message === message ? null : curr));
    }, 4000);
  };

  return (
    <AppContext.Provider
      value={{
        activeTab,
        setActiveTab,
        navigateTo: setActiveTab,
        notification,
        setNotification,
        showNotification,
        apiBaseUrl,
        setApiBaseUrl,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useApp must be used within an AppProvider');
  }
  return context;
};
