import React, { useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { Activity, Brain, ShieldCheck, Terminal } from 'lucide-react';
import './styles.css';

type RiskResponse = {
  decisionId: string;
  decision: 'APPROVE' | 'REVIEW' | 'DECLINE';
  riskScore: number;
  reasons: string[];
  recommendation: string;
  createdAt: string;
};

const defaultPayload = JSON.stringify({
  customerId: 'cust-1001',
  transactionId: `tx-${Date.now()}`,
  transactionAmount: 12500,
  currency: 'USD',
  countryCode: 'US',
  merchantCategory: 'WIRE_TRANSFER',
  accountAgeDays: 14,
  failedLoginCount: 5,
  newDevice: true,
  deviceTrustScore: 35,
  velocity30m: 6,
  previousChargebacks: 1,
  ipRiskScore: 62
}, null, 2);

function App() {
  const [apiKey, setApiKey] = useState('');
  const [baseUrl, setBaseUrl] = useState(import.meta.env.VITE_API_BASE_URL || '');
  const [payload, setPayload] = useState(defaultPayload);
  const [response, setResponse] = useState<RiskResponse | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const decisionClass = useMemo(() => {
    if (!response) return '';
    return response.decision.toLowerCase();
  }, [response]);

  async function submit() {
    setError('');
    setLoading(true);
    setResponse(null);
    try {
      const headers: Record<string, string> = {
        'Content-Type': 'application/json',
        'Idempotency-Key': `dashboard-${Date.now()}`
      };
      if (apiKey.trim()) {
        headers['X-API-Key'] = apiKey.trim();
      }

      const res = await fetch(`${baseUrl}/api/v1/risk/score`, {
        method: 'POST',
        headers,
        body: payload
      });
      const body = await res.json();
      if (!res.ok) throw new Error(body.message || 'Request failed');
      setResponse(body);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unexpected error');
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="shell">
      <section className="hero">
        <div>
          <p className="eyebrow">Java 21 • Spring Boot • Spring AI MCP • Risk decisioning</p>
          <h1>AI API Gateway MCP</h1>
          <p className="subtitle">Recruiter-facing demo of backend platform engineering, explainable risk decisions, observability, and AI tool orchestration.</p>
        </div>
        <div className="heroCards">
          <Metric icon={<ShieldCheck />} label="Security" value="API key" />
          <Metric icon={<Activity />} label="Signals" value="Explainable" />
          <Metric icon={<Brain />} label="AI layer" value="Spring AI MCP" />
        </div>
      </section>

      <section className="grid">
        <div className="card wide">
          <h2><Terminal size={20} /> Request</h2>
          <div className="formRow">
            <input aria-label="API base URL" placeholder="API base URL (blank = Docker proxy)" value={baseUrl} onChange={e => setBaseUrl(e.target.value)} />
            <input aria-label="API key" placeholder="API key (blank = nginx injects it in Docker)" value={apiKey} onChange={e => setApiKey(e.target.value)} />
          </div>
          <textarea value={payload} onChange={e => setPayload(e.target.value)} rows={20} />
          <p className="muted small">Docker mode: leave both fields blank. Nginx proxies /api to the backend and injects the API key from an environment variable.</p>
          <button onClick={submit} disabled={loading}>{loading ? 'Scoring…' : 'Score transaction'}</button>
          {error && <p className="error">{error}</p>}
        </div>

        <div className="card">
          <h2>Decision</h2>
          {!response && <p className="muted">Submit a transaction to see the decision output.</p>}
          {response && (
            <div>
              <div className={`decision ${decisionClass}`}>{response.decision}</div>
              <div className="score">Risk score: <strong>{response.riskScore}</strong>/100</div>
              <p>{response.recommendation}</p>
              <ul>
                {response.reasons.map(reason => <li key={reason}>{reason}</li>)}
              </ul>
              <p className="muted">Decision ID: {response.decisionId}</p>
            </div>
          )}
        </div>
      </section>
    </main>
  );
}

function Metric({ icon, label, value }: { icon: React.ReactNode; label: string; value: string }) {
  return <div className="metric"><span>{icon}</span><small>{label}</small><strong>{value}</strong></div>;
}

createRoot(document.getElementById('root')!).render(<App />);
