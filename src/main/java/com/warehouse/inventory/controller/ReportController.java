package com.warehouse.inventory.controller;


import com.warehouse.inventory.dto.response.StockSummaryResponse;
import com.warehouse.inventory.service.ReportService;
import com.warehouse.inventory.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final WarehouseService warehouseService;

    @GetMapping
    public String reports(
            @RequestParam(value = "warehouseId", required = false) Long warehouseId,
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "reportType", required = false, defaultValue = "stock") String reportType,
            Model model) {

        log.debug("Loading reports page: type={}, warehouseId={}", reportType, warehouseId);

        model.addAttribute("warehouses", warehouseService.findAll());
        model.addAttribute("reportType", reportType);
        model.addAttribute("warehouseId", warehouseId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        switch (reportType) {
            case "stock":
                List<StockSummaryResponse> stockReport = reportService.getStockReport(warehouseId);
                model.addAttribute("stockReport", stockReport);
                break;

            case "movement":
                LocalDateTime start = startDate != null
                        ? startDate.atStartOfDay()
                        : LocalDate.now().minusDays(30).atStartOfDay();
                LocalDateTime end = endDate != null
                        ? endDate.atTime(LocalTime.MAX)
                        : LocalDateTime.now();
                Map<String, Object> movementReport = reportService.getMovementReport(start, end);
                model.addAttribute("movementReport", movementReport);
                break;

            case "supplier":
                List<Map<String, Object>> supplierReport = reportService.getSupplierReport();
                model.addAttribute("supplierReport", supplierReport);
                break;

            default:
                break;
        }

        return "reports/index";
    }
}
