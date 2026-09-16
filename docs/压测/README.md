# L3 容量压测指南

## 一、先造数据

在**服务器上**执行（数据量较大，别在本地跑）：

```bash
# 安装 pymysql
pip install pymysql -i https://pypi.tuna.tsinghua.edu.cn/simple

# 生成数据（100万用户 + 1000站 + 1万设备 + 50万订单，约几分钟）
cd /root/ncs
python gen_data.py

# 或自定义规模：python gen_data.py 1000000 500000
```

生成后验证：

```bash
mysql -p"$DB_PASSWORD" -e "SELECT COUNT(*) AS users FROM ncs.t_user; SELECT COUNT(*) AS stations FROM ncs.t_station; SELECT COUNT(*) AS devices FROM ncs.t_device; SELECT COUNT(*) AS orders FROM ncs.t_charging_order;"
```

预期：`users=1000000`、`stations=1000`、`devices=10000`、`orders=500000`。

## 二、压测目标（L3）

| 接口 | 目标 QPS | JMeter 线程组 |
|------|---------|--------------|
| 查询充电站 `GET /api/station/nearby` | **2000** | 查询充电站(2000QPS) |
| 查看设备状态 `GET /api/station/1` | **1000** | 查看设备状态(1000QPS) |
| 开始充电 `POST /api/charge/start` | **300** | 开始充电(300QPS) |
| 结束充电 `POST /api/charge/end` | **300** | 结束充电(300QPS) |
| 用户登录 `POST /api/auth/login` | **500** | 用户登录(500QPS) |

## 三、运行压测

### 前提
- 本机安装 **Apache JMeter 5.x**（https://jmeter.apache.org/download_jmeter.cgi，解压即用）
- 压测机与服务器网络通畅（建议压测机也在云上，否则本地带宽会先成为瓶颈）

### 命令行运行（推荐，省资源）

```bash
# 进入 JMeter bin 目录
cd apache-jmeter-5.6.3/bin

# 跑压测（先跑登录取 token，再并发压目标接口，生成 result.jtl）
jmeter -n -t D:/NCS/docs/压测/L3压测.jmx -l result.jtl -e -o report
```

`-n` 非 GUI 模式；`-l` 结果文件；`-e -o` 生成 HTML 报告。

### 图形界面运行（看实时曲线）

```bash
jmeter
```

打开 `L3压测.jmx`，点绿色三角运行，`汇总报告` 面板实时看吞吐量/错误率。

### 关键参数（按需调）

打开 .jmx，每个线程组里：

- `ThreadGroup.num_threads`：并发线程数（越大越能打高 QPS，但受机器限制）
- `ConstantThroughputTimer.throughput`：目标吞吐（**单位是 请求/分钟**，QPS×60）
  - 2000 QPS = 120000；1000 = 60000；500 = 30000；300 = 18000
- `ThreadGroup.duration`：压测时长（秒），默认 60s

> ⚠️ 重要：JMeter 的 Constant Throughput Timer **只能限速、不能提量**。如果机器线程数不够，实际 QPS 会低于目标。想真正打到 2000 QPS，可能需要：① 提高线程数到 400-800 ② 压测机用云服务器（同地域）③ 关闭聚合报告等监听器。

## 四、怎么看结果（验收证据）

1. **吞吐量（Throughput）**：汇总报告里的 `Throughput` 列，单位是 请求/秒，即 QPS。对照上表目标。
2. **错误率（Error %）**：应接近 0%。如果开始充电/结束充电错误率高，多半是「同一设备被并发占用」或 token 过期，属于正常业务校验，说明防重复充电在起作用。
3. **平均响应时间（Average）**：观察是否随 QPS 升高而暴涨（说明到瓶颈了）。
4. **截图留证**：汇总报告 + HTML 报告（`report/index.html`）都要截图，答辩用。

## 五、服务器怎么监控

压测时另开一个 SSH 窗口连服务器，实时看：

```bash
# 整体负载（1/5/15分钟平均负载，超过 CPU 核数说明过载）
uptime

# CPU/内存/IO 综合
top

# 内存
free -h

# 各服务 CPU/内存占用
ps aux --sort=-%cpu | head -15

# 网络连接数（TIME_WAIT 太多说明连接没复用）
ss -s

# 网关/订单服务日志看报错
tail -f /root/ncs/logs/gateway.log
tail -f /root/ncs/logs/order.log
```

**判断是否达标**：目标 QPS 打出来了 + 错误率≈0 + 服务器负载没打满（有富余），才算「容量达标」。如果某个服务 CPU 打满、响应时间暴涨，就是瓶颈点，对应优化（加缓存/加实例/调连接池）。

## 六、备注

- 压测前先把无关服务（设备模拟器、EMQX、RocketMQ）停掉，避免抢资源干扰结果。
- 压测结束后记得**清掉测试数据**（`TRUNCATE t_user; TRUNCATE t_charging_order;` 等），或恢复种子数据，避免演示时数据太乱。
