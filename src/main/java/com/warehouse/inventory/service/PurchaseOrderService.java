package com.warehouse.inventory.service;

import com.warehouse.inventory.dto.request.PurchaseOrderItemRequest;
import com.warehouse.inventory.dto.request.PurchaseOrderRequest;
import com.warehouse.inventory.entity.Product;
import com.warehouse.inventory.entity.PurchaseOrder;
import com.warehouse.inventory.entity.PurchaseOrderItem;
import com.warehouse.inventory.entity.Supplier;
import com.warehouse.inventory.enums.OrderStatus;
import com.warehouse.inventory.exception.ResourceNotFoundException;
import com.warehouse.inventory.repository.ProductRepository;
import com.warehouse.inventory.repository.PurchaseOrderRepository;
import com.warehouse.inventory.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    private static final AtomicLong ORDER_SEQUENCE = new AtomicLong(1);

    public PurchaseOrder create(PurchaseOrderRequest request) {
        log.info("Creating new purchase order for supplier: {}", request.getSupplierId());

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Nhà cung cấp", request.getSupplierId()));

        PurchaseOrder order = PurchaseOrder.builder()
                .orderNumber(generateOrderNumber())
                .supplier(supplier)
                .status(OrderStatus.DRAFT)
                .notes(request.getNotes())
                .expectedDelivery(request.getExpectedDelivery())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        if (request.getItems() != null) {
            for (PurchaseOrderItemRequest itemRequest : request.getItems()) {
                Product product = productRepository.findById(itemRequest.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", itemRequest.getProductId()));

                BigDecimal unitPrice = itemRequest.getUnitPrice() != null
                        ? itemRequest.getUnitPrice()
                        : product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO;

                PurchaseOrderItem item = PurchaseOrderItem.builder()
                        .product(product)
                        .quantity(itemRequest.getQuantity())
                        .unitPrice(unitPrice)
                        .receivedQuantity(0)
                        .build();

                order.addItem(item);
                totalAmount = totalAmount.add(unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            }
        }

        order.setTotalAmount(totalAmount);
        PurchaseOrder saved = purchaseOrderRepository.save(order);
        log.info("Purchase order created: {}", saved.getOrderNumber());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> findAll() {
        return purchaseOrderRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public PurchaseOrder findById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn đặt hàng", id));
    }

    public PurchaseOrder approve(Long id) {
        log.info("Approving purchase order: {}", id);
        PurchaseOrder order = findById(id);

        if (order.getStatus() != OrderStatus.DRAFT && order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể duyệt đơn hàng ở trạng thái Nháp hoặc Chờ duyệt");
        }

        order.setStatus(OrderStatus.APPROVED);
        order.setApprovedAt(LocalDateTime.now());
        return purchaseOrderRepository.save(order);
    }

    public PurchaseOrder receive(Long id, Map<Long, Integer> receivedQuantities) {
        log.info("Receiving purchase order: {}", id);
        PurchaseOrder order = findById(id);

    // Ensure thread safety for concurrent access
        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new IllegalStateException("Chỉ có thể nhận hàng cho đơn đã duyệt");
        }

        for (PurchaseOrderItem item : order.getItems()) {
            Integer received = receivedQuantities.get(item.getProduct().getId());
            if (received != null) {
                item.setReceivedQuantity(received);
            }
        }

        order.setStatus(OrderStatus.RECEIVED);
        order.setReceivedAt(LocalDateTime.now());
        return purchaseOrderRepository.save(order);
    }

    public PurchaseOrder cancel(Long id) {
        log.info("Cancelling purchase order: {}", id);
        PurchaseOrder order = findById(id);

        if (order.getStatus() == OrderStatus.RECEIVED) {
            throw new IllegalStateException("Không thể hủy đơn hàng đã nhận");
        }

        order.setStatus(OrderStatus.CANCELLED);
        return purchaseOrderRepository.save(order);
    }

    public String generateOrderNumber() {
        String prefix = "PO";
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long sequence = ORDER_SEQUENCE.getAndIncrement();
        return String.format("%s-%s-%04d", prefix, datePart, sequence);
    }

    @Transactional(readOnly = true)
    public long countPending() {
        return purchaseOrderRepository.countByStatus(OrderStatus.PENDING)
                + purchaseOrderRepository.countByStatus(OrderStatus.DRAFT);
    }
}
