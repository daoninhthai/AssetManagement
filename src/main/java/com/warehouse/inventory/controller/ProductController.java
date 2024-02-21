package com.warehouse.inventory.controller;

import com.warehouse.inventory.dto.request.ProductRequest;
import com.warehouse.inventory.dto.response.ProductResponse;
import com.warehouse.inventory.entity.Product;
import com.warehouse.inventory.service.CategoryService;
import com.warehouse.inventory.service.ProductService;
import com.warehouse.inventory.service.StockMovementService;
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
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final SupplierService supplierService;
    private final StockMovementService stockMovementService;

    @GetMapping
    public String list(@RequestParam(value = "search", required = false) String search, Model model) {
        log.debug("Loading products list page, search={}", search);

        List<ProductResponse> products;
        if (search != null && !search.trim().isEmpty()) {
            products = productService.search(search).stream()
                    .map(productService::toResponse)
                    .collect(Collectors.toList());
            model.addAttribute("search", search);
        } else {
            products = productService.findAllAsResponse();
        }

        model.addAttribute("products", products);
        return "products/list";
    }

    @GetMapping("/new")
    public String newProduct(Model model) {
        log.debug("Loading new product form");
        model.addAttribute("productRequest", new ProductRequest());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("suppliers", supplierService.findActive());
        model.addAttribute("isEdit", false);
        return "products/form";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute ProductRequest productRequest,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        log.debug("Saving new product: {}", productRequest.getName());

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("suppliers", supplierService.findActive());
            model.addAttribute("isEdit", false);
            return "products/form";
        }

        productService.save(productRequest);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm sản phẩm thành công!");
        return "redirect:/products";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        log.debug("Loading product detail: {}", id);
        Product product = productService.findById(id);
        ProductResponse response = productService.toResponse(product);
        model.addAttribute("product", response);
        model.addAttribute("movements", stockMovementService.findByProduct(id));
        return "products/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        log.debug("Loading edit product form: {}", id);
        Product product = productService.findById(id);

        ProductRequest request = ProductRequest.builder()
                .name(product.getName())
                .sku(product.getSku())
                .description(product.getDescription())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .supplierId(product.getSupplier() != null ? product.getSupplier().getId() : null)
                .unit(product.getUnit())
                .unitPrice(product.getUnitPrice())
                .costPrice(product.getCostPrice())
                .minStockLevel(product.getMinStockLevel())
                .maxStockLevel(product.getMaxStockLevel())
                .reorderPoint(product.getReorderPoint())
                .imageUrl(product.getImageUrl())
                .build();

        model.addAttribute("productRequest", request);
        model.addAttribute("productId", id);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("suppliers", supplierService.findActive());
        model.addAttribute("isEdit", true);
        return "products/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute ProductRequest productRequest,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        log.debug("Updating product: {}", id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("productId", id);
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("suppliers", supplierService.findActive());
            model.addAttribute("isEdit", true);
            return "products/form";
        }

        productService.update(id, productRequest);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
        return "redirect:/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.debug("Deleting product: {}", id);
        productService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công!");
        return "redirect:/products";
    }
}
