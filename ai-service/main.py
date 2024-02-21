"""Smart Inventory AI Service — FastAPI application entry point."""

import logging
import traceback

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from config.settings import settings
from models.schemas import (
    AnomalyRequest,
    AnomalyResponse,
    ForecastRequest,
    ForecastResponse,
    QueryRequest,
    QueryResponse,
    ReorderRequest,
    ReorderResponse,
)
from services import forecast_service, reorder_service, anomaly_service, query_service

# ── Logging ──────────────────────────────────────────────────────────────────
logging.basicConfig(
    level=getattr(logging, settings.LOG_LEVEL.upper(), logging.INFO),
    format="%(asctime)s | %(levelname)-8s | %(name)s | %(message)s",
)
logger = logging.getLogger(__name__)

# ── App ──────────────────────────────────────────────────────────────────────
app = FastAPI(
    title="Smart Inventory AI Service",
    description=(
        "AI/ML microservice providing demand forecasting, reorder-point "
        "optimization, anomaly detection, and natural-language inventory queries."
    ),
    version="1.0.0",
)

# ── CORS (allow all origins for development) ─────────────────────────────────
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ── Startup event ────────────────────────────────────────────────────────────
@app.on_event("startup")
async def on_startup() -> None:
    logger.info("Smart Inventory AI Service is starting up...")
    logger.info("AI Model       : %s", settings.AI_MODEL)
    logger.info("Log Level      : %s", settings.LOG_LEVEL)
    openai_status = "configured" if settings.OPENAI_API_KEY else "NOT configured (fallback mode)"
    logger.info("OpenAI API Key : %s", openai_status)
    logger.info("Service ready on port 8000")


# ── Global exception handler ────────────────────────────────────────────────
@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception) -> JSONResponse:
    logger.error(
        "Unhandled exception on %s %s: %s\n%s",
        request.method,
        request.url.path,
        exc,
        traceback.format_exc(),
    )
    return JSONResponse(
        status_code=500,
        content={
            "error": "Internal server error",
            "detail": str(exc),
            "path": str(request.url.path),
        },
    )


# ── Health check ─────────────────────────────────────────────────────────────
@app.get("/health")
async def health_check() -> dict:
    return {"status": "ok", "service": "ai-inventory", "version": "1.0.0"}


# ── Forecast ─────────────────────────────────────────────────────────────────
@app.post("/api/ai/forecast", response_model=ForecastResponse)
async def forecast(request: ForecastRequest) -> ForecastResponse:
    logger.info("Forecast request for product %s (%s)", request.product_id, request.product_name)
    return forecast_service.predict_demand(request)


# ── Reorder ──────────────────────────────────────────────────────────────────
@app.post("/api/ai/reorder", response_model=ReorderResponse)
async def reorder(request: ReorderRequest) -> ReorderResponse:
    logger.info("Reorder request for product %s (%s)", request.product_id, request.product_name)
    return reorder_service.calculate_reorder(request)


# ── Anomaly detection ────────────────────────────────────────────────────────
@app.post("/api/ai/anomaly", response_model=AnomalyResponse)
async def anomaly(request: AnomalyRequest) -> AnomalyResponse:
    logger.info("Anomaly detection request with %d movements", len(request.movements))
    return anomaly_service.detect_anomalies(request)


# ── Natural-language query ───────────────────────────────────────────────────
@app.post("/api/ai/query", response_model=QueryResponse)
async def query(request: QueryRequest) -> QueryResponse:
    logger.info("Query request: '%s' (lang=%s)", request.question[:80], request.language)
    return query_service.process_query(request)


# ── Main ─────────────────────────────────────────────────────────────────────
if __name__ == "__main__":
    import uvicorn

    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
