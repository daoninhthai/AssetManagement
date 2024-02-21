from typing import List, Optional
from pydantic import BaseModel, Field


# ──────────────────────────────────────────────
# Shared / nested models
# ──────────────────────────────────────────────

class DailyMovement(BaseModel):
    """A single day's inventory movement (in and out)."""
    date: str = Field(..., description="ISO date string, e.g. 2024-01-15")
    quantity_in: int = Field(..., ge=0)
    quantity_out: int = Field(..., ge=0)


class MovementData(BaseModel):
    """A single inventory movement record."""
    product_id: int
    product_name: str
    date: str = Field(..., description="ISO date string, e.g. 2024-01-15")
    quantity: int
    movement_type: str = Field(..., description="IN or OUT")


class InventoryContext(BaseModel):
    """Contextual inventory data supplied alongside a natural-language query."""
    total_products: int = 0
    total_warehouses: int = 0
    low_stock_products: List[dict] = Field(default_factory=list)
    recent_movements: List[dict] = Field(default_factory=list)
    top_categories: List[dict] = Field(default_factory=list)


# ──────────────────────────────────────────────
# Request models
# ──────────────────────────────────────────────

class ForecastRequest(BaseModel):
    """Request body for demand forecasting."""
    product_id: int
    product_name: str
    historical_data: List[DailyMovement]
    forecast_days: int = Field(default=30, ge=1, le=365)


class ReorderRequest(BaseModel):
    """Request body for reorder-point / EOQ calculation."""
    product_id: int
    product_name: str
    current_stock: int = Field(..., ge=0)
    avg_daily_usage: float = Field(..., ge=0)
    lead_time_days: int = Field(..., ge=1)
    unit_cost: float = Field(..., gt=0)
    ordering_cost: float = Field(default=50.0, gt=0)
    holding_cost_rate: float = Field(default=0.2, gt=0, le=1)
    service_level: float = Field(default=0.95, gt=0, lt=1)


class AnomalyRequest(BaseModel):
    """Request body for anomaly detection."""
    movements: List[MovementData]


class QueryRequest(BaseModel):
    """Request body for natural-language inventory queries."""
    question: str
    language: str = Field(default="vi", description="Response language code")
    inventory_context: Optional[InventoryContext] = None


# ──────────────────────────────────────────────
# Response models
# ──────────────────────────────────────────────

class DayPrediction(BaseModel):
    """A single day's demand prediction."""
    date: str
    predicted_demand: float
    lower_bound: float
    upper_bound: float


class ForecastResponse(BaseModel):
    """Response for demand forecasting."""
    product_id: int
    product_name: str
    predictions: List[DayPrediction]
    confidence: float = Field(..., ge=0, le=1)
    trend: str = Field(..., description="increasing, decreasing, or stable")
    recommendation: str


class ReorderResponse(BaseModel):
    """Response for reorder-point calculation."""
    product_id: int
    product_name: str
    reorder_point: int
    reorder_quantity: int
    safety_stock: int
    estimated_cost: float
    days_until_stockout: int
    urgency: str = Field(..., description="critical, high, medium, or low")
    recommendation: str


class AnomalyDetail(BaseModel):
    """Details about a single detected anomaly."""
    product_id: int
    product_name: str
    anomaly_type: str = Field(..., description="spike, drop, or pattern_break")
    description: str
    score: float = Field(..., ge=0, le=1)
    severity: str = Field(..., description="high, medium, or low")
    detected_at: str
    expected_value: float
    actual_value: float


class AnomalyResponse(BaseModel):
    """Response for anomaly detection."""
    anomalies: List[AnomalyDetail]
    total_checked: int
    anomaly_count: int


class QueryResponse(BaseModel):
    """Response for natural-language queries."""
    question: str
    answer: str
    confidence: float = Field(..., ge=0, le=1)
    sources: List[str]
    language: str
