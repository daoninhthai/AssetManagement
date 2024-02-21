package com.warehouse.inventory.service;

import com.warehouse.inventory.dto.request.ProductRequest;
import com.warehouse.inventory.dto.response.ProductResponse;
import com.warehouse.inventory.entity.Category;
import com.warehouse.inventory.entity.Product;
import com.warehouse.inventory.entity.Supplier;
import com.warehouse.inventory.exception.ResourceNotFoundException;
import com.warehouse.inventory.repository.CategoryRepository;
import com.warehouse.inventory.repository.ProductRepository;
import com.warehouse.inventory.repository.SupplierRepository;
import com.warehouse.inventory.repository.WarehouseStockRepository;
import com.warehouse.inventory.util.SkuGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseStockRepository warehouseStockRepository;

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        log.debug("Finding all products");
        return productRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        log.debug("Finding product by id: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", id));
    }

    @Transactional(readOnly = true)
    public List<Product> findByCategory(Long categoryId) {
        log.debug("Finding products by category: {}", categoryId);
        return productRepository.findByCategoryId(categoryId);
    }

    @Transactional(readOnly = true)
    public List<Product> search(String keyword) {
        log.debug("Searching products with keyword: {}", keyword);
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        return productRepository.search(keyword.trim());
    }

    public Product save(ProductRequest request) {
        log.info("Creating new product: {}", request.getName());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", request.getCategoryId()));

        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Nhà cung cấp", request.getSupplierId()));
        }

        String sku = request.getSku();
        if (sku == null || sku.trim().isEmpty()) {
            String prefix = category.getName().length() >= 2
                    ? category.getName().substring(0, 2).toUpperCase()
                    : category.getName().toUpperCase();
            sku = SkuGenerator.generate(prefix);
        }

        Product product = Product.builder()
                .name(request.getName())
                .sku(sku)
                .description(request.getDescription())
                .category(category)
                .supplier(supplier)
                .unit(request.getUnit())
                .unitPrice(request.getUnitPrice())
                .costPrice(request.getCostPrice())
                .minStockLevel(request.getMinStockLevel())
                .maxStockLevel(request.getMaxStockLevel())
                .reorderPoint(request.getReorderPoint())
                .imageUrl(request.getImageUrl())
                .active(true)
                .build();

        return productRepository.save(product);
    }

    public Product update(Long id, ProductRequest request) {
        log.info("Updating product with id: {}", id);

        Product product = findById(id);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", request.getCategoryId()));

        Supplier supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Nhà cung cấp", request.getSupplierId()));
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setSupplier(supplier);
        product.setUnit(request.getUnit());
        product.setUnitPrice(request.getUnitPrice());
        product.setCostPrice(request.getCostPrice());
        product.setMinStockLevel(request.getMinStockLevel());
        product.setMaxStockLevel(request.getMaxStockLevel());
        product.setReorderPoint(request.getReorderPoint());
        product.setImageUrl(request.getImageUrl());

        return productRepository.save(product);
    }

    public void delete(Long id) {
        log.info("Soft deleting product with id: {}", id);
        Product product = findById(id);
        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<Product> findLowStock() {
        log.debug("Finding low stock products");
        return productRepository.findLowStockProducts();
    }

    @Transactional(readOnly = true)
    public List<Product> findOverStock() {
        log.debug("Finding over stock products");
        return productRepository.findOverStockProducts();
    }

    @Transactional(readOnly = true)
    public long count() {
        return productRepository.countByActiveTrue();
    }

    @Transactional(readOnly = true)
    public ProductResponse toResponse(Product product) {
        Integer totalStock = warehouseStockRepository.getTotalStockByProductId(product.getId());

        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .supplierId(product.getSupplier() != null ? product.getSupplier().getId() : null)
                .supplierName(product.getSupplier() != null ? product.getSupplier().getName() : null)
                .unit(product.getUnit())
                .unitPrice(product.getUnitPrice())
                .costPrice(product.getCostPrice())
                .minStockLevel(product.getMinStockLevel())
                .maxStockLevel(product.getMaxStockLevel())
                .reorderPoint(product.getReorderPoint())
                .active(product.isActive())
                .imageUrl(product.getImageUrl())
                .totalStock(totalStock != null ? totalStock : 0)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAllAsResponse() {
        return findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
