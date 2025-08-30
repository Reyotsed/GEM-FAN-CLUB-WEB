# GEM Fan Club Web Application

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-green.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.8+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📖 项目简介

GEM Fan Club Web Application 是一个基于Spring Boot的Web应用程序，为GEM粉丝俱乐部提供全面的在线服务。该应用集成了用户管理、演唱会信息、歌曲管理、AI聊天、限流控制等多项功能。

## ✨ 主要功能

### 🎵 音乐相关
- **歌曲管理**: 完整的歌曲CRUD操作，支持歌曲信息维护
- **游戏歌曲**: 互动式歌曲游戏功能
- **歌词接龙排行榜**: 用户参与的歌词接龙游戏及排行榜系统

### 🎭 演唱会管理
- **演唱会信息**: 演唱会详情、时间、地点等信息管理
- **票务系统**: 演唱会票务相关功能

### 👥 用户系统
- **用户管理**: 用户注册、登录、信息管理
- **账户系统**: 完整的账户管理功能
- **用户互动**: 用户之间的互动功能

### 🤖 AI服务
- **AI聊天**: 集成AI聊天服务，提供智能对话功能
- **智能回复**: 基于AI的智能回复系统

### 🎮 游戏功能
- **抢票游戏**: 互动式抢票游戏系统
- **游戏排行榜**: 游戏成绩统计和排行榜

### 💬 社区功能
- **语录系统**: 用户分享语录、评论功能
- **图片管理**: 支持语录配图功能

### 🚀 技术特性
- **限流控制**: Redis + Lua脚本实现的智能限流系统
- **秒杀系统**: 高性能秒杀功能支持
- **异步处理**: 异步任务处理，提升系统性能
- **缓存优化**: Redis缓存优化，提升响应速度

## 🛠️ 技术栈

### 后端技术
- **Java 17**: 使用最新的LTS版本
- **Spring Boot 3.4.3**: 现代化的Spring框架
- **Spring Data JPA**: 数据持久化
- **Spring Web**: Web服务支持
- **Spring Data Redis**: Redis缓存支持
- **Spring Boot Actuator**: 应用监控

### 数据库
- **MySQL**: 主数据库
- **Redis**: 缓存和限流控制

### 工具库
- **Lombok**: 简化Java代码
- **Hutool**: 实用工具库
- **Apache Commons Lang3**: 通用工具库
- **Easy Captcha**: 验证码生成

### 配置管理
- **Maven**: 项目构建和依赖管理
- **YAML**: 配置文件格式

## 🚀 快速开始

### 环境要求
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+

### 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/Reyotsed/GEM-FAN-CLUB-WEB.git
cd GEM-FAN-CLUB-WEB
```

2. **配置数据库**
   - 创建MySQL数据库
   - 修改 `src/main/resources/application.yml` 中的数据库配置

3. **配置Redis**
   - 确保Redis服务运行
   - 修改Redis连接配置

4. **构建项目**
```bash
mvn clean install
```

5. **运行应用**
```bash
mvn spring-boot:run
```

或者使用打包后的jar文件：
```bash
java -jar target/gem_fan_club_web-0.0.1-SNAPSHOT.jar
```

### 配置说明

主要配置文件位于 `src/main/resources/application.yml`，包含：
- 数据库连接配置
- Redis连接配置
- 应用端口配置
- 其他业务配置

## 📁 项目结构

```
src/main/java/com/example/gem_fan_club_web/
├── config/          # 配置类
│   ├── AsyncConfig.java      # 异步配置
│   ├── RedisConfig.java      # Redis配置
│   ├── RestConfig.java       # REST配置
│   └── SnowflakeConfig.java  # 雪花算法配置
├── controller/      # 控制器层
│   ├── AIController.java     # AI服务控制器
│   ├── UserController.java   # 用户控制器
│   ├── SongController.java   # 歌曲控制器
│   └── ...                  # 其他控制器
├── service/         # 服务层
│   ├── AIService.java        # AI服务
│   ├── RateLimitService.java # 限流服务
│   └── ...                  # 其他服务
├── model/           # 数据模型
├── dto/             # 数据传输对象
├── repository/      # 数据访问层
├── redis/           # Redis相关
├── utils/           # 工具类
└── constants/       # 常量定义
```

## 🔧 核心功能实现

### 限流系统
使用Redis + Lua脚本实现高性能限流控制：
- 支持多种限流策略
- 高性能Lua脚本执行
- 灵活的限流规则配置

### AI聊天服务
集成AI服务，提供智能对话功能：
- 支持多种AI模型
- 智能回复生成
- 对话历史管理

### 秒杀系统
高性能秒杀功能支持：
- Redis队列优化
- 库存预扣减
- 防超卖机制

## 📊 性能优化

- **Redis缓存**: 热点数据缓存，提升响应速度
- **异步处理**: 非阻塞异步任务处理
- **连接池**: 数据库和Redis连接池优化
- **限流控制**: 防止系统过载，保护服务稳定性

## 🤝 贡献指南

欢迎提交Issue和Pull Request来帮助改进项目！

### 贡献步骤
1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系方式

- 项目地址: [https://github.com/Reyotsed/GEM-FAN-CLUB-WEB](https://github.com/Reyotsed/GEM-FAN-CLUB-WEB)
- 问题反馈: [Issues](https://github.com/Reyotsed/GEM-FAN-CLUB-WEB/issues)

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者和用户！

---

⭐ 如果这个项目对你有帮助，请给我们一个星标！
