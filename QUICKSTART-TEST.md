# OpenOrderController 测试快速入门

## 快速开始

### 方式一：使用PowerShell脚本（推荐）

#### 1. 运行所有测试
```powershell
.\run-order-tests.ps1
```

#### 2. 只运行单元测试（快速）
```powershell
.\run-order-tests.ps1 -TestType unit
```

#### 3. 只运行集成测试
```powershell
.\run-order-tests.ps1 -TestType integration
```

#### 4. 运行单个测试方法
```powershell
.\run-order-tests.ps1 -TestType single -TestMethod testPreCreateOrder_Success
```

### 方式二：使用Maven命令

```bash
# 进入项目目录
cd d:\Project\Aite\fuint-food-boot

# 运行所有测试
mvn test -f fuint-application/pom.xml

# 运行单元测试
mvn test -f fuint-application/pom.xml -Dtest=OpenOrderControllerTest

# 运行集成测试
mvn test -f fuint-application/pom.xml -Dtest=OpenOrderControllerIntegrationTest

# 运行指定测试方法
mvn test -f fuint-application/pom.xml -Dtest=OpenOrderControllerTest#testPreCreateOrder_Success
```

### 方式三：使用IDE

#### IntelliJ IDEA
1. 打开文件：`OpenOrderControllerTest.java`
2. 右键点击类名 → 选择 "Run 'OpenOrderControllerTest'"
3. 或者点击类名左侧的绿色运行按钮

---

## 测试说明

### 单元测试 vs 集成测试

| 特性 | 单元测试 | 集成测试 |
|------|---------|---------|
| 执行速度 | 快（秒级） | 慢（需启动Spring） |
| 数据库依赖 | 不需要 | 需要 |
| 适用场景 | 开发阶段频繁运行 | 发布前完整验证 |
| 推荐频率 | 每次代码修改 | 提交代码前 |

### 建议执行顺序
1. ✅ **先运行单元测试** - 快速验证业务逻辑
2. ✅ **再运行集成测试** - 完整验证API功能

---

## 测试覆盖的接口

| 接口 | 方法 | 路径 | 测试覆盖 |
|------|------|------|---------|
| 订单预创建 | POST | `/api/v1/order/pre-create` | ✅ |
| 创建订单 | POST | `/api/v1/order/create` | ✅ |
| 取消订单 | POST | `/api/v1/order/cancel` | ✅ |
| 支付订单 | POST | `/api/v1/order/pay` | ✅ |
| 订单退款 | POST | `/api/v1/order/refund` | ✅ |
| 订单详情 | GET | `/api/v1/order/detail/{id}` | ✅ |
| 订单列表 | GET | `/api/v1/order/list` | ✅ |
| 订单评价 | POST | `/api/v1/order/evaluate` | ✅ |
| 评价列表 | GET | `/api/v1/order/evaluations` | ✅ |

---

## 集成测试准备（仅首次需要）

### 1. 启动数据库
确保MySQL服务正在运行

### 2. 创建测试数据
```sql
-- 创建测试用户
INSERT INTO mt_user (id, name, mobile, status, create_time, update_time) 
VALUES (1, '测试用户', '13800138000', 'A', NOW(), NOW());

-- 创建测试商品
INSERT INTO mt_goods (id, name, price, line_price, logo, status, create_time, update_time)
VALUES (1, '测试商品', 50.00, 60.00, '/images/test.jpg', 'A', NOW(), NOW());

-- 创建测试SKU
INSERT INTO mt_goods_sku (id, goods_id, sku_no, price, stock, status, create_time, update_time)
VALUES (1, 1, 'SKU001', 50.00, 100, 'A', NOW(), NOW());
```

### 3. 配置测试环境
创建 `src/test/resources/application-test.yaml`（如未创建）

---

## 查看测试结果

### 控制台输出
测试运行时会在控制台实时显示：
- ✅ 通过的测试（绿色）
- ❌ 失败的测试（红色）
- 测试统计信息

### 测试报告文件
测试完成后，报告位于：
```
fuint-application/target/surefire-reports/
```

### 生成HTML报告
```powershell
mvn surefire-report:report -f fuint-application/pom.xml
```
报告位置：`fuint-application/target/site/surefire-report.html`

---

## 常见测试场景示例

### 场景1：验证订单创建流程
```powershell
# 运行订单创建相关测试
.\run-order-tests.ps1 -TestType single -TestMethod testCreateOrder_Success
```

### 场景2：验证价格校验逻辑
```powershell
# 测试价格不一致场景
.\run-order-tests.ps1 -TestType single -TestMethod testCreateOrder_PriceMismatch
```

### 场景3：验证退款流程
```powershell
# 测试已支付订单取消（含退款）
.\run-order-tests.ps1 -TestType single -TestMethod testCancelOrder_PaidWithRefund
```

---

## 故障排查

### 问题1：PowerShell脚本无法运行
**错误**: "无法加载，因为在此系统上禁止运行脚本"

**解决**:
```powershell
# 以管理员身份运行PowerShell，执行：
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### 问题2：Maven命令未找到
**错误**: "mvn不是内部或外部命令"

**解决**:
1. 下载并安装Maven
2. 配置环境变量 `MAVEN_HOME`
3. 将 `%MAVEN_HOME%\bin` 添加到 `PATH`
4. 重启PowerShell

### 问题3：测试编译失败
**解决**:
```bash
# 先清理并编译
mvn clean compile -f fuint-application/pom.xml
# 再运行测试
mvn test -f fuint-application/pom.xml
```

### 问题4：集成测试连接数据库失败
**解决**:
1. 检查数据库服务是否启动
2. 验证 `application.yaml` 中的数据库配置
3. 确认用户名和密码正确

---

## 测试最佳实践

### ✅ 推荐做法
- 每次修改代码后运行单元测试
- 提交代码前运行集成测试
- 定期查看测试覆盖率报告
- 保持测试数据独立，不依赖生产数据

### ❌ 避免做法
- 跳过测试直接提交代码
- 在生产数据库上运行测试
- 测试用例之间相互依赖
- 忽略失败的测试用例

---

## 下一步

- 📖 阅读完整测试文档：[README-TEST.md](./fuint-application/src/test/java/com/fuint/openapi/v1/order/README-TEST.md)
- 🔍 查看测试代码：
  - [单元测试](./fuint-application/src/test/java/com/fuint/openapi/v1/order/OpenOrderControllerTest.java)
  - [集成测试](./fuint-application/src/test/java/com/fuint/openapi/v1/order/OpenOrderControllerIntegrationTest.java)
- 🚀 开始编写新的测试用例

---

**快速命令参考**

```powershell
# 最常用的命令
.\run-order-tests.ps1 -TestType unit          # 单元测试（推荐日常使用）
.\run-order-tests.ps1 -TestType integration   # 集成测试（提交前运行）
.\run-order-tests.ps1                         # 运行所有测试
```

**测试愉快！** 🎉
