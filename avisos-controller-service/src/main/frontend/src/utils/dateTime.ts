import type { ApiDateTime } from "../types/models";

export function toDate(value: ApiDateTime | null | undefined): Date | null {
  if (!value) return null;

  if (Array.isArray(value)) {
    const [year, month, day, hour = 0, minute = 0, second = 0, nano = 0] = value;
    if (!year || !month || !day) return null;

    const millis = Math.floor(nano / 1_000_000);
    const date = new Date(year, month - 1, day, hour, minute, second, millis);
    return Number.isNaN(date.getTime()) ? null : date;
  }

  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : date;
}

export function dateTimeMillis(value: ApiDateTime | null | undefined): number {
  return toDate(value)?.getTime() ?? 0;
}

export function formatDateTime(value: ApiDateTime | null | undefined): string {
  const date = toDate(value);
  if (!date) return "-";

  return date.toLocaleString("en-US", { hour12: false });
}
