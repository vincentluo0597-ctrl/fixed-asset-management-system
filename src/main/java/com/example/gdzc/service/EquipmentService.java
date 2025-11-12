package com.example.gdzc.service;

import com.example.gdzc.domain.Equipment;
import com.example.gdzc.domain.EquipmentStatus;
import com.example.gdzc.dto.EquipmentDTO;
import com.example.gdzc.repository.EquipmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EquipmentService {
    /**
     * 设备领域服务：负责设备的持久化、派生字段计算（如保修到期、当前价值）、以及操作日志记录。
     */
    private final EquipmentRepository equipmentRepository;
    private final OperationLogService operationLogService;

    public List<Equipment> list() {
        return equipmentRepository.findAll();
    }

    public Page<Equipment> page(Pageable pageable) {
        return equipmentRepository.findAll(pageable);
    }

    public Page<Equipment> page(Specification<Equipment> spec, Pageable pageable) {
        return equipmentRepository.findAll(spec, pageable);
    }

    /**
     * 根据动态条件筛选返回全部设备列表（不分页），用于导出等场景。
     */
    public List<Equipment> list(Specification<Equipment> spec) {
        return equipmentRepository.findAll(spec);
    }

    public Optional<Equipment> findById(Long id) {
        return equipmentRepository.findById(id);
    }

    @Transactional
    public Equipment create(EquipmentDTO dto) {
        // 输入校验与派生字段计算：保修到期、初始价值
        String name = dto.getName() != null ? dto.getName().trim() : null;
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("设备名称不能为空");
        }
        
        // Calculate warranty expiry date if warranty period is provided
        LocalDate warrantyExpiryDate = dto.getWarrantyExpiryDate();
        if (warrantyExpiryDate == null && dto.getPurchaseDate() != null && dto.getWarrantyPeriodMonths() != null) {
            warrantyExpiryDate = dto.getPurchaseDate().plusMonths(dto.getWarrantyPeriodMonths());
        }
        
        // Calculate current value if depreciation rate is provided
        BigDecimal currentValue = dto.getCurrentValue();
        if (currentValue == null && dto.getPurchasePrice() != null) {
            currentValue = dto.getPurchasePrice(); // Default to purchase price
        }
        
        Equipment equipment = Equipment.builder()
                .name(name)
                .model(dto.getModel())
                .specifications(dto.getSpecifications())
                .brand(dto.getBrand())
                .categoryId(dto.getCategoryId())
                .supplierId(dto.getSupplierId())
                .purchaseContractId(dto.getPurchaseContractId())
                .technicalParameters(dto.getTechnicalParameters())
                .purchasePrice(dto.getPurchasePrice())
                .purchaseDate(dto.getPurchaseDate())
                .source(dto.getSource())
                .serialNumber(dto.getSerialNumber())
                .assetNumber(dto.getAssetNumber())
                .warrantyPeriodMonths(dto.getWarrantyPeriodMonths())
                .warrantyExpiryDate(warrantyExpiryDate)
                .imageUrls(dto.getImageUrls())
                .documentUrls(dto.getDocumentUrls())
                .manualUrl(dto.getManualUrl())
                .keeperId(dto.getKeeperId())
                .locationId(dto.getLocationId())
                .status(dto.getStatus())
                .manufactureDate(dto.getManufactureDate())
                .serviceLifeYears(dto.getServiceLifeYears())
                .depreciationRate(dto.getDepreciationRate())
                .currentValue(currentValue)
                .quantity(dto.getQuantity() != null ? dto.getQuantity() : 1)
                .remarks(dto.getRemarks())
                .build();
        Equipment saved = equipmentRepository.save(equipment);
        // 写入操作日志，记录当前登录用户与动作
        operationLogService.logWithCurrentUser("Equipment", saved.getId(), "CREATE", "创建设备: " + saved.getName());
        return saved;
    }
    
    @Transactional
    public Equipment update(Long id, EquipmentDTO dto) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在"));
        
        String name = dto.getName() != null ? dto.getName().trim() : null;
        if (StringUtils.hasText(name)) {
            equipment.setName(name);
        }
        
        // Update all fields
        equipment.setModel(dto.getModel());
        equipment.setSpecifications(dto.getSpecifications());
        equipment.setBrand(dto.getBrand());
        equipment.setCategoryId(dto.getCategoryId());
        equipment.setSupplierId(dto.getSupplierId());
        equipment.setPurchaseContractId(dto.getPurchaseContractId());
        equipment.setTechnicalParameters(dto.getTechnicalParameters());
        equipment.setPurchasePrice(dto.getPurchasePrice());
        equipment.setPurchaseDate(dto.getPurchaseDate());
        equipment.setSource(dto.getSource());
        equipment.setSerialNumber(dto.getSerialNumber());
        equipment.setAssetNumber(dto.getAssetNumber());
        equipment.setWarrantyPeriodMonths(dto.getWarrantyPeriodMonths());
        equipment.setWarrantyExpiryDate(dto.getWarrantyExpiryDate());
        equipment.setImageUrls(dto.getImageUrls());
        equipment.setDocumentUrls(dto.getDocumentUrls());
        equipment.setManualUrl(dto.getManualUrl());
        equipment.setKeeperId(dto.getKeeperId());
        equipment.setLocationId(dto.getLocationId());
        equipment.setStatus(dto.getStatus());
        equipment.setManufactureDate(dto.getManufactureDate());
        equipment.setServiceLifeYears(dto.getServiceLifeYears());
        equipment.setDepreciationRate(dto.getDepreciationRate());
        equipment.setCurrentValue(dto.getCurrentValue());
        equipment.setRemarks(dto.getRemarks());
        // 若请求未传数量，则保留原值；传了则更新
        if (dto.getQuantity() != null) {
            equipment.setQuantity(dto.getQuantity());
        }
        
        Equipment saved = equipmentRepository.save(equipment);
        // 写入操作日志
        operationLogService.logWithCurrentUser("Equipment", saved.getId(), "UPDATE", "更新设备: " + saved.getName());
        return saved;
    }
    
    @Transactional
    public void delete(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在"));
        equipmentRepository.delete(equipment);
        // 写入操作日志
        operationLogService.logWithCurrentUser("Equipment", id, "DELETE", "删除设备: " + equipment.getName());
    }
    
    public List<Equipment> findByStatus(EquipmentStatus status) {
        return equipmentRepository.findByStatus(status);
    }
    
    public List<Equipment> findByLocationId(Long locationId) {
        return equipmentRepository.findByLocationId(locationId);
    }
    
    public List<Equipment> findByKeeperId(Long keeperId) {
        return equipmentRepository.findByKeeperId(keeperId);
    }
}
