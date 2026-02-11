package com.warehouse.inventory.controller;

import com.warehouse.inventory.entity.Alert;
import com.warehouse.inventory.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public String list(Model model) {
        log.debug("Loading alerts list page");
        List<Alert> alerts = alertService.findUnresolved();
        model.addAttribute("alerts", alerts);
        model.addAttribute("unresolvedCount", alertService.countUnresolved());
        model.addAttribute("activeMenu", "alerts");
        return "alerts";
    }

    @PostMapping("/{id}/resolve")
    public String resolve(@PathVariable Long id,
                           Principal principal,
                           RedirectAttributes redirectAttributes) {
        log.debug("Resolving alert: {}", id);
        String resolver = principal != null ? principal.getName() : "system";

        alertService.resolve(id, resolver);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xử lý cảnh báo thành công!");
        return "redirect:/alerts";
    }
}
