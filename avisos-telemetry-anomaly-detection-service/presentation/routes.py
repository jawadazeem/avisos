from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel
from business.services import UserService

router = APIRouter()

# Schema for incoming request data validation
class UserRegisterRequest(BaseModel):
    username: str
    email: str

@router.post("/telemetry", status_code=status.HTTP_201_CREATED)
def create_user(payload: UserRegisterRequest, service: UserService = Depends()):
    try:
        # Pass raw strings down to business logic
        result = service.register_user(payload.username, payload.email)
        return result
    except ValueError as e:
        # Catch business rule failures and output proper HTTP statuses
        raise HTTPException(status_code=400, detail=str(e))
