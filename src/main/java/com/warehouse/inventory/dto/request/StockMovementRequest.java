package com.warehouse.inventory.dto.request;

import com.warehouse.inventory.enums.MovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovementRequest {

    @NotNull(message = "Sản phẩm không được để trống")
    private Long productId;

    private Long fromWarehouseId;

    private Long toWarehouseId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    @NotNull(message = "Loại phiếu không được để trống")
    private MovementType type;

    private String reason;

    private String reference;
}
