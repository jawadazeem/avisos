import { NavLink } from "react-router-dom";
import "./Sidebar.css";

const navItems = [
  { path: "/", label: "Dashboard", icon: "grid" },
  { path: "/nodes", label: "Nodes", icon: "cpu" },
  { path: "/alarms", label: "Alarms", icon: "alert" },
  { path: "/cli", label: "Terminal", icon: "terminal" },
];

const icons: Record<string, string> = {
  grid: "\u25A6",
  cpu: "\u2338",
  alert: "\u26A0",
  terminal: ">_",
};

export function Sidebar() {
  return (
    <nav className="sidebar">
      <div className="sidebar-brand">
        <span className="brand-icon">{"\u25C8"}</span>
        <span className="brand-text">AVISOS</span>
      </div>
      <div className="sidebar-subtitle">Command Center</div>
      <ul className="sidebar-nav">
        {navItems.map((item) => (
          <li key={item.path}>
            <NavLink
              to={item.path}
              className={({ isActive }) =>
                `sidebar-link ${isActive ? "active" : ""}`
              }
              end={item.path === "/"}
            >
              <span className="nav-icon">{icons[item.icon]}</span>
              <span>{item.label}</span>
            </NavLink>
          </li>
        ))}
      </ul>
      <div className="sidebar-footer">v1.0-SNAPSHOT</div>
    </nav>
  );
}
