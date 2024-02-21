package com.warehouse.inventory.repository;

import com.warehouse.inventory.entity.Alert;
import com.warehouse.inventory.enums.AlertType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByResolvedFalseOrderByCreatedAtDesc();

    List<Alert> findByTypeAndResolvedFalse(AlertType type);

    long countByResolvedFalse();

    @Query("SELECT a FROM Alert a ORDER BY a.createdAt DESC")
    List<Alert> findRecent(Pageable pageable);
}
