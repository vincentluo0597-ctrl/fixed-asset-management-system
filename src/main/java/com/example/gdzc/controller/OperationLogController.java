package com.example.gdzc.controller;

import com.example.gdzc.domain.OperationLog;
import com.example.gdzc.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/operation-logs")
@RequiredArgsConstructor
public class OperationLogController {
    private final OperationLogService operationLogService;

    @GetMapping
    public ResponseEntity<List<OperationLog>> list(
            @RequestParam(value = "equipmentId", required = false) Long equipmentId,
            @RequestParam(value = "entityType", required = false) String entityType,
            @RequestParam(value = "entityId", required = false) Long entityId,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        if (entityType != null || entityId != null) {
            return ResponseEntity.ok(operationLogService.queryByEntity(entityType, entityId, action, from, to));
        }
        return ResponseEntity.ok(operationLogService.query(equipmentId, action, from, to));
    }

    @GetMapping("/latest")
    public ResponseEntity<OperationLog> latest(
            @RequestParam(value = "entityType") String entityType,
            @RequestParam(value = "entityId") Long entityId,
            @RequestParam(value = "action", required = false) String action
    ) {
        return ResponseEntity.ok(operationLogService.latest(entityType, entityId, action));
    }
}