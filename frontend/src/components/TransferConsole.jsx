import React, { useState, useRef } from 'react';

export const TransferConsole = ({ onTransferCreated }) => {
  // Unit 2 Syllabus: Controlled form state
  const [fileName, setFileName] = useState('');
  const [sender, setSender] = useState('127.0.0.1:8080 (Host Peer)');
  const [receiver, setReceiver] = useState('127.0.0.1:52140 (Client Peer)');
  const [sizeBytes, setSizeBytes] = useState(1048576); // Default 1 MB
  const [status, setStatus] = useState('Success');
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState('');

  // Unit 2 Syllabus: useRef for DOM manipulation / element focus
  const fileNameInputRef = useRef(null);
  const filePickerRef = useRef(null);

  const handleFilePicked = (e) => {
    const file = e.target.files?.[0];
    if (file) {
      setFileName(file.name);
      setSizeBytes(file.size);
      setFormError('');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');

    // Input validation
    if (!fileName.trim()) {
      setFormError('File name cannot be blank.');
      fileNameInputRef.current?.focus();
      return;
    }

    if (sizeBytes <= 0) {
      setFormError('File size must be greater than 0 bytes.');
      return;
    }

    setSubmitting(true);
    try {
      await onTransferCreated({
        fileName: fileName.trim(),
        sender: sender.trim(),
        receiver: receiver.trim(),
        sizeBytes: Number(sizeBytes),
        status,
      });

      // Reset form
      setFileName('');
      if (filePickerRef.current) filePickerRef.current.value = '';
      fileNameInputRef.current?.focus();
    } catch (err) {
      setFormError(err.message || 'Error submitting transfer record');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section className="section-card" aria-label="Direct Metadata Transfer Console">
      <div className="section-header">
        <div>
          <h2 className="section-title">Direct Metadata Transfer Console</h2>
          <p className="section-subtitle">
            Log and coordinate transfer metadata through the Spring Boot Core Service & Audit Microservice.
          </p>
        </div>
      </div>

      <div className="alert-banner info" style={{ marginBottom: '1.25rem' }}>
        <span>
          ℹ️ <strong>System Architecture Note:</strong> Actual high-speed, 64 KB chunked streaming with AES-GCM 
          encryption is executed natively by the <strong>JavaFX Desktop Client</strong> via raw TCP sockets. This web 
          console communicates with the <strong>Spring Boot REST API</strong> to track, persist, and audit session records.
        </span>
      </div>

      {formError && (
        <div className="alert-banner error" style={{ marginBottom: '1rem' }}>
          <span>⚠️ {formError}</span>
        </div>
      )}

      <form className="transfer-form" onSubmit={handleSubmit} noValidate>
        <div className="form-group">
          <label className="form-label" htmlFor="local-file-picker">
            Select Local File (Optional helper)
          </label>
          <input
            id="local-file-picker"
            ref={filePickerRef}
            type="file"
            className="form-input"
            onChange={handleFilePicked}
          />
        </div>

        <div className="form-group">
          <label className="form-label" htmlFor="transfer-file-name">
            Target File Name *
          </label>
          <input
            id="transfer-file-name"
            ref={fileNameInputRef}
            type="text"
            className="form-input"
            placeholder="e.g. Project_Report.pdf"
            value={fileName}
            onChange={(e) => setFileName(e.target.value)}
            required
          />
        </div>

        <div className="form-group">
          <label className="form-label" htmlFor="transfer-size-bytes">
            File Size (Bytes) *
          </label>
          <input
            id="transfer-size-bytes"
            type="number"
            min="1"
            className="form-input"
            value={sizeBytes}
            onChange={(e) => setSizeBytes(Number(e.target.value))}
            required
          />
        </div>

        <div className="form-group">
          <label className="form-label" htmlFor="transfer-sender">
            Sender Node Description
          </label>
          <input
            id="transfer-sender"
            type="text"
            className="form-input"
            value={sender}
            onChange={(e) => setSender(e.target.value)}
          />
        </div>

        <div className="form-group">
          <label className="form-label" htmlFor="transfer-receiver">
            Receiver Node Description
          </label>
          <input
            id="transfer-receiver"
            type="text"
            className="form-input"
            value={receiver}
            onChange={(e) => setReceiver(e.target.value)}
          />
        </div>

        <div className="form-group">
          <label className="form-label" htmlFor="transfer-status">
            Transfer Outcome Status
          </label>
          <select
            id="transfer-status"
            className="form-select"
            value={status}
            onChange={(e) => setStatus(e.target.value)}
          >
            <option value="Success">Success (AES-GCM Stream Verified)</option>
            <option value="Failed: Socket Timeout">Failed: Socket Timeout (30s)</option>
            <option value="Failed: Tag mismatch!">Failed: AEAD Tag Mismatch</option>
            <option value="Cancelled">Cancelled (User Abort)</option>
          </select>
        </div>

        <div className="form-actions">
          <button
            type="submit"
            className="btn btn-primary"
            disabled={submitting}
          >
            {submitting ? 'Registering...' : '💾 Persist via Core API'}
          </button>
          <button
            type="button"
            className="btn btn-secondary"
            onClick={() => {
              setFileName('');
              setSizeBytes(1048576);
              if (filePickerRef.current) filePickerRef.current.value = '';
              fileNameInputRef.current?.focus();
            }}
          >
            Reset
          </button>
        </div>
      </form>
    </section>
  );
};
