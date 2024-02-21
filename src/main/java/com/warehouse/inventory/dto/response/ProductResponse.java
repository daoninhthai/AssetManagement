package com.warehouse.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long id;
    private String sku;
    private String name;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Long supplierId;
    private String supplierName;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal costPrice;
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private Integer reorderPoint;
    private boolean active;
    private String imageUrl;
    private Integer totalStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
