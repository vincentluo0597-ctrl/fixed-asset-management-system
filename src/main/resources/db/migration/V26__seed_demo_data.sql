-- Seed demo data for categories, suppliers, locations and equipment
-- Safe for re-runs via INSERT IGNORE and unique codes/names

-- Locations
INSERT IGNORE INTO locations (name, type, manager_id) VALUES
  ('总部', '办公室', NULL),
  ('IT部门', '办公室', NULL),
  ('财务部门', '办公室', NULL),
  ('仓库A', '仓库', NULL);

SET @loc_root := (SELECT id FROM locations WHERE name='总部');
UPDATE locations SET level=0 WHERE name='总部';
UPDATE locations SET parent_id=@loc_root, level=1 WHERE name IN ('IT部门','财务部门','仓库A');

-- Suppliers
INSERT IGNORE INTO suppliers (code, name, contact_person, phone, email, type, is_active) VALUES
  ('SUP-LENOVO', '联想集团', '张经理', '13800138000', 'zhang@lenovo.com', '制造商', 1),
  ('SUP-DELL',   '戴尔中国', '李经理', '13900139000', 'li@dell.com',     '制造商', 1),
  ('SUP-HP',     '惠普中国', '王经理', '13700137000', 'wang@hp.com',     '制造商', 1);

-- Equipment Categories
INSERT IGNORE INTO equipment_categories (code, name, type, level, is_active) VALUES
  ('CAT-COMPUTER', '计算机设备', '办公设备', 0, 1),
  ('CAT-LAPTOP',   '笔记本电脑', '办公设备', 1, 1),
  ('CAT-DESKTOP',  '台式电脑',   '办公设备', 1, 1),
  ('CAT-PRINTER',  '打印机设备', '办公设备', 0, 1),
  ('CAT-NETWORK',  '网络设备',   '办公设备', 0, 1);

SET @cat_root := (SELECT id FROM equipment_categories WHERE code='CAT-COMPUTER');
UPDATE equipment_categories SET parent_id=@cat_root WHERE code IN ('CAT-LAPTOP','CAT-DESKTOP');

-- Resolve foreign keys for equipment inserts
SET @cat_laptop := (SELECT id FROM equipment_categories WHERE code='CAT-LAPTOP');
SET @cat_printer := (SELECT id FROM equipment_categories WHERE code='CAT-PRINTER');

SET @sup_lenovo := (SELECT id FROM suppliers WHERE code='SUP-LENOVO');
SET @sup_hp     := (SELECT id FROM suppliers WHERE code='SUP-HP');

SET @loc_it  := (SELECT id FROM locations WHERE name='IT部门');
SET @loc_fin := (SELECT id FROM locations WHERE name='财务部门');

-- Equipment demo
INSERT INTO equipment (name, status, brand, model, category_id, supplier_id, location_id, quantity, asset_number, serial_number, purchase_date)
VALUES
  ('笔记本电脑 ThinkPad X1', '在用', '联想', 'X1 Carbon', @cat_laptop, @sup_lenovo, @loc_it, 10, 'ASSET-001', 'SN-LENOVO-X1-001', CURRENT_DATE()),
  ('打印机 HP LaserJet',     '在用', '惠普', 'LaserJet 1020', @cat_printer, @sup_hp, @loc_fin, 3, 'ASSET-002', 'SN-HP-PRN-1020', CURRENT_DATE()),
  ('笔记本电脑 ThinkPad T14','维修中', '联想', 'T14', @cat_laptop, @sup_lenovo, @loc_it, 2, 'ASSET-003', 'SN-LENOVO-T14-001', CURRENT_DATE());

