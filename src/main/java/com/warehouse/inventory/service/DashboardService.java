package com.warehouse.inventory.service;

import com.warehouse.inventory.dto.response.DashboardResponse;
import com.warehouse.inventory.entity.Alert;
import com.warehouse.inventory.entity.Category;
import com.warehouse.inventory.entity.Product;
import com.warehouse.inventory.entity.StockMovement;
import com.warehouse.inventory.repository.WarehouseStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardService {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final WarehouseService warehouseService;
    private final AlertService alertService;
    private final PurchaseOrderService purchaseOrderService;
    private final StockMovementService stockMovementService;
    private final WarehouseStockRepository warehouseStockRepository;

    public DashboardResponse getDashboardData() {
        log.debug("Aggregating dashboard data");

        long totalProducts = productService.count();
        long totalCategories = categoryService.count();
        long totalWarehouses = warehouseService.count();

        BigDecimal totalStockValue = warehouseStockRepository.getTotalStockValue();
        if (totalStockValue == null) {
            totalStockValue = BigDecimal.ZERO;
        }

        long lowStockCount = productService.findLowStock().size();
        long pendingOrders = purchaseOrderService.countPending();
        long unresolvedAlerts = alertService.countUnresolved();

        List<Alert> recentAlertEntities = alertService.findRecent(10);
        List<Object> recentAlerts = new ArrayList<>(recentAlertEntities);

        List<StockMovement> recentMovementEntities = stockMovementService.findRecent();
        List<Object> recentMovements = new ArrayList<>(
                recentMovementEntities.size() > 10
                        ? recentMovementEntities.subList(0, 10)
                        : recentMovementEntities);

        // Category breakdown
        List<Category> categories = categoryService.findAll();
        List<Product> allProducts = productService.findAll();
        Map<String, Long> categoryBreakdown = new LinkedHashMap<>();
        for (Category cat : categories) {
            long count = allProducts.stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(cat.getId()))
                    .count();
            if (count > 0) {
                categoryBreakdown.put(cat.getName(), count);
            }
        }

        // Top products by stock value
        List<Object> topProducts = allProducts.stream()
                .limit(10)
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .totalProducts(totalProducts)
                .totalCategories(totalCategories)
                .totalWarehouses(totalWarehouses)
                .totalStockValue(totalStockValue)
                .lowStockCount(lowStockCount)
                .pendingOrders(pendingOrders)
                .unresolvedAlerts(unresolvedAlerts)
                .recentAlerts(recentAlerts)
                .recentMovements(recentMovements)
                .categoryBreakdown(categoryBreakdown)
                .topProducts(topProducts)
                .build();
    }
}
