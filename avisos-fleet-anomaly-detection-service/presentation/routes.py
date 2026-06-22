from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel, Field
from service import AnomalyDetectionService
import time

router = APIRouter()

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
    timestamp: time
    fleet_metrics: FleetMetrics

    

@router.post("/fleet-health", status_code=status.HTTP_201_CREATED)
def ingest_fleet_health_snapshot(payload: FleetMetricRecord, service: AnomalyDetectionService = Depends()):
    try:
        # Pass raw strings down to business logic
        result = service.detect_anomalies(payload.timestamp, payload.email)
        return result
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
