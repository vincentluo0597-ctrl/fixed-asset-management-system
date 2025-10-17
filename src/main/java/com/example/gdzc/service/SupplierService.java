package com.example.gdzc.service;

import com.example.gdzc.domain.Supplier;
import com.example.gdzc.domain.SupplierType;
import com.example.gdzc.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SupplierService {
    
    @Autowired
    private SupplierRepository supplierRepository;
    
    public Page<Supplier> findAll(Pageable pageable) {
        return supplierRepository.findAll(pageable);
    }
    
    public List<Supplier> findActiveSuppliers() {
        return supplierRepository.findByIsActiveTrue();
    }
    
    public List<Supplier> findByType(SupplierType type) {
        return supplierRepository.findByTypeAndIsActiveTrue(type);
    }
    
    public Optional<Supplier> findById(Long id) {
        return supplierRepository.findById(id);
    }
    
    public Optional<Supplier> findByCode(String code) {
        return supplierRepository.findByCode(code);
    }
    
    public Page<Supplier> searchByKeyword(String keyword, Pageable pageable) {
        return supplierRepository.searchByKeyword(keyword, pageable);
    }
    
    @Transactional
    public Supplier create(Supplier supplier) {
        // 验证编码唯一性
        if (supplierRepository.existsByCode(supplier.getCode())) {
            throw new IllegalArgumentException("供应商编码已存在: " + supplier.getCode());
        }
        
        // 设置默认值
        if (supplier.getCreditRating() == null) {
            supplier.setCreditRating("A");
        }
        if (supplier.getIsActive() == null) {
            supplier.setIsActive(true);
        }
        
        supplier.setCreatedAt(LocalDateTime.now());
        supplier.setUpdatedAt(LocalDateTime.now());
        
        return supplierRepository.save(supplier);
    }
    
    @Transactional
    public Supplier update(Long id, Supplier supplier) {
        Supplier existing = supplierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("供应商不存在: " + id));
        
        // 验证编码唯一性（排除当前记录）
        Optional<Supplier> byCode = supplierRepository.findByCode(supplier.getCode());
        if (byCode.isPresent() && !byCode.get().getId().equals(id)) {
            throw new IllegalArgumentException("供应商编码已存在: " + supplier.getCode());
        }
        
        // 更新字段
        existing.setCode(supplier.getCode());
        existing.setName(supplier.getName());
        existing.setContactPerson(supplier.getContactPerson());
        existing.setPhone(supplier.getPhone());
        existing.setEmail(supplier.getEmail());
        existing.setAddress(supplier.getAddress());
        existing.setWebsite(supplier.getWebsite());
        existing.setType(supplier.getType());
        existing.setCreditRating(supplier.getCreditRating());
        existing.setIsActive(supplier.getIsActive());
        existing.setRemarks(supplier.getRemarks());
        existing.setUpdatedAt(LocalDateTime.now());
        
        return supplierRepository.save(existing);
    }
    
    @Transactional
    public void delete(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("供应商不存在: " + id));
        
        // 逻辑删除
        supplier.setIsActive(false);
        supplier.setUpdatedAt(LocalDateTime.now());
        supplierRepository.save(supplier);
    }
    
    @Transactional
    public void activate(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("供应商不存在: " + id));
        
        supplier.setIsActive(true);
        supplier.setUpdatedAt(LocalDateTime.now());
        supplierRepository.save(supplier);
    }
    
    @Transactional
    public void updateCreditRating(Long id, String creditRating) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("供应商不存在: " + id));
        
        supplier.setCreditRating(creditRating);
        supplier.setUpdatedAt(LocalDateTime.now());
        supplierRepository.save(supplier);
    }
}