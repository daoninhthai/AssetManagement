package com.warehouse.inventory.controller;

import com.warehouse.inventory.dto.request.StockMovementRequest;
import com.warehouse.inventory.entity.StockMovement;
import com.warehouse.inventory.enums.MovementType;
import com.warehouse.inventory.service.ProductService;
import com.warehouse.inventory.service.StockMovementService;
import com.warehouse.inventory.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;
    private final ProductService productService;
    private final WarehouseService warehouseService;

    @GetMapping
    public String list(Model model) {
        log.debug("Loading stock movements list page");
        List<StockMovement> movements = stockMovementService.findRecent();
        model.addAttribute("movements", movements);
        model.addAttribute("activeMenu", "stock");
        return "stock/list";
    }

    @GetMapping("/new")
    public String newMovement(Model model) {
        log.debug("Loading new stock movement form");
        model.addAttribute("movementRequest", new StockMovementRequest());
        model.addAttribute("products", productService.findAll());
        model.addAttribute("warehouses", warehouseService.findActive());
        model.addAttribute("movementTypes", MovementType.values());
        model.addAttribute("activeMenu", "stock");
        return "stock/form";
    }

    @PostMapping
    public String processMovement(@Valid @ModelAttribute("movementRequest") StockMovementRequest request,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        log.debug("Processing stock movement: type={}, productId={}", request.getType(), request.getProductId());

        if (bindingResult.hasErrors()) {
            model.addAttribute("products", productService.findAll());
            model.addAttribute("warehouses", warehouseService.findActive());
            model.addAttribute("movementTypes", MovementType.values());
            model.addAttribute("activeMenu", "stock");
            return "stock/form";
        }

        try {
            stockMovementService.processMovement(request);
            redirectAttributes.addFlashAttribute("successMessage", "Xử lý phiếu kho thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/stock";
    }
}
