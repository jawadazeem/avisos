from fastapi import FastAPI
from api.fleet import fleet_router

app = FastAPI(
    title="Fleet Anomaly Detection Service",
    version="1.0.0"
)

app.include_router(fleet_router)