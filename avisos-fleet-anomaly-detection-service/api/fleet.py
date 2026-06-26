from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel, Field
from model.models import FleetMetricRecord, FleetAnomalyReport
from service.anomaly_detection_service import AnomalyDetectionService
from dependencies import get_anomaly_service
from datetime import datetime

fleet_router = APIRouter()

@fleet_router.post("/fleet-health", status_code=status.HTTP_201_CREATED)
def ingest(
    payload: FleetMetricRecord,
    service: AnomalyDetectionService = Depends(get_anomaly_service)
) -> FleetAnomalyReport:
    try:
        result = service.detect_anomalies(payload)
        return result
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
