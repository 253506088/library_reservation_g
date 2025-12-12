@echo off
echo 启动图书馆座位预约系统前端...
cd frontend
echo 正在安装依赖...
call npm install
if %errorlevel% neq 0 (
    echo 依赖安装失败，请检查网络连接
    pause
    exit /b 1
)
echo 依赖安装成功，正在启动开发服务器...
call npm run serve
pause