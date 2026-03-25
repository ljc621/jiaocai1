# 教材循环平台 (Textbook Recycling Platform)

这是一个基于 Spring Boot + Vue 3 + MySQL 的全功能校园教材循环利用与社区交互平台。旨在解决高校教材闲置浪费问题，提供安全、便捷的二手教材交易、租赁及捐赠服务，并融合社区讨论功能，打造活跃的校园学习资源共享圈。

## ✨ 核心功能

### 👤 用户中心
- **身份认证**：手机号注册/登录，实名/学生认证状态管理。
- **个人档案**：头像、昵称、所属高校、信用分（Credit Score）体系。
- **资产管理**：
  - **钱包系统**：支持余额充值、提现、交易明细查询。
  - **积分体系**：积分获取与兑换（100积分 = 1元）。
- **地址管理**：多收货地址管理，支持默认地址设置。

### 📚 教材流转
- **多模式交易**：支持 **出售**、**租赁**（按日计费）、**捐赠** 三种流转模式。
- **商品发布**：支持上传封面、多图、ISBN、成色、分类等详细信息。
- **商品管理**：
  - **编辑功能**：发布后可修改教材信息（如价格、描述、图片）。
  - **上下架管理**：卖家可随时将教材下架（暂停售卖）或重新上架。
- **智能检索**：支持按关键词（标题/作者/ISBN）、分类、价格区间、排序方式筛选。
- **状态追踪**：商品状态实时更新（在售/已售/已租/已下架）。

### 🛒 交易系统
- **订单流程**：下单 -> 支付（余额扣款） -> 发货 -> 收货/评价。
- **资金安全**：交易资金流转记录，支持订单评价打分。

### 💬 互动社区
- **帖子类型**：支持 **提问**、**交易**、**测评**、**讨论** 等多种话题。
- **社交互动**：点赞、评论、浏览量统计。
- **内容关联**：用户信用分与社区表现挂钩。

### 🛡️ 管理后台
- **数据看板**：平台关键数据概览（用户数、交易额、活跃度等）。
- **内容监管**：
  - **用户管理**：查看用户信息、信用分调整。
  - **教材管理**：违规教材下架/删除。
  - **订单管理**：监控异常订单。
  - **社区管理**：帖子内容审核与删除。

## 🛠 技术栈

- **前端框架**: [Vue.js 3](https://vuejs.org/) (Composition API)
- **UI 库**: [Element Plus](https://element-plus.org/), [Tailwind CSS](https://tailwindcss.com/)
- **后端框架**: [Spring Boot 2.7](https://spring.io/projects/spring-boot)
- **数据库**: MySQL 8.0+
- **ORM**: [MyBatis Plus](https://baomidou.com/)
- **语言**: Java 17 (后端), TypeScript (前端)
- **安全**: Spring Security + JWT


## 🚀 快速开始

### 1. 环境准备
确保本地已安装：
- JDK 17+
- Node.js (v16+)
- Maven 3.6+

### 2. 后端启动 (Spring Boot)

本项目默认使用 H2 嵌入式数据库，无需额外安装 MySQL 即可运行。

```bash
cd backend
mvn spring-boot:run
```

后端服务将启动在 `http://localhost:8080`。
API 文档 (如有 Swagger): `http://localhost:8080/swagger-ui.html` (视配置而定)

### 3. 前端启动 (Vue 3)

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

访问应用：[http://localhost:5173](http://localhost:5173) (端口视 Vite 配置而定，通常为 5173)

### 4. 数据库配置 (可选)

如果需要使用 MySQL 替代 H2：
1. 修改 `backend/src/main/resources/application.yml`。
2. 将 `driver-class-name` 改为 `com.mysql.cj.jdbc.Driver`。
3. 更新 `url`, `username`, `password`。
4. 确保 MySQL 数据库已创建。

## 📂 目录结构

```
├── backend/               # Spring Boot 后端项目
│   ├── src/main/java/     # Java 源代码
│   │   ├── com/jiaocai/   # 业务包
│   │   │   ├── controller # 控制器层
│   │   │   ├── service    # 业务逻辑层
│   │   │   ├── entity     # 实体类
│   │   │   └── mapper     # MyBatis Mapper 接口
│   └── src/main/resources # 配置文件 (application.yml)
├── frontend/              # Vue.js 前端项目
│   ├── src/
│   │   ├── api/           # Axios 请求封装
│   │   ├── components/    # Vue 组件
│   │   ├── views/         # 页面视图
│   │   └── router/        # 路由配置
│   └── public/            # 静态资源
```

## ⚠️ 注意事项

- 本项目默认使用 MySQL 数据库，请确保本地 MySQL 服务已启动且 `.env` 配置正确。
- 图片上传功能目前可能使用临时存储或直接链接，生产环境建议接入对象存储服务（如 AWS S3, 阿里云 OSS）。
- 支付功能为模拟余额扣减，不涉及真实网银网关。
