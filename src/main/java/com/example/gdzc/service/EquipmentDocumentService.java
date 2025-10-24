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
import java.util.List;

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
        // 校验：是否选择文件、文件名有效、类型与大小
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空或未选择文件");
        }
        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) {
            throw new IllegalArgumentException("文件名无效");
        }
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.') + 1).toLowerCase() : "";
        List<String> allowed = List.of("pdf","doc","docx","xls","xlsx","ppt","pptx","jpg","jpeg","png","gif","svg","txt","zip");
        if (!allowed.contains(ext)) {
            throw new IllegalArgumentException("不支持的文件类型：" + ext + "，允许类型：" + String.join(", ", allowed));
        }
        long maxSize = 20L * 1024 * 1024; // 20MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小超过 20MB 上限");
        }

        // 确保上传目录存在（使用绝对路径，避免相对路径在嵌入式 Tomcat 下解析到临时工作目录）
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        // 保存文件到本地
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path target = dir.resolve(filename);
        file.transferTo(target.toFile());
        // 生成 URL（通过静态资源映射 /uploads/** 提供访问）
        String url = "/uploads/" + filename;
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