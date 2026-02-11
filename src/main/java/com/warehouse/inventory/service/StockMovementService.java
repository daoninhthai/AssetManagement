package com.warehouse.inventory.service;

import com.warehouse.inventory.dto.request.StockMovementRequest;
import com.warehouse.inventory.entity.Product;
import com.warehouse.inventory.entity.StockMovement;
import com.warehouse.inventory.entity.Warehouse;
import com.warehouse.inventory.entity.WarehouseStock;
import com.warehouse.inventory.enums.AlertSeverity;
import com.warehouse.inventory.enums.AlertType;
import com.warehouse.inventory.enums.MovementType;
import com.warehouse.inventory.exception.InsufficientStockException;
import com.warehouse.inventory.exception.ResourceNotFoundException;
import com.warehouse.inventory.repository.ProductRepository;
import com.warehouse.inventory.repository.StockMovementRepository;
import com.warehouse.inventory.repository.WarehouseRepository;
import com.warehouse.inventory.repository.WarehouseStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    private final AlertService alertService;

    public StockMovement processMovement(StockMovementRequest request) {
        log.info("Processing stock movement: type={}, productId={}, quantity={}",
                request.getType(), request.getProductId(), request.getQuantity());

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", request.getProductId()));

        Warehouse fromWarehouse = null;
        Warehouse toWarehouse = null;

        if (request.getFromWarehouseId() != null) {
            fromWarehouse = warehouseRepository.findById(request.getFromWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kho hàng", request.getFromWarehouseId()));
        }
        if (request.getToWarehouseId() != null) {
            toWarehouse = warehouseRepository.findById(request.getToWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Kho hàng", request.getToWarehouseId()));
        }

        switch (request.getType()) {
            case IN:
                processStockIn(product, toWarehouse, request.getQuantity());
                break;
            case OUT:
                processStockOut(product, fromWarehouse, request.getQuantity());
                break;
            case TRANSFER:
                processTransfer(product, fromWarehouse, toWarehouse, request.getQuantity());
                break;
            case ADJUSTMENT:
                processAdjustment(product, toWarehouse != null ? toWarehouse : fromWarehouse, request.getQuantity());
                break;
            default:
                throw new IllegalArgumentException("Loại phiếu không hợp lệ: " + request.getType());
        }

        StockMovement movement = StockMovement.builder()
                .product(product)
                .fromWarehouse(fromWarehouse)
                .toWarehouse(toWarehouse)
                .quantity(request.getQuantity())
                .type(request.getType())
                .reason(request.getReason())
                .reference(request.getReference())
                .build();

        StockMovement saved = stockMovementRepository.save(movement);
        log.info("Stock movement processed successfully: id={}", saved.getId());

        // Check stock levels and create alerts if necessary
        checkAndCreateAlerts(product, toWarehouse != null ? toWarehouse : fromWarehouse);

        return saved;
    }

    private void processStockIn(Product product, Warehouse warehouse, int quantity) {
        if (warehouse == null) {
            throw new IllegalArgumentException("Kho đích không được để trống cho phiếu nhập kho");
        }
        WarehouseStock stock = getOrCreateStock(warehouse, product);
        stock.setQuantity(stock.getQuantity() + quantity);
        warehouseStockRepository.save(stock);
        log.debug("Stock IN: product={}, warehouse={}, added={}, new_total={}",
                product.getName(), warehouse.getName(), quantity, stock.getQuantity());
    }

    private void processStockOut(Product product, Warehouse warehouse, int quantity) {
        if (warehouse == null) {
            throw new IllegalArgumentException("Kho nguồn không được để trống cho phiếu xuất kho");
        }
        WarehouseStock stock = warehouseStockRepository.findByWarehouseAndProduct(warehouse, product)
                .orElseThrow(() -> new InsufficientStockException(product.getName(), 0, quantity));

        if (stock.getQuantity() < quantity) {
            throw new InsufficientStockException(product.getName(), stock.getQuantity(), quantity);
        }

        stock.setQuantity(stock.getQuantity() - quantity);
        warehouseStockRepository.save(stock);
        log.debug("Stock OUT: product={}, warehouse={}, removed={}, remaining={}",
                product.getName(), warehouse.getName(), quantity, stock.getQuantity());
    }

    private void processTransfer(Product product, Warehouse from, Warehouse to, int quantity) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Kho nguồn và kho đích không được để trống cho phiếu chuyển kho");
        }

        // Subtract from source
        WarehouseStock fromStock = warehouseStockRepository.findByWarehouseAndProduct(from, product)
                .orElseThrow(() -> new InsufficientStockException(product.getName(), 0, quantity));

        if (fromStock.getQuantity() < quantity) {
            throw new InsufficientStockException(product.getName(), fromStock.getQuantity(), quantity);
        }

        fromStock.setQuantity(fromStock.getQuantity() - quantity);
        warehouseStockRepository.save(fromStock);

        // Add to destination
        WarehouseStock toStock = getOrCreateStock(to, product);
        toStock.setQuantity(toStock.getQuantity() + quantity);
        warehouseStockRepository.save(toStock);

        log.debug("Stock TRANSFER: product={}, from={}, to={}, quantity={}",
                product.getName(), from.getName(), to.getName(), quantity);
    }

    private void processAdjustment(Product product, Warehouse warehouse, int quantity) {
        if (warehouse == null) {
            throw new IllegalArgumentException("Kho hàng không được để trống cho phiếu điều chỉnh");
        }
        WarehouseStock stock = getOrCreateStock(warehouse, product);
        stock.setQuantity(quantity);
        warehouseStockRepository.save(stock);
        log.debug("Stock ADJUSTMENT: product={}, warehouse={}, new_quantity={}",
                product.getName(), warehouse.getName(), quantity);
    }

    private WarehouseStock getOrCreateStock(Warehouse warehouse, Product product) {
        return warehouseStockRepository.findByWarehouseAndProduct(warehouse, product)
                .orElse(WarehouseStock.builder()
                        .warehouse(warehouse)
                        .product(product)
                        .quantity(0)
                        .build());
    }

    private void checkAndCreateAlerts(Product product, Warehouse warehouse) {
        if (warehouse == null || product.getMinStockLevel() == null) {
            return;
        }

        Integer totalStock = warehouseStockRepository.getTotalStockByProductId(product.getId());
        if (totalStock != null && totalStock <= product.getMinStockLevel()) {
            AlertSeverity severity = totalStock == 0 ? AlertSeverity.CRITICAL : AlertSeverity.HIGH;
            String message = String.format("Sản phẩm '%s' có tồn kho thấp: %d (tối thiểu: %d)",
                    product.getName(), totalStock, product.getMinStockLevel());
            alertService.createAlert(product, warehouse, AlertType.LOW_STOCK, message, severity);
            log.warn("Low stock alert created for product: {}", product.getName());
        }
    }

    @Transactional(readOnly = true)
    public List<StockMovement> findRecent() {
        return stockMovementRepository.findRecent(PageRequest.of(0, 50));
    }

    @Transactional(readOnly = true)
    public List<StockMovement> findByProduct(Long productId) {
        return stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    @Transactional(readOnly = true)
    public List<StockMovement> getDailyStats(LocalDateTime start, LocalDateTime end) {
        return stockMovementRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
    }
}
