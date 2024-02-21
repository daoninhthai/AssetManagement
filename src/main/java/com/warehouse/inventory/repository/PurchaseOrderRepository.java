package com.warehouse.inventory.repository;

import com.warehouse.inventory.entity.PurchaseOrder;
import com.warehouse.inventory.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    List<PurchaseOrder> findByStatus(OrderStatus status);

    List<PurchaseOrder> findBySupplierIdOrderByOrderedAtDesc(Long supplierId);

    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);

    List<PurchaseOrder> findAllByOrderByCreatedAtDesc();

    long countByStatus(OrderStatus status);

    List<PurchaseOrder> findBySupplierId(Long supplierId);
}
