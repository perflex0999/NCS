-- 测试数据
USE ncs;

-- 示例用户
INSERT INTO t_user (phone, nickname) VALUES ('13800000001', '测试用户1')
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname);

-- 示例充电站
INSERT INTO t_station (name, address, city, lat, lng, business_hours, contact, parking_info, status) VALUES
('城西快充站', '幸福路1号', '杭州', 30.274100, 120.155100, '00:00-24:00', '0571-1234567', '地面停车场，免费停车2小时', 1),
('滨江超级充电站', '江南大道100号', '杭州', 30.208400, 120.212000, '06:00-22:00', '0571-7654321', '地下车库B2层', 1),
('西湖景区充电站', '南山路88号', '杭州', 30.245000, 120.150000, '08:00-20:00', '0571-8888888', '景区停车场，收费', 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 示例设备（1快充=120kW、2慢充=7kW；状态：0空闲 2故障）
INSERT INTO t_device (device_no, station_id, device_type, power_kw, status) VALUES
('DEV-1001', 1, 1, 120.00, 0),
('DEV-1002', 1, 1, 120.00, 0),
('DEV-1003', 1, 2, 7.00, 0),
('DEV-2001', 2, 1, 160.00, 0),
('DEV-2002', 2, 2, 7.00, 0),
('DEV-3001', 3, 1, 120.00, 2)
ON DUPLICATE KEY UPDATE device_no = VALUES(device_no);

-- 分时段价格（站1快充：白天/夜间不同价；其余全天单一价）
INSERT INTO t_price (station_id, device_type, start_time, end_time, elec_price, service_price) VALUES
(1, 1, '08:00:00', '22:00:00', 0.8000, 0.4000),
(1, 1, '22:00:00', '08:00:00', 0.4000, 0.2000),
(1, 2, '00:00:00', '23:59:59', 0.6000, 0.3000),
(2, 1, '00:00:00', '23:59:59', 0.9000, 0.5000),
(2, 2, '00:00:00', '23:59:59', 0.7000, 0.3000),
(3, 1, '00:00:00', '23:59:59', 0.7500, 0.3500);

-- 示例故障
INSERT INTO t_fault (device_id, device_no, fault_type, fault_time, description, status) VALUES
(6, 'DEV-3001', '充电枪无法启动', NOW(), '用户反馈充电枪插入后无法启动', 0),
(1, 'DEV-1001', '通信超时', NOW(), '设备离线，通信超时', 1);
