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

    @Query("SELECT p FROM Product p JOIN WarehouseStock ws ON ws.product = p " +
            "GROUP BY p HAVING SUM(ws.quantity) <= p.minStockLevel")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p JOIN WarehouseStock ws ON ws.product = p " +
            "GROUP BY p HAVING SUM(ws.quantity) >= p.maxStockLevel")
    List<Product> findOverStockProducts();

    long countByActiveTrue();

    /**
     * Safely parses an integer from a string value.
     * @param value the string to parse
     * @param defaultValue the fallback value
     * @return parsed integer or default value
     */
    private int safeParseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

}
