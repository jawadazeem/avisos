import { useNavigate } from "react-router-dom";
import "./AboutPage.css";

const techStack = [
  { label: "Backend", value: "Spring Boot 3.4 · Java 25 · Virtual Threads" },
  { label: "Frontend", value: "React 19 · TypeScript · Vite" },
  { label: "Database", value: "SQLite · JDBI SQL Objects" },
  { label: "Messaging", value: "MQTT (Eclipse Mosquitto) · Protobuf" },
  { label: "Real-Time", value: "STOMP WebSocket · SockJS" },
  { label: "Vision AI", value: "CodeProject.AI · Object Detection" },
  { label: "Hardware Sim", value: "C++17 · Boost · cpp-httplib · spdlog" },
  { label: "Infrastructure", value: "Docker Compose · LocalStack (AWS)" },
];

export function AboutPage() {
  const navigate = useNavigate();

  return (
    <div className="about-page">
      <button className="about-back-btn" onClick={() => navigate(-1)}>
        &larr; Back
      </button>

      <div className="about-header">
        <div className="about-title">AVISOS</div>
        <div className="about-tagline">
          Advanced Visual Infrastructure Secure Operational Systems
        </div>
      </div>

      <div className="about-section">
        <h2>What Is Avisos</h2>
        <p>
          Avisos is a SCADA orchestration platform that combines computer vision
          AI with real-time sensor telemetry for predictive threat modeling.
          It monitors distributed IoT sensor nodes, processes camera feeds
          through object detection pipelines, and raises alarms when
          threats are identified.
        </p>
      </div>

      <div className="about-section">
        <h2>How It Works</h2>
        <p>
          Lightweight node services deployed at the edge collect hardware
          telemetry (battery, temperature, pressure, humidity, signal quality,
          leak detection) and stream it to the central controller over MQTT
          using Protobuf-encoded messages.
        </p>
        <p>
          Camera frames from each node are submitted to a CodeProject.AI
          vision server for object detection. When matched labels exceed
          confidence thresholds, the system generates alarms with severity
          classification and notifies operators through the dashboard,
          email, and SMS channels.
        </p>
        <p>
          This web dashboard provides real-time visibility into all nodes,
          alarms, and system health through live WebSocket feeds. An embedded
          CLI terminal gives operators direct command-line access to the
          platform without leaving the browser.
        </p>
      </div>

      <div className="about-section">
        <h2>Architecture</h2>
        <p>
          The platform is a multi-module Maven monorepo with two primary
          services: a Spring Boot controller service (central orchestration,
          REST API, dashboard) and a lightweight Java node service (edge
          telemetry, custom IoC framework for minimal footprint). A C++
          hardware simulator generates realistic sensor data for testing
          at scale.
        </p>
      </div>

      <div className="about-section">
        <h2>Technology Stack</h2>
        <div className="tech-grid">
          {techStack.map((tech) => (
            <div className="tech-card" key={tech.label}>
              <div className="tech-card-label">{tech.label}</div>
              <div className="tech-card-value">{tech.value}</div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
