from data.repository import UserRepository

class AnomalyDetectionService:
    def __init__(self, repository: UserRepository):
        self.repository = repository

    def detect_anomalies(self, username: str, email: str) -> dict:
        # Enforce business rules
        if len(username) < 3:
            raise ValueError("Username must be at least 3 characters long.")
            
        existing_user = self.repository.get_by_username(username)
        if existing_user:
            raise ValueError("Username is already taken.")
            
        # Trigger persistence if rules pass
        user = self.repository.save(username, email)
        return {"id": user.id, "username": user.username, "status": "ACTIVE"}