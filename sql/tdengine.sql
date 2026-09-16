-- ============================================================
-- TDengine 时序库脚本（生产级设备原始数据存储）
-- 说明：L3 容量下设备上报约 2000 条/秒，原始数据量大，
--       生产环境用 TDengine 这类时序库存储，而非 MySQL。
-- 当前 demo 中设备原始数据写入 MySQL 的 t_device_data 表；
-- 此脚本为升级到 TDengine 时的建库建表语句。
-- ============================================================

-- 建库：保留 365 天，每个数据文件 10 天
CREATE DATABASE IF NOT EXISTS ncs_iot KEEP 365 DURATION 10 BUFFER 96;

USE ncs_iot;

-- 超级表：设备上报数据（每个设备一张子表，用 TAGS 区分）
CREATE STABLE IF NOT EXISTS device_data (
    ts        TIMESTAMP,
    voltage   DOUBLE,
    current   DOUBLE,
    power     DOUBLE,
    energy    DOUBLE,
    status    TINYINT
) TAGS (
    device_no  NCHAR(50),
    station_id BIGINT
);

-- 示例：为某个设备建子表并插入（自动建表写法）
-- INSERT INTO d_1001 USING device_data TAGS ('DEV-1001', 1) VALUES (NOW, 220.5, 10.2, 2.2, 123.4, 0);

-- 查询示例：最近 1 小时某设备的电压趋势
-- SELECT ts, voltage FROM device_data WHERE device_no = 'DEV-1001' AND ts > NOW - 1h;
