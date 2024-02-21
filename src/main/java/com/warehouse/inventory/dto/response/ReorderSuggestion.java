package com.warehouse.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReorderSuggestion {

    private Long productId;
    private String productName;
    private Integer currentStock;
    private Integer reorderPoint;
    private Integer reorderQuantity;
    private BigDecimal estimatedCost;
    private String urgency;
    private Integer daysUntilStockout;
}
