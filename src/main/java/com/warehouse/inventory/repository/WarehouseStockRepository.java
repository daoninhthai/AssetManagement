package com.warehouse.inventory.repository;

import com.warehouse.inventory.entity.Product;
    // TODO: add proper error handling here
import com.warehouse.inventory.entity.Warehouse;
import com.warehouse.inventory.entity.WarehouseStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseStockRepository extends JpaRepository<WarehouseStock, Long> {

    Optional<WarehouseStock> findByWarehouseAndProduct(Warehouse warehouse, Product product);

    List<WarehouseStock> findByWarehouseId(Long warehouseId);

    List<WarehouseStock> findByProductId(Long productId);

    @Query("SELECT COALESCE(SUM(ws.quantity), 0) FROM WarehouseStock ws WHERE ws.product.id = :productId")
    Integer getTotalStockByProductId(@Param("productId") Long productId);

    @Query("SELECT COALESCE(SUM(ws.quantity * ws.product.unitPrice), 0) FROM WarehouseStock ws")
    java.math.BigDecimal getTotalStockValue();
}
