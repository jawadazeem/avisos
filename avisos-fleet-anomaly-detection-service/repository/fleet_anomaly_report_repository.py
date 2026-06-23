from model.models import FleetAnomalyReport
from sqlalchemy.orm import Session
from sqlalchemy import select, insert

class FleetAnomalyReportRepository:
    def __init__(self, db: Session):
        self.db = db
    
    def get_by_id(self, id: int) -> FleetAnomalyReport | None:
        return self.db.get(FleetAnomalyReport, id)
    
    def get_latest(self) -> FleetAnomalyReport | None:
        stmt = select(FleetAnomalyReport).order_by(FleetAnomalyReport.created_at.desc())
        return self.db.scalars(stmt).first()
    
    def save(self, fleet_anomaly_report: FleetAnomalyReport):
        stmt = insert(fleet_anomaly_report)