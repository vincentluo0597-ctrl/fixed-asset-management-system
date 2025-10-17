package com.example.gdzc.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "suppliers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 50, unique = true)
    @NotBlank(message = "供应商编码不能为空")
    private String code; // 供应商编码

    @Column(name = "name", nullable = false, length = 200)
    @NotBlank(message = "供应商名称不能为空")
    private String name; // 供应商名称

    @Column(name = "contact_person", length = 100)
    private String contactPerson; // 联系人

    @Column(name = "phone", length = 50)
    private String phone; // 联系电话

    @Column(name = "email", length = 100)
    private String email; // 邮箱

    @Column(name = "address", length = 500)
    private String address; // 地址

    @Column(name = "website", length = 200)
    private String website; // 网站

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20)
    private SupplierType type; // 供应商类型

    @Column(name = "credit_rating", length = 20)
    private String creditRating; // 信用评级

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true; // 是否启用

    @Column(name = "remarks", length = 1000)
    private String remarks; // 备注

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}