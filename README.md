# Java_gdzc 资产管理系统（局域网办公增强版）

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)
[![Code Copyright](https://img.shields.io/badge/%C2%A9-2025--FZTBU__xxzx-green.svg)](https://gitee.com/luo-yinhe/fixed-asset-management-system)

本项目基于 Spring Boot 3，聚焦局域网办公场景下的设备/耗材/备件/位置/供应商管理，提供易用的后台管理页与完整的接口文档。近期已增强全局搜索、仪表盘指标、设备 CSV 导入与动态库存 UI。

> 许可证：GNU Affero General Public License v3.0（AGPL-3.0）  
> 著作权：© 2025 FZTBU_xxzx  
> 若您修改本程序并对外提供服务，必须同样以 AGPL-3.0 公开完整源码。

## 主要功能
- 设备管理：分页查询、组合筛选（状态/分类/供应商/位置/保管人/关键字）、CSV 导出与导入
- 耗材管理：低库存筛选、登记更换（自动计算到期）、库存统计卡片、风险徽章与仪表条
- 备件管理：分页查询、按设备型号关联、库存调整、库存统计与风险展示
- 位置管理：层级树与基础维护
- 供应商管理：分页/搜索与基础维护
- 操作日志：分页筛选与 CSV 导出；设备页显示最近操作者与时间
- 全局搜索：设备/供应商/位置 TopN 概览，仪表盘快速跳转
- 仪表盘指标：设备总数、按状态分布、保修到期 Top10、近7天操作趋势
- API 文档：SpringDoc 提供 `swagger-ui`

## 技术栈
- Java 17、Spring Boot 3.x
- Spring Data JPA、Spring Security、Validation
- Thymeleaf 模板 + 原生样式（`admin.css`）
- Flyway 数据库版本迁移
- MySQL 8（开发也可用 H2）
- Jackson Hibernate 模块（jakarta）

## 目录结构（关键）
- 控制器：`src/main/java/com/example/gdzc/controller`
- 服务：`src/main/java/com/example/gdzc/service`
- 仓库：`src/main/java/com/example/gdzc/repository`
- 模板页：`src/main/resources/templates/admin/...`
- 静态样式：`src/main/resources/static/css/admin.css`
- 数据迁移：`src/main/resources/db/migration`

## 快速启动
1. 启动 MySQL 并创建数据库 `gdzc`（utf8mb4）。
2. 检查 `src/main/resources/application.properties` 的连接信息（默认本机 root/123456）。
3. 构建：`./mvnw.cmd -DskipTests package`
4. 运行：`./mvnw.cmd -DskipTests spring-boot:run`
5. 访问：
   - 后台首页：`http://localhost:13697/admin`
   - 接口文档：`http://localhost:13697/swagger-ui/index.html`
   - 健康检查：`http://localhost:13697/actuator/health`

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

### 操作日志接口（说明）
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
- 字符集：统一使用 utf8mb4；连接串包含 `useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai`
- 端口占用：运行时可通过 `--server.port` 调整

## 部署建议
- MySQL 8 + Java 17 + 可执行 JAR
- 外部化配置注入敏感信息
- Actuator 监控与日志收集
- Flyway 管理迁移

## 后续升级方向
- UI/交互：响应式主题、图表看板、暗色模式
- 业务：审批流程、附件/图片、通知提醒、批量操作与 Excel 导入导出
- 权限与安全：角色细分、基于资源的权限控制、SSO/OIDC
- 可观测性与性能：查询优化、缓存（Redis）、压测与指标监控
- DevOps：Docker、CI/CD、自动化测试

## 版权与协议
- 该项目用于企业内部/学习参考，版权与著作权归属 @FTBU_xxzx（All rights reserved）

> 说明：本项目的源代码版权与著作权归属 @FTBU_xxzx。未经授权不得以任何形式复制、分发或用于商业用途。如需授权或合作，请联系 @FTBU_xxzx。
@author Vincent Luo
## CSV 导入说明（设备）
- 建议先通过“设备管理”页面点击“导出 CSV”获取模板，再编辑后导入
- 支持的表头（顺序不限）：
  `ID, 名称, 状态, 品牌, 型号, 数量, 分类ID, 供应商ID, 位置ID, 保管人ID, 采购日期, 资产编号, 序列号`
- 上传：设备管理页“导入 CSV”按钮，或接口 `POST /api/equipment/import`
