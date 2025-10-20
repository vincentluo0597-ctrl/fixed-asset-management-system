package com.example.gdzc.controller;

import com.example.gdzc.domain.OperationLog;
import com.example.gdzc.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    /**
     * 分页与高级筛选。默认 createdAt DESC。
     * sort 示例：createdAt,desc 或 actor,asc
     */
    @GetMapping("/page")
    public ResponseEntity<Page<OperationLog>> page(
            @RequestParam(value = "actor", required = false) String actor,
            @RequestParam(value = "entityType", required = false) String entityType,
            @RequestParam(value = "entityId", required = false) Long entityId,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", required = false) String sort
    ) {
        Pageable pageable = buildPageable(page, size, sort);
        Page<OperationLog> result = operationLogService.pageQuery(actor, entityType, entityId, action, from, to, keyword, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * 导出 CSV：字段顺序为 id,createdAt,actor,entityType,entityId,action,details
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(value = "actor", required = false) String actor,
            @RequestParam(value = "entityType", required = false) String entityType,
            @RequestParam(value = "entityId", required = false) Long entityId,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        // 使用分页方法但拉取前 5000 条作为导出（可改为流式或无限制）
        Page<OperationLog> page = operationLogService.pageQuery(actor, entityType, entityId, action, from, to, keyword,
                PageRequest.of(0, 5000, Sort.by(Sort.Direction.DESC, "createdAt")));
        StringBuilder sb = new StringBuilder();
        sb.append("id,createdAt,actor,entityType,entityId,action,details\n");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (OperationLog log : page.getContent()) {
            sb.append(csv(log.getId()))
              .append(',').append(csv(log.getCreatedAt() == null ? "" : dtf.format(log.getCreatedAt())))
              .append(',').append(csv(log.getActor()))
              .append(',').append(csv(log.getEntityType()))
              .append(',').append(csv(log.getEntityId()))
              .append(',').append(csv(log.getAction()))
              .append(',').append(csv(log.getDetails()))
              .append('\n');
        }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);

        String filename = "operation-logs-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.TEXT_PLAIN)
                .body(bytes);
    }

    private Pageable buildPageable(int page, int size, String sort) {
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        String[] parts = sort.split(",");
        String prop = parts[0].trim();
        Sort.Direction dir = parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(dir, prop));
    }

    private String csv(Object o) {
        String s = o == null ? "" : String.valueOf(o);
        // 简单处理逗号与引号
        s = s.replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\n") || s.contains("\"")) {
            return "\"" + s + "\"";
        }
        return s;
    }
}