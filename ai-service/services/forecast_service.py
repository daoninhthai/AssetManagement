"""Demand forecasting service using scikit-learn linear regression."""

import logging
from datetime import datetime, timedelta
from typing import List

import numpy as np
import pandas as pd
from sklearn.linear_model import LinearRegression

from models.schemas import (
    DayPrediction,
    ForecastRequest,
    ForecastResponse,
)
from utils.data_processor import fill_missing_dates, calculate_moving_average

logger = logging.getLogger(__name__)

MIN_DATA_POINTS = 7


def predict_demand(request: ForecastRequest) -> ForecastResponse:
    """Generate demand predictions for the requested product.

    Uses linear regression trained on engineered time-series features derived
    from the supplied historical data.  Falls back to simple average-based
    predictions when there is insufficient data (fewer than 7 days).
    """
    forecast_days = request.forecast_days
    product_id = request.product_id
    product_name = request.product_name

    # ── 1. Convert historical data to DataFrame ──────────────────────────
    records = []
    for entry in request.historical_data:
        records.append(
            {
                "date": entry.date,
                "quantity_in": entry.quantity_in,
                "quantity_out": entry.quantity_out,
            }
        )

    if not records:
        return _default_response(product_id, product_name, forecast_days)

    df = pd.DataFrame(records)
    df["date"] = pd.to_datetime(df["date"])
    df = df.sort_values("date").reset_index(drop=True)

    # Fill gaps in the date range
    df = fill_missing_dates(df, date_col="date", fill_value=0)

    # ── 2. Calculate daily net demand ────────────────────────────────────
    df["demand"] = df["quantity_out"].clip(lower=0)

    if df["demand"].sum() == 0:
        return _default_response(product_id, product_name, forecast_days)

    if len(df) < MIN_DATA_POINTS:
        return _simple_average_response(df, product_id, product_name, forecast_days)

    # ── 3. Feature engineering ───────────────────────────────────────────
    df["day_of_week"] = df["date"].dt.dayofweek
    df["day_of_month"] = df["date"].dt.day
    df["is_weekend"] = (df["day_of_week"] >= 5).astype(int)
    df["rolling_mean_7"] = calculate_moving_average(df["demand"], window=7)
    df["rolling_mean_14"] = calculate_moving_average(df["demand"], window=14)
    df["trend"] = np.arange(len(df))

    feature_cols = [
        "day_of_week",
        "day_of_month",
        "is_weekend",
        "rolling_mean_7",
        "rolling_mean_14",
        "trend",
    ]

    X_train = df[feature_cols].values
    y_train = df["demand"].values

    # ── 4. Train model ───────────────────────────────────────────────────
    model = LinearRegression()
    model.fit(X_train, y_train)

    # Training residuals for confidence intervals
    y_train_pred = model.predict(X_train)
    residual_std = float(np.std(y_train - y_train_pred)) if len(y_train) > 1 else 0.0
    r_squared = float(model.score(X_train, y_train))
    confidence = max(0.0, min(1.0, r_squared))

    # ── 5. Generate future features and predict ──────────────────────────
    last_date = df["date"].max()
    last_rolling_7 = float(df["rolling_mean_7"].iloc[-1])
    last_rolling_14 = float(df["rolling_mean_14"].iloc[-1])
    last_trend = int(df["trend"].iloc[-1])

    predictions: List[DayPrediction] = []
    for i in range(1, forecast_days + 1):
        future_date = last_date + timedelta(days=i)
        dow = future_date.dayofweek
        dom = future_date.day
        is_wknd = 1 if dow >= 5 else 0
        trend_val = last_trend + i

        features = np.array(
            [[dow, dom, is_wknd, last_rolling_7, last_rolling_14, trend_val]]
        )
        pred = float(model.predict(features)[0])
        pred = max(0.0, pred)

        lower = max(0.0, pred - 1.96 * residual_std)
        upper = pred + 1.96 * residual_std

        predictions.append(
            DayPrediction(
                date=future_date.strftime("%Y-%m-%d"),
                predicted_demand=round(pred, 2),
                lower_bound=round(lower, 2),
                upper_bound=round(upper, 2),
            )
        )

        # Update rolling averages incrementally (simple exponential blend)
        last_rolling_7 = last_rolling_7 * 0.85 + pred * 0.15
        last_rolling_14 = last_rolling_14 * 0.93 + pred * 0.07

    # ── 6. Determine trend ───────────────────────────────────────────────
    trend_coeff = float(model.coef_[feature_cols.index("trend")])
    if trend_coeff > 0.05:
        trend_label = "increasing"
    elif trend_coeff < -0.05:
        trend_label = "decreasing"
    else:
        trend_label = "stable"

    # ── 7. Recommendation ────────────────────────────────────────────────
    recommendation = _build_recommendation(trend_label)

    return ForecastResponse(
        product_id=product_id,
        product_name=product_name,
        predictions=predictions,
        confidence=round(confidence, 3),
        trend=trend_label,
        recommendation=recommendation,
    )


# ─── Helper functions ────────────────────────────────────────────────────────


def _build_recommendation(trend: str) -> str:
    if trend == "increasing":
        return (
            "Nhu cầu đang tăng, nên tăng tồn kho dự phòng và "
            "xem xét đặt hàng sớm hơn để tránh thiếu hụt."
        )
    if trend == "decreasing":
        return (
            "Nhu cầu giảm, có thể giảm đặt hàng để tránh tồn kho quá mức "
            "và tiết kiệm chi phí lưu kho."
        )
    return (
        "Nhu cầu ổn định, giữ mức tồn kho hiện tại và tiếp tục "
        "theo dõi xu hướng."
    )


def _default_response(
    product_id: int, product_name: str, forecast_days: int
) -> ForecastResponse:
    """Return a zeroed-out response when no usable data is available."""
    base = datetime.utcnow()
    predictions = [
        DayPrediction(
            date=(base + timedelta(days=i)).strftime("%Y-%m-%d"),
            predicted_demand=0.0,
            lower_bound=0.0,
            upper_bound=0.0,
        )
        for i in range(1, forecast_days + 1)
    ]
    return ForecastResponse(
        product_id=product_id,
        product_name=product_name,
        predictions=predictions,
        confidence=0.0,
        trend="stable",
        recommendation="Không đủ dữ liệu lịch sử để dự báo. Cần ít nhất 7 ngày dữ liệu.",
    )


def _simple_average_response(
    df: pd.DataFrame, product_id: int, product_name: str, forecast_days: int
) -> ForecastResponse:
    """Use a plain average when there are fewer than MIN_DATA_POINTS days."""
    avg_demand = float(df["demand"].mean())
    std_demand = float(df["demand"].std()) if len(df) > 1 else avg_demand * 0.25
    last_date = df["date"].max()

    predictions = [
        DayPrediction(
            date=(last_date + timedelta(days=i)).strftime("%Y-%m-%d"),
            predicted_demand=round(avg_demand, 2),
            lower_bound=round(max(0.0, avg_demand - 1.96 * std_demand), 2),
            upper_bound=round(avg_demand + 1.96 * std_demand, 2),
        )
        for i in range(1, forecast_days + 1)
    ]

    return ForecastResponse(
        product_id=product_id,
        product_name=product_name,
        predictions=predictions,
        confidence=0.3,
        trend="stable",
        recommendation=(
            "Dữ liệu lịch sử hạn chế (ít hơn 7 ngày). "
            "Dự báo dựa trên trung bình đơn giản, độ tin cậy thấp."
        ),
    )
