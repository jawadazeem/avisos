import { TerminalWidget } from "../components/terminal/TerminalWidget";
import "./CliPage.css";

const quickCommands = ["help", "health", "nodes", "alarms", "stats", "about"];

export function CliPage() {
  return (
    <div className="cli-page">
      <div className="cli-toolbar">
        <span className="toolbar-label">Quick Commands:</span>
        {quickCommands.map((cmd) => (
          <button
            key={cmd}
            className="quick-cmd-btn"
            title={`Run '${cmd}'`}
            onClick={() => {
              // Dispatch a custom event for the terminal to pick up
              window.dispatchEvent(
                new CustomEvent("cli-quick-command", { detail: cmd }),
              );
            }}
          >
            {cmd}
          </button>
        ))}
      </div>
      <div className="cli-terminal-wrapper">
        <TerminalWidget />
      </div>
    </div>
  );
}
