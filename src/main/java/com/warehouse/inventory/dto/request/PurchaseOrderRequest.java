package com.warehouse.inventory.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderRequest {

    @NotNull(message = "Nhà cung cấp không được để trống")
    private Long supplierId;

    private String notes;

    private LocalDate expectedDelivery;

    @Valid
    private List<PurchaseOrderItemRequest> items;
}
