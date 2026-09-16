# 智能充电桩运营服务平台 (NCS)

新能源汽车充电桩运营服务平台，一期包含 H5 用户端、Web 运营后台、AI Agent 智能助手。

## 目录结构

- `docs/需求说明.md` — 需求基线（三端功能、角色、L3 容量目标、模块划分）
- `backend/` — Spring Boot 3 多模块微服务后端
- `docker-compose.yml` — 本地中间件一键启动
- `project_materials/` — 甲方原始需求 PDF

## 快速开始

1. 启动中间件（需已安装 Docker Desktop）：

   ```bash
   docker compose up -d
   ```

2. 构建后端（需 JDK 17 + Maven）：

   ```bash
   cd backend
   mvn clean install -DskipTests
   ```

3. 各服务独立启动，端口见 `docs/需求说明.md` 第十节。

## 中间件默认信息

| 中间件 | 端口 | 说明 |
|--------|------|------|
| MySQL | 3306 | root / `${ROOT_PASSWORD}`（通过 `.env` 设置） |
| Redis | 6379 | 密码 `${ROOT_PASSWORD}` |
| RocketMQ | 9876 / 10911 | NameServer / Broker |
| EMQX | 1883 / 18083 | MQTT / Dashboard（admin / public） |
| Nacos | 8848 | 注册 + 配置中心 |

> 密码通过 `.env` 的 `ROOT_PASSWORD` 统一控制，复制 `.env.example` 为 `.env` 可覆盖。

## 后端服务环境变量

后端各服务的数据库/缓存密码通过环境变量注入（不在代码中写死明文）：

| 环境变量 | 说明 | 必填 |
|---------|------|------|
| `DB_PASSWORD` | MySQL 密码 | 是 |
| `REDIS_PASSWORD` | Redis 密码（仅 order 服务） | 是 |
| `NACOS_ADDR` | Nacos 地址（默认 `localhost:8848`） | 否 |
| `MQTT_BROKER` | EMQX 地址（默认 `tcp://localhost:1883`） | 否 |
| `ROCKETMQ_NAMESRV` | RocketMQ NameServer（默认 `localhost:9876`） | 否 |

启动示例：

```bash
export DB_PASSWORD=your_password
export REDIS_PASSWORD=your_password
java -jar order/target/ncs-order-1.0.0.jar
```
