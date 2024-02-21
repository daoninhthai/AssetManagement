"""Statistical anomaly detection service for inventory movements."""

import logging
from collections import defaultdict
from typing import List

import numpy as np
import pandas as pd

from config.settings import settings
from models.schemas import (
    AnomalyDetail,
    AnomalyRequest,
    AnomalyResponse,
)
from utils.data_processor import fill_missing_dates

logger = logging.getLogger(__name__)

ZSCORE_THRESHOLD = settings.ANOMALY_ZSCORE_THRESHOLD
IQR_MULTIPLIER = 1.5


def detect_anomalies(request: AnomalyRequest) -> AnomalyResponse:
    """Detect anomalies across all products in the supplied movement data.

    Two complementary statistical methods are used:
    1. Z-score — flags values far from the product's historical mean.
    2. IQR (inter-quartile range) — flags values outside the typical box-plot
       whiskers.

    Each flagged data point is classified as a *spike*, *drop*, or
    *pattern_break* and assigned a severity score.
    """
    if not request.movements:
        return AnomalyResponse(anomalies=[], total_checked=0, anomaly_count=0)

    # ── 1. Group movements by product ────────────────────────────────────
    product_groups: dict = defaultdict(list)
    product_names: dict = {}
    for m in request.movements:
        product_groups[m.product_id].append(
            {"date": m.date, "quantity": m.quantity, "movement_type": m.movement_type}
        )
        product_names[m.product_id] = m.product_name

    anomalies: List[AnomalyDetail] = []
    total_checked = 0

    for product_id, records in product_groups.items():
        product_name = product_names[product_id]
        df = pd.DataFrame(records)
        df["date"] = pd.to_datetime(df["date"])
        df = df.sort_values("date").reset_index(drop=True)

        # Aggregate quantities per date (sum in case of multiple movements)
        daily = df.groupby("date")["quantity"].sum().reset_index()
        daily.columns = ["date", "quantity"]
        daily = fill_missing_dates(daily, date_col="date", fill_value=0)
        daily = daily.sort_values("date").reset_index(drop=True)

        total_checked += len(daily)

        if len(daily) < 3:
            continue  # not enough data for statistical tests

        quantities = daily["quantity"].values.astype(float)
        mean_val = float(np.mean(quantities))
        std_val = float(np.std(quantities, ddof=1)) if len(quantities) > 1 else 1.0
        if std_val == 0:
            std_val = 1.0  # prevent division by zero

        q1 = float(np.percentile(quantities, 25))
        q3 = float(np.percentile(quantities, 75))
        iqr = q3 - q1

        lower_iqr = q1 - IQR_MULTIPLIER * iqr
        upper_iqr = q3 + IQR_MULTIPLIER * iqr

        for idx, row in daily.iterrows():
            value = float(row["quantity"])
            z = (value - mean_val) / std_val

            is_zscore_outlier = abs(z) > ZSCORE_THRESHOLD
            is_iqr_outlier = value < lower_iqr or value > upper_iqr

            if not is_zscore_outlier and not is_iqr_outlier:
                continue

            # ── Classify anomaly type ────────────────────────────────
            if is_zscore_outlier and z > 0:
                anomaly_type = "spike"
            elif is_zscore_outlier and z < 0:
                anomaly_type = "drop"
            else:
                anomaly_type = "pattern_break"

            # ── Severity based on |z| ────────────────────────────────
            abs_z = abs(z)
            if abs_z >= 3.5:
                severity = "high"
            elif abs_z >= ZSCORE_THRESHOLD:
                severity = "medium"
            else:
                severity = "low"

            # Normalise score to 0-1 range (cap at z=5 for scaling)
            score = round(min(abs_z / 5.0, 1.0), 3)

            description = _describe_anomaly(
                anomaly_type=anomaly_type,
                product_name=product_name,
                value=value,
                mean_val=mean_val,
            )

            anomalies.append(
                AnomalyDetail(
                    product_id=product_id,
                    product_name=product_name,
                    anomaly_type=anomaly_type,
                    description=description,
                    score=score,
                    severity=severity,
                    detected_at=pd.Timestamp(row["date"]).strftime("%Y-%m-%d"),
                    expected_value=round(mean_val, 2),
                    actual_value=value,
                )
            )

    # ── Sort by score descending ─────────────────────────────────────────
    anomalies.sort(key=lambda a: a.score, reverse=True)

    return AnomalyResponse(
        anomalies=anomalies,
        total_checked=total_checked,
        anomaly_count=len(anomalies),
    )


def _describe_anomaly(
    anomaly_type: str, product_name: str, value: float, mean_val: float
) -> str:
    """Generate a human-readable Vietnamese description for an anomaly."""
    if anomaly_type == "spike":
        return (
            f"Phát hiện đột biến tăng cho '{product_name}': "
            f"số lượng {value:.0f} cao hơn nhiều so với trung bình {mean_val:.1f}."
        )
    if anomaly_type == "drop":
        return (
            f"Phát hiện sụt giảm bất thường cho '{product_name}': "
            f"số lượng {value:.0f} thấp hơn nhiều so với trung bình {mean_val:.1f}."
        )
    return (
        f"Phát hiện mẫu bất thường cho '{product_name}': "
        f"số lượng {value:.0f} nằm ngoài phạm vi thông thường (trung bình {mean_val:.1f})."
    )
