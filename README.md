# 网上商城（Vue3 + Laravel）

一个前后端分离的 B2C 网上商城示例项目：前端使用 **Vue 3 + Vite + Element Plus**，后端使用 **Laravel**，数据库为 **MySQL**。包含商品展示、购物车、订单、收货地址、个人中心以及后台管理（用户 / 商品 / 充值）等功能模块。

## 技术栈

| 层次 | 技术 |
| --- | --- |
| 前端 | Vue 3.4、Vite 5、Vue Router 4、Pinia、Element Plus、Axios |
| 后端 | Laravel 7、PHP 8.0、firebase/php-jwt、fruitcake/laravel-cors |
| 数据库 | MySQL 5.7+ |
| 其他 | RESTful API、JWT 登录鉴权、CORS 跨域 |

## 功能模块

### 用户端
- **首页**：轮播图、商品分类、热销商品、手机专区
- **全部商品 / 商品详情**（主图 + 多张子图）
- **购物车**：加入购物车、修改数量、删除
- **收货地址**：新增 / 修改 / 删除
- **订单**：下单、订单列表、订单详情、确认收货、结算
- **个人中心 / 账户充值**

### 管理端
- **用户管理**：列表、封禁 / 解封、删除
- **商品管理**：新增 / 修改 / 删除、分页查询
- **充值管理**：充值流水

### 登录鉴权
- 登录接口返回 JWT，前端保存后用于后续请求

## 目录结构

```
商场vue3+laravel/
├── g-eshop.sql                 # 数据库结构与示例数据
├── laravelY/                   # Laravel 后端
│   ├── app/Http/Controllers/   # 控制器（登录、首页、购物车、订单、用户、充值等）
│   ├── routes/api.php          # 后端 API 路由
│   ├── config/                 # 配置（数据库、CORS、JWT 等）
│   └── ...
└── vue-laravel-shopping/       # Vue3 前端
    ├── src/
    │   ├── views/              # 页面（首页、商品、购物车、订单、管理、登录、404）
    │   ├── router/             # 路由表
    │   ├── axios/              # axios 实例（请求后端）
    │   └── assets/             # 图片与样式
    └── ...
```

## 环境要求

- PHP >= 8.0（需要 `pdo_mysql`、`mbstring`、`openssl`、`tokenizer`、`xml`、`ctype`、`json` 扩展）
- Composer 2.x
- Node.js >= 18、npm
- MySQL 5.7 及以上

## 快速开始

### 1. 导入数据库

创建数据库 `g-eshop`，并导入根目录的 `g-eshop.sql`：

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS \`g-eshop\` DEFAULT CHARSET utf8mb4;"
mysql -u root -p g-eshop < g-eshop.sql
```

> 也可以通过 Navicat / phpMyAdmin 直接导入 `g-eshop.sql`。

### 2. 启动后端

```bash
cd laravelY
copy .env.example .env          # Windows；macOS/Linux 使用 cp .env.example .env
composer install
php artisan key:generate
php artisan jwt:secret          # 生成 JWT_SECRET
```

编辑 `laravelY/.env`，按本机 MySQL 实际情况填写：

```ini
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=g-eshop
DB_USERNAME=root
DB_PASSWORD=你的密码
```

启动后端服务（默认 8000 端口）：

```bash
php artisan serve --port=8000
```

### 3. 启动前端

```bash
cd vue-laravel-shopping
npm install
npm run dev
```

浏览器访问 <http://localhost:5173/>。

> 前端通过 `src/axios/request.js` 中的 `baseURL` 请求后端（默认 `http://localhost:8000/api`）。如后端端口不同，请同步修改该地址。

## 默认账号

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `123456` |

## 接口约定

- 基础地址：`http://localhost:8000/api`
- 返回格式：`{ "code": 200, "status": 20000, "data": ..., "message": ... }`
- 主要接口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/login` | 登录（返回 JWT） |
| GET | `/gate/{id}` | 首页分类 / 楼层数据 |
| GET | `/product/hot` | 热销商品 |
| GET | `/product/phone` | 手机专区 |
| GET | `/product/{id}` | 商品详情 |
| GET | `/page/product/whole` | 商品分页 |
| GET / POST / DELETE | `/cart`、`/cart/{id}`、`/cart/add` | 购物车 |
| GET / POST / DELETE | `/address`、`/address/{id}` | 收货地址 |
| GET / POST | `/order`、`/order/{id}`、`/order/detail/{id}` | 订单 |
| GET / POST / DELETE | `/user`、`/user/{id}`、`/users/page`、`/users/re/{id}` | 用户 |
| POST / GET | `/recharge/info`、`/recharge`、`/sss/balance/{id}` | 充值 |

## 说明

- 本项目为课程 / 学习用途的前后端分离示例，接口未做完善的权限校验与参数防护，请勿直接用于生产环境。
- 商品图片存放在 `vue-laravel-shopping/src/assets/images`，数据库中的 `main_image` / `sub_images` 字段保存相对路径。
