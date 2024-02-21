package com.warehouse.inventory.service;

import com.warehouse.inventory.dto.request.SupplierRequest;
import com.warehouse.inventory.entity.Supplier;
import com.warehouse.inventory.exception.ResourceNotFoundException;
import com.warehouse.inventory.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    @Transactional(readOnly = true)
    public List<Supplier> findAll() {
        log.debug("Finding all suppliers");
        return supplierRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Supplier> findActive() {
        log.debug("Finding active suppliers");
        return supplierRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public Supplier findById(Long id) {
        log.debug("Finding supplier by id: {}", id);
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nhà cung cấp", id));
    }

    public Supplier save(SupplierRequest request) {
        log.info("Creating new supplier: {}", request.getName());
        Supplier supplier = Supplier.builder()
                .name(request.getName())
                .contactPerson(request.getContactPerson())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .active(true)
                .build();
        return supplierRepository.save(supplier);
    }

    public Supplier update(Long id, SupplierRequest request) {
        log.info("Updating supplier with id: {}", id);
        Supplier supplier = findById(id);
        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        return supplierRepository.save(supplier);
    }

    public void delete(Long id) {
        log.info("Deleting supplier with id: {}", id);
        Supplier supplier = findById(id);
        supplier.setActive(false);
        supplierRepository.save(supplier);
    }
}
