import { useEffect, useState } from "react";
import { api } from "../api/client";
import { useSubscription } from "../hooks/useSubscription";
import { StatusBadge } from "../components/ui/StatusBadge";
import type { AlarmRecord } from "../types/models";
import { dateTimeMillis, formatDateTime } from "../utils/dateTime";
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
    setAlarms((prev) => upsertAlarm(prev, alarmUpdate));
  }, [alarmUpdate]);

  async function handleResolve(id: string) {
    try {
      await api.resolveAlarm(id);
      setAlarms((prev) => prev.filter((a) => a.id !== id));
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Failed to resolve alarm");
    }
  }

  if (error) return <div className="page-error">Error: {error}</div>;

  const sorted = [...alarms].sort((a, b) => {
    const sevOrder = { CRITICAL: 0, WARNING: 1, NONE: 2 };
    const severityDelta = sevOrder[a.severity] - sevOrder[b.severity];
    if (severityDelta !== 0) return severityDelta;
    return dateTimeMillis(b.triggeredAtTimestamp) - dateTimeMillis(a.triggeredAtTimestamp);
  });

  return (
    <div className="alarms-page">
      <div className="page-heading-row">
        <h2 className="page-title">Active Alarm Monitor</h2>
        <span className="page-subtitle">{sorted.length} active</span>
      </div>
      <table className="scada-table">
        <thead>
          <tr>
            <th>Severity</th>
            <th>Status</th>
            <th>Reason</th>
            <th>Device</th>
            <th>Evidence</th>
            <th>Triggered</th>
            <th>Resolved</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {sorted.map((alarm) => (
            <tr
              key={alarm.id}
              className={alarm.severity === "CRITICAL" && alarm.status === "ACTIVE" ? "row-critical" : ""}
            >
              <td>
                <StatusBadge status={alarm.severity} />
              </td>
              <td>
                <StatusBadge status={alarm.status} />
              </td>
              <td className="alarm-reason">{alarm.reason}</td>
              <td className="uuid">{alarm.deviceUuid}</td>
              <td>
                <AlarmEvidence alarmId={alarm.id} s3ImageKey={alarm.s3ImageKey} />
              </td>
              <td className="timestamp">{formatDateTime(alarm.triggeredAtTimestamp)}</td>
              <td className="timestamp">{formatDateTime(alarm.resolvedAtTimestamp)}</td>
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
              <td colSpan={8} className="empty-row">
                No active alarms
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

function AlarmEvidence({ alarmId, s3ImageKey }: { alarmId: string; s3ImageKey: string | null }) {
  const [imageUnavailable, setImageUnavailable] = useState(false);

  if (!s3ImageKey) {
    return <span className="muted-cell">Pending</span>;
  }

  const imageUrl = api.getAlarmImageUrl(alarmId);

  if (imageUnavailable) {
    return (
      <a className="evidence-link evidence-link-broken" href={imageUrl} target="_blank" rel="noreferrer">
        Image unavailable
      </a>
    );
  }

  return (
    <a className="alarm-image-link" href={imageUrl} target="_blank" rel="noreferrer" title={s3ImageKey}>
      <img
        className="alarm-image-thumb"
        src={imageUrl}
        alt="Alarm evidence"
        loading="lazy"
        onError={() => setImageUnavailable(true)}
      />
      <span className="evidence-key">{s3ImageKey}</span>
    </a>
  );
}

function upsertAlarm(existing: AlarmRecord[], incoming: AlarmRecord): AlarmRecord[] {
  const index = existing.findIndex((alarm) => alarm.id === incoming.id);
  if (index < 0) return [incoming, ...existing];
  const updated = [...existing];
  updated[index] = incoming;
  return updated;
}
