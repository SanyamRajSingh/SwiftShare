import { useState, useEffect, useCallback } from 'react';
import { useApp } from '../context/AppContext';

export const useTransferApi = () => {
  const { apiBaseUrl, showNotification } = useApp();
  const [transfers, setTransfers] = useState([]);
  const [auditEvents, setAuditEvents] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Fetch all transfers from the core service
  const loadTransfers = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(`${apiBaseUrl}/transfers`);
      if (!res.ok) {
        throw new Error(`HTTP ${res.status}: ${res.statusText}`);
      }
      const data = await res.json();
      setTransfers(Array.isArray(data) ? data : []);
    } catch (err) {
      console.warn('Backend core service unavailable or returned error:', err.message);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [apiBaseUrl]);

  // Fetch audit events from core service (which calls audit microservice)
  const loadAuditEvents = useCallback(async () => {
    try {
      const res = await fetch(`${apiBaseUrl}/transfers/audit`);
      if (res.ok) {
        const data = await res.json();
        setAuditEvents(Array.isArray(data) ? data : []);
      }
    } catch (err) {
      console.warn('Audit microservice unreachable:', err.message);
    }
  }, [apiBaseUrl]);

  // Create new transfer record
  const createTransfer = async (transferData) => {
    setLoading(true);
    try {
      const res = await fetch(`${apiBaseUrl}/transfers`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(transferData),
      });
      if (!res.ok) {
        const errorBody = await res.json().catch(() => ({}));
        throw new Error(errorBody.message || `Failed to create transfer: HTTP ${res.status}`);
      }
      const created = await res.json();
      showNotification(`Transfer record logged: ${created.fileName}`, 'success');
      await loadTransfers();
      await loadAuditEvents();
      return created;
    } catch (err) {
      showNotification(err.message, 'error');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  // Delete transfer record
  const deleteTransfer = async (id) => {
    try {
      const res = await fetch(`${apiBaseUrl}/transfers/${id}`, {
        method: 'DELETE',
      });
      if (!res.ok) {
        throw new Error(`Failed to delete record: HTTP ${res.status}`);
      }
      showNotification(`Record #${id} removed successfully`, 'info');
      await loadTransfers();
      await loadAuditEvents();
    } catch (err) {
      showNotification(err.message, 'error');
    }
  };

  // Initial load and periodic refresh
  useEffect(() => {
    loadTransfers();
    loadAuditEvents();
    const interval = setInterval(() => {
      loadTransfers();
      loadAuditEvents();
    }, 10000);
    return () => clearInterval(interval);
  }, [loadTransfers, loadAuditEvents]);

  return {
    transfers,
    auditEvents,
    loading,
    error,
    refresh: loadTransfers,
    createTransfer,
    deleteTransfer,
  };
};
