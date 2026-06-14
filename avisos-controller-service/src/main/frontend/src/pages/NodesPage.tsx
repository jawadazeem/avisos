import { useEffect, useMemo, useState } from "react";
import { api } from "../api/client";
import { useSubscription } from "../hooks/useSubscription";
import { StatusBadge } from "../components/ui/StatusBadge";
import type { NodeRecord } from "../types/models";
import { dateTimeMillis, formatDateTime } from "../utils/dateTime";
import "./NodesPage.css";

const PAGE_SIZE = 20;

export function NodesPage() {
  const [nodes, setNodes] = useState<NodeRecord[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(1);
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

  const sortedNodes = useMemo(
    () =>
      [...nodes].sort((a, b) => {
        const seen = dateTimeMillis(b.lastSeen) - dateTimeMillis(a.lastSeen);
        if (Number.isFinite(seen) && seen !== 0) return seen;
        return a.name.localeCompare(b.name);
      }),
    [nodes],
  );

  const pageCount = Math.max(1, Math.ceil(sortedNodes.length / PAGE_SIZE));
  const currentPage = Math.min(page, pageCount);
  const pageStart = (currentPage - 1) * PAGE_SIZE;
  const visibleNodes = sortedNodes.slice(pageStart, pageStart + PAGE_SIZE);

  useEffect(() => {
    if (page > pageCount) {
      setPage(pageCount);
    }
  }, [page, pageCount]);

  if (error) return <div className="page-error">Error: {error}</div>;

  return (
    <div className="nodes-page">
      <div className="page-header">
        <div>
          <h2 className="page-title">Registered Nodes</h2>
          <span className="page-subtitle">
            {nodes.length} total / {nodes.filter((node) => node.status === "RESPONSIVE").length} responsive
          </span>
        </div>
        <div className="pagination-summary">
          Showing {nodes.length === 0 ? 0 : pageStart + 1}-{Math.min(pageStart + PAGE_SIZE, sortedNodes.length)} of{" "}
          {sortedNodes.length}
        </div>
      </div>

      <div className="table-shell">
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
            {visibleNodes.map((node) => (
              <tr key={node.uuid}>
                <td className="node-name">{node.name}</td>
                <td>{node.type}</td>
                <td>
                  <StatusBadge status={node.status} />
                </td>
                <td>
                  <div className="battery-cell">
                    <div className="battery-bar-container">
                      <div
                        className={`battery-bar ${batteryColor(node.batteryLevel)}`}
                        style={{ width: `${Math.min(100, Math.max(0, node.batteryLevel))}%` }}
                      />
                      <span className="battery-text">{node.batteryLevel.toFixed(0)}%</span>
                    </div>
                  </div>
                </td>
                <td className="timestamp">{formatDateTime(node.lastSeen)}</td>
                <td className="uuid">{node.uuid}</td>
              </tr>
            ))}
            {nodes.length === 0 && (
              <tr>
                <td colSpan={6} className="empty-row">
                  No nodes registered
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <div className="pagination-controls" aria-label="Node list pagination">
        <button type="button" onClick={() => setPage(1)} disabled={currentPage === 1}>
          First
        </button>
        <button type="button" onClick={() => setPage((value) => Math.max(1, value - 1))} disabled={currentPage === 1}>
          Previous
        </button>
        <span className="pagination-page">
          Page {currentPage} of {pageCount}
        </span>
        <button
          type="button"
          onClick={() => setPage((value) => Math.min(pageCount, value + 1))}
          disabled={currentPage === pageCount}
        >
          Next
        </button>
        <button type="button" onClick={() => setPage(pageCount)} disabled={currentPage === pageCount}>
          Last
        </button>
      </div>
    </div>
  );
}

function batteryColor(level: number): string {
  if (level > 50) return "battery-green";
  if (level > 20) return "battery-amber";
  return "battery-red";
}
