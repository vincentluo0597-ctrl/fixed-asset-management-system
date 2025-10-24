// 引入必要的模块
const Sequelize = require('sequelize');
const mysql = require('mysql2/promise');
const bcrypt = require('bcryptjs');
require('dotenv').config();

// 导入数据库配置
const { connectDB } = require('./src/config/db');

// 导入所有模型，确保它们与sequelize实例关联
const models = require('./src/models');

// 从数据库配置中获取sequelize实例
const { sequelize } = require('./src/config/db');

// 初始化数据库
const initDatabase = async () => {
  try {
    console.log('开始初始化数据库...');
    
    // 连接数据库
    await connectDB();
    
    // 同步所有模型到数据库（创建表）
    try {
      await sequelize.sync({ force: true }); // 强制创建表，确保表结构存在
      console.log('数据库表结构创建成功');

      // 在同步完成后添加 FULLTEXT 索引（如果不存在则尝试创建，存在则忽略错误）
      try {
        // 设备表：名称、型号、供应商、规格
        await sequelize.query(
          "ALTER TABLE equipment ADD FULLTEXT idx_equipment_fulltext (name, model, supplier, specification)"
        );
        console.log('equipment 表 FULLTEXT 索引创建完成');
      } catch (err) {
        console.warn('equipment 表 FULLTEXT 索引创建时出现提示/错误（可能已存在）：', err.message || err);
      }

      try {
        // 维修记录表：多个文本字段
        await sequelize.query(
          "ALTER TABLE maintenance_record ADD FULLTEXT idx_maintenance_fulltext (fault_description, maintenance_project, initial_inspection, response_measures, review_situation, follow_up_summary, notes)"
        );
        console.log('maintenance_record 表 FULLTEXT 索引创建完成');
      } catch (err) {
        console.warn('maintenance_record 表 FULLTEXT 索引创建时出现提示/错误（可能已存在）：', err.message || err);
      }

      try {
        // 择期维修任务表：标题与描述相关字段
        await sequelize.query(
          "ALTER TABLE scheduled_maintenance ADD FULLTEXT idx_sched_fulltext (task_title, task_description, basic_situation, completion_notes)"
        );
        console.log('scheduled_maintenance 表 FULLTEXT 索引创建完成');
      } catch (err) {
        console.warn('scheduled_maintenance 表 FULLTEXT 索引创建时出现提示/错误（可能已存在）：', err.message || err);
      }
    } catch (syncError) {
      console.error('表结构同步失败:', syncError);
      // 继续执行，不中断初始化流程
    }
    
    // 创建初始管理员用户
    try {
      const adminUser = await models.User.findOne({
        where: {
          username: 'admin'
        }
      });
      
      if (!adminUser) {
        await models.User.create({
          name: '管理员',
          department: '信息与设备管理中心',
          role: 'admin',
          contact: 'admin@example.com',
          username: 'admin',
          password: 'admin123'  // 密码会在模型的beforeCreate钩子中自动加密
        });
        console.log('初始管理员用户创建成功');
      } else {
        console.log('管理员用户已存在，跳过创建');
      }
    } catch (userError) {
      console.error('创建管理员用户时出错:', userError);
      // 继续执行，不中断初始化流程
    }
    
    // 创建初始设备类别
    try {
      const categories = [
        { name: '办公设备' },
        { name: '教学设备' },
        { name: '实验设备' },
        { name: 'IT设备' }
      ];
      
      for (const category of categories) {
        const existingCategory = await models.EquipmentCategory.findOne({
          where: {
            name: category.name
          }
        });
        
        if (!existingCategory) {
          await models.EquipmentCategory.create(category);
        }
      }
      
      console.log('初始设备类别创建成功');
    } catch (categoryError) {
      console.error('创建设备类别时出错:', categoryError);
      // 继续执行，不中断初始化流程
    }
    
    // 创建初始位置
    try {
      const locations = [
        { name: '励群楼办公室', location_type: '教室', building: '主教学楼', floor: '1楼' },
        { name: '主教学楼201办公室', location_type: '办公室', building: '主教学楼', floor: '2楼' },
        { name: '实验楼101实验室', location_type: '实验室', building: '实验楼', floor: '1楼' },
        { name: '仓库A', location_type: '仓库', building: '仓库区', floor: '1楼' }
      ];
      
      for (const location of locations) {
        const existingLocation = await models.Location.findOne({
          where: {
            name: location.name
          }
        });
        
        if (!existingLocation) {
          await models.Location.create(location);
        }
      }
      
      console.log('初始位置创建成功');
    } catch (locationError) {
      console.error('创建位置时出错:', locationError);
      // 继续执行，不中断初始化流程
    }
    
    console.log('数据库初始化完成！');
  } catch (error) {
    console.error('数据库初始化过程中出现错误:', error);
  } finally {
    try {
      await sequelize.close();
      console.log('数据库连接已关闭');
    } catch (closeError) {
      console.error('关闭数据库连接时出错:', closeError);
    }
  }
};

// 运行初始化
initDatabase();