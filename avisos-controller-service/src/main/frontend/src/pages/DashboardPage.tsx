import { useEffect, useState } from "react";
import { api } from "../api/client";
import { useSubscription } from "../hooks/useSubscription";
import { DataCard } from "../components/ui/DataCard";
import { StatusBadge } from "../components/ui/StatusBadge";
import type {
  NodeRecord,
  AlarmRecord,
  SystemHealthReport,
  SystemStats,
  VisionEvent,
} from "../types/models";
import "./DashboardPage.css";

export function DashboardPage() {
  const [health, setHealth] = useState<SystemHealthReport | null>(null);
  const [nodes, setNodes] = useState<NodeRecord[]>([]);
  const [alarms, setAlarms] = useState<AlarmRecord[]>([]);
  const [stats, setStats] = useState<SystemStats | null>(null);
  const [error, setError] = useState<string | null>(null);

  const nodeUpdate = useSubscription<NodeRecord>("/topic/nodes");
  const alarmUpdate = useSubscription<AlarmRecord>("/topic/alarms");
  const visionUpdate = useSubscription<VisionEvent>("/topic/vision");

  useEffect(() => {
    Promise.all([api.getHealth(), api.getNodes(), api.getAlarms(), api.getStats()])
      .then(([h, n, a, s]) => {
        setHealth(h);
        setNodes(n);
        setAlarms(a);
        setStats(s);
      })
      .catch((e) => setError(e.message));
  }, []);

  useEffect(() => {
    if (!nodeUpdate) return;
    setNodes((prev) => {
      const idx = prev.findIndex((n) => n.uuid === nodeUpdate.uuid);
      if (idx >= 0) {
        const updated = [...prev];
        updated[idx] = nodeUpdate;
        return updated;
      }
      return [...prev, nodeUpdate];
    });
  }, [nodeUpdate]);

  useEffect(() => {
    if (!alarmUpdate) return;
    setAlarms((prev) => [alarmUpdate, ...prev]);
  }, [alarmUpdate]);

  if (error) {
    return <div className="dash-error">Error: {error}</div>;
  }

  const responsive = nodes.filter((n) => n.status === "RESPONSIVE").length;
  const critical = alarms.filter((a) => a.severity === "CRITICAL" && a.status === "ACTIVE").length;
  const warnings = alarms.filter((a) => a.severity === "WARNING" && a.status === "ACTIVE").length;

  return (
    <div className="dashboard">
      <div className="dash-grid">
        <DataCard title="System Health" accent={health?.overallStatus === "HEALTHY" ? "green" : health?.overallStatus === "DEGRADED" ? "amber" : "red"}>
          {health ? (
            <div className="health-panel">
              <div className="health-overall">
                <StatusBadge status={health.overallStatus} />
              </div>
              <div className="health-components">
                {health.components.map((c) => (
                  <div key={c.component} className="health-row">
                    <span className="comp-name">{c.component}</span>
                    <StatusBadge status={c.status} />
                    <span className="comp-latency">{c.latencyMs}ms</span>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <span className="loading">Loading...</span>
          )}
        </DataCard>

        <DataCard title="Node Overview" accent="blue">
          <div className="stat-grid">
            <div className="stat-item">
              <span className="stat-value green">{responsive}</span>
              <span className="stat-label">Online</span>
            </div>
            <div className="stat-item">
              <span className="stat-value amber">{nodes.length - responsive}</span>
              <span className="stat-label">Offline</span>
            </div>
            <div className="stat-item">
              <span className="stat-value">{nodes.length}</span>
              <span className="stat-label">Total</span>
            </div>
          </div>
        </DataCard>

        <DataCard title="Active Alarms" accent={critical > 0 ? "red" : warnings > 0 ? "amber" : "green"}>
          <div className="stat-grid">
            <div className="stat-item">
              <span className="stat-value red">{critical}</span>
              <span className="stat-label">Critical</span>
            </div>
            <div className="stat-item">
              <span className="stat-value amber">{warnings}</span>
              <span className="stat-label">Warnings</span>
            </div>
            <div className="stat-item">
              <span className="stat-value green">{alarms.filter((a) => a.status === "RESOLVED").length}</span>
              <span className="stat-label">Resolved</span>
            </div>
          </div>
        </DataCard>

        <DataCard title="System Stats" accent="blue">
          {stats ? (
            <div className="stats-panel">
              <div className="stats-row">
                <span>Heap</span>
                <span className="stats-value">{stats.heapUsedMb}/{stats.heapMaxMb} MB</span>
              </div>
              <div className="stats-row">
                <span>Threads</span>
                <span className="stats-value">{stats.activeThreads}</span>
              </div>
              <div className="stats-row">
                <span>CPUs</span>
                <span className="stats-value">{stats.availableProcessors}</span>
              </div>
              <div className="stats-row">
                <span>Uptime</span>
                <span className="stats-value">{formatUptime(stats.uptimeSeconds)}</span>
              </div>
              <div className="stats-row">
                <span>Java</span>
                <span className="stats-value">{stats.javaVersion}</span>
              </div>
            </div>
          ) : (
            <span className="loading">Loading...</span>
          )}
        </DataCard>

        {visionUpdate && (
          <DataCard title="Last Vision Analysis" accent="amber">
            <div className="vision-panel">
              <div className="stats-row">
                <span>Node</span>
                <span className="stats-value">{visionUpdate.nodeId}</span>
              </div>
              <div className="vision-labels">
                {visionUpdate.response.predictions?.map((p, i) => (
                  <div key={i} className="vision-label">
                    <span>{p.label}</span>
                    <span className="confidence">{(p.confidence * 100).toFixed(1)}%</span>
                  </div>
                ))}
              </div>
            </div>
          </DataCard>
        )}
      </div>
    </div>
  );
}

function formatUptime(seconds: number): string {
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = Math.floor(seconds % 60);
  return `${h}h ${m}m ${s}s`;
}
