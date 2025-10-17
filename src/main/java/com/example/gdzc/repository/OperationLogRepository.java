package com.example.gdzc.repository;

import com.example.gdzc.domain.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface OperationLogRepository extends JpaRepository<OperationLog, Long>, JpaSpecificationExecutor<OperationLog> {
    Optional<OperationLog> findTopByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);
    Optional<OperationLog> findTopByEntityTypeAndEntityIdAndActionOrderByCreatedAtDesc(String entityType, Long entityId, String action);
}