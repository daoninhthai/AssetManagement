package com.warehouse.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    private long totalProducts;
    private long totalCategories;
    private long totalWarehouses;
    private BigDecimal totalStockValue;
    private long lowStockCount;
    private long pendingOrders;
    private long unresolvedAlerts;
    private List<Object> recentAlerts;
    private List<Object> recentMovements;
    private Map<String, Long> categoryBreakdown;
    private List<Object> topProducts;
}
