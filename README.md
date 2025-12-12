# 图书馆座位预约信息管理系统

## 项目简介

这是一个基于Spring Boot + Vue的图书馆座位预约管理系统，支持学生预约座位和管理员管理功能。
本项目为贺东泽临沂大学2024级本科毕业设计论文的案例项目。

## 技术栈

### 后端
- Spring Boot 2.7.18
- MySQL 5.7
- MyBatis Plus 3.5.3.1
- JDK 8
- Hutool工具库

### 前端
- Vue 2.6.14
- Element UI (PC端)
- Vant (移动端)
- Axios
- Vuex
- Vue Router

## 功能特性

### 学生端（移动端）
- 用户注册/登录
- 选择时间、图书馆、座位进行预约
- 查看预约记录
- 生成预约二维码
- 取消预约

### 管理员端（PC端）
- 管理员登录
- 图书馆管理（增删改查）
- 座位管理（增删改查）
- 预约记录管理
- 扫码验证预约

### 系统特性
- 预约冲突检测
- 自动处理过期预约
- 流水号自动生成
- 定时任务处理

## 项目结构

```
library-reservation-system/
├── backend/                    # Spring Boot后端
│   ├── src/main/java/com/library/
│   │   ├── controller/         # 控制器
│   │   ├── service/           # 服务层
│   │   ├── mapper/            # 数据访问层
│   │   ├── entity/            # 实体类
│   │   ├── dto/               # 数据传输对象
│   │   ├── vo/                # 视图对象
│   │   ├── utils/             # 工具类
│   │   └── config/            # 配置类
│   └── src/main/resources/
│       ├── application.yml    # 配置文件
│       └── mapper/            # MyBatis XML
├── frontend/                   # Vue前端
│   ├── src/
│   │   ├── views/            # 页面组件
│   │   │   ├── mobile/       # 移动端页面
│   │   │   └── admin/        # 管理端页面
│   │   ├── api/              # API接口
│   │   ├── utils/            # 工具函数
│   │   ├── router/           # 路由配置
│   │   └── store/            # 状态管理
│   └── package.json
└── database_design.sql        # 数据库设计
```

## 安装部署

### 1. 环境要求

- JDK 8+
- MySQL 5.7+
- Node.js 14+
- Maven 3.6+

### 2. 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE library_reservation DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行数据库脚本：
```bash
mysql -u root -p library_reservation < database_design.sql
```

3. 修改数据库连接配置（如需要）：
编辑 `backend/src/main/resources/application.yml`

### 3. 后端启动

1. 进入后端目录：
```bash
cd backend
```

2. 使用Maven启动：
```bash
# 清理并编译
mvn clean compile

# 启动应用
mvn spring-boot:run
```

或者使用IDE：
- 导入Maven项目
- 直接运行 `com.library.LibraryApplication` 主类

后端服务将在 `http://localhost:8080/api` 启动

### 4. 前端启动

1. 进入前端目录：
```bash
cd frontend
```

2. 安装依赖：
```bash
# 使用npm
npm install

# 或使用yarn
yarn install
```

3. 启动开发服务器：
```bash
# 使用npm
npm run serve

# 或使用yarn
yarn serve
```

前端服务将在 `http://localhost:3000` 启动

### 5. 验证安装

1. 访问后端API：http://localhost:8080/api/auth/current
2. 访问前端页面：http://localhost:3000
3. 检查数据库连接是否正常

## 使用说明

### 默认账号

系统初始化时会创建以下测试账号：

**管理员账号：**
- 用户名：admin
- 密码：admin

**学生账号：**
- 用户名：student001
- 密码：student001
- 用户名：student002
- 密码：student002

### 访问地址

- **移动端（学生）：** http://localhost:3000/#/mobile
- **PC端（管理员）：** http://localhost:3000/#/admin
- **首页（自动跳转）：** http://localhost:3000

### 预约流程

1. **学生预约：**
   - 登录移动端
   - 选择预约时间
   - 选择图书馆
   - 选择可用座位
   - 确认预约
   - 获取预约二维码

2. **管理员验证：**
   - 登录管理端
   - 使用扫码功能
   - 扫描学生二维码
   - 确认签到

### 预约规则

1. 同一用户同一时间段只能预约一个座位
2. 同一座位同一时间段只能被一个用户预约
3. 预约时间不能是过去时间
4. 允许迟到签到，但不能超过预约结束时间
5. 超过预约结束时间未签到将自动标记为"爽约"
6. 预约时间精确到小时（8:00-22:00），无需选择分钟

## API接口

### 认证接口
- POST `/api/auth/login` - 用户登录
- POST `/api/auth/register` - 用户注册
- POST `/api/auth/logout` - 用户登出
- GET `/api/auth/current` - 获取当前用户

### 图书馆接口
- GET `/api/library/list` - 获取图书馆列表
- GET `/api/library/page` - 分页查询图书馆
- POST `/api/library` - 新增图书馆
- PUT `/api/library/{id}` - 更新图书馆
- DELETE `/api/library/{id}` - 删除图书馆

### 座位接口
- GET `/api/seat/library/{libraryId}` - 获取图书馆座位
- GET `/api/seat/available` - 获取可用座位
- POST `/api/seat` - 新增座位
- PUT `/api/seat/{id}` - 更新座位
- DELETE `/api/seat/{id}` - 删除座位

### 预约接口
- POST `/api/reservation` - 创建预约
- PUT `/api/reservation/{id}/cancel` - 取消预约
- POST `/api/reservation/checkin` - 扫码签到
- GET `/api/reservation/page` - 分页查询预约记录

## 注意事项

1. 确保MySQL服务正在运行，端口为33006
2. 确保数据库连接信息正确
3. 前端代理配置指向后端服务地址
4. 移动端和PC端使用不同的UI组件库，会根据设备自动切换
5. 定时任务每5分钟检查一次过期预约，每天凌晨重置流水号

## 开发说明

- 后端使用MyBatis Plus简化数据库操作
- 前端使用Vuex进行状态管理
- 支持跨域请求和Session管理
- 二维码使用QRCode.js生成
- 时间格式统一使用 `yyyy-MM-dd HH:mm:ss`

## 故障排除

1. **数据库连接失败：** 检查MySQL服务状态和连接配置
2. **前端无法访问后端：** 检查代理配置和后端服务状态
3. **登录失败：** 检查用户类型和权限配置
4. **预约冲突：** 检查时间格式和冲突检测逻辑

## 联系方式

如有问题，请联系开发团队。

## 🔧 故障排除

### 前端编译错误修复

如果遇到前端编译错误，请按以下步骤操作：

#### 方法1：使用修复脚本（推荐）
```bash
双击 fix-frontend.bat
```

#### 方法2：手动修复
```bash
cd frontend
# 删除依赖目录
rm -rf node_modules
# 删除锁定文件
rm package-lock.json
# 清理缓存
npm cache clean --force
# 重新安装
npm install
```

#### 方法3：使用国内镜像
```bash
npm install --registry=https://registry.npm.taobao.org
```

### 后端编译错误修复

```bash
cd backend
mvn clean compile
# 如果还有问题，查看详细错误
mvn clean compile -X
```

### 常见问题

1. **路由文件找不到**：已修复，移除了不存在的 Reservation.vue 引用
2. **依赖安装失败**：使用 fix-frontend.bat 脚本自动修复
3. **端口被占用**：修改 vue.config.js 中的端口配置
4. **数据库连接失败**：检查 MySQL 服务和端口配置

更多详细的故障排除信息请参考：`TROUBLESHOOTING.md`

首页（导航页面）：http://localhost:3000
移动端登录：http://localhost:3000/#/mobile/login
管理端登录：http://localhost:3000/#/admin/login

