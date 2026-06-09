import { Routes, Route } from "react-router-dom";
import { Sidebar } from "./components/layout/Sidebar";
import { Header } from "./components/layout/Header";
import { Footer } from "./components/layout/Footer";
import { DashboardPage } from "./pages/DashboardPage";
import { NodesPage } from "./pages/NodesPage";
import { AlarmsPage } from "./pages/AlarmsPage";
import { CliPage } from "./pages/CliPage";
import { AboutPage } from "./pages/AboutPage";

export function App() {
  return (
    <div className="app-layout">
      <Header />
      <Sidebar />
      <main className="app-content">
        <Routes>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/nodes" element={<NodesPage />} />
          <Route path="/alarms" element={<AlarmsPage />} />
          <Route path="/cli" element={<CliPage />} />
          <Route path="/about" element={<AboutPage />} />
        </Routes>
      </main>
      <Footer />
    </div>
  );
}
