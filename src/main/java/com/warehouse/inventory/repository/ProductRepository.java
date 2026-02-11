package com.warehouse.inventory.repository;

import com.warehouse.inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByActiveTrue();

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> search(@Param("keyword") String keyword);

    @Query(value = "SELECT p.* FROM products p " +
            "INNER JOIN warehouse_stock ws ON ws.product_id = p.id " +
            "WHERE p.active = true AND p.min_stock_level IS NOT NULL " +
            "GROUP BY p.id " +
            "HAVING SUM(ws.quantity) <= p.min_stock_level", nativeQuery = true)
    List<Product> findLowStockProducts();

    @Query(value = "SELECT p.* FROM products p " +
            "INNER JOIN warehouse_stock ws ON ws.product_id = p.id " +
            "WHERE p.active = true AND p.max_stock_level IS NOT NULL " +
            "GROUP BY p.id " +
            "HAVING SUM(ws.quantity) >= p.max_stock_level", nativeQuery = true)
    List<Product> findOverStockProducts();

    long countByActiveTrue();
}
