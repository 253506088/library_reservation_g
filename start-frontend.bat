cd frontend
npm run serve

@REM @echo off
@REM echo 启动图书馆座位预约系统前端...
@REM cd frontend
@REM
@REM echo 检查Node.js版本...
@REM node --version
@REM if %errorlevel% neq 0 (
@REM     echo 请先安装Node.js 14+
@REM     pause
@REM     exit /b 1
@REM )
@REM
@REM echo 正在安装依赖...
@REM call npm install
@REM if %errorlevel% neq 0 (
@REM     echo 依赖安装失败，尝试清理缓存...
@REM     call npm cache clean --force
@REM     echo 重新安装依赖...
@REM     call npm install
@REM     if %errorlevel% neq 0 (
@REM         echo 依赖安装失败，请检查网络连接或使用淘宝镜像：
@REM         echo npm install --registry=https://registry.npm.taobao.org
@REM         pause
@REM         exit /b 1
@REM     )
@REM )
@REM
@REM echo 依赖安装成功，正在启动开发服务器...
@REM echo 前端将在 http://localhost:3000 启动
@REM echo 学生端访问：http://localhost:3000/#/mobile
@REM echo 管理端访问：http://localhost:3000/#/admin
@REM call npm run serve
@REM pause