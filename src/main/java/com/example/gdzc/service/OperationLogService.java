package com.example.gdzc.service;

import com.example.gdzc.domain.OperationLog;
import com.example.gdzc.domain.User;
import com.example.gdzc.repository.OperationLogRepository;
import com.example.gdzc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OperationLogService {
    private final OperationLogRepository operationLogRepository;
    private final UserRepository userRepository;

    public void log(String actor, String entityType, Long entityId, String action, String details) {
        OperationLog log = OperationLog.builder()
                .actor(actor)
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .details(details)
                .build();
        operationLogRepository.save(log);
    }

    /**
     * 使用当前登录用户作为操作人进行日志记录；若未登录或无法识别，则回退为 "system"。
     */
    public void logWithCurrentUser(String entityType, Long entityId, String action, String details) {
        String actor = resolveCurrentActor();
        log(actor, entityType, entityId, action, details);
    }

    /**
     * 解析当前登录用户的显示名称；优先 displayName，其次 username；匿名用户回退为 "anonymous"；无上下文回退为 "system"。
     */
    public String resolveCurrentActor() {
        try {
            Authentication auth = SecurityContextHolder.getContext() != null ? SecurityContextHolder.getContext().getAuthentication() : null;
            if (auth != null && auth.isAuthenticated()) {
                String username = auth.getName();
                if (username == null) return "system";
                if ("anonymousUser".equalsIgnoreCase(username)) return "anonymous";
                Optional<User> uOpt = userRepository.findByUsername(username);
                if (uOpt.isPresent()) {
                    User u = uOpt.get();
                    String dn = u.getDisplayName();
                    if (dn != null && !dn.isBlank()) return dn;
                    return u.getUsername();
                }
                return username;
            }
        } catch (Exception ignored) { }
        return "system";
    }

    public List<OperationLog> query(Long equipmentId, String action, LocalDateTime from, LocalDateTime to) {
        Specification<OperationLog> spec = Specification.where(null);
        if (equipmentId != null) {
            spec = spec.and((root, q, cb) -> cb.and(
                    cb.equal(root.get("entityType"), "Equipment"),
                    cb.equal(root.get("entityId"), equipmentId)
            ));
        }
        if (action != null && !action.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("action"), action));
        }
        if (from != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        }
        if (to != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to));
        }
        return operationLogRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    /**
     * 通用查询：可按任意实体类型与实体ID过滤，并按时间范围与动作过滤，默认按时间倒序。
     */
    public List<OperationLog> queryByEntity(String entityType, Long entityId, String action, LocalDateTime from, LocalDateTime to) {
        Specification<OperationLog> spec = Specification.where(null);
        if (entityType != null && !entityType.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("entityType"), entityType));
        }
        if (entityId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("entityId"), entityId));
        }
        if (action != null && !action.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("action"), action));
        }
        if (from != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        }
        if (to != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to));
        }
        return operationLogRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public OperationLog latest(String entityType, Long entityId, String action) {
        if (entityType == null || entityType.isBlank() || entityId == null) return null;
        if (action != null && !action.isBlank()) {
            return operationLogRepository
                    .findTopByEntityTypeAndEntityIdAndActionOrderByCreatedAtDesc(entityType, entityId, action)
                    .orElse(null);
        }
        return operationLogRepository
                .findTopByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId)
                .orElse(null);
    }
}