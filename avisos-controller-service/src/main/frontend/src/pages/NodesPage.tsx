import { useEffect, useState } from "react";
import { api } from "../api/client";
import { useSubscription } from "../hooks/useSubscription";
import { StatusBadge } from "../components/ui/StatusBadge";
import type { NodeRecord } from "../types/models";
import "./NodesPage.css";

export function NodesPage() {
  const [nodes, setNodes] = useState<NodeRecord[]>([]);
  const [error, setError] = useState<string | null>(null);
  const nodeUpdate = useSubscription<NodeRecord>("/topic/nodes");

  useEffect(() => {
    api.getNodes().then(setNodes).catch((e) => setError(e.message));
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

  if (error) return <div className="page-error">Error: {error}</div>;

  return (
    <div className="nodes-page">
      <h2 className="page-title">Registered Nodes</h2>
      <table className="scada-table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Status</th>
            <th>Battery</th>
            <th>Last Seen</th>
            <th>UUID</th>
          </tr>
        </thead>
        <tbody>
          {nodes.map((node) => (
            <tr key={node.uuid}>
              <td className="node-name">{node.name}</td>
              <td>{node.type}</td>
              <td><StatusBadge status={node.status} /></td>
              <td>
                <div className="battery-bar-container">
                  <div
                    className={`battery-bar ${batteryColor(node.batteryLevel)}`}
                    style={{ width: `${Math.min(100, node.batteryLevel)}%` }}
                  />
                  <span className="battery-text">{node.batteryLevel.toFixed(0)}%</span>
                </div>
              </td>
              <td className="timestamp">{formatTime(node.lastSeen)}</td>
              <td className="uuid">{node.uuid}</td>
            </tr>
          ))}
          {nodes.length === 0 && (
            <tr>
              <td colSpan={6} className="empty-row">No nodes registered</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}

function batteryColor(level: number): string {
  if (level > 50) return "battery-green";
  if (level > 20) return "battery-amber";
  return "battery-red";
}

function formatTime(ts: string): string {
  if (!ts) return "-";
  try {
    return new Date(ts).toLocaleString("en-US", { hour12: false });
  } catch {
    return ts;
  }
}
