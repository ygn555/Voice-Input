# 语音输入法后端服务

基于Spring Boot和七牛云服务的语音输入法后端系统。

## 功能特性

- 音频文件上传到七牛云Kodo对象存储
- 集成七牛云语音识别服务
- 识别结果查询和历史记录管理
- RESTful API设计

## 技术栈

- Java 21
- Spring Boot 3.2.5
- Spring Data JPA
- H2/MySQL数据库
- 七牛云SDK
- Maven

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.6+

### 配置

复制 `src/main/resources/application.yml` 并配置七牛云密钥：

```yaml
qiniu:
  access-key: your-access-key
  secret-key: your-secret-key
  bucket: your-bucket-name
```

### 运行

```bash
mvn spring-boot:run
```

服务将在 http://localhost:8080 启动

## API文档

待完善...

## 开发计划

- [x] PR#1: 项目初始化
- [ ] PR#2: 数据库设计
- [ ] PR#3: 七牛云Kodo集成
- [ ] PR#4: 音频上传API
- [ ] PR#5: 七牛云ASR集成
- [ ] PR#6: 识别任务创建API
- [ ] PR#7: 识别结果查询API
- [ ] PR#8: 历史记录API
- [ ] PR#9: 错误处理和日志
- [ ] PR#10: API优化和测试
- [ ] PR#11: Web前端界面
- [ ] PR#12: 文档和部署

## Demo视频

待上传...

## 许可证

MIT
