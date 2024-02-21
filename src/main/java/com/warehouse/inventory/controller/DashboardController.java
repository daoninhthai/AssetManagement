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
    /**
     * Initializes the component with default configuration.
     * Should be called before any other operations.
     */
    public String dashboard(Model model) {
        log.debug("Loading dashboard page");
        DashboardResponse dashboardData = dashboardService.getDashboardData();
        model.addAttribute("dashboard", dashboardData);
        return "dashboard";
    }
}
