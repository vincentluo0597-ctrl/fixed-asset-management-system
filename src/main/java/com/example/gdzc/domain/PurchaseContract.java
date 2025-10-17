package com.example.gdzc.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_contracts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_number", nullable = false, length = 100, unique = true)
    @NotBlank(message = "合同编号不能为空")
    private String contractNumber; // 合同编号

    @Column(name = "contract_name", nullable = false, length = 200)
    @NotBlank(message = "合同名称不能为空")
    private String contractName; // 合同名称

    @Column(name = "supplier_id", nullable = false)
    @NotNull(message = "供应商不能为空")
    private Long supplierId; // 供应商ID

    @Column(name = "contract_amount", precision = 12, scale = 2)
    private BigDecimal contractAmount; // 合同金额

    @Column(name = "contract_date")
    private LocalDate contractDate; // 合同签订日期

    @Column(name = "start_date")
    private LocalDate startDate; // 合同开始日期

    @Column(name = "end_date")
    private LocalDate endDate; // 合同结束日期

    @Column(name = "delivery_date")
    private LocalDate deliveryDate; // 交付日期

    @Column(name = "warranty_period_months")
    private Integer warrantyPeriodMonths; // 保修期（月）

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ContractStatus status = ContractStatus.DRAFT; // 合同状态

    @Column(name = "payment_terms", length = 500)
    private String paymentTerms; // 付款条款

    @Column(name = "delivery_address", length = 500)
    private String deliveryAddress; // 交付地址

    @Column(name = "contact_person", length = 100)
    private String contactPerson; // 联系人

    @Column(name = "contact_phone", length = 50)
    private String contactPhone; // 联系电话

    @Column(name = "contract_file_url", length = 500)
    private String contractFileUrl; // 合同文件URL

    @Column(name = "remarks", length = 1000)
    private String remarks; // 备注

    @Column(name = "created_by")
    private Long createdBy; // 创建人

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now(); // 创建时间

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 更新时间
}