@echo off
chcp 65001 >nul
echo ================================
echo AI客服订单查询功能测试脚本
echo ================================
echo.

set BASE_URL=http://localhost:9091

echo [测试1] 检测订单查询意图
echo 请求: %BASE_URL%/api/ai/order-test/is-order-query?question=我的订单
curl -s "%BASE_URL%/api/ai/order-test/is-order-query?question=我的订单"
echo.
echo.

echo [测试2] 查询用户订单列表
echo 请求: %BASE_URL%/api/ai/order-test/query?question=我的订单^&userId=1
curl -s "%BASE_URL%/api/ai/order-test/query?question=我的订单&userId=1"
echo.
echo.

echo [测试3] 查询待付款订单
echo 请求: %BASE_URL%/api/ai/order-test/query?question=待付款订单^&userId=1
curl -s "%BASE_URL%/api/ai/order-test/query?question=待付款订单&userId=1"
echo.
echo.

echo [测试4] 查询已发货订单
echo 请求: %BASE_URL%/api/ai/order-test/query?question=已发货订单^&userId=1
curl -s "%BASE_URL%/api/ai/order-test/query?question=已发货订单&userId=1"
echo.
echo.

echo ================================
echo 测试完成!
echo ================================
echo.
echo 提示:
echo 1. 确保后端服务运行在 %BASE_URL%
echo 2. 确保数据库中有测试订单数据
echo 3. userId=1 可以根据实际情况修改
echo.
pause
