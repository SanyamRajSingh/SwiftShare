import React from 'react';

export const Footer = () => {
  return (
    <footer className="app-footer">
      <div className="footer-content">
        <div>
          <span><strong>SwiftShare Capstone System</strong> &bull; Complete 5-Unit Academic Syllabus Implementation</span>
        </div>
        <div>
          <span style={{ marginRight: '1rem' }}>
            Desktop Engine: <strong>JavaFX 11 / Java 17</strong>
          </span>
          <span style={{ marginRight: '1rem' }}>
            Backend: <strong>Spring Boot 3.3.4 (Core :8080 & Audit :8081)</strong>
          </span>
          <span>
            Web Console: <strong>React 18 / Vite 5</strong>
          </span>
        </div>
      </div>
    </footer>
  );
};
