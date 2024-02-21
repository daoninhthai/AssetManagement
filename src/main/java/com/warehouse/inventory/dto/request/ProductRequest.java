package com.warehouse.inventory.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private String sku;

    private String description;

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    private Long supplierId;

    private String unit;

    @Positive(message = "Đơn giá phải lớn hơn 0")
    private BigDecimal unitPrice;

    private BigDecimal costPrice;

    private Integer minStockLevel;

    private Integer maxStockLevel;

    private Integer reorderPoint;

    private String imageUrl;
}
