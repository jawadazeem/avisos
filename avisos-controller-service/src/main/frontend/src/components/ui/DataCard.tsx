import type { ReactNode } from "react";
import "./DataCard.css";

interface DataCardProps {
  title: string;
  children: ReactNode;
  accent?: "green" | "amber" | "red" | "blue";
}

export function DataCard({ title, children, accent = "green" }: DataCardProps) {
  return (
    <div className={`data-card card-accent-${accent}`}>
      <div className="card-header">
        <span className="card-title">{title}</span>
        <span className="card-accent-bar" />
      </div>
      <div className="card-body">{children}</div>
    </div>
  );
}
