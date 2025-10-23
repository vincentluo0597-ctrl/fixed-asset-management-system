package com.example.gdzc.service;

import com.example.gdzc.domain.FaultCase;
import com.example.gdzc.repository.FaultCaseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {
    private final FaultCaseRepository faultCaseRepository;
    private final OperationLogService operationLogService;

    public Page<FaultCase> page(Long equipmentId, String keyword, Pageable pageable) {
        Specification<FaultCase> spec = Specification.where(null);
        if (equipmentId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("equipmentId"), equipmentId));
        }
        if (keyword != null && !keyword.isBlank()) {
            String like = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("phenomenon")), like),
                    cb.like(cb.lower(root.get("cause")), like),
                    cb.like(cb.lower(root.get("solution")), like),
                    cb.like(cb.lower(root.get("tags")), like)
            ));
        }
        return faultCaseRepository.findAll(spec, pageable);
    }

    @Transactional
    public FaultCase create(FaultCase faultCase) {
        FaultCase saved = faultCaseRepository.save(faultCase);
        operationLogService.logWithCurrentUser("FaultCase", saved.getId(), "CREATE", "新增故障案例：" + saved.getTitle());
        return saved;
    }
}