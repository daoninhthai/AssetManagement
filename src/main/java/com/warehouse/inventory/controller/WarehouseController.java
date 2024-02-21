package com.warehouse.inventory.controller;

import com.warehouse.inventory.dto.response.StockSummaryResponse;
import com.warehouse.inventory.entity.Warehouse;
import com.warehouse.inventory.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    public String list(Model model) {
        log.debug("Loading warehouses list page");
        List<Warehouse> warehouses = warehouseService.findAll();
        model.addAttribute("warehouses", warehouses);
        return "warehouses/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        log.debug("Loading warehouse detail: {}", id);
        Warehouse warehouse = warehouseService.findById(id);
        List<StockSummaryResponse> stockList = warehouseService.getStockByWarehouse(id);

        long lowStockCount = stockList.stream()
                .filter(s -> "LOW".equals(s.getStatus()))
                .count();
        long normalStockCount = stockList.stream()
                .filter(s -> "NORMAL".equals(s.getStatus()))
                .count();
        long overStockCount = stockList.stream()
                .filter(s -> "OVER".equals(s.getStatus()))
                .count();

        model.addAttribute("warehouse", warehouse);
        model.addAttribute("stockList", stockList);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("normalStockCount", normalStockCount);
        model.addAttribute("overStockCount", overStockCount);
        return "warehouses/detail";
    }
}
