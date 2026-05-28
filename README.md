# 语音输入法后端服务 🎤

基于Spring Boot和七牛云服务的语音输入法后端系统，支持音频文件上传、语音识别和结果管理。

## ✨ 功能特性

- 🎵 **音频文件上传**：支持多种音频格式，自动上传到七牛云Kodo对象存储
- 🗣️ **语音识别**：集成七牛云ASR服务，自动将音频转换为文字
- 📝 **结果管理**：查询识别结果、历史记录管理
- 🌐 **Web界面**：提供美观易用的Web前端界面
- 🔄 **状态跟踪**：实时跟踪识别状态（待识别/识别中/已完成/失败）
- 📊 **分页查询**：支持按状态过滤和分页查询历史记录

## 🛠️ 技术栈

### 后端
- **Java 21** - 最新LTS版本
- **Spring Boot 3.2.5** - 企业级应用框架
- **Spring Data JPA** - 数据持久化
- **H2 Database** - 内存数据库（开发环境）
- **MySQL** - 生产数据库（可选）
- **七牛云SDK** - 对象存储和语音识别
- **OkHttp** - HTTP客户端
- **Lombok** - 简化代码

### 前端
- **HTML5 + CSS3 + JavaScript** - 原生Web技术
- **Fetch API** - RESTful API调用
- **响应式设计** - 适配多种设备

## 🚀 快速开始

### 环境要求

- JDK 21+
- Maven 3.6+
- 七牛云账号（需要配置AccessKey和SecretKey）

### 1. 克隆项目

```bash
git clone https://github.com/ygn555/Voice-Input.git
cd Voice-Input
```

### 2. 配置七牛云

编辑 `src/main/resources/application.yml`，配置七牛云密钥：

```yaml
qiniu:
  access-key: your-access-key        # 七牛云AccessKey
  secret-key: your-secret-key        # 七牛云SecretKey
  bucket: your-bucket-name           # 对象存储空间名称
  domain: your-domain.com            # 对象存储域名
  asr:
    url: https://ap-gate-z0.qiniuapi.com/asr/v1  # ASR服务地址
```

### 3. 编译项目

```bash
mvn clean compile
```

### 4. 运行应用

```bash
mvn spring-boot:run
```

服务将在 http://localhost:8080 启动

### 5. 访问Web界面

打开浏览器访问：http://localhost:8080

## 📖 API文档

### 音频管理

#### 上传音频文件
```http
POST /api/audio/upload
Content-Type: multipart/form-data

参数：
- file: 音频文件（必填）
- duration: 音频时长（选填，单位：秒）

响应：
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "id": 1,
    "fileName": "test.mp3",
    "qiniuUrl": "http://your-domain.com/audio/xxx.mp3",
    "fileSize": 1024000,
    "status": "PENDING"
  }
}
```

#### 获取音频详情
```http
GET /api/audio/{id}

响应：
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "fileName": "test.mp3",
    "recognizedText": "识别的文本内容",
    "status": "COMPLETED"
  }
}
```

#### 查询音频列表
```http
GET /api/audio/list?page=0&size=10&status=COMPLETED

参数：
- page: 页码（默认0）
- size: 每页数量（默认10）
- status: 状态过滤（可选：PENDING/PROCESSING/COMPLETED/FAILED）

响应：
{
  "code": 200,
  "message": "Success",
  "data": {
    "content": [...],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 100,
    "totalPages": 10
  }
}
```

#### 删除音频
```http
DELETE /api/audio/{id}

响应：
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 语音识别

#### 提交识别任务
```http
POST /api/asr/recognize/{audioFileId}

响应：
{
  "code": 200,
  "message": "识别任务已提交",
  "data": {
    "audioFileId": 1,
    "recognizedText": "识别的文本内容",
    "status": "SUCCESS"
  }
}
```

### 健康检查

```http
GET /api/health

响应：
{
  "status": "UP",
  "service": "voice-input-service",
  "timestamp": "2026-05-28T18:30:00"
}
```

## 🏗️ 项目结构

```
src/main/java/com/qiniu/voiceinput/
├── config/              # 配置类
│   └── QiniuConfig.java
├── controller/          # 控制器
│   ├── AudioController.java
│   ├── AsrController.java
│   └── HealthController.java
├── dto/                 # 数据传输对象
│   ├── ApiResponse.java
│   ├── AudioFileDTO.java
│   └── PageResponse.java
├── entity/              # 实体类
│   └── AudioFile.java
├── exception/           # 异常处理
│   ├── BusinessException.java
│   ├── ResourceNotFoundException.java
│   └── GlobalExceptionHandler.java
├── repository/          # 数据访问层
│   └── AudioFileRepository.java
├── service/             # 业务逻辑层
│   ├── AudioFileService.java
│   ├── QiniuService.java
│   └── AsrService.java
└── VoiceInputApplication.java  # 应用入口

src/main/resources/
├── application.yml      # 应用配置
└── static/
    └── index.html       # Web前端界面
```

## 🐳 Docker部署

### 构建镜像

```bash
docker build -t voice-input-service .
```

### 运行容器

```bash
docker run -d \
  -p 8080:8080 \
  -e QINIU_ACCESS_KEY=your-access-key \
  -e QINIU_SECRET_KEY=your-secret-key \
  -e QINIU_BUCKET=your-bucket \
  -e QINIU_DOMAIN=your-domain.com \
  --name voice-input \
  voice-input-service
```

## 📊 开发进度

- [x] PR#1: 项目初始化（Spring Boot结构、pom.xml、配置文件）
- [x] PR#2: 数据库设计（实体类、Repository、数据库配置）
- [x] PR#3: 七牛云Kodo集成（SDK集成、上传工具类）
- [x] PR#4: 音频上传API（Controller、Service、文件处理）
- [x] PR#5: 七牛云ASR集成（语音识别API调用）
- [x] PR#6: Web前端界面（演示页面）
- [x] PR#7: 文档和部署（README、API文档、Docker）

## 🎯 核心功能展示

### 1. 音频上传
- 支持点击上传和拖拽上传
- 自动验证文件类型和大小
- 实时显示上传进度

### 2. 语音识别
- 自动提交识别任务
- 实时显示识别状态
- 识别完成后展示结果

### 3. 历史记录
- 分页显示所有记录
- 按状态筛选
- 点击查看详情

### 4. 结果管理
- 一键复制识别结果
- 查看识别详情
- 删除历史记录

## 🔧 配置说明

### 数据库配置

**开发环境（H2）**：
```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/voiceinput
    driver-class-name: org.h2.Driver
```

**生产环境（MySQL）**：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/voiceinput?useSSL=false&serverTimezone=UTC
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: your-password
```

### 文件上传配置

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 50MB      # 最大文件大小
      max-request-size: 50MB   # 最大请求大小
```

## 🤝 贡献指南

欢迎提交Issue和Pull Request！

## 📝 许可证

MIT License

## 👨‍💻 作者

YangGan

## 🙏 致谢

- 七牛云提供对象存储和语音识别服务
- Spring Boot团队提供优秀的开发框架
