from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel, Field
from model.models import FleetMetricsDTO, FleetMetricRecordDTO, FleetAnomalyReport
from service import AnomalyDetectionService
from datetime import datetime

fleet_router = APIRouter()

@fleet_router.post("/fleet-health", status_code=status.HTTP_201_CREATED)
def ingest_fleet_health_snapshot(payload: FleetMetricRecord, service: AnomalyDetectionService = Depends()) -> FleetAnomalyReport:
    try:
        # Pass raw strings down to business logic
        result = service.detect_anomalies(payload.timestamp, payload.fleet_metrics)
        return result
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
