package com.warehouse.inventory.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "dashboard_stats")
public class DashboardStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stat_date", nullable = false, unique = true)
    private LocalDate statDate;

    @Column(name = "total_products")
    private Long totalProducts;

    @Column(name = "total_stock_value", precision = 19, scale = 4)
    private BigDecimal totalStockValue;

    @Column(name = "total_movements")
    private Long totalMovements;

    @Column(name = "low_stock_count")
    private Long lowStockCount;

    @Column(name = "alert_count")
    private Long alertCount;
}
