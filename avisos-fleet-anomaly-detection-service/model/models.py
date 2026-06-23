from datetime import datetime
from controller.fleet import FleetMetricRecord

# DTO for persitence
class FleetAnomalyReport():
    id: int
    timestamp: datetime
    is_anomaly: bool
    diagnostic_reason: str
    raw_payload: FleetMetricRecord