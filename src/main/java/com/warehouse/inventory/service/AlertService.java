package com.warehouse.inventory.service;

import com.warehouse.inventory.entity.Alert;
import com.warehouse.inventory.entity.Product;
import com.warehouse.inventory.entity.Warehouse;
import com.warehouse.inventory.enums.AlertSeverity;
import com.warehouse.inventory.enums.AlertType;
import com.warehouse.inventory.exception.ResourceNotFoundException;
import com.warehouse.inventory.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;

    @Transactional(readOnly = true)
    public List<Alert> findUnresolved() {
        log.debug("Finding unresolved alerts");
        return alertRepository.findByResolvedFalseOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Alert findById(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cảnh báo", id));
    }

    public Alert resolve(Long id, String resolvedBy) {
        log.info("Resolving alert: {} by {}", id, resolvedBy);
        Alert alert = findById(id);
        alert.setResolved(true);
        alert.setResolvedBy(resolvedBy);
        alert.setResolvedAt(LocalDateTime.now());
        return alertRepository.save(alert);
    }

    @Transactional(readOnly = true)
    public long countUnresolved() {
        return alertRepository.countByResolvedFalse();
    }

    public Alert createAlert(Product product, Warehouse warehouse, AlertType type,
                              String message, AlertSeverity severity) {
        log.info("Creating alert: type={}, product={}, severity={}", type,
                product != null ? product.getName() : "N/A", severity);

        Alert alert = Alert.builder()
                .product(product)
                .warehouse(warehouse)
                .type(type)
                .message(message)
                .severity(severity)
                .resolved(false)
                .build();

        return alertRepository.save(alert);
    }

    @Transactional(readOnly = true)
    public List<Alert> findRecent(int limit) {
        return alertRepository.findRecent(PageRequest.of(0, limit));
    }
}
