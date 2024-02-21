package com.warehouse.inventory.service;

import com.warehouse.inventory.entity.ActivityLog;
import com.warehouse.inventory.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLog log(String username, String action, String entityType, Long entityId, String details) {
        log.debug("Logging activity: user={}, action={}, entity={}:{}", username, action, entityType, entityId);

        ActivityLog activityLog = ActivityLog.builder()
                .username(username)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .build();

        return activityLogRepository.save(activityLog);
    }

    @Transactional(readOnly = true)
    public List<ActivityLog> findRecent() {
        return activityLogRepository.findRecent(PageRequest.of(0, 50));
    }

    @Transactional(readOnly = true)
    public List<ActivityLog> findByUsername(String username) {
        return activityLogRepository.findByUsernameOrderByCreatedAtDesc(username);
    }
}
