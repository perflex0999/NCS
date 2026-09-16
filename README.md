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
| MySQL | 3306 | root / `${ROOT_PASSWORD}`（默认 `Ncs@123456`） |
| Redis | 6379 | 密码 `${ROOT_PASSWORD}` |
| RocketMQ | 9876 / 10911 | NameServer / Broker |
| EMQX | 1883 / 18083 | MQTT / Dashboard（admin / public） |
| Nacos | 8848 | 注册 + 配置中心 |

> 密码通过 `.env` 的 `ROOT_PASSWORD` 统一控制，复制 `.env.example` 为 `.env` 可覆盖。
