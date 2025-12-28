# AI客服订单功能 - 快速开始指南

## 🚀 5分钟快速启动

### 步骤1: 确认后端服务运行

确保Spring Boot应用已启动:
```bash
# Windows
cd service
.\ry.bat

# 或者直接运行主类
# NeuEduApplication.java
```

### 步骤2: 测试订单查询功能

使用测试接口验证功能:
```bash
# 测试1: 判断是否为订单查询
curl "http://localhost:9091/api/ai/order-test/is-order-query?question=我的订单"

# 测试2: 查询订单
curl "http://localhost:9091/api/ai/order-test/query?question=我的订单&userId=1"

# 测试3: 查询特定订单(需要真实订单号)
curl "http://localhost:9091/api/ai/order-test/query?question=查询订单O20231227143021123456&userId=1"
```

### 步骤3: 打开前端示例

1. 打开文件: `AI客服订单查询前端示例.html`
2. 修改WebSocket地址(如果需要):
   ```javascript
   ws = new WebSocket('ws://localhost:9091/ai/chat');
   ```
3. 修改用户ID(替换为实际用户ID):
   ```javascript
   const userId = 1; // 改为实际用户ID
   ```
4. 在浏览器中打开文件

### 步骤4: 开始对话

在聊天框中输入以下问题测试:

#### 测试1: 查询所有订单
```
我的订单
```

#### 测试2: 查询待付款订单
```
我的待付款订单
```

#### 测试3: 查询特定订单
```
查询订单O20231227143021123456
```
(注意: 需要替换为真实的订单号)

#### 测试4: 查询已发货订单
```
已发货的订单
```

## 📝 常用查询语句示例

### 查询订单列表
```
我的订单
查看订单
订单列表
所有订单
```

### 按状态查询
```
待付款订单
已发货订单
已完成订单
取消的订单
```

### 查询特定订单
```
查询订单O20231227143021123456
订单O20231227143021123456到哪了
O20231227143021123456什么状态
```

### 物流查询
```
我的包裹到哪了
物流信息
发货了吗
什么时候到
```

## 🔍 调试技巧

### 1. 查看后端日志
```
[INFO] 处理订单查询请求：我的订单, userId=1
[INFO] 查询用户订单列表, userId=1, status=null
```

### 2. 查看浏览器控制台
按F12打开开发者工具,查看:
- WebSocket连接状态
- 发送/接收的消息
- JavaScript错误

### 3. 使用Postman测试
导入请求:
```
GET http://localhost:9091/api/ai/order-test/query
Params:
  - question: 我的订单
  - userId: 1
```

## ⚠️ 常见问题

### Q1: WebSocket连接失败
**原因**: 后端服务未启动或端口不正确
**解决**: 
1. 确认后端服务运行在9091端口
2. 检查防火墙设置
3. 查看后端日志确认WebSocket配置

### Q2: 查询不到订单
**原因**: userId不正确或数据库中无订单数据
**解决**:
1. 确认userId是否正确
2. 检查数据库是否有订单数据
3. 查看后端日志确认查询参数

### Q3: 前端显示"未连接"
**原因**: WebSocket地址不正确
**解决**:
1. 检查WebSocket地址配置
2. 确认端口号是否正确
3. 检查网络连接

### Q4: 订单号格式不正确
**原因**: 订单号格式必须是 O + 20位数字
**解决**: 使用正确格式的订单号,例如: O20231227143021123456

## 📊 测试数据准备

### 创建测试订单
```sql
-- 如果数据库中没有订单,可以手动插入测试数据
INSERT INTO order_info (
    order_no, user_id, status, status_desc, 
    payment, create_time, delete_flag
) VALUES (
    'O20231227143021123456', 1, 10, '待付款', 
    1299.00, NOW(), 0
);
```

### 查询现有订单
```sql
-- 查看用户1的所有订单
SELECT order_no, status, status_desc, payment 
FROM order_info 
WHERE user_id = 1 AND delete_flag = 0
ORDER BY create_time DESC
LIMIT 10;
```

## 🎯 下一步

完成基础测试后,可以:

1. **集成到现有前端项目**
   - 复制WebSocket连接代码
   - 集成到Vue/React组件中
   - 添加样式美化

2. **扩展功能**
   - 添加物流追踪
   - 添加订单评价
   - 支持订单修改/取消

3. **优化用户体验**
   - 添加订单状态图标
   - 支持订单搜索
   - 添加历史对话记录

## 📞 获取帮助

如果遇到问题:
1. 查看详细文档: `AI客服订单功能说明.md`
2. 查看实现总结: `AI客服订单功能实现总结.md`
3. 检查代码注释
4. 查看后端日志输出

---

**祝您使用愉快! 🎉**
