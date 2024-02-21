"""Tests for the demand forecasting service."""

from datetime import datetime, timedelta

import pytest

from models.schemas import DailyMovement, ForecastRequest
from services.forecast_service import predict_demand


def _make_historical_data(num_days: int, base_out: int = 10, base_in: int = 3):
    """Generate synthetic historical data for testing."""
    start = datetime(2024, 1, 1)
    data = []
    for i in range(num_days):
        date = start + timedelta(days=i)
        # Add slight variation to make it realistic
        qty_out = base_out + (i % 5)
        qty_in = base_in + (i % 3)
        data.append(
            DailyMovement(
                date=date.strftime("%Y-%m-%d"),
                quantity_in=qty_in,
                quantity_out=qty_out,
            )
        )
    return data


class TestPredictDemandBasic:
    """Test normal forecasting with adequate data (30 days)."""

    def test_returns_correct_product_info(self):
        request = ForecastRequest(
            product_id=1,
            product_name="Widget A",
            historical_data=_make_historical_data(30),
            forecast_days=7,
        )
        response = predict_demand(request)

        assert response.product_id == 1
        assert response.product_name == "Widget A"

    def test_returns_correct_number_of_predictions(self):
        request = ForecastRequest(
            product_id=1,
            product_name="Widget A",
            historical_data=_make_historical_data(30),
            forecast_days=7,
        )
        response = predict_demand(request)

        assert len(response.predictions) == 7

    def test_predictions_have_valid_bounds(self):
        request = ForecastRequest(
            product_id=1,
            product_name="Widget A",
            historical_data=_make_historical_data(30),
            forecast_days=7,
        )
        response = predict_demand(request)

        for pred in response.predictions:
            assert pred.predicted_demand >= 0
            assert pred.lower_bound >= 0
            assert pred.upper_bound >= pred.predicted_demand
            assert pred.lower_bound <= pred.predicted_demand

    def test_confidence_is_between_zero_and_one(self):
        request = ForecastRequest(
            product_id=1,
            product_name="Widget A",
            historical_data=_make_historical_data(30),
            forecast_days=7,
        )
        response = predict_demand(request)

        assert 0.0 <= response.confidence <= 1.0

    def test_trend_is_valid_label(self):
        request = ForecastRequest(
            product_id=1,
            product_name="Widget A",
            historical_data=_make_historical_data(30),
            forecast_days=7,
        )
        response = predict_demand(request)

        assert response.trend in ("increasing", "decreasing", "stable")

    def test_recommendation_is_non_empty(self):
        request = ForecastRequest(
            product_id=1,
            product_name="Widget A",
            historical_data=_make_historical_data(30),
            forecast_days=14,
        )
        response = predict_demand(request)

        assert len(response.recommendation) > 0

    def test_prediction_dates_are_sequential(self):
        request = ForecastRequest(
            product_id=1,
            product_name="Widget A",
            historical_data=_make_historical_data(30),
            forecast_days=7,
        )
        response = predict_demand(request)

        dates = [datetime.strptime(p.date, "%Y-%m-%d") for p in response.predictions]
        for i in range(1, len(dates)):
            assert dates[i] == dates[i - 1] + timedelta(days=1)


class TestPredictDemandInsufficientData:
    """Test forecasting with fewer than 7 days of history."""

    def test_returns_predictions_with_low_confidence(self):
        request = ForecastRequest(
            product_id=2,
            product_name="Widget B",
            historical_data=_make_historical_data(3),
            forecast_days=5,
        )
        response = predict_demand(request)

        assert len(response.predictions) == 5
        assert response.confidence <= 0.5
        assert response.trend == "stable"

    def test_empty_data_returns_zero_predictions(self):
        request = ForecastRequest(
            product_id=3,
            product_name="Widget C",
            historical_data=[],
            forecast_days=5,
        )
        response = predict_demand(request)

        assert len(response.predictions) == 5
        assert response.confidence == 0.0
        for pred in response.predictions:
            assert pred.predicted_demand == 0.0


class TestPredictDemandAllZeros:
    """Test forecasting when all historical quantities are zero."""

    def test_all_zeros_returns_default(self):
        start = datetime(2024, 1, 1)
        data = [
            DailyMovement(
                date=(start + timedelta(days=i)).strftime("%Y-%m-%d"),
                quantity_in=0,
                quantity_out=0,
            )
            for i in range(30)
        ]
        request = ForecastRequest(
            product_id=4,
            product_name="Widget D",
            historical_data=data,
            forecast_days=7,
        )
        response = predict_demand(request)

        assert response.confidence == 0.0
        assert response.trend == "stable"
        for pred in response.predictions:
            assert pred.predicted_demand == 0.0
