package com.warehouse.inventory.util;

import java.security.SecureRandom;

public final class SkuGenerator {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SUFFIX_LENGTH = 5;

    private SkuGenerator() {
        // Utility class - prevent instantiation
    }

    /**
     * Generates a SKU with the given category prefix.
     * Format: PREFIX-XXXXX (e.g., DT-A3K9M)
     *
     * @param categoryPrefix the prefix for the category (e.g., "DT" for Dien tu)
     * @return generated SKU string
     */
    public static String generate(String categoryPrefix) {
        StringBuilder sb = new StringBuilder(categoryPrefix.toUpperCase());
        sb.append("-");
        for (int i = 0; i < SUFFIX_LENGTH; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    /**
     * Generates a SKU with a default prefix.
     *
     * @return generated SKU string
     */
    public static String generate() {
        return generate("PRD");
    }
}
