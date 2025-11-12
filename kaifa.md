快速预览地址（请在浏览器打开）

- 登录页： http://localhost:13697/login
- 管理首页： http://localhost:13697/admin
- 设备管理页： http://localhost:13697/admin/equipment
- Swagger 文档： http://localhost:13697/swagger-ui/index.html
默认登录账号

- 用户名：admin
- 密码：Admin@123
  （来自 DataInitializer，首次启动会自动创建）
如何联调和冒烟测试（建议步骤）

1. 1.
   登录系统
- 打开 http://localhost:13697/login ，用 admin / Admin@123 登录。
1. 2.
   打开设备管理页
- 进入 http://localhost:13697/admin/equipment 。
- 页面顶部可刷新数据；左侧“按状态筛选”下拉可以筛选“在用/闲置/借用中/维修中/报废”。
1. 3.
   新增设备
- 在“新增设备”表单中，最少填写“名称”和“状态”，其它字段可选。
- 提交后会调用 POST /api/equipment，成功后刷新列表并显示新设备 ID。
1. 4.
   编辑设备
- 在表格中点击某行的“编辑”，右侧“编辑设备”表单会自动填充该设备数据。
- 修改后点击“保存更新”，调用 PUT /api/equipment/{id}，成功后列表刷新。
1. 5.
   删除设备
- 在表格中点击“删除”或在编辑表单中点击“删除该设备”，调用 DELETE /api/equipment/{id}，成功后列表刷新。
1. 6.
   分类下拉
- 新增/编辑表单中的“分类”下拉来源于 GET /api/equipment-categories（只显示启用的分类）。
- 若下拉为空，可先到分类管理页 http://localhost:13697/admin/categories 新增分类（已提供前端管理页，支持树形展示、增删改）。