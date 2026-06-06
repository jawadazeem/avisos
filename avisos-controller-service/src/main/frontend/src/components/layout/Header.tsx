import { useEffect, useState } from "react";
import { useWebSocket } from "../../context/WebSocketContext";
import "./Header.css";

export function Header() {
  const { connected } = useWebSocket();
  const [clock, setClock] = useState(formatClock());

  useEffect(() => {
    const interval = setInterval(() => setClock(formatClock()), 1000);
    return () => clearInterval(interval);
  }, []);

  return (
    <header className="header">
      <div className="header-left">
        <span className="header-label">SCADA MONITORING</span>
      </div>
      <div className="header-right">
        <span
          className={`connection-indicator ${connected ? "online" : "offline"}`}
        >
          <span className="indicator-dot" />
          {connected ? "CONNECTED" : "DISCONNECTED"}
        </span>
        <span className="header-clock">{clock}</span>
      </div>
    </header>
  );
}

function formatClock(): string {
  return new Date().toLocaleTimeString("en-US", { hour12: false });
}
