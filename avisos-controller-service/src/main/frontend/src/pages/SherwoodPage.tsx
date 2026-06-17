import { useEffect, useState } from "react";
import { api } from "../api/client";
import { useSubscription } from "../hooks/useSubscription";
import type { AlarmAnalysisRecord } from "../types/models";
import { formatDateTime } from "../utils/dateTime";
import "./SherwoodPage.css";

export function SherwoodPage() {
  const [analyses, setAnalyses] = useState<AlarmAnalysisRecord[]>([]);
  const [expandedId, setExpandedId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const analysisUpdate = useSubscription<AlarmAnalysisRecord>("/topic/alarm");

  useEffect(() => {
    api.getAnalyses().then(setAnalyses).catch((e) => setError(e.message));
  }, []);

  useEffect(() => {
    if (!analysisUpdate) return;
    setAnalyses((prev) => {
      const idx = prev.findIndex((a) => a.alarmId === analysisUpdate.alarmId);
      if (idx >= 0) {
        const updated = [...prev];
        updated[idx] = analysisUpdate;
        return updated;
      }
      return [analysisUpdate, ...prev];
    });
  }, [analysisUpdate]);

  if (error) return <div className="page-error">Error: {error}</div>;

  return (
    <div className="sherwood-page">
      <div className="page-heading-row">
        <h2 className="page-title">Sherwood</h2>
        <span className="page-subtitle">AI Incident Analyst &middot; {analyses.length} analyses</span>
      </div>
      <table className="scada-table">
        <thead>
          <tr>
            <th>Alarm ID</th>
            <th>Analysis</th>
            <th>Prompt</th>
            <th>Timestamp</th>
          </tr>
        </thead>
        <tbody>
          {analyses.map((record) => (
            <tr
              key={record.alarmId}
              className={`sherwood-row ${expandedId === record.alarmId ? "expanded" : ""}`}
              onClick={() =>
                setExpandedId(expandedId === record.alarmId ? null : record.alarmId)
              }
            >
              <td className="uuid">{record.alarmId}</td>
              <td className="analysis-cell">
                {expandedId === record.alarmId ? (
                  <pre className="analysis-full">{record.analysisText}</pre>
                ) : (
                  <span className="analysis-truncated">
                    {record.analysisText.substring(0, 100)}
                    {record.analysisText.length > 100 ? "..." : ""}
                  </span>
                )}
              </td>
              <td className="prompt-version">{record.promptVersion}</td>
              <td className="timestamp">{formatDateTime(record.createdAt)}</td>
            </tr>
          ))}
          {analyses.length === 0 && (
            <tr>
              <td colSpan={4} className="empty-row">
                No analyses generated yet
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}