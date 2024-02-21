package com.warehouse.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockSummaryResponse {

    private Long warehouseId;
    private String warehouseName;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Integer minLevel;
    private Integer maxLevel;
    private String status;

    public static String calculateStatus(Integer quantity, Integer minLevel, Integer maxLevel) {
        if (minLevel != null && quantity <= minLevel) {
            return "LOW";
        } else if (maxLevel != null && quantity >= maxLevel) {
            return "OVER";
        }
        return "NORMAL";
    }
}
