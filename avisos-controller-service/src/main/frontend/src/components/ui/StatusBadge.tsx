import "./StatusBadge.css";

interface StatusBadgeProps {
  status: string;
  variant?: "green" | "amber" | "red" | "muted";
}

const autoVariant: Record<string, StatusBadgeProps["variant"]> = {
  RESPONSIVE: "green",
  HEALTHY: "green",
  ACTIVE: "red",
  CRITICAL: "red",
  UNHEALTHY: "red",
  WARNING: "amber",
  DEGRADED: "amber",
  UNRESPONSIVE: "amber",
  OFFLINE: "muted",
  RESOLVED: "muted",
  NONE: "muted",
};

export function StatusBadge({ status, variant }: StatusBadgeProps) {
  const v = variant ?? autoVariant[status] ?? "muted";
  return <span className={`status-badge badge-${v}`}>{status}</span>;
}
