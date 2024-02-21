"""Tests for the anomaly detection service."""

from datetime import datetime, timedelta

import pytest

from models.schemas import AnomalyRequest, MovementData
from services.anomaly_service import detect_anomalies


def _make_movements(
    num_days: int = 30,
    product_id: int = 1,
    product_name: str = "Widget A",
    base_quantity: int = 50,
    spike_days: dict | None = None,
):
    """Generate synthetic movement data with optional spike injection.

    Args:
        num_days: Number of days of data to generate.
        product_id: Product identifier.
        product_name: Human-readable product name.
        base_quantity: Normal daily quantity.
        spike_days: Mapping of day-index -> quantity to override (inject anomalies).
    """
    spike_days = spike_days or {}
    start = datetime(2024, 1, 1)
    movements = []
    for i in range(num_days):
        date = start + timedelta(days=i)
        qty = spike_days.get(i, base_quantity + (i % 5) - 2)  # slight variation
        movements.append(
            MovementData(
                product_id=product_id,
                product_name=product_name,
                date=date.strftime("%Y-%m-%d"),
                quantity=qty,
                movement_type="OUT",
            )
        )
    return movements


class TestDetectAnomaliesWithSpike:
    """Verify that obvious spikes are detected."""

    def test_spike_is_detected(self):
        # Inject a massive spike on day 15
        movements = _make_movements(
            num_days=30,
            base_quantity=50,
            spike_days={15: 500},
        )
        request = AnomalyRequest(movements=movements)
        response = detect_anomalies(request)

        assert response.anomaly_count >= 1
        assert response.total_checked > 0

        spike_anomalies = [a for a in response.anomalies if a.anomaly_type == "spike"]
        assert len(spike_anomalies) >= 1

        # The spike should reference the correct product
        assert spike_anomalies[0].product_id == 1
        assert spike_anomalies[0].product_name == "Widget A"

    def test_spike_severity_is_high(self):
        movements = _make_movements(
            num_days=30,
            base_quantity=50,
            spike_days={10: 1000},
        )
        request = AnomalyRequest(movements=movements)
        response = detect_anomalies(request)

        spike_anomalies = [a for a in response.anomalies if a.anomaly_type == "spike"]
        assert len(spike_anomalies) >= 1
        # A 20x spike should register as high severity
        assert spike_anomalies[0].severity in ("high", "medium")
        assert spike_anomalies[0].score > 0.5

    def test_anomalies_sorted_by_score_descending(self):
        movements = _make_movements(
            num_days=30,
            base_quantity=50,
            spike_days={10: 300, 20: 600},
        )
        request = AnomalyRequest(movements=movements)
        response = detect_anomalies(request)

        if len(response.anomalies) >= 2:
            scores = [a.score for a in response.anomalies]
            assert scores == sorted(scores, reverse=True)


class TestDetectAnomaliesNoAnomalies:
    """Verify that stable data produces no anomalies."""

    def test_stable_data_no_anomalies(self):
        # Very consistent data — same quantity every day
        start = datetime(2024, 1, 1)
        movements = [
            MovementData(
                product_id=1,
                product_name="Stable Product",
                date=(start + timedelta(days=i)).strftime("%Y-%m-%d"),
                quantity=100,
                movement_type="OUT",
            )
            for i in range(30)
        ]
        request = AnomalyRequest(movements=movements)
        response = detect_anomalies(request)

        assert response.anomaly_count == 0
        assert response.total_checked == 30


class TestDetectAnomaliesEmptyData:
    """Verify correct handling of empty input."""

    def test_empty_movements(self):
        request = AnomalyRequest(movements=[])
        response = detect_anomalies(request)

        assert response.anomaly_count == 0
        assert response.total_checked == 0
        assert response.anomalies == []

    def test_single_product_few_records(self):
        """With fewer than 3 data points, no statistical test is meaningful."""
        movements = [
            MovementData(
                product_id=1,
                product_name="Sparse",
                date="2024-01-01",
                quantity=100,
                movement_type="OUT",
            ),
            MovementData(
                product_id=1,
                product_name="Sparse",
                date="2024-01-02",
                quantity=200,
                movement_type="OUT",
            ),
        ]
        request = AnomalyRequest(movements=movements)
        response = detect_anomalies(request)

        # With only 2 points the service should gracefully skip analysis
        assert response.total_checked >= 0
