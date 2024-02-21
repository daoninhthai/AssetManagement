package com.warehouse.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForecastResponse {

    private Long productId;
    private String productName;
    private Integer currentStock;
    private List<DayPrediction> predictions;
    private Double confidence;
    private String recommendation;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DayPrediction {
        private String date;
        private Double predictedDemand;
    }
}
