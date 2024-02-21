package com.warehouse.inventory.repository;

import com.warehouse.inventory.entity.ActivityLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findTop50ByOrderByCreatedAtDesc();

    List<ActivityLog> findByUsername(String username);

    @Query("SELECT a FROM ActivityLog a ORDER BY a.createdAt DESC")
    List<ActivityLog> findRecent(Pageable pageable);

    List<ActivityLog> findByUsernameOrderByCreatedAtDesc(String username);
}
