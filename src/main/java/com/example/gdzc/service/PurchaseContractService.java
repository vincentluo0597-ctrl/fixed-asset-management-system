package com.example.gdzc.service;

import com.example.gdzc.domain.ContractStatus;
import com.example.gdzc.domain.PurchaseContract;
import com.example.gdzc.repository.PurchaseContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PurchaseContractService {
    
    @Autowired
    private PurchaseContractRepository purchaseContractRepository;
    
    public Page<PurchaseContract> findAll(Pageable pageable) {
        return purchaseContractRepository.findAll(pageable);
    }
    
    public List<PurchaseContract> findActiveContracts() {
        return purchaseContractRepository.findByStatusNot(ContractStatus.ARCHIVED);
    }
    
    public List<PurchaseContract> findByStatus(ContractStatus status) {
        return purchaseContractRepository.findByStatus(status);
    }
    
    public List<PurchaseContract> findBySupplierId(Long supplierId) {
        return purchaseContractRepository.findBySupplierId(supplierId);
    }
    
    public Optional<PurchaseContract> findById(Long id) {
        return purchaseContractRepository.findById(id);
    }
    
    public Optional<PurchaseContract> findByContractNumber(String contractNumber) {
        return purchaseContractRepository.findByContractNumber(contractNumber);
    }
    
    public Page<PurchaseContract> searchByKeyword(String keyword, Pageable pageable) {
        return purchaseContractRepository.findByKeywordContainingIgnoreCase(keyword, pageable);
    }
    
    @Transactional
    public PurchaseContract create(PurchaseContract contract) {
        // 验证合同编号唯一性
        if (purchaseContractRepository.existsByContractNumber(contract.getContractNumber())) {
            throw new IllegalArgumentException("合同编号已存在: " + contract.getContractNumber());
        }
        
        // 设置默认值
        if (contract.getStatus() == null) {
            contract.setStatus(ContractStatus.DRAFT);
        }
        if (contract.getCreatedAt() == null) {
            contract.setCreatedAt(LocalDateTime.now());
        }
        contract.setUpdatedAt(LocalDateTime.now());
        
        return purchaseContractRepository.save(contract);
    }
    
    @Transactional
    public PurchaseContract update(Long id, PurchaseContract contract) {
        PurchaseContract existing = purchaseContractRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("采购合同不存在: " + id));
        
        // 验证合同编号唯一性（排除当前记录）
        Optional<PurchaseContract> byNumber = purchaseContractRepository.findByContractNumber(contract.getContractNumber());
        if (byNumber.isPresent() && !byNumber.get().getId().equals(id)) {
            throw new IllegalArgumentException("合同编号已存在: " + contract.getContractNumber());
        }
        
        // 更新字段
        existing.setContractNumber(contract.getContractNumber());
        existing.setContractName(contract.getContractName());
        existing.setSupplierId(contract.getSupplierId());
        existing.setContractAmount(contract.getContractAmount());
        existing.setContractDate(contract.getContractDate());
        existing.setStartDate(contract.getStartDate());
        existing.setEndDate(contract.getEndDate());
        existing.setWarrantyPeriodMonths(contract.getWarrantyPeriodMonths());
        existing.setStatus(contract.getStatus());
        existing.setPaymentTerms(contract.getPaymentTerms());
        existing.setDeliveryDate(contract.getDeliveryDate());
        existing.setDeliveryAddress(contract.getDeliveryAddress());
        existing.setContactPerson(contract.getContactPerson());
        existing.setContactPhone(contract.getContactPhone());
        existing.setContractFileUrl(contract.getContractFileUrl());
        existing.setRemarks(contract.getRemarks());
        existing.setUpdatedAt(LocalDateTime.now());
        
        return purchaseContractRepository.save(existing);
    }
    
    @Transactional
    public void updateStatus(Long id, ContractStatus status) {
        PurchaseContract contract = purchaseContractRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("采购合同不存在: " + id));
        
        contract.setStatus(status);
        contract.setUpdatedAt(LocalDateTime.now());
        purchaseContractRepository.save(contract);
    }
    
    @Transactional
    public void archiveContract(Long id) {
        PurchaseContract contract = purchaseContractRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("采购合同不存在: " + id));
        
        contract.setStatus(ContractStatus.ARCHIVED);
        contract.setUpdatedAt(LocalDateTime.now());
        purchaseContractRepository.save(contract);
    }
    
    public List<PurchaseContract> findExpiringContracts(int daysBeforeExpiry) {
        LocalDate expiryDate = LocalDate.now().plusDays(daysBeforeExpiry);
        return purchaseContractRepository.findByEndDateLessThanEqual(expiryDate);
    }
    
    public BigDecimal calculateTotalContractAmountBySupplier(Long supplierId) {
        List<PurchaseContract> contracts = purchaseContractRepository.findBySupplierId(supplierId);
        return contracts.stream()
                .map(PurchaseContract::getContractAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public long countContractsByStatus(ContractStatus status) {
        return purchaseContractRepository.countByStatus(status);
    }
    
    public long countContractsBySupplier(Long supplierId) {
        return purchaseContractRepository.countBySupplierId(supplierId);
    }
}