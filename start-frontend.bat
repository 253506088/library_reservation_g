@echo off
echo 启动图书馆座位预约系统前端...
cd frontend

echo 检查Node.js版本...
node --version
if %errorlevel% neq 0 (
    echo 请先安装Node.js 14+
    pause
    exit /b 1
)

echo 正在安装依赖...
call npm install
if %errorlevel% neq 0 (
    echo 依赖安装失败，尝试清理缓存...
    call npm cache clean --force
    echo 重新安装依赖...
    call npm install
    if %errorlevel% neq 0 (
        echo 依赖安装失败，请检查网络连接或使用淘宝镜像：
        echo npm install --registry=https://registry.npm.taobao.org
        pause
        exit /b 1
    )
)

echo 依赖安装成功，正在启动开发服务器...
echo 前端将在 http://localhost:3000 启动
echo 学生端访问：http://localhost:3000/#/mobile
echo 管理端访问：http://localhost:3000/#/admin
call npm run serve
pause