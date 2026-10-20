import React, { useState, useEffect, useRef } from 'react';

export const SyllabusShowcase = () => {
  // Unit 2 Syllabus: Vanilla JS DOM manipulation & event demonstration
  const [vanillaMessage, setVanillaMessage] = useState('Default React State');
  const [demoCount, setDemoCount] = useState(0);

  // Unit 2 Syllabus: JavaScript Conditional Logic State
  const [testFileSize, setTestFileSize] = useState(5242880); // 5 MB
  const [isEncrypted, setIsEncrypted] = useState(true);

  // Unit 2 Syllabus: Canvas ref for interactive multimedia animation
  const canvasRef = useRef(null);

  // Demonstrate native DOM access alongside React (document.getElementById, style mutations)
  const runVanillaDomDemo = () => {
    const targetElement = document.getElementById('vanilla-demo-target');
    if (targetElement) {
      targetElement.innerText = `Vanilla DOM directly updated via document.getElementById at ${new Date().toLocaleTimeString()}!`;
      targetElement.style.color = '#4338ca';
      targetElement.style.fontWeight = 'bold';
      setDemoCount((c) => c + 1);
    }
  };

  // Unit 2 Syllabus: JavaScript Conditional Statements (if/else if/else, switch/case, ternary)
  const evaluateTransferPriority = (sizeBytes, encrypted) => {
    let priorityTier;
    if (sizeBytes > 10 * 1024 * 1024) {
      priorityTier = 'Tier 1 — High (Bulk TCP Streaming)';
    } else if (sizeBytes > 1024 * 1024) {
      priorityTier = 'Tier 2 — Medium (Buffered Transfer)';
    } else {
      priorityTier = 'Tier 3 — Immediate (Direct Chunk)';
    }

    let cipherMode;
    switch (encrypted ? 'AES_GCM' : 'PLAINTEXT') {
      case 'AES_GCM':
        cipherMode = 'Authenticated Cipher (AES-256-GCM)';
        break;
      case 'PLAINTEXT':
        cipherMode = 'Unencrypted (Direct TCP)';
        break;
      default:
        cipherMode = 'Unknown Protocol';
    }

    const readiness = sizeBytes > 0 && encrypted ? 'Ready & Protected' : 'Requires Review';
    return { priorityTier, cipherMode, readiness };
  };

  const conditionResult = evaluateTransferPriority(testFileSize, isEncrypted);

  // Unit 2 Syllabus: JavaScript Loops (for, while, for...of)
  const executeLoopsDemo = () => {
    // 1. Classic for-loop: Generate chunk packets
    const chunks = [];
    for (let i = 1; i <= 5; i++) {
      chunks.push({ id: i, label: `Packet #${i}`, hash: `0x${(i * 4096).toString(16).toUpperCase()}` });
    }

    // 2. While-loop: Simulate connection retry counter
    let attempts = 0;
    let isConnected = false;
    while (!isConnected && attempts < 3) {
      attempts++;
      if (attempts === 2) {
        isConnected = true;
      }
    }

    // 3. For...of loop: Iterate over packet collections
    const verifiedLabels = [];
    for (const chunk of chunks) {
      verifiedLabels.push(`${chunk.label} [${chunk.hash}]`);
    }

    return { chunks, attempts, isConnected, verifiedSummary: verifiedLabels.join(', ') };
  };

  const loopsResult = executeLoopsDemo();

  // Unit 2 Syllabus: ES6+ Array Processing (filter, map, reduce)
  const sampleData = [
    { name: 'swiftshare_spec.pdf', size: 1200000, secure: true },
    { name: 'architecture_diagram.png', size: 450000, secure: true },
    { name: 'legacy_test.bin', size: 12000, secure: false },
  ];
  const secureFiles = sampleData.filter((item) => item.secure).map((item) => item.name);
  const totalDemoBytes = sampleData.reduce((acc, curr) => acc + curr.size, 0);

  // Unit 1 & 2 Syllabus: Interactive HTML5 Canvas Animation (Multimedia packet streaming)
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    let x = 10;
    let animId;

    const render = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height);

      // Draw background channel
      ctx.fillStyle = '#e0e7ff';
      ctx.fillRect(0, 15, canvas.width, 20);

      // Draw transmitting node
      ctx.fillStyle = '#4f46e5';
      ctx.beginPath();
      ctx.arc(15, 25, 10, 0, Math.PI * 2);
      ctx.fill();

      // Draw receiving node
      ctx.fillStyle = '#059669';
      ctx.beginPath();
      ctx.arc(canvas.width - 15, 25, 10, 0, Math.PI * 2);
      ctx.fill();

      // Animate packet
      ctx.fillStyle = '#f59e0b';
      ctx.fillRect(x, 20, 14, 10);
      x = (x + 2) % (canvas.width - 35);
      if (x < 10) x = 10;

      animId = requestAnimationFrame(render);
    };

    render();
    return () => cancelAnimationFrame(animId);
  }, []);

  return (
    <section className="section-card" aria-label="Syllabus Verification and Demonstrations">
      <div className="section-header">
        <div>
          <h2 className="section-title">Academic Syllabus Coverage Demonstration</h2>
          <p className="section-subtitle">
            Direct, verifiable demonstrations of Unit 1 (HTML/CSS) and Unit 2 (JS/React) academic requirements.
          </p>
        </div>
      </div>

      {/* 1. Vanilla JavaScript DOM Manipulation Showcase */}
      <div style={{ marginBottom: '1.5rem', padding: '1rem', background: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
        <h3 style={{ fontSize: '1rem', fontWeight: 600, marginBottom: '0.5rem', color: '#1e293b' }}>
          Interactive Vanilla JavaScript DOM & Browser Events
        </h3>
        <p style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '0.75rem' }}>
          Native <code>document.getElementById()</code> and direct style manipulation:
        </p>

        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center', flexWrap: 'wrap' }}>
          <button
            type="button"
            className="btn btn-primary"
            onClick={runVanillaDomDemo}
          >
            ⚡ Run Vanilla DOM Manipulation
          </button>

          <span
            id="vanilla-demo-target"
            style={{ padding: '0.4rem 0.8rem', background: '#ffffff', border: '1px dashed #cbd5e1', borderRadius: '4px', fontSize: '0.85rem' }}
          >
            Target DOM Node (#vanilla-demo-target) awaiting native click event...
          </span>

          <span style={{ fontSize: '0.75rem', color: '#64748b' }}>
            Trigger count: {demoCount}
          </span>
        </div>
      </div>

      {/* 2. JavaScript Conditional Statements (if/else, switch/case, ternary) */}
      <div style={{ marginBottom: '1.5rem', padding: '1rem', background: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
        <h3 style={{ fontSize: '1rem', fontWeight: 600, marginBottom: '0.5rem', color: '#1e293b' }}>
          JavaScript Conditional Statements (if-else, switch, ternary)
        </h3>
        <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'center', marginBottom: '0.75rem' }}>
          <label style={{ fontSize: '0.85rem', color: '#475569' }}>
            Simulated Payload:
            <select
              className="form-select"
              style={{ display: 'inline-block', width: 'auto', marginLeft: '0.5rem', padding: '0.2rem 0.5rem' }}
              value={testFileSize}
              onChange={(e) => setTestFileSize(Number(e.target.value))}
            >
              <option value={512000}>500 KB (Direct Chunk)</option>
              <option value={5242880}>5 MB (Standard Buffer)</option>
              <option value={15728640}>15 MB (Bulk Streaming)</option>
            </select>
          </label>

          <label style={{ fontSize: '0.85rem', color: '#475569', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
            <input
              type="checkbox"
              checked={isEncrypted}
              onChange={(e) => setIsEncrypted(e.target.checked)}
            />
            AES-GCM Encryption
          </label>
        </div>

        <ul style={{ fontSize: '0.85rem', color: '#475569', paddingLeft: '1.25rem' }}>
          <li><strong>if-else if-else branch:</strong> Evaluated Priority = <code>{conditionResult.priorityTier}</code></li>
          <li><strong>switch-case statement:</strong> Encryption Classification = <code>{conditionResult.cipherMode}</code></li>
          <li><strong>Ternary Operator (condition ? a : b):</strong> Readiness State = <span className="status-badge success">{conditionResult.readiness}</span></li>
        </ul>
      </div>

      {/* 3. JavaScript Loops (for, while, for...of) */}
      <div style={{ marginBottom: '1.5rem', padding: '1rem', background: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
        <h3 style={{ fontSize: '1rem', fontWeight: 600, marginBottom: '0.5rem', color: '#1e293b' }}>
          JavaScript Loops & Iteration (for, while, for...of)
        </h3>
        <ul style={{ fontSize: '0.85rem', color: '#475569', paddingLeft: '1.25rem' }}>
          <li>
            <strong>Classic for-loop (<code>for (let i = 1; i &lt;= 5; i++)</code>):</strong> Generated {loopsResult.chunks.length} transfer chunk checksums.
          </li>
          <li>
            <strong>while-loop (<code>while (!connected &amp;&amp; retries &lt; 3)</code>):</strong> Resolved connection on attempt #{loopsResult.attempts}.
          </li>
          <li>
            <strong>for...of loop (<code>for (const chunk of chunks)</code>):</strong> Iterated and formatted: <code>{loopsResult.verifiedSummary}</code>
          </li>
          <li>
            <strong>ES6 Array.filter &amp; map:</strong> Filtered secure files = <code>[{secureFiles.join(', ')}]</code>
          </li>
          <li>
            <strong>ES6 Array.reduce:</strong> Total demo payload = <code>{(totalDemoBytes / 1024 / 1024).toFixed(2)} MB</code>
          </li>
        </ul>
      </div>

      {/* 4. Semantic HTML5 Multimedia & Accessible Content (Audio, Video, Canvas, Figure) */}
      <div style={{ marginBottom: '1.5rem', padding: '1rem', background: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
        <h3 style={{ fontSize: '1rem', fontWeight: 600, marginBottom: '0.5rem', color: '#1e293b' }}>
          Semantic HTML5 Multimedia &amp; Accessible Content
        </h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1.25rem', alignItems: 'start' }}>
          {/* Vector SVG in Semantic Figure */}
          <figure style={{ margin: 0, padding: '0.75rem', background: '#ffffff', borderRadius: '6px', border: '1px solid #e2e8f0', textAlign: 'center' }}>
            <svg width="60" height="60" viewBox="0 0 24 24" fill="#6366f1" style={{ borderRadius: '8px', background: '#e0e7ff', padding: '10px' }}>
              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
            </svg>
            <figcaption style={{ fontSize: '0.75rem', color: '#64748b', marginTop: '0.35rem' }}>
              Semantic <code>&lt;figure&gt;</code> &amp; Vector SVG
            </figcaption>
          </figure>

          {/* HTML5 Audio */}
          <div style={{ padding: '0.75rem', background: '#ffffff', borderRadius: '6px', border: '1px solid #e2e8f0' }}>
            <p style={{ fontSize: '0.8rem', fontWeight: 600, color: '#334155', marginBottom: '0.35rem' }}>
              HTML5 <code>&lt;audio&gt;</code> Transfer Chime
            </p>
            <audio controls style={{ width: '100%', height: '36px' }}>
              <source src="data:audio/wav;base64,UklGRjIAAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YRAAAAAAAA==" type="audio/wav" />
              Your browser does not support the audio element.
            </audio>
            <span style={{ fontSize: '0.7rem', color: '#64748b' }}>WAV Synthesized Notification</span>
          </div>

          {/* HTML5 Video / Canvas Stream Simulation */}
          <div style={{ padding: '0.75rem', background: '#ffffff', borderRadius: '6px', border: '1px solid #e2e8f0' }}>
            <p style={{ fontSize: '0.8rem', fontWeight: 600, color: '#334155', marginBottom: '0.35rem' }}>
              HTML5 <code>&lt;canvas&gt;</code> &amp; Stream Animation
            </p>
            <canvas
              ref={canvasRef}
              width="240"
              height="50"
              style={{ width: '100%', height: '50px', borderRadius: '4px', background: '#f1f5f9' }}
              aria-label="P2P Transfer packet streaming animation"
            />
            <span style={{ fontSize: '0.7rem', color: '#64748b' }}>Live TCP Packet Transmission Sim</span>
          </div>
        </div>
      </div>

      {/* 5. Complete Syllabus Mapping Grid */}
      <div className="showcase-grid">
        <div className="showcase-card">
          <h4>Unit 1: HTML &amp; CSS</h4>
          <ul>
            <li>✅ Semantic layout: <code>header</code>, <code>nav</code>, <code>main</code>, <code>section</code>, <code>figure</code>, <code>footer</code></li>
            <li>✅ Form controls: <code>input</code>, <code>select</code>, <code>label</code>, <code>button</code></li>
            <li>✅ CSS Box model reset (<code>box-sizing: border-box</code>)</li>
            <li>✅ Flexbox &amp; CSS Grid responsive layouts</li>
            <li>✅ Transitions, <code>@keyframes pulse</code>, media queries</li>
            <li>✅ Multimedia: <code>&lt;audio&gt;</code>, <code>&lt;canvas&gt;</code>, vector SVG</li>
          </ul>
        </div>

        <div className="showcase-card">
          <h4>Unit 2: JavaScript &amp; React</h4>
          <ul>
            <li>✅ Variables, arrays, objects, functions</li>
            <li>✅ Conditional statements: <code>if-else</code>, <code>switch</code>, ternary</li>
            <li>✅ Loops &amp; iteration: <code>for</code>, <code>while</code>, <code>for...of</code></li>
            <li>✅ Vanilla DOM manipulation (<code>document.getElementById</code>)</li>
            <li>✅ Client-side routing with hash navigation &amp; browser history</li>
            <li>✅ Hooks: <code>useState</code>, <code>useEffect</code>, <code>useContext</code>, <code>useMemo</code>, <code>useRef</code></li>
            <li>✅ Custom hook: <code>useTransferApi()</code></li>
          </ul>
        </div>

        <div className="showcase-card">
          <h4>Unit 3: SQL &amp; JDBC</h4>
          <ul>
            <li>✅ Preserved SQLite JDBC in JavaFX desktop app</li>
            <li>✅ Spring Data JPA relational persistence in Core service</li>
            <li>✅ Executable SQL demonstration script (CRUD, JOINs, UNION, EXCEPT)</li>
            <li>✅ Parameterized queries preventing SQL injection</li>
            <li>✅ Relational foreign keys and entity associations</li>
          </ul>
        </div>

        <div className="showcase-card">
          <h4>Units 4 &amp; 5: Spring Boot &amp; Microservices</h4>
          <ul>
            <li>✅ Core Service (8080): <code>@RestController</code>, Service, Repository</li>
            <li>✅ Audit Service (8081): Independent microservice persistence</li>
            <li>✅ Service-to-service HTTP communication via <code>RestClient</code></li>
            <li>✅ Resilient audit handling: honest <code>auditStatus</code> distinction</li>
            <li>✅ JPA entities, relationships, validation, exception advice</li>
          </ul>
        </div>
      </div>
    </section>
  );
};
