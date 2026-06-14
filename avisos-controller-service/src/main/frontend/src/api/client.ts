import type {
  NodeRecord,
  AlarmRecord,
  SystemHealthReport,
  SystemStats,
  SystemAbout,
} from "../types/models";

const BASE = "";

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
  getAlarmImageUrl: (alarmId: string) => `${BASE}/api/alarms/${encodeURIComponent(alarmId)}/image`,
};
