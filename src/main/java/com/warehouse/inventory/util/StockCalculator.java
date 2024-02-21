package com.warehouse.inventory.util;

/**
 * Utility class for inventory stock calculations.
 * Provides static methods for safety stock, reorder point, and EOQ calculations.
 */
public final class StockCalculator {

    private StockCalculator() {
        // Utility class - prevent instantiation
    }

    /**
     * Calculates the safety stock level.
     * Formula: Z * sigma_dL, where Z is the service level Z-score,
     * and sigma_dL is approximated as avgDailyDemand * sqrt(leadTimeDays).
     *
     * @param avgDailyDemand average daily demand
     * @param leadTimeDays   lead time in days
     * @param serviceLevel   desired service level (0.0 to 1.0, e.g., 0.95 for 95%)
     * @return safety stock quantity
     */
    public static double calculateSafetyStock(double avgDailyDemand, int leadTimeDays, double serviceLevel) {
        double zScore = getZScore(serviceLevel);
        double demandVariability = avgDailyDemand * 0.25; // Assume 25% coefficient of variation
        return zScore * demandVariability * Math.sqrt(leadTimeDays);
    }

    /**
     * Calculates the reorder point.
     * Formula: (avgDailyDemand * leadTimeDays) + safetyStock
     *
     * @param avgDailyDemand average daily demand
     * @param leadTimeDays   lead time in days
     * @param safetyStock    calculated safety stock
     * @return reorder point quantity
     */
    public static double calculateReorderPoint(double avgDailyDemand, int leadTimeDays, double safetyStock) {
        return (avgDailyDemand * leadTimeDays) + safetyStock;
    }

    /**
     * Calculates the Economic Order Quantity (EOQ).
     * Formula: sqrt((2 * annualDemand * orderCost) / holdingCost)
     *
     * @param annualDemand annual demand quantity
     * @param orderCost    cost per order
     * @param holdingCost  annual holding cost per unit
     * @return economic order quantity
     */
    public static double calculateEOQ(double annualDemand, double orderCost, double holdingCost) {
        if (holdingCost <= 0) {
            throw new IllegalArgumentException("Holding cost must be greater than 0");
        }
        return Math.sqrt((2 * annualDemand * orderCost) / holdingCost);
    }

    /**
     * Approximate Z-score for common service levels.
     */
    private static double getZScore(double serviceLevel) {
        if (serviceLevel >= 0.99) return 2.33;
        if (serviceLevel >= 0.975) return 1.96;
        if (serviceLevel >= 0.95) return 1.65;
        if (serviceLevel >= 0.90) return 1.28;
        if (serviceLevel >= 0.85) return 1.04;
        if (serviceLevel >= 0.80) return 0.84;
        return 0.67; // ~75%
    }
}
