package com.example.gdzc.service;

import com.example.gdzc.domain.EquipmentDocument;
import com.example.gdzc.repository.EquipmentDocumentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class EquipmentDocumentService {
    private final EquipmentDocumentRepository equipmentDocumentRepository;
    private final OperationLogService operationLogService;

    @Value("${upload.dir:uploads}")
    private String uploadDir;

    public Page<EquipmentDocument> page(Long equipmentId, Pageable pageable) {
        Pageable effective = pageable == null ? PageRequest.of(0, 20) : pageable;
        if (equipmentId == null) return equipmentDocumentRepository.findAll(effective);
        return equipmentDocumentRepository.findAll((root, q, cb) -> cb.equal(root.get("equipmentId"), equipmentId), effective);
    }

    @Transactional
    public EquipmentDocument upload(Long equipmentId, String title, EquipmentDocument.DocType docType, MultipartFile file, String uploadedBy) throws IOException {
        // 确保上传目录存在
        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        // 保存文件到本地
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path target = dir.resolve(filename);
        file.transferTo(target.toFile());
        // 生成 URL（在 Spring Boot 中可通过静态资源映射提供访问）
        String url = "/" + uploadDir + "/" + filename;
        EquipmentDocument doc = EquipmentDocument.builder()
                .equipmentId(equipmentId)
                .title(title)
                .docType(docType)
                .fileUrl(url)
                .uploadedBy(uploadedBy)
                .build();
        EquipmentDocument saved = equipmentDocumentRepository.save(doc);
        operationLogService.logWithCurrentUser("EquipmentDocument", saved.getId(), "UPLOAD", "上传设备文档：" + title);
        return saved;
    }
}