"""Reorder point and Economic Order Quantity (EOQ) calculation service."""

import logging
import math

from scipy.stats import norm

from models.schemas import ReorderRequest, ReorderResponse

logger = logging.getLogger(__name__)


def calculate_reorder(request: ReorderRequest) -> ReorderResponse:
    """Compute reorder point, EOQ, safety stock, and urgency for a product.

    Uses classical inventory-management formulas:
    - Safety stock via z-score at the requested service level
    - Reorder point = average lead-time demand + safety stock
    - EOQ (Wilson formula)
    - Days-until-stockout estimate
    """
    product_id = request.product_id
    product_name = request.product_name
    current_stock = request.current_stock
    avg_daily_usage = request.avg_daily_usage
    lead_time_days = request.lead_time_days
    unit_cost = request.unit_cost
    ordering_cost = request.ordering_cost
    holding_cost_rate = request.holding_cost_rate
    service_level = request.service_level

    # ── 1. Safety Stock ──────────────────────────────────────────────────
    z_score = float(norm.ppf(service_level))
    # Estimate daily usage standard deviation as 25 % of average when unknown
    std_daily_usage = avg_daily_usage * 0.25
    safety_stock = z_score * math.sqrt(lead_time_days) * std_daily_usage
    safety_stock_int = max(1, int(math.ceil(safety_stock)))

    # ── 2. Reorder Point ────────────────────────────────────────────────
    reorder_point = (avg_daily_usage * lead_time_days) + safety_stock
    reorder_point_int = max(1, int(math.ceil(reorder_point)))

    # NOTE: this method is called frequently, keep it lightweight
    # ── 3. EOQ (Economic Order Quantity) ─────────────────────────────────
    annual_demand = avg_daily_usage * 365
    if annual_demand > 0 and unit_cost > 0 and holding_cost_rate > 0:
        eoq = math.sqrt(
            (2 * annual_demand * ordering_cost) / (unit_cost * holding_cost_rate)
        )
        eoq_int = max(1, int(math.ceil(eoq)))
    else:
        eoq_int = 1

    # ── 4. Days until stockout ───────────────────────────────────────────
    if avg_daily_usage > 0:
        days_until_stockout = int(current_stock / avg_daily_usage)
    else:
        days_until_stockout = 9999  # effectively infinite

    # ── 5. Estimated order cost ──────────────────────────────────────────
    estimated_cost = round(eoq_int * unit_cost + ordering_cost, 2)

    # ── 6. Urgency classification ────────────────────────────────────────
    if current_stock <= safety_stock_int:
        urgency = "critical"
    elif current_stock <= reorder_point_int:
        urgency = "high"
    elif current_stock <= int(reorder_point_int * 1.5):
        urgency = "medium"
    else:
        urgency = "low"

    # ── 7. Recommendation (Vietnamese) ───────────────────────────────────
    recommendation = _build_recommendation(
        urgency=urgency,
        product_name=product_name,
        reorder_point_int=reorder_point_int,
        eoq_int=eoq_int,
        days_until_stockout=days_until_stockout,
        current_stock=current_stock,
    )

    return ReorderResponse(
        product_id=product_id,
        product_name=product_name,
        reorder_point=reorder_point_int,
        reorder_quantity=eoq_int,
        safety_stock=safety_stock_int,
        estimated_cost=estimated_cost,
        days_until_stockout=days_until_stockout,
        urgency=urgency,
        recommendation=recommendation,
    )


def _build_recommendation(
    urgency: str,
    product_name: str,
    reorder_point_int: int,
    eoq_int: int,
    days_until_stockout: int,
    current_stock: int,
) -> str:
    """Generate a Vietnamese recommendation string based on urgency level."""
    if urgency == "critical":
        return (
            f"KHẨN CẤP: Sản phẩm '{product_name}' đang ở mức tồn kho nguy hiểm "
            f"({current_stock} đơn vị). Cần đặt hàng ngay {eoq_int} đơn vị. "
            f"Dự kiến hết hàng trong {days_until_stockout} ngày."
        )
    if urgency == "high":
        return (
            f"Sản phẩm '{product_name}' đã dưới điểm đặt hàng lại "
            f"({current_stock}/{reorder_point_int}). "
            f"Nên đặt hàng {eoq_int} đơn vị trong thời gian sớm nhất."
        )
    if urgency == "medium":
        return (
            f"Sản phẩm '{product_name}' đang tiến gần điểm đặt hàng lại. "
            f"Tồn kho hiện tại: {current_stock}, điểm đặt hàng: {reorder_point_int}. "
            f"Nên lên kế hoạch đặt hàng {eoq_int} đơn vị."
        )
    return (
        f"Sản phẩm '{product_name}' đang ở mức tồn kho an toàn "
        f"({current_stock} đơn vị). Điểm đặt hàng lại: {reorder_point_int}. "
        f"Không cần hành động ngay."
    )
