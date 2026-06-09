import { useNavigate } from "react-router-dom";
import "./Footer.css";

export function Footer() {
  const navigate = useNavigate();

  return (
    <footer className="app-footer">
      <span>Avisos</span>
      <span className="footer-dot">&middot;</span>
      <span>&copy; 2026 Jawad Azeem</span>
      <span className="footer-dot">&middot;</span>
      <button
        className="footer-info-btn"
        title="About Avisos"
        onClick={() => navigate("/about")}
      >
        i
      </button>
    </footer>
  );
}
