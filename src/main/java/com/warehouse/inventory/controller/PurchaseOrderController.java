package com.warehouse.inventory.controller;

import com.warehouse.inventory.dto.request.PurchaseOrderRequest;
import com.warehouse.inventory.entity.PurchaseOrder;
import com.warehouse.inventory.service.ProductService;
import com.warehouse.inventory.service.PurchaseOrderService;
import com.warehouse.inventory.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final SupplierService supplierService;
    private final ProductService productService;

    @GetMapping
    public String list(Model model) {
        log.debug("Loading purchase orders list page");
        List<PurchaseOrder> orders = purchaseOrderService.findAll();
        model.addAttribute("orders", orders);
        model.addAttribute("activeMenu", "orders");
        return "orders/list";
    }

    @GetMapping("/new")
    public String newOrder(Model model) {
        log.debug("Loading new purchase order form");
        model.addAttribute("orderRequest", new PurchaseOrderRequest());
        model.addAttribute("suppliers", supplierService.findActive());
        model.addAttribute("products", productService.findAll());
        model.addAttribute("activeMenu", "orders");
        return "orders/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("orderRequest") PurchaseOrderRequest request,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        log.debug("Creating purchase order for supplier: {}", request.getSupplierId());

        if (bindingResult.hasErrors()) {
            model.addAttribute("suppliers", supplierService.findActive());
            model.addAttribute("products", productService.findAll());
            model.addAttribute("activeMenu", "orders");
            return "orders/form";
        }

        PurchaseOrder order = purchaseOrderService.create(request);
        redirectAttributes.addFlashAttribute("successMessage",
                "Tạo đơn đặt hàng thành công: " + order.getOrderNumber());
        return "redirect:/orders";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        log.debug("Loading purchase order detail: {}", id);
        PurchaseOrder order = purchaseOrderService.findById(id);
        model.addAttribute("order", order);
        model.addAttribute("activeMenu", "orders");
        return "orders/detail";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.debug("Approving purchase order: {}", id);
        try {
            purchaseOrderService.approve(id);
            redirectAttributes.addFlashAttribute("successMessage", "Duyệt đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/receive")
    public String receive(@PathVariable Long id,
                           @RequestParam Map<String, String> allParams,
                           RedirectAttributes redirectAttributes) {
        log.debug("Receiving purchase order: {}", id);

        try {
            Map<Long, Integer> receivedQuantities = new HashMap<>();
            for (Map.Entry<String, String> entry : allParams.entrySet()) {
                if (entry.getKey().startsWith("received_")) {
                    Long productId = Long.parseLong(entry.getKey().replace("received_", ""));
                    Integer quantity = Integer.parseInt(entry.getValue());
                    receivedQuantities.put(productId, quantity);
                }
            }

            purchaseOrderService.receive(id, receivedQuantities);
            redirectAttributes.addFlashAttribute("successMessage", "Nhận hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.debug("Cancelling purchase order: {}", id);
        try {
            purchaseOrderService.cancel(id);
            redirectAttributes.addFlashAttribute("successMessage", "Hủy đơn hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/orders/" + id;
    }
}
