# 固资设备管理与调用系统（Java_gdzc）

本项目是一个以 Spring Boot 为基础的固资设备管理与调用系统雏形，提供设备调用（调拨/借出/归还/移动）全流程管理、分页与筛选、逾期提示、统计看板，以及基础的后台管理 UI（Thymeleaf + Bootstrap）。

## 主要功能
- 设备调用管理：支持创建、完成、取消等操作
- 调用类型与状态：TRANSFER/LOAN/RETURN/MOVE 以及 ACTIVE/COMPLETED/CANCELLED
- 分页与筛选：分页查询，并可按类型或状态过滤；支持“只看我的”视图
- 逾期提示：借出（LOAN）且未归还、超过预期归还日期的记录，列表行高亮
- 统计汇总：总调用数、进行中、逾期数量展示
- 后台页面：/admin/transfers 管理页面（带筛选、分页、创建模态框等）
- API 文档：集成 springdoc-openapi，可通过浏览器查看接口文档（路径见下文）

## 技术栈
- Java 17、Spring Boot 3.x
- Spring Data JPA、Spring Security、Validation
- Thymeleaf + Bootstrap 前端模板
- Flyway 数据库版本迁移
- H2（开发环境可用）/ MySQL（生产环境推荐）
- Jackson Hibernate 模块（jackson-datatype-hibernate5-jakarta）用于处理 JPA 代理序列化

## 目录结构（简要）
- src/main/java/com/example/gdzc/... 业务代码（Controller/Service/Repository/Domain 等）
- src/main/resources/templates/... 页面模板（如 transfers/index.html）
- src/main/resources/static/... 静态资源（如 logo.jpg）
- db/migration/... Flyway SQL 脚本（在 resources 下）
- pom.xml 项目依赖与构建配置

## 快速开始
1) 环境准备
- 安装 Java 17（JDK）
- 安装 Maven（建议 3.9+）
- 数据库（开发可用 H2；生产推荐 MySQL 8.x）

2) 构建与运行（Windows PowerShell 示例）
- 构建：
  - mvn -q -DskipTests package
- 运行（指定端口 13700，可按需调整）：
  - java -jar target\java-gdzc-0.0.1-SNAPSHOT.jar --server.port=13700
- 浏览器访问：
  - 后台管理页面：http://localhost:13700/admin/transfers
  - API 文档（springdoc）：http://localhost:13700/swagger-ui/index.html

3) 数据库配置（application.properties）
- 如使用 MySQL，请在配置文件中设置：
  - spring.datasource.url=jdbc:mysql://<host>:<port>/gdzc?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
  - spring.datasource.username=<你的用户名>
  - spring.datasource.password=<你的密码>
  - spring.flyway.enabled=true
  - spring.jpa.hibernate.ddl-auto=none
  - spring.jpa.show-sql=true
- 字符集建议：数据库与数据表统一使用 utf8mb4，避免中文乱码问题。

4) 初次运行与账号
- 系统集成了 Spring Security。请在数据层或初始化脚本中准备用户与角色；登录后可使用后台页面。
- 左上角 Logo：将图片放置在 src/main/resources/static/logo.jpg，前端页面会在侧边栏顶部显示。

## 前后端交互要点
- 分页与筛选接口：
  - GET /api/equipment-transfers/page?page=0&size=10
  - 支持可选筛选参数：type（如 LOAN、TRANSFER）、status（如 ACTIVE、COMPLETED）
    - 例如：/api/equipment-transfers/page?page=0&size=10&type=LOAN
    - 例如：/api/equipment-transfers/page?page=0&size=10&status=ACTIVE
  - 仅看我的：/api/equipment-transfers/my-transfers?page=0&size=10
- 创建调用：
  - POST /api/equipment-transfers
  - 请求体示例：
    {
      "equipmentId": 1,
      "transferType": "LOAN",
      "fromLocationId": 1,
      "toLocationId": 2,
      "fromUserId": 1,
      "toUserId": 2,
      "transferReason": "借出测试",
      "expectedReturnDate": "2025-10-20T00:00:00"
    }
- 完成/取消调用：
  - PUT /api/equipment-transfers/{id}/complete
  - PUT /api/equipment-transfers/{id}/cancel
- 为避免懒加载序列化问题，后端在 Repository 层使用了 @EntityGraph 的带关联查询方法，并在保存后返回完整对象；同时集成了 jackson-datatype-hibernate5-jakarta。

### 操作日志接口（新增）
- 分页与高级筛选：
  - GET /api/operation-logs/page
  - 参数：actor（包含匹配）、entityType、entityId、action、from、to、keyword、page、size、sort
  - 示例：/api/operation-logs/page?actor=张&entityType=Equipment&action=UPDATE&page=0&size=20&sort=createdAt,desc
- 最新记录：
  - GET /api/operation-logs/latest?entityType=Equipment&entityId=1[&action=CREATE]
  - 说明：按时间倒序返回单条最新日志；action 可选
- 导出 CSV：
  - GET /api/operation-logs/export
  - 参数与分页一致（不含 page/size），返回 CSV 文本流；字段列顺序为
    id,createdAt,actor,entityType,entityId,action,details
- 后台页面：/admin/operation-logs（Thymeleaf 模板，带筛选、分页、导出按钮）

## 常见问题与排查
- 中文编码：
  - 确保数据库为 utf8mb4，连接串包含 useUnicode=true 与 characterEncoding=utf8（或 utf8mb4）。
  - 前后端页面模板与响应头统一使用 UTF-8。
- 接口 500 异常（Hibernate Lazy）：
  - 使用带 @EntityGraph 的分页/查询方法；保存后通过 findByIdWithAssociations 返回带完整关联的实体，避免序列化代理问题。
- 端口占用：
  - 运行时可通过 --server.port 指定其他端口，如 13701。

## 部署建议
- 生产环境：MySQL 8.x + Java 17 + 已编译可执行 JAR
- 配置：将敏感信息（数据库账号、密码）通过环境变量或外部化配置注入
- 日志与监控：启用 Spring Boot Actuator，按需接入日志收集与监控系统
- 数据迁移：使用 Flyway 管理数据库结构变更

## 后续升级方向
- UI 与交互：更完善的主题与样式、响应式布局、暗色模式、图表看板
- 业务能力：审批流程、附件与图片、通知提醒（邮件/企业微信）、批量操作、导入导出（Excel/CSV）
- 权限与安全：角色细分、基于资源的权限控制、SSO/OIDC 集成
- 可观测性与性能：查询优化、缓存（Redis）、压测与指标监控
- DevOps：Docker 容器化、CI/CD 流水线、自动化测试与质量门禁

## 版权与协议
- 该项目用于企业内部/学习参考，版权与著作权归属 @FTBU_xxzx（All rights reserved）

> 说明：本项目的源代码版权与著作权归属 @FTBU_xxzx。未经授权不得以任何形式复制、分发或用于商业用途。如需授权或合作，请联系 @FTBU_xxzx。
@author Vincent Luo
