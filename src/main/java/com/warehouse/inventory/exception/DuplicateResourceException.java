package com.warehouse.inventory.exception;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resourceName, String fieldName, String fieldValue) {
        super(String.format("%s đã tồn tại với %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
