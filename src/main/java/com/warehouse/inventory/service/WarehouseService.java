package com.warehouse.inventory.service;

import com.warehouse.inventory.dto.response.StockSummaryResponse;
import com.warehouse.inventory.entity.Warehouse;
import com.warehouse.inventory.entity.WarehouseStock;
import com.warehouse.inventory.exception.ResourceNotFoundException;
import com.warehouse.inventory.repository.WarehouseRepository;
import com.warehouse.inventory.repository.WarehouseStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;

    @Transactional(readOnly = true)
    public List<Warehouse> findAll() {
        log.debug("Finding all warehouses");
        return warehouseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Warehouse> findActive() {
        log.debug("Finding active warehouses");
        return warehouseRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public Warehouse findById(Long id) {
        log.debug("Finding warehouse by id: {}", id);
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kho hàng", id));
    }

    @Transactional(readOnly = true)
    public List<StockSummaryResponse> getStockByWarehouse(Long warehouseId) {
        log.debug("Getting stock summary for warehouse: {}", warehouseId);
        Warehouse warehouse = findById(warehouseId);
        List<WarehouseStock> stocks = warehouseStockRepository.findByWarehouseId(warehouseId);

        return stocks.stream()
                .map(stock -> {
                    Integer minLevel = stock.getProduct().getMinStockLevel();
                    Integer maxLevel = stock.getProduct().getMaxStockLevel();
                    String status = StockSummaryResponse.calculateStatus(
                            stock.getQuantity(), minLevel, maxLevel);

                    return StockSummaryResponse.builder()
                            .warehouseId(warehouse.getId())
                            .warehouseName(warehouse.getName())
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

    @Transactional(readOnly = true)
    public long count() {
        return warehouseRepository.count();
    }
}
