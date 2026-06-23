from data.repository import FleetAnomalyReportRepository
from controller.fleet import FleetMetrics
from model.models import FleetAnomalyReport
from datetime import datetime

class AnomalyDetectionService:
    def __init__(self, repository: FleetAnomalyReportRepository):
        self.repository = repository

    def detect_anomalies(self, timestamp: datetime, fleet_metrics: FleetMetrics) -> FleetAnomalyReport:
        # TODO: Use the model to detect whether or not anomalies exist
        fleet_anomaly_report = None
        
        self.repository.save(fleet_anomaly_report)
        return fleet_anomaly_report