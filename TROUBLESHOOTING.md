# 故障排除指南

## 常见问题及解决方案

### 1. 后端编译问题

#### 问题：MySQL驱动版本缺失
```
'dependencies.dependency.version' for mysql:mysql-connector-java:jar is missing
```

**解决方案：**
确保 `backend/pom.xml` 中MySQL驱动有版本号：
```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
    <scope>runtime</scope>
</dependency>
```

#### 问题：Java版本不兼容
**解决方案：**
1. 确保安装了JDK 8或更高版本
2. 检查JAVA_HOME环境变量
3. 运行 `java -version` 确认版本

#### 问题：Maven依赖下载失败
**解决方案：**
1. 检查网络连接
2. 清理Maven缓存：`mvn clean`
3. 使用国内镜像源（修改 `~/.m2/settings.xml`）

### 2. 数据库连接问题

#### 问题：连接被拒绝
```
Connection refused: connect
```

**解决方案：**
1. 确保MySQL服务正在运行
2. 检查端口33006是否正确
3. 验证数据库用户名密码
4. 确保数据库 `library_reservation` 已创建

#### 问题：时区问题
```
The server time zone value 'XXX' is unrecognized
```

**解决方案：**
在数据库URL中添加时区参数：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:33006/library_reservation?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
```

### 3. 前端启动问题

#### 问题：Node.js版本过低
**解决方案：**
1. 升级到Node.js 14+
2. 使用nvm管理Node版本

#### 问题：依赖安装失败
**解决方案：**
1. 清理缓存：`npm cache clean --force`
2. 删除node_modules：`rm -rf node_modules`
3. 重新安装：`npm install`
4. 使用淘宝镜像：`npm install --registry=https://registry.npm.taobao.org`

#### 问题：端口被占用
```
Port 3000 is already in use
```

**解决方案：**
1. 修改端口：在 `vue.config.js` 中设置不同端口
2. 或杀死占用进程：`netstat -ano | findstr :3000`

### 4. 运行时问题

#### 问题：跨域请求失败
**解决方案：**
1. 确保后端 `WebConfig` 中配置了CORS
2. 检查前端代理配置 `vue.config.js`

#### 问题：Session失效
**解决方案：**
1. 确保请求携带Cookie：`withCredentials: true`
2. 检查后端Session配置

#### 问题：二维码生成失败
**解决方案：**
1. 确保安装了qrcode依赖：`npm install qrcode`
2. 检查Canvas支持

### 5. 数据库初始化

#### 执行SQL脚本
```bash
# 登录MySQL
mysql -u root -p -P 33006

# 创建数据库
CREATE DATABASE library_reservation DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 使用数据库
USE library_reservation;

# 执行脚本
source database_design.sql;
```

### 6. 开发环境配置

#### IDEA配置
1. 导入Maven项目
2. 设置JDK版本为8+
3. 启用自动导入
4. 配置代码格式化

#### VSCode配置
1. 安装Vue插件
2. 安装ESLint插件
3. 配置自动格式化

### 7. 生产环境部署

#### 后端打包
```bash
cd backend
mvn clean package -Dmaven.test.skip=true
java -jar target/library-reservation-1.0.0.jar
```

#### 前端打包
```bash
cd frontend
npm run build
# 将dist目录部署到Web服务器
```

### 8. 性能优化

#### 数据库优化
1. 添加索引
2. 优化查询语句
3. 配置连接池

#### 前端优化
1. 启用Gzip压缩
2. 使用CDN
3. 代码分割

### 9. 监控和日志

#### 后端日志
- 查看控制台输出
- 配置日志文件
- 使用日志级别

#### 前端调试
- 使用浏览器开发者工具
- 检查网络请求
- 查看控制台错误

### 10. 联系支持

如果以上方案都无法解决问题，请：
1. 收集错误日志
2. 描述复现步骤
3. 提供环境信息
4. 联系开发团队