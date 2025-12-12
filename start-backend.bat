@echo off
echo 启动图书馆座位预约系统后端...
cd backend
echo 正在编译项目...
call mvn clean compile
if %errorlevel% neq 0 (
    echo 编译失败，请检查错误信息
    pause
    exit /b 1
)
echo 编译成功，正在启动应用...
call mvn spring-boot:run
pause