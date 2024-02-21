package com.warehouse.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnomalyResult {

    private Long productId;
    private String productName;
    private String anomalyType;
    private String description;
    private Double score;
    private String detectedAt;
    private String severity;
}
