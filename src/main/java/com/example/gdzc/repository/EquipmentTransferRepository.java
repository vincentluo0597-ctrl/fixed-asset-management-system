package com.example.gdzc.repository;

import com.example.gdzc.domain.EquipmentTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentTransferRepository extends JpaRepository<EquipmentTransfer, Long> {
    
    // 根据设备ID查询调用记录（不带关联加载，保留原方法）
    List<EquipmentTransfer> findByEquipmentIdOrderByTransferDateDesc(Long equipmentId);
    
    // 分页查询设备的调用记录
    Page<EquipmentTransfer> findByEquipmentIdOrderByTransferDateDesc(Long equipmentId, Pageable pageable);
    
    // 查询指定用户的调用记录（创建者）（不带关联加载，保留原方法）
    Page<EquipmentTransfer> findByCreatedByOrderByTransferDateDesc(String createdBy, Pageable pageable);
    
    // 查询指定状态的调用记录
    Page<EquipmentTransfer> findByStatusOrderByTransferDateDesc(EquipmentTransfer.TransferStatus status, Pageable pageable);
    
    // 查询指定类型的调用记录
    Page<EquipmentTransfer> findByTransferTypeOrderByTransferDateDesc(EquipmentTransfer.TransferType transferType, Pageable pageable);
    
    // 查询指定时间段内的调用记录
    @Query("SELECT et FROM EquipmentTransfer et WHERE et.transferDate BETWEEN :startDate AND :endDate ORDER BY et.transferDate DESC")
    Page<EquipmentTransfer> findByTransferDateBetween(@Param("startDate") LocalDateTime startDate, 
                                                     @Param("endDate") LocalDateTime endDate, 
                                                     Pageable pageable);
    
    // 查询设备的当前活跃调用记录
    Optional<EquipmentTransfer> findByEquipmentIdAndStatus(Long equipmentId, EquipmentTransfer.TransferStatus status);
    
    // 查询指定位置相关的调用记录
    @Query("SELECT et FROM EquipmentTransfer et WHERE et.fromLocation.id = :locationId OR et.toLocation.id = :locationId ORDER BY et.transferDate DESC")
    Page<EquipmentTransfer> findByLocationId(@Param("locationId") Long locationId, Pageable pageable);
    
    // 查询指定用户相关的调用记录（作为from_user或to_user）
    @Query("SELECT et FROM EquipmentTransfer et WHERE et.fromUser.id = :userId OR et.toUser.id = :userId ORDER BY et.transferDate DESC")
    Page<EquipmentTransfer> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    // 统计指定设备的调用次数
    long countByEquipmentId(Long equipmentId);
    
    // 统计指定用户的调用次数
    long countByCreatedBy(String createdBy);
    
    // 统计指定状态的调用次数
    long countByStatus(EquipmentTransfer.TransferStatus status);
    
    // 查询需要归还的设备（借出状态且预期归还日期已过）
    @EntityGraph(attributePaths = {"equipment", "fromLocation", "toLocation", "fromUser", "toUser"})
    @Query("SELECT et FROM EquipmentTransfer et WHERE et.transferType = 'LOAN' AND et.status = 'ACTIVE' AND et.expectedReturnDate < :currentDate")
    List<EquipmentTransfer> findOverdueLoans(@Param("currentDate") LocalDateTime currentDate);

    // 带分页的全量查询，加载关联对象，避免懒加载序列化异常（自定义方法，确保注解生效）
    @EntityGraph(attributePaths = {"equipment", "fromLocation", "toLocation", "fromUser", "toUser"})
    @Query("SELECT et FROM EquipmentTransfer et")
    Page<EquipmentTransfer> findAllWithAssociations(Pageable pageable);

    // 非分页的全量查询，加载关联对象，避免懒加载序列化异常（自定义方法，确保注解生效）
    @EntityGraph(attributePaths = {"equipment", "fromLocation", "toLocation", "fromUser", "toUser"})
    @Query("SELECT et FROM EquipmentTransfer et")
    List<EquipmentTransfer> findAllWithAssociations();

    // 根据设备ID查询调用记录（加载关联对象）
    @EntityGraph(attributePaths = {"equipment", "fromLocation", "toLocation", "fromUser", "toUser"})
    @Query("SELECT et FROM EquipmentTransfer et WHERE et.equipment.id = :equipmentId ORDER BY et.transferDate DESC")
    List<EquipmentTransfer> findByEquipmentIdOrderByTransferDateDescWithAssociations(@Param("equipmentId") Long equipmentId);

    // 根据创建者查询调用记录（加载关联对象）
    @EntityGraph(attributePaths = {"equipment", "fromLocation", "toLocation", "fromUser", "toUser"})
    @Query("SELECT et FROM EquipmentTransfer et WHERE et.createdBy = :createdBy ORDER BY et.transferDate DESC")
    Page<EquipmentTransfer> findByCreatedByOrderByTransferDateDescWithAssociations(@Param("createdBy") String createdBy, Pageable pageable);

    // 根据状态查询调用记录（加载关联对象，分页）
    @EntityGraph(attributePaths = {"equipment", "fromLocation", "toLocation", "fromUser", "toUser"})
    @Query("SELECT et FROM EquipmentTransfer et WHERE et.status = :status ORDER BY et.transferDate DESC")
    Page<EquipmentTransfer> findByStatusOrderByTransferDateDescWithAssociations(@Param("status") EquipmentTransfer.TransferStatus status, Pageable pageable);

    // 根据类型查询调用记录（加载关联对象，分页）
    @EntityGraph(attributePaths = {"equipment", "fromLocation", "toLocation", "fromUser", "toUser"})
    @Query("SELECT et FROM EquipmentTransfer et WHERE et.transferType = :transferType ORDER BY et.transferDate DESC")
    Page<EquipmentTransfer> findByTransferTypeOrderByTransferDateDescWithAssociations(@Param("transferType") EquipmentTransfer.TransferType transferType, Pageable pageable);

    // 根据ID查询（加载关联对象），用于保存后返回完整对象，避免序列化懒加载异常
    @EntityGraph(attributePaths = {"equipment", "fromLocation", "toLocation", "fromUser", "toUser"})
    @Query("SELECT et FROM EquipmentTransfer et WHERE et.id = :id")
    Optional<EquipmentTransfer> findByIdWithAssociations(@Param("id") Long id);
}