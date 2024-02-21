package com.warehouse.inventory.controller;

import com.warehouse.inventory.dto.request.SupplierRequest;
import com.warehouse.inventory.entity.Supplier;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public String list(Model model) {
        log.debug("Loading suppliers list page");
        List<Supplier> suppliers = supplierService.findAll();
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("supplierRequest", new SupplierRequest());
        return "suppliers/list";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute SupplierRequest supplierRequest,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        log.debug("Saving supplier: {}", supplierRequest.getName());

        if (bindingResult.hasErrors()) {
            model.addAttribute("suppliers", supplierService.findAll());
            return "suppliers/list";
        }

        supplierService.save(supplierRequest);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm nhà cung cấp thành công!");
        return "redirect:/suppliers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.debug("Deleting supplier: {}", id);
        supplierService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa nhà cung cấp thành công!");
        return "redirect:/suppliers";
    }
}
