from pydantic import BaseModel, Field
from datetime import datetime

# DTOs
class FleetMetrics(BaseModel):
    total_nodes_evaluated: int = Field(alias="totalNodesEvaluated")
    responsive_ratio: float = Field(alias="responsiveRatio")
    battery_above_50_ratio: float = Field(alias="batteryAbove50Ratio")
    avg_seconds_since_last_seen: float = Field(alias="avgSecondsSinceLastSeen")

    model_config = {"populate_by_name": True}

class FleetMetricRecord(BaseModel):
    timestamp: datetime
    fleet_metrics: FleetMetrics

class FleetAnomalyReport(BaseModel):
    id: int
    timestamp: datetime
    is_anomaly: bool
    diagnostic_reason: str | None
    raw_payload: FleetMetrics