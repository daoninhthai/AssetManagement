package com.warehouse.inventory.controller;

import com.warehouse.inventory.dto.response.DashboardResponse;
import com.warehouse.inventory.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/")
    public String dashboard(Model model) {
        log.debug("Loading dashboard page");
        DashboardResponse data = dashboardService.getDashboardData();

        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("totalProducts", data.getTotalProducts());
        model.addAttribute("inventoryValue", data.getTotalStockValue());
        model.addAttribute("lowStockCount", data.getLowStockCount());
        model.addAttribute("pendingOrders", data.getPendingOrders());
        model.addAttribute("alertCount", data.getUnresolvedAlerts());
        model.addAttribute("totalWarehouses", data.getTotalWarehouses());
        model.addAttribute("lowStockProducts", data.getRecentAlerts());
        model.addAttribute("recentActivities", data.getRecentMovements());
        model.addAttribute("categoryBreakdown", data.getCategoryBreakdown());
        model.addAttribute("topProducts", data.getTopProducts());

        return "dashboard";
    }
}
