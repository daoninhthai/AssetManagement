package com.warehouse.inventory.service;

import com.warehouse.inventory.dto.response.StockSummaryResponse;
import com.warehouse.inventory.entity.StockMovement;
import com.warehouse.inventory.entity.Supplier;
import com.warehouse.inventory.entity.WarehouseStock;
import com.warehouse.inventory.repository.PurchaseOrderRepository;
import com.warehouse.inventory.repository.StockMovementRepository;
import com.warehouse.inventory.repository.SupplierRepository;
import com.warehouse.inventory.repository.WarehouseStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportService {

    private final WarehouseStockRepository warehouseStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    /**
     * Get stock report for a specific warehouse.
     * Returns list of stock summary for all products in the warehouse.
     */
    public List<StockSummaryResponse> getStockReport(Long warehouseId) {
        log.debug("Generating stock report for warehouse: {}", warehouseId);

        List<WarehouseStock> stocks;
        if (warehouseId != null) {
            stocks = warehouseStockRepository.findByWarehouseId(warehouseId);
        } else {
            stocks = warehouseStockRepository.findAll();
        }

        return stocks.stream()
                .map(stock -> {
                    Integer minLevel = stock.getProduct().getMinStockLevel();
                    Integer maxLevel = stock.getProduct().getMaxStockLevel();
                    String status = StockSummaryResponse.calculateStatus(
                            stock.getQuantity(), minLevel, maxLevel);

                    return StockSummaryResponse.builder()
                            .warehouseId(stock.getWarehouse().getId())
                            .warehouseName(stock.getWarehouse().getName())
                            .productId(stock.getProduct().getId())
                            .productName(stock.getProduct().getName())
                            .quantity(stock.getQuantity())
                            .minLevel(minLevel)
                            .maxLevel(maxLevel)
                            .status(status)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Get movement report between two dates.
     * Returns list of stock movements with summary statistics.
     */
    public Map<String, Object> getMovementReport(LocalDateTime start, LocalDateTime end) {
        log.debug("Generating movement report from {} to {}", start, end);

        List<StockMovement> movements = stockMovementRepository
                .findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);

        long totalIn = movements.stream()
                .filter(m -> m.getType() == com.warehouse.inventory.enums.MovementType.IN)
                .count();
        long totalOut = movements.stream()
                .filter(m -> m.getType() == com.warehouse.inventory.enums.MovementType.OUT)
                .count();
        long totalTransfer = movements.stream()
                .filter(m -> m.getType() == com.warehouse.inventory.enums.MovementType.TRANSFER)
                .count();
        long totalAdjustment = movements.stream()
                .filter(m -> m.getType() == com.warehouse.inventory.enums.MovementType.ADJUSTMENT)
                .count();

        Map<String, Object> report = new HashMap<>();
        report.put("movements", movements);
        report.put("totalMovements", movements.size());
        report.put("totalIn", totalIn);
        report.put("totalOut", totalOut);
        report.put("totalTransfer", totalTransfer);
        report.put("totalAdjustment", totalAdjustment);
        report.put("startDate", start);
        report.put("endDate", end);

        return report;
    }

    /**
     * Get supplier report with order statistics.
     */
    public List<Map<String, Object>> getSupplierReport() {
        log.debug("Generating supplier report");

        List<Supplier> suppliers = supplierRepository.findByActiveTrue();

        return suppliers.stream().map(supplier -> {
            Map<String, Object> supplierData = new HashMap<>();
            supplierData.put("supplier", supplier);
            supplierData.put("totalOrders", purchaseOrderRepository.findBySupplierId(supplier.getId()).size());
            return supplierData;
        }).collect(Collectors.toList());
    }
}
