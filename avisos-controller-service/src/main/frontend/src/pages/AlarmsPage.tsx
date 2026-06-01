import { useEffect, useState } from "react";
import { api } from "../api/client";
import { useSubscription } from "../hooks/useSubscription";
import { StatusBadge } from "../components/ui/StatusBadge";
import type { AlarmRecord } from "../types/models";
import "./AlarmsPage.css";

export function AlarmsPage() {
  const [alarms, setAlarms] = useState<AlarmRecord[]>([]);
  const [error, setError] = useState<string | null>(null);
  const alarmUpdate = useSubscription<AlarmRecord>("/topic/alarms");

  useEffect(() => {
    api.getAlarms().then(setAlarms).catch((e) => setError(e.message));
  }, []);

  useEffect(() => {
    if (!alarmUpdate) return;
    setAlarms((prev) => [alarmUpdate, ...prev]);
  }, [alarmUpdate]);

  async function handleResolve(id: string) {
    try {
      await api.resolveAlarm(id);
      setAlarms((prev) =>
        prev.map((a) =>
          a.id === id ? { ...a, status: "RESOLVED", resolvedAtTimestamp: new Date().toISOString() } : a,
        ),
      );
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Failed to resolve alarm");
    }
  }

  if (error) return <div className="page-error">Error: {error}</div>;

  const sorted = [...alarms].sort((a, b) => {
    const sevOrder = { CRITICAL: 0, WARNING: 1, NONE: 2 };
    const statusOrder = { ACTIVE: 0, RESOLVED: 1 };
    const s = statusOrder[a.status] - statusOrder[b.status];
    if (s !== 0) return s;
    return sevOrder[a.severity] - sevOrder[b.severity];
  });

  return (
    <div className="alarms-page">
      <h2 className="page-title">Alarm Monitor</h2>
      <table className="scada-table">
        <thead>
          <tr>
            <th>Severity</th>
            <th>Status</th>
            <th>Reason</th>
            <th>Device</th>
            <th>Triggered</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {sorted.map((alarm) => (
            <tr key={alarm.id} className={alarm.severity === "CRITICAL" && alarm.status === "ACTIVE" ? "row-critical" : ""}>
              <td><StatusBadge status={alarm.severity} /></td>
              <td><StatusBadge status={alarm.status} /></td>
              <td className="alarm-reason">{alarm.reason}</td>
              <td className="uuid">{alarm.deviceUuid}</td>
              <td className="timestamp">{formatTime(alarm.triggeredAtTimestamp)}</td>
              <td>
                {alarm.status === "ACTIVE" && (
                  <button className="resolve-btn" onClick={() => handleResolve(alarm.id)}>
                    Resolve
                  </button>
                )}
              </td>
            </tr>
          ))}
          {alarms.length === 0 && (
            <tr>
              <td colSpan={6} className="empty-row">No alarms</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

function formatTime(ts: string): string {
  if (!ts) return "-";
  try {
    return new Date(ts).toLocaleString("en-US", { hour12: false });
  } catch {
    return ts;
  }
}
