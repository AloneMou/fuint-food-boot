# OpenOrderController 测试说明文档

## 测试概述

本文档描述了 `OpenOrderController` 的测试用例设计和执行方法。测试分为两类：
1. **单元测试** - 使用 Mockito 模拟依赖，快速验证业务逻辑
2. **集成测试** - 启动完整Spring Boot环境，测试完整的API调用流程

---

## 测试文件说明

### 1. OpenOrderControllerTest.java (单元测试)
**位置**: `src/test/java/com/fuint/openapi/v1/order/OpenOrderControllerTest.java`

**特点**:
- 使用 Mockito 框架模拟所有依赖服务
- 不需要数据库和完整Spring容器
- 执行速度快，适合开发阶段频繁运行
- 覆盖所有业务逻辑分支

**测试用例列表**:

| 测试方法 | 测试场景 | 验证点 |
|---------|---------|-------|
| `testPreCreateOrder_Success` | 订单预创建成功 | 价格计算正确、优惠券匹配、商品信息完整 |
| `testPreCreateOrder_UserNotFound` | 用户不存在 | 返回404错误码 |
| `testCreateOrder_Success` | 创建订单成功 | 订单创建成功、返回订单信息 |
| `testCreateOrder_PriceMismatch` | 价格不一致 | 返回400错误，提示重新下单 |
| `testCancelOrder_Unpaid` | 取消未支付订单 | 订单取消成功 |
| `testCancelOrder_PaidWithRefund` | 取消已支付订单 | 自动触发退款 |
| `testCancelOrder_OrderNotFound` | 订单不存在 | 返回404错误码 |
| `testPayOrder_Success` | 支付订单成功 | 订单状态更新、触发回调 |
| `testPayOrder_OrderNotFound` | 订单不存在 | 返回404错误码 |
| `testRefundOrder_Success` | 订单退款成功 | 退款处理成功、触发回调 |
| `testGetOrderDetail_Success` | 获取订单详情 | 返回完整订单信息和队列信息 |
| `testGetOrderDetail_NotFound` | 订单不存在 | 返回404错误码 |
| `testGetOrderList_Success` | 订单列表查询 | 返回分页数据 |
| `testEvaluateOrder_Success` | 订单评价成功 | 评价数据保存成功 |
| `testGetEvaluations_Success` | 评价列表查询 | 返回评价列表 |
| `testGetEvaluations_WithSkuFilter` | 按SKU筛选评价 | SKU筛选逻辑正确 |

### 2. OpenOrderControllerIntegrationTest.java (集成测试)
**位置**: `src/test/java/com/fuint/openapi/v1/order/OpenOrderControllerIntegrationTest.java`

**特点**:
- 启动完整的Spring Boot应用上下文
- 使用真实的数据库连接
- 测试完整的HTTP请求/响应流程
- 适合发布前的完整性验证

**测试用例列表**:

| 测试方法 | API端点 | HTTP方法 | 测试内容 |
|---------|---------|---------|---------|
| `testPreCreateOrderAPI` | `/api/v1/order/pre-create` | POST | 预创建订单API |
| `testCreateOrderAPI` | `/api/v1/order/create` | POST | 创建订单API |
| `testGetOrderDetailAPI` | `/api/v1/order/detail/{id}` | GET | 订单详情API |
| `testGetOrderListAPI` | `/api/v1/order/list` | GET | 订单列表API |
| `testCancelOrderAPI` | `/api/v1/order/cancel` | POST | 取消订单API |
| `testPayOrderAPI` | `/api/v1/order/pay` | POST | 支付订单API |
| `testRefundOrderAPI` | `/api/v1/order/refund` | POST | 订单退款API |
| `testEvaluateOrderAPI` | `/api/v1/order/evaluate` | POST | 订单评价API |
| `testGetEvaluationsAPI` | `/api/v1/order/evaluations` | GET | 评价列表API |

---

## 运行测试

### 方式一：使用 Maven 命令

#### 1. 运行所有测试
```bash
mvn test
```

#### 2. 只运行单元测试
```bash
mvn test -Dtest=OpenOrderControllerTest
```

#### 3. 只运行集成测试
```bash
mvn test -Dtest=OpenOrderControllerIntegrationTest
```

#### 4. 运行单个测试方法
```bash
mvn test -Dtest=OpenOrderControllerTest#testPreCreateOrder_Success
```

#### 5. 跳过测试（构建时）
```bash
mvn clean package -DskipTests
```

### 方式二：使用 IDE 运行

#### IntelliJ IDEA
1. 打开测试类文件
2. 点击类名或方法名旁的绿色运行按钮
3. 选择 "Run '测试类名'" 或 "Debug '测试类名'"

#### Eclipse
1. 右键点击测试类
2. 选择 "Run As" -> "JUnit Test"

### 方式三：使用 PowerShell 脚本

创建 `run-tests.ps1`:
```powershell
# 进入项目目录
Set-Location "d:\Project\Aite\fuint-food-boot"

# 运行所有测试
Write-Host "开始运行测试..." -ForegroundColor Green
mvn test -f fuint-application/pom.xml

# 检查测试结果
if ($LASTEXITCODE -eq 0) {
    Write-Host "测试全部通过!" -ForegroundColor Green
} else {
    Write-Host "测试失败，请查看日志" -ForegroundColor Red
}
```

---

## 测试前准备

### 单元测试（无需准备）
单元测试使用Mock对象，不需要任何外部依赖，可以直接运行。

### 集成测试准备工作

#### 1. 数据库准备
确保数据库服务已启动，并执行以下SQL创建测试数据：

```sql
-- 创建测试用户
INSERT INTO mt_user (id, name, mobile, status, create_time, update_time) 
VALUES (1, '测试用户', '13800138000', 'A', NOW(), NOW());

-- 创建测试商品
INSERT INTO mt_goods (id, name, price, line_price, logo, status, create_time, update_time)
VALUES (1, '测试商品', 50.00, 60.00, '/images/test.jpg', 'A', NOW(), NOW());

-- 创建测试商品SKU
INSERT INTO mt_goods_sku (id, goods_id, sku_no, price, stock, status, create_time, update_time)
VALUES (1, 1, 'SKU001', 50.00, 100, 'A', NOW(), NOW());

-- 创建测试门店
INSERT INTO mt_store (id, name, merchant_id, status, create_time, update_time)
VALUES (1, '测试门店', 1, 'A', NOW(), NOW());
```

#### 2. 配置文件
创建测试配置文件 `src/test/resources/application-test.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fuint_test?useUnicode=true&characterEncoding=utf-8&useSSL=false
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: none

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

logging:
  level:
    com.fuint: DEBUG
```

#### 3. 修改测试类中的测试数据
根据实际数据库中的数据，修改测试类中的ID值：
- 用户ID (`userId`)
- 商品ID (`goodsId`)
- SKU ID (`skuId`)
- 订单ID (`orderId`)

---

## 测试覆盖范围

### 功能覆盖
- ✅ 订单预创建（实时算价）
- ✅ 订单创建
- ✅ 订单取消（含退款）
- ✅ 订单支付
- ✅ 订单退款
- ✅ 订单详情查询
- ✅ 订单列表查询
- ✅ 订单评价
- ✅ 评价列表查询

### 异常场景覆盖
- ✅ 用户不存在
- ✅ 订单不存在
- ✅ 价格不一致
- ✅ 已支付订单取消（自动退款）
- ✅ SKU筛选评价

### 业务规则验证
- ✅ 价格计算正确性
- ✅ 优惠券匹配逻辑
- ✅ 积分使用逻辑
- ✅ 队列信息计算
- ✅ 事件回调触发

---

## 测试报告

### 查看测试报告
Maven测试完成后，会在以下目录生成测试报告：
```
fuint-application/target/surefire-reports/
```

报告文件包括：
- `TEST-*.xml` - JUnit XML格式报告
- `*.txt` - 文本格式报告

### 生成HTML测试报告
可以使用 Maven Surefire Report 插件生成HTML报告：

```bash
mvn surefire-report:report
```

报告将生成在：`target/site/surefire-report.html`

---

## 常见问题

### Q1: 单元测试运行失败，提示找不到类
**解决方案**: 确保已执行 `mvn clean compile` 编译项目

### Q2: 集成测试无法连接数据库
**解决方案**: 
1. 检查数据库服务是否启动
2. 验证 `application-test.yaml` 中的数据库配置
3. 确认数据库用户权限

### Q3: 集成测试提示数据不存在
**解决方案**: 按照"测试前准备"章节执行测试数据初始化SQL

### Q4: Mock对象返回null
**解决方案**: 检查 `@Mock` 和 `@InjectMocks` 注解是否正确使用，确保使用了 `@RunWith(MockitoJUnitRunner.class)`

### Q5: 测试运行时间过长
**解决方案**: 
- 单元测试应该很快，如果慢可能是依赖注入问题
- 集成测试需要启动Spring容器，首次运行较慢是正常的

---

## 持续集成

### Jenkins 集成
在 Jenkins Pipeline 中添加测试阶段：

```groovy
stage('Test') {
    steps {
        sh 'mvn test -f fuint-application/pom.xml'
    }
    post {
        always {
            junit 'fuint-application/target/surefire-reports/*.xml'
        }
    }
}
```

### GitLab CI 集成
在 `.gitlab-ci.yml` 中添加：

```yaml
test:
  stage: test
  script:
    - mvn test -f fuint-application/pom.xml
  artifacts:
    reports:
      junit: fuint-application/target/surefire-reports/TEST-*.xml
```

---

## 最佳实践

1. **频繁运行单元测试** - 每次代码修改后都应运行单元测试
2. **定期运行集成测试** - 提交代码前运行一次集成测试
3. **保持测试数据独立** - 不要依赖生产环境数据
4. **及时更新测试用例** - 业务逻辑变更时同步更新测试
5. **关注测试覆盖率** - 目标至少达到80%的代码覆盖率
6. **隔离测试环境** - 使用独立的测试数据库

---

## 扩展测试

### 性能测试
可以使用 JMeter 或 Gatling 进行接口性能测试：
- 订单创建并发测试
- 订单列表查询压力测试
- 支付接口响应时间测试

### 安全测试
- SQL注入测试
- XSS攻击测试
- 参数篡改测试
- 权限越权测试

---

## 联系方式

如有问题，请联系：
- 项目负责人：[姓名]
- 测试负责人：[姓名]
- 邮箱：[email@example.com]

---

**最后更新时间**: 2026-01-16
