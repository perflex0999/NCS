-- 智能充电桩运营服务平台 数据库初始化
CREATE DATABASE IF NOT EXISTS ncs DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ncs;

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 充电站表
CREATE TABLE IF NOT EXISTS t_station (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '充电站名称',
    address VARCHAR(200) DEFAULT NULL COMMENT '地址',
    city VARCHAR(50) DEFAULT NULL COMMENT '所属城市',
    lat DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
    lng DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
    business_hours VARCHAR(50) DEFAULT '00:00-24:00' COMMENT '营业时间',
    contact VARCHAR(50) DEFAULT NULL COMMENT '联系方式',
    parking_info VARCHAR(200) DEFAULT NULL COMMENT '停车说明',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1营业 0停业',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充电站表';

-- 充电桩（设备）表
CREATE TABLE IF NOT EXISTS t_device (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_no VARCHAR(50) NOT NULL COMMENT '设备编号',
    station_id BIGINT NOT NULL COMMENT '所属充电站',
    device_type TINYINT NOT NULL COMMENT '1快充 2慢充',
    power_kw DECIMAL(6,2) NOT NULL DEFAULT 0 COMMENT '额定功率kW',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0空闲 1使用中 2故障 3离线 4维修中',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_device_no (device_no),
    KEY idx_station (station_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充电桩设备表';

-- 分时段价格表
CREATE TABLE IF NOT EXISTS t_price (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    station_id BIGINT NOT NULL COMMENT '充电站',
    device_type TINYINT NOT NULL COMMENT '1快充 2慢充',
    start_time TIME NOT NULL COMMENT '时段开始(HH:mm:ss)',
    end_time TIME NOT NULL COMMENT '时段结束(HH:mm:ss)',
    elec_price DECIMAL(8,4) NOT NULL COMMENT '电费 元/kWh',
    service_price DECIMAL(8,4) NOT NULL COMMENT '服务费 元/kWh',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_station_type (station_id, device_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分时段价格表';

-- 充电订单表
CREATE TABLE IF NOT EXISTS t_charging_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户',
    station_id BIGINT NOT NULL COMMENT '充电站',
    device_id BIGINT NOT NULL COMMENT '充电桩',
    device_no VARCHAR(50) DEFAULT NULL COMMENT '设备编号冗余',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    charged_kwh DECIMAL(10,2) DEFAULT NULL COMMENT '充电量kWh',
    elec_price DECIMAL(8,4) DEFAULT NULL COMMENT '开始充电时电费快照',
    service_price DECIMAL(8,4) DEFAULT NULL COMMENT '开始充电时服务费快照',
    amount DECIMAL(10,2) DEFAULT NULL COMMENT '订单金额(元)',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0充电中 1已完成 2已支付',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user (user_id),
    KEY idx_device (device_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充电订单表';

-- 设备上报原始数据表（时序，供设备接入链路写入）
CREATE TABLE IF NOT EXISTS t_device_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_no VARCHAR(50) NOT NULL COMMENT '设备编号',
    station_id BIGINT DEFAULT NULL COMMENT '充电站',
    voltage DECIMAL(8,2) DEFAULT NULL COMMENT '电压',
    current DECIMAL(8,2) DEFAULT NULL COMMENT '电流',
    power DECIMAL(8,2) DEFAULT NULL COMMENT '功率',
    energy DECIMAL(12,2) DEFAULT NULL COMMENT '累计电量',
    status TINYINT DEFAULT NULL COMMENT '状态',
    report_time DATETIME DEFAULT NULL COMMENT '上报时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_device_time (device_no, report_time),
    KEY idx_time (report_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备上报原始数据表';

-- 充电桩预约表
CREATE TABLE IF NOT EXISTS t_reservation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_no VARCHAR(50) NOT NULL COMMENT '设备编号',
    user_id BIGINT NOT NULL COMMENT '预约用户',
    reserve_time DATETIME NOT NULL COMMENT '预约时间',
    expire_time DATETIME NOT NULL COMMENT '预约截止时间(30分钟)',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0预约中 1已使用 2已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_device (device_no),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充电桩预约表';

-- 故障表
CREATE TABLE IF NOT EXISTS t_fault (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT NOT NULL COMMENT '故障设备ID',
    device_no VARCHAR(50) DEFAULT NULL COMMENT '设备编号',
    fault_type VARCHAR(50) DEFAULT NULL COMMENT '故障类型',
    fault_time DATETIME DEFAULT NULL COMMENT '故障时间',
    description VARCHAR(500) DEFAULT NULL COMMENT '故障描述',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0待处理 1处理中 2已处理',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_device (device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='故障表';
