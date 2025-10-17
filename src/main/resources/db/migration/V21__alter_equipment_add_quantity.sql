-- 为 equipment 表增加数量字段，默认 1，非空，用于盘库统计与批量入库
ALTER TABLE equipment
    ADD COLUMN quantity INT NOT NULL DEFAULT 1;

-- 兼容已有数据：新列默认值为 1，无需额外更新操作