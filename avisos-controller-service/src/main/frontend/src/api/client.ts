import type {
  NodeRecord,
  AlarmRecord,
  SystemHealthReport,
  SystemStats,
  SystemAbout,
} from "../types/models";

const BASE = "";
const S3_PUBLIC_BASE =
  import.meta.env.VITE_AVISOS_S3_PUBLIC_BASE_URL ?? "http://localhost:4567/avisos-flagged-images";

async function fetchJson<T>(url: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE}${url}`, init);
  if (!res.ok) {
    const body = await res.text();
    throw new Error(`${res.status}: ${body}`);
  }
  return res.json();
}

export const api = {
  getNodes: () => fetchJson<NodeRecord[]>("/api/nodes"),
  getNode: (id: string) => fetchJson<NodeRecord>(`/api/nodes/${id}`),

  getAlarms: () => fetchJson<AlarmRecord[]>("/api/alarms"),
  resolveAlarm: (id: string) =>
    fetchJson<void>(`/api/alarms/${id}/resolve`, { method: "POST" }),

  getHealth: () => fetchJson<SystemHealthReport>("/api/health"),

  getStats: () => fetchJson<SystemStats>("/api/system/stats"),
  getAbout: () => fetchJson<SystemAbout>("/api/system/about"),
  getAlarmImageUrl: (s3ImageKey: string) => buildS3ObjectUrl(s3ImageKey),
};

function buildS3ObjectUrl(s3ImageKey: string): string {
  const base = S3_PUBLIC_BASE.replace(/\/+$/, "");
  const encodedKey = s3ImageKey
    .split("/")
    .map((segment) => encodeURIComponent(segment))
    .join("/");

  return `${base}/${encodedKey}`;
}
