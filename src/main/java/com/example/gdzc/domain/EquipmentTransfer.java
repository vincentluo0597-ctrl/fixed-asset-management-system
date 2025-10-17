package com.example.gdzc.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_transfers")
public class EquipmentTransfer {
    
    public enum TransferType {
        TRANSFER("调拨"),
        LOAN("借出"),
        RETURN("归还"),
        MOVE("移动");
        
        private final String description;
        
        TransferType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum TransferStatus {
        ACTIVE("进行中"),
        COMPLETED("已完成"),
        CANCELLED("已取消");
        
        private final String description;
        
        TransferStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_type", nullable = false, length = 50)
    private TransferType transferType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_location_id")
    private Location fromLocation;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_location_id")
    private Location toLocation;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id")
    private User fromUser;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id")
    private User toUser;
    
    @Column(name = "transfer_reason", nullable = false, columnDefinition = "TEXT")
    private String transferReason;
    
    @Column(name = "transfer_date", nullable = false)
    private LocalDateTime transferDate;
    
    @Column(name = "expected_return_date")
    private LocalDateTime expectedReturnDate;
    
    @Column(name = "actual_return_date")
    private LocalDateTime actualReturnDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransferStatus status = TransferStatus.ACTIVE;
    
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (transferDate == null) {
            transferDate = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Equipment getEquipment() {
        return equipment;
    }
    
    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }
    
    public TransferType getTransferType() {
        return transferType;
    }
    
    public void setTransferType(TransferType transferType) {
        this.transferType = transferType;
    }
    
    public Location getFromLocation() {
        return fromLocation;
    }
    
    public void setFromLocation(Location fromLocation) {
        this.fromLocation = fromLocation;
    }
    
    public Location getToLocation() {
        return toLocation;
    }
    
    public void setToLocation(Location toLocation) {
        this.toLocation = toLocation;
    }
    
    public User getFromUser() {
        return fromUser;
    }
    
    public void setFromUser(User fromUser) {
        this.fromUser = fromUser;
    }
    
    public User getToUser() {
        return toUser;
    }
    
    public void setToUser(User toUser) {
        this.toUser = toUser;
    }
    
    public String getTransferReason() {
        return transferReason;
    }
    
    public void setTransferReason(String transferReason) {
        this.transferReason = transferReason;
    }
    
    public LocalDateTime getTransferDate() {
        return transferDate;
    }
    
    public void setTransferDate(LocalDateTime transferDate) {
        this.transferDate = transferDate;
    }
    
    public LocalDateTime getExpectedReturnDate() {
        return expectedReturnDate;
    }
    
    public void setExpectedReturnDate(LocalDateTime expectedReturnDate) {
        this.expectedReturnDate = expectedReturnDate;
    }
    
    public LocalDateTime getActualReturnDate() {
        return actualReturnDate;
    }
    
    public void setActualReturnDate(LocalDateTime actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
    }
    
    public TransferStatus getStatus() {
        return status;
    }
    
    public void setStatus(TransferStatus status) {
        this.status = status;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}