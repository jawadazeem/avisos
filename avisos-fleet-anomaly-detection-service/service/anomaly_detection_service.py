from repository.fleet_anomaly_report_repository import FleetAnomalyReportRepository
from model.models import FleetAnomalyReport, FleetMetrics
from datetime import datetime
import pandas as pd
from util.model_loader import MODEL
import uuid
import time
from datetime import datetime

class AnomalyDetectionService:
    def __init__(self, repository: FleetAnomalyReportRepository):
        self.repository = repository
        self.ml_model = MODEL

    def generate_id(self, bit_mask: int = 0xFFFFFF) -> int:
        """
        Generates a unique ID
        """
        full_uuid_int = uuid.uuid4().int
        truncated_uuid = full_uuid_int & bit_mask
        return (int(time.time()) << 24) | truncated_uuid

    def detect_anomalies(self, fleet_metrics: FleetMetrics) -> FleetAnomalyReport:
        df = pd.DataFrame([fleet_metrics.__dict__])

        detect = self.ml_model.predict(df)
        is_anomaly = bool(detect[0] < 0)

        # For now, there is no way to determine the reason of the anomaly.
        # This is something to add.
        fleet_anomaly_report = FleetAnomalyReport(
            id=self.generate_id(), 
            timestamp=datetime.now(),
            is_anomaly=is_anomaly,
            reason=None,
            record=fleet_metrics
        )

        self.repository.save(fleet_anomaly_report, datetime.now())
        print(fleet_anomaly_report)
        return fleet_anomaly_report