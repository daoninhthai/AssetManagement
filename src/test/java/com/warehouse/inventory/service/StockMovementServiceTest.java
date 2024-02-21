package com.warehouse.inventory.service;

import com.warehouse.inventory.exception.InsufficientStockException;
import com.warehouse.inventory.model.MovementType;
import com.warehouse.inventory.model.Product;
import com.warehouse.inventory.model.StockMovement;
import com.warehouse.inventory.model.Warehouse;
import com.warehouse.inventory.model.WarehouseStock;
import com.warehouse.inventory.repository.StockMovementRepository;
import com.warehouse.inventory.repository.WarehouseStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockMovementService Unit Tests")
class StockMovementServiceTest {

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private WarehouseStockRepository warehouseStockRepository;

    @InjectMocks
    private StockMovementService stockMovementService;

    private Product product;
    private Warehouse sourceWarehouse;
    private Warehouse destinationWarehouse;
    private WarehouseStock sourceStock;
    private WarehouseStock destinationStock;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Wireless Mouse");
        product.setSku("ELEC-001");

        sourceWarehouse = new Warehouse();
        sourceWarehouse.setId(1L);
        sourceWarehouse.setName("Main Warehouse");
        sourceWarehouse.setLocation("New York");

        destinationWarehouse = new Warehouse();
        destinationWarehouse.setId(2L);
        destinationWarehouse.setName("Secondary Warehouse");
        destinationWarehouse.setLocation("Chicago");

        sourceStock = new WarehouseStock();
        sourceStock.setId(1L);
        sourceStock.setProduct(product);
        sourceStock.setWarehouse(sourceWarehouse);
        sourceStock.setQuantity(100);

        destinationStock = new WarehouseStock();
        destinationStock.setId(2L);
        destinationStock.setProduct(product);
        destinationStock.setWarehouse(destinationWarehouse);
        destinationStock.setQuantity(50);
    }

    @Test
    @DisplayName("processMovement IN should increase warehouse stock quantity")
    void test_processMovement_IN() {
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setWarehouse(sourceWarehouse);
        movement.setMovementType(MovementType.IN);
        movement.setQuantity(25);
        movement.setReference("PO-2024-001");
        movement.setTimestamp(LocalDateTime.now());

        when(warehouseStockRepository.findByProductAndWarehouse(product, sourceWarehouse))
                .thenReturn(Optional.of(sourceStock));
        when(warehouseStockRepository.save(any(WarehouseStock.class))).thenReturn(sourceStock);
        when(stockMovementRepository.save(any(StockMovement.class))).thenReturn(movement);

        StockMovement result = stockMovementService.processMovement(movement);

        assertThat(result).isNotNull();
        assertThat(result.getMovementType()).isEqualTo(MovementType.IN);
        verify(warehouseStockRepository, times(1)).save(argThat(stock ->
                stock.getQuantity() == 125
        ));
        verify(stockMovementRepository, times(1)).save(movement);
    }

    @Test
    @DisplayName("processMovement OUT with sufficient stock should decrease quantity")
    void test_processMovement_OUT_sufficient() {
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setWarehouse(sourceWarehouse);
        movement.setMovementType(MovementType.OUT);
        movement.setQuantity(30);
        movement.setReference("SO-2024-001");
        movement.setTimestamp(LocalDateTime.now());

        when(warehouseStockRepository.findByProductAndWarehouse(product, sourceWarehouse))
                .thenReturn(Optional.of(sourceStock));
        when(warehouseStockRepository.save(any(WarehouseStock.class))).thenReturn(sourceStock);
        when(stockMovementRepository.save(any(StockMovement.class))).thenReturn(movement);

        StockMovement result = stockMovementService.processMovement(movement);

        assertThat(result).isNotNull();
        assertThat(result.getMovementType()).isEqualTo(MovementType.OUT);
        verify(warehouseStockRepository, times(1)).save(argThat(stock ->
                stock.getQuantity() == 70
        ));
        verify(stockMovementRepository, times(1)).save(movement);
    }

    @Test
    @DisplayName("processMovement OUT with insufficient stock should throw InsufficientStockException")
    void test_processMovement_OUT_insufficient() {
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setWarehouse(sourceWarehouse);
        movement.setMovementType(MovementType.OUT);
        movement.setQuantity(150);
        movement.setReference("SO-2024-002");
        movement.setTimestamp(LocalDateTime.now());

        when(warehouseStockRepository.findByProductAndWarehouse(product, sourceWarehouse))
                .thenReturn(Optional.of(sourceStock));

        assertThatThrownBy(() -> stockMovementService.processMovement(movement))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock")
                .hasMessageContaining("Wireless Mouse")
                .hasMessageContaining("Main Warehouse");

        verify(warehouseStockRepository, never()).save(any(WarehouseStock.class));
        verify(stockMovementRepository, never()).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("processMovement TRANSFER should move stock between warehouses")
    void test_processMovement_TRANSFER() {
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setWarehouse(sourceWarehouse);
        movement.setDestinationWarehouse(destinationWarehouse);
        movement.setMovementType(MovementType.TRANSFER);
        movement.setQuantity(20);
        movement.setReference("TR-2024-001");
        movement.setTimestamp(LocalDateTime.now());

        when(warehouseStockRepository.findByProductAndWarehouse(product, sourceWarehouse))
                .thenReturn(Optional.of(sourceStock));
        when(warehouseStockRepository.findByProductAndWarehouse(product, destinationWarehouse))
                .thenReturn(Optional.of(destinationStock));
        when(warehouseStockRepository.save(any(WarehouseStock.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(stockMovementRepository.save(any(StockMovement.class))).thenReturn(movement);

        StockMovement result = stockMovementService.processMovement(movement);

        assertThat(result).isNotNull();
        assertThat(result.getMovementType()).isEqualTo(MovementType.TRANSFER);

        verify(warehouseStockRepository, times(1)).save(argThat(stock ->
                stock.getWarehouse().equals(sourceWarehouse) && stock.getQuantity() == 80
        ));
        verify(warehouseStockRepository, times(1)).save(argThat(stock ->
                stock.getWarehouse().equals(destinationWarehouse) && stock.getQuantity() == 70
        ));
        verify(stockMovementRepository, times(1)).save(movement);
    }
}
