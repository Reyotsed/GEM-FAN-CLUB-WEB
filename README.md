# 🌐 GEM Fan Club - 后端服务

<div align="center">

![Java](https://img.shields.io/badge/Java-17-ED8B00.svg?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-6DB33F.svg?logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1.svg?logo=mysql)
![Redis](https://img.shields.io/badge/Redis-6.0+-DC382D.svg?logo=redis)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

基于 Spring Boot 的粉丝俱乐部后端服务，提供用户管理、歌曲管理、AI 聊天代理、游戏排行、秒杀限流等功能。

</div>

---

## ✨ 功能模块

| 模块           | Controller                         | 说明                             |
| -------------- | ---------------------------------- | -------------------------------- |
| 🤖 **AI 聊天**  | `AIController`                     | 转发请求至 RAG 服务，IP 限流保护 |
| 👤 **账户**     | `AccountController`                | 注册、登录、验证码、会话管理     |
| 👥 **用户**     | `UserController`                   | 用户信息查询与管理               |
| 🎵 **歌曲**     | `SongController`                   | 歌曲 CRUD                        |
| 🎮 **猜歌游戏** | `GameSongController`               | 游戏歌曲数据接口                 |
| 🎫 **抢票游戏** | `GameRobTicketController`          | 秒杀抢票 + Redis 队列            |
| 📝 **歌词接龙** | `LyricsChainLeaderboardController` | 排行榜 CRUD                      |
| 🎤 **演唱会**   | `ConcertController`                | 演唱会信息查询                   |
| 💬 **语录**     | `QuoteController`                  | 语录发布、评论、点赞、图片标签   |
| 🖼️ **图片**     | `ImageController`                  | 图片上传与管理                   |

## 🛠️ 技术栈

| 类别    | 技术                   | 说明                             |
| ------- | ---------------------- | -------------------------------- |
| 语言    | Java 17                | LTS 版本                         |
| 框架    | Spring Boot 3.4.3      | Web + JPA + Redis + Actuator     |
| 模板    | Thymeleaf              | 服务端渲染（部分页面）           |
| ORM     | Spring Data JPA        | 数据持久化                       |
| 缓存    | Spring Data Redis      | 缓存 + 限流 + 秒杀队列           |
| 数据库  | MySQL 8.0              | 主数据库                         |
| 限流    | Redis + Lua 脚本       | `RateLimitService` 高性能限流    |
| 唯一 ID | 雪花算法               | `SnowflakeConfig` 分布式 ID 生成 |
| 异步    | `@Async` + 线程池      | `AsyncConfig` 异步任务处理       |
| 工具库  | Hutool / Commons Lang3 | 通用工具                         |
| 验证码  | Easy Captcha           | 登录验证码生成                   |
| 监控    | Spring Actuator        | 健康检查 + 指标                  |

## 📁 项目结构

```
src/main/java/com/example/gem_fan_club_web/
├── GemFanClubWebApplication.java   # Spring Boot 启动类
├── config/
│   ├── AsyncConfig.java            # 异步线程池配置
│   ├── CorsConfig.java             # CORS 跨域配置
│   ├── RedisConfig.java            # Redis 序列化配置
│   ├── RestConfig.java             # RestTemplate 配置（含超时）
│   └── SnowflakeConfig.java        # 雪花算法 ID 生成器
├── constants/
│   └── Constants.java              # 全局常量
├── controller/                     # 控制器层（10 个）
│   ├── AIController.java
│   ├── AccountController.java
│   ├── UserController.java
│   ├── SongController.java
│   ├── ConcertController.java
│   ├── QuoteController.java
│   ├── ImageController.java
│   ├── GameSongController.java
│   ├── GameRobTicketController.java
│   └── LyricsChainLeaderboardController.java
├── dto/                            # 数据传输对象
│   ├── AiChatRequest / AiResponse
│   ├── ChatMessage / ChatRequest
│   └── ResponseDTO
├── model/                          # JPA 实体
│   ├── User / Song / Concert / Order
│   ├── GameSong / LyricsChainLeaderboard
│   └── quote/ (Quote, QuoteComment, QuoteLike, QuotePicture, QuotePictureTag)
├── repository/                     # Spring Data JPA 仓库
├── redis/
│   ├── RedisService.java           # Redis 业务封装
│   └── RedisUtils.java             # Redis 工具类
├── service/                        # 服务层（10 个）
│   ├── AIService.java              # AI 聊天代理
│   ├── RateLimitService.java       # Redis + Lua 限流
│   ├── OrderStreamService.java     # 秒杀抢票服务
│   ├── QuoteService.java           # 语录业务
│   ├── QuoteCommentService.java    # 评论业务
│   └── UserService / SongService / ConcertService / GameSongService / ...
└── utils/
    ├── AssertTools.java            # 断言工具
    ├── FileTools.java              # 文件工具
    └── StringTools.java            # 字符串工具
```

## 🚀 快速开始

### 环境要求

- Java 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+

### 安装与运行

```bash
# 克隆项目
git clone https://github.com/Reyotsed/GEM-FAN-CLUB-WEB.git
cd GEM-FAN-CLUB-WEB

# 配置数据库和 Redis
# 编辑 src/main/resources/application.yml

# 构建
mvn clean install

# 运行
mvn spring-boot:run
# 或
java -jar target/gem_fan_club_web-0.0.1-SNAPSHOT.jar
```

### 关键配置 (`application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gem_fan_club
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
```

## 🔗 关联项目

| 项目                 | 说明            | 仓库                                                   |
| -------------------- | --------------- | ------------------------------------------------------ |
| **GEM-FAN-CLUB-VUE** | Vue 3 前端应用  | [GitHub](https://github.com/Reyotsed/GEM-FAN-CLUB-VUE) |
| **GEM_FAN_CLUB_RAG** | RAG AI 聊天服务 | [GitHub](https://github.com/Reyotsed/GEM_FAN_CLUB_RAG) |

## 📄 许可证

MIT License

---

<div align="center">

⭐ **如果这个项目对你有帮助，请给一个 Star！**

Made with ❤️ for G.E.M. fans

</div>
