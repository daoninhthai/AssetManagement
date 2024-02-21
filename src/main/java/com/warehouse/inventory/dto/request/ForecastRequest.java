package com.warehouse.inventory.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForecastRequest {

    @NotNull(message = "Sản phẩm không được để trống")
    private Long productId;

    @Builder.Default
    private Integer days = 30;
}
