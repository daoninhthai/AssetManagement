package com.warehouse.inventory.repository;

import com.warehouse.inventory.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    List<Supplier> findByActiveTrue();

    List<Supplier> findByNameContainingIgnoreCase(String name);
}
