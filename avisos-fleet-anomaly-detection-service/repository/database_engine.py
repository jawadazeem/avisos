from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

# Database A (e.g., Fleet Tracking DB on port 5432)
fleet_engine = create_engine("postgresql+psycopg2://user:pass@localhost:5432/fleet_db")
FleetSession = sessionmaker(bind=fleet_engine)