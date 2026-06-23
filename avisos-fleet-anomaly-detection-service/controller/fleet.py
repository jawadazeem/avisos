from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel, Field
from service import AnomalyDetectionService
from datetime import datetime

fleet_router = APIRouter()

class FleetMetrics(BaseModel):
    total_nodes_evaluated: int = Field(alias="totalNodesEvaluated")
    responsive_ratio: float = Field(alias="responsiveRatio")
    battery_above_50_ratio: float = Field(alias="batteryAbove50Ratio")
    avg_seconds_since_last_seen: float = Field(
        alias="avgSecondsSinceLastSeen"
    )

    model_config = {
        "populate_by_name": True
    }

class FleetMetricRecord(BaseModel):
    # Deserializes Java's Unix epoch (Instant datatype)
    timestamp: datetime
    fleet_metrics: FleetMetrics

    

@fleet_router.post("/fleet-health", status_code=status.HTTP_201_CREATED)
def ingest_fleet_health_snapshot(payload: FleetMetricRecord, service: AnomalyDetectionService = Depends()):
    try:
        # Pass raw strings down to business logic
        result = service.detect_anomalies(payload.timestamp, payload.fleet_metrics)
        return result
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
