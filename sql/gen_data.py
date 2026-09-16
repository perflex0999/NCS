#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
L3 容量测试数据生成脚本
生成：100万注册用户、1000个充电站、1万个充电设备、分时段价格、50万历史订单
分批插入，避免一次性把内存撑爆。

用法：python gen_data.py [用户数] [订单数]
默认：1000000 用户、500000 订单
"""
import pymysql
import random
import time
import sys
import os

DB = dict(host='localhost', port=3306, user='root',
          password=os.environ.get('DB_PASSWORD', ''),
          database='ncs', charset='utf8mb4')

USERS = int(sys.argv[1]) if len(sys.argv) > 1 else 1_000_000
STATIONS = 1_000
DEVICES = 10_000
ORDERS = int(sys.argv[2]) if len(sys.argv) > 2 else 500_000
BATCH = 5000

CITIES = ['杭州', '北京', '上海', '广州', '深圳', '成都', '武汉', '南京']


def batch_insert(conn, sql, rows, desc):
    """分批插入，每批 BATCH 条"""
    cur = conn.cursor()
    total = len(rows)
    done = 0
    t0 = time.time()
    for i in range(0, total, BATCH):
        cur.executemany(sql, rows[i:i + BATCH])
        conn.commit()
        done += len(rows[i:i + BATCH])
        if done % (BATCH * 10) == 0 or done == total:
            cost = time.time() - t0
            speed = done / cost if cost > 0 else 0
            print(f'  {desc}: {done}/{total}  耗时 {cost:.1f}s  速度 {speed:.0f}条/秒')
    cur.close()


def main():
    conn = pymysql.connect(**DB)
    print(f'开始生成：{USERS} 用户、{STATIONS} 站、{DEVICES} 设备、{ORDERS} 订单')

    # 1. 用户
    print('生成用户...')
    users = [(f'139{i:08d}', f'用户{i}') for i in range(1, USERS + 1)]
    batch_insert(conn, 'INSERT INTO t_user (phone, nickname) VALUES (%s, %s)', users, '用户')

    # 2. 充电站
    print('生成充电站...')
    stations = []
    for i in range(1, STATIONS + 1):
        stations.append((f'充电站{i}', f'{random.choice(CITIES)}区测试路{i}号', random.choice(CITIES),
                         30.0 + random.random() * 1.0, 120.0 + random.random() * 1.0,
                         '00:00-24:00', f'0571-{random.randint(1000000, 9999999)}',
                         '地面停车场，免费2小时', 1))
    batch_insert(conn,
        'INSERT INTO t_station (name, address, city, lat, lng, business_hours, contact, parking_info, status) '
        'VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s)', stations, '充电站')

    # 3. 设备（每站 10 个）
    print('生成充电设备...')
    devices = []
    for i in range(1, DEVICES + 1):
        station_id = (i - 1) % STATIONS + 1
        dtype = 1 if random.random() < 0.7 else 2  # 70% 快充
        power = 120.0 if dtype == 1 else 7.0
        status = random.choices([0, 1, 2], weights=[85, 10, 5])[0]  # 空闲/使用/故障
        devices.append((f'DEV-{i:05d}', station_id, dtype, power, status))
    batch_insert(conn,
        'INSERT INTO t_device (device_no, station_id, device_type, power_kw, status) VALUES (%s,%s,%s,%s,%s)',
        devices, '设备')

    # 4. 分时段价格（每站快充+慢充各一条）
    print('生成价格...')
    prices = []
    for sid in range(1, STATIONS + 1):
        prices.append((sid, 1, '08:00:00', '22:00:00', round(0.6 + random.random() * 0.5, 4), round(0.2 + random.random() * 0.3, 4)))
        prices.append((sid, 1, '22:00:00', '08:00:00', round(0.3 + random.random() * 0.3, 4), round(0.1 + random.random() * 0.2, 4)))
        prices.append((sid, 2, '00:00:00', '23:59:59', round(0.5 + random.random() * 0.3, 4), round(0.2 + random.random() * 0.2, 4)))
    batch_insert(conn,
        'INSERT INTO t_price (station_id, device_type, start_time, end_time, elec_price, service_price) '
        'VALUES (%s,%s,%s,%s,%s,%s)', prices, '价格')

    # 5. 历史订单
    print('生成历史订单...')
    orders = []
    for i in range(1, ORDERS + 1):
        user_id = random.randint(1, USERS)
        station_id = random.randint(1, STATIONS)
        device_id = random.randint(1, DEVICES)
        day = random.randint(0, 30)
        hh = random.randint(0, 23)
        mm = random.randint(0, 59)
        start = f"2026-08-{(day % 28) + 1:02d} {hh:02d}:{mm:02d}:00"
        end = f"2026-08-{(day % 28) + 1:02d} {(hh + 1) % 24:02d}:{mm:02d}:00"
        kwh = round(random.uniform(1, 60), 2)
        amount = round(kwh * random.uniform(0.8, 1.4), 2)
        status = random.choices([1, 2], weights=[60, 40])[0]
        orders.append((f'CD{i:09d}', user_id, station_id, device_id, f'DEV-{device_id:05d}',
                       start, end, kwh, round(0.8, 4), round(0.4, 4), amount, status))
    batch_insert(conn,
        'INSERT INTO t_charging_order (order_no, user_id, station_id, device_id, device_no, '
        'start_time, end_time, charged_kwh, elec_price, service_price, amount, status) '
        'VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)', orders, '订单')

    conn.close()
    print('全部完成')


if __name__ == '__main__':
    main()
