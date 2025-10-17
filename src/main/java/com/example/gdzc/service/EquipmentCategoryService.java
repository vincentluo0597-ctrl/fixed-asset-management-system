package com.example.gdzc.service;

import com.example.gdzc.domain.EquipmentCategory;
import com.example.gdzc.domain.EquipmentType;
import com.example.gdzc.repository.EquipmentCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EquipmentCategoryService {
    
    @Autowired
    private EquipmentCategoryRepository equipmentCategoryRepository;
    
    public List<EquipmentCategory> findAll() {
        return equipmentCategoryRepository.findAll();
    }
    
    public List<EquipmentCategory> findActiveCategories() {
        return equipmentCategoryRepository.findByIsActiveTrue();
    }
    
    public List<EquipmentCategory> findRootCategories() {
        return equipmentCategoryRepository.findByParentIdIsNullAndIsActiveTrueOrderBySortOrder();
    }
    
    public List<EquipmentCategory> findByParentId(Long parentId) {
        return equipmentCategoryRepository.findByParentIdAndIsActiveTrueOrderBySortOrder(parentId);
    }
    
    public Optional<EquipmentCategory> findById(Long id) {
        return equipmentCategoryRepository.findById(id);
    }
    
    public Optional<EquipmentCategory> findByCode(String code) {
        return equipmentCategoryRepository.findByCode(code);
    }
    
    public List<EquipmentCategory> findByType(EquipmentType type) {
        return equipmentCategoryRepository.findByTypeAndIsActiveTrue(type);
    }
    
    @Transactional
    public EquipmentCategory create(EquipmentCategory category) {
        // 验证编码唯一性
        if (equipmentCategoryRepository.existsByCode(category.getCode())) {
            throw new IllegalArgumentException("分类编码已存在: " + category.getCode());
        }
        
        // 设置默认值
        if (category.getLevel() == null) {
            category.setLevel(calculateLevel(category.getParentId()));
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(999);
        }
        if (category.getIsActive() == null) {
            category.setIsActive(true);
        }
        
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        
        return equipmentCategoryRepository.save(category);
    }
    
    @Transactional
    public EquipmentCategory update(Long id, EquipmentCategory category) {
        EquipmentCategory existing = equipmentCategoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("分类不存在: " + id));
        
        // 验证编码唯一性（排除当前记录）
        Optional<EquipmentCategory> byCode = equipmentCategoryRepository.findByCode(category.getCode());
        if (byCode.isPresent() && !byCode.get().getId().equals(id)) {
            throw new IllegalArgumentException("分类编码已存在: " + category.getCode());
        }
        
        // 更新字段
        existing.setCode(category.getCode());
        existing.setName(category.getName());
        existing.setParentId(category.getParentId());
        existing.setLevel(calculateLevel(category.getParentId()));
        existing.setDescription(category.getDescription());
        existing.setType(category.getType());
        existing.setSortOrder(category.getSortOrder());
        existing.setIsActive(category.getIsActive());
        existing.setRemarks(category.getRemarks());
        existing.setUpdatedAt(LocalDateTime.now());
        
        return equipmentCategoryRepository.save(existing);
    }
    
    @Transactional
    public void delete(Long id) {
        EquipmentCategory category = equipmentCategoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("分类不存在: " + id));
        
        // 检查是否有子分类
        List<EquipmentCategory> children = equipmentCategoryRepository.findByParentId(id);
        if (!children.isEmpty()) {
            throw new IllegalArgumentException("该分类存在子分类，无法删除");
        }
        
        // 逻辑删除
        category.setIsActive(false);
        category.setUpdatedAt(LocalDateTime.now());
        equipmentCategoryRepository.save(category);
    }
    
    public List<EquipmentCategory> buildCategoryTree() {
        List<EquipmentCategory> allCategories = equipmentCategoryRepository.findByIsActiveTrue();
        return buildTree(allCategories);
    }
    
    public List<EquipmentCategory> buildCategoryTreeByType(EquipmentType type) {
        List<EquipmentCategory> allCategories = equipmentCategoryRepository.findByTypeAndIsActiveTrue(type);
        return buildTree(allCategories);
    }
    
    private List<EquipmentCategory> buildTree(List<EquipmentCategory> categories) {
        return categories.stream()
                .filter(cat -> cat.getParentId() == null)
                .map(root -> {
                    root.setChildren(getChildren(root.getId(), categories));
                    return root;
                })
                .sorted((a, b) -> {
                    int sortOrderCompare = Integer.compare(a.getSortOrder(), b.getSortOrder());
                    return sortOrderCompare != 0 ? sortOrderCompare : a.getName().compareTo(b.getName());
                })
                .collect(Collectors.toList());
    }
    
    private List<EquipmentCategory> getChildren(Long parentId, List<EquipmentCategory> categories) {
        return categories.stream()
                .filter(cat -> parentId.equals(cat.getParentId()))
                .map(child -> {
                    child.setChildren(getChildren(child.getId(), categories));
                    return child;
                })
                .sorted((a, b) -> {
                    int sortOrderCompare = Integer.compare(a.getSortOrder(), b.getSortOrder());
                    return sortOrderCompare != 0 ? sortOrderCompare : a.getName().compareTo(b.getName());
                })
                .collect(Collectors.toList());
    }
    
    private Integer calculateLevel(Long parentId) {
        if (parentId == null) {
            return 1;
        }
        Optional<EquipmentCategory> parent = equipmentCategoryRepository.findById(parentId);
        return parent.map(p -> p.getLevel() + 1).orElse(1);
    }
}