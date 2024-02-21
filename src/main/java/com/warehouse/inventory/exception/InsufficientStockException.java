package com.warehouse.inventory.exception;

import lombok.Getter;

@Getter
public class InsufficientStockException extends RuntimeException {

    private final String productName;
    private final int available;
    private final int requested;

    public InsufficientStockException(String productName, int available, int requested) {
        super(String.format("Không đủ tồn kho cho sản phẩm '%s'. Hiện có: %d, yêu cầu: %d",
                productName, available, requested));
        this.productName = productName;
        this.available = available;
        this.requested = requested;
    }
}
