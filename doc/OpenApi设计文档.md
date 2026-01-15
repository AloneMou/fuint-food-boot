## 安克咖啡 OpenAPI 设计文档（草案）

> **说明**：本设计文档基于《需求文档.md》整理，仅定义接口规范，不包含任何具体代码实现。可作为后续基于 OpenAPI/Swagger（如 `openapi.yaml`）文件编写的蓝本。

---

## 一、总览

- **接口风格**：RESTful API
- **协议**：仅支持 `HTTPS`，TLS 版本不低于 `TLS 1.2`
- **数据格式**：`application/json; charset=utf-8`
- **版本号**：推荐通过 URL 前缀控制，如：`/api/v1/...`
- **认证机制**：支持以下至少一种或组合
  - OAuth2（Client Credentials / Authorization Code 等）
  - JWT Token（Bearer Token）
  - API Key（Header 传递）
- **安全机制**：
  - 接口限流（按应用、按 IP、按接口粒度）
  - IP 白名单（由平台方配置）
  - 请求签名（可选，建议对关键接口采用）

---

## 二、技术与集成要求设计

### 2.1 RESTful API 架构

- **资源命名**：
  - 用户：`/api/v1/users`
  - 订单：`/api/v1/orders`
  - 商品：`/api/v1/products`
  - 回调：`/api/v1/webhook/...`
- **HTTP 方法语义**：
  - `GET`：查询
  - `POST`：创建、复杂查询、动作（支付、取消等）
  - `PUT/PATCH`：更新（本方案主要使用 `POST` 承载动作）
  - `DELETE`：删除（当前需求暂无）

### 2.2 安全协议（HTTPS/TLS 1.2+）

- 所有接口仅通过 `HTTPS` 暴露，拒绝 `HTTP` 访问。
- 支持 TLS1.2 / TLS1.3，禁用弱加密套件（如 RC4）。
- 对接方需配置服务器证书信任（若为自签名证书需另行说明）。

### 2.3 身份认证与授权

#### 2.3.1 OAuth2 / JWT / API Key 模式

- **OAuth2 Token 获取示例接口（示意）**

  - **路径**：`/oauth/token`
  - **方法**：`POST`
  - **请求参数（application/x-www-form-urlencoded）**：
    - `grant_type`：`client_credentials` / `authorization_code`
    - `client_id`：应用 ID
    - `client_secret`：应用密钥
    - `code`：授权码（authorization_code 场景）
  - **响应字段**：
    - `access_token`：访问令牌（JWT 或随机字符串）
    - `token_type`：`Bearer`
    - `expires_in`：过期时间（秒）
    - `scope`：权限范围

- **JWT / API Key 使用方式**

  - Header 中：
    - `Authorization: Bearer <a ccess_token>`
    - 或 `X-API-Key: <api_key>`
  - 所有业务 API 均需携带上述至少一种认证凭证。

### 2.4 接口限流设计

- **限流粒度**：
  - 每应用（`client_id`）
  - 每 IP
  - 每接口路径
- **建议策略**：
  - 如：单应用 1000 次/分钟，单 IP 300 次/分钟，单接口 200 次/分钟（可按业务调整）
- **超限响应**：
  - HTTP 状态码：`429 Too Many Requests`
  - 业务错误码：`429000`
  - 响应示例：
    ```json
    {
      "code": "429000",
      "message": "请求过于频繁，请稍后重试",
      "traceId": "xxx",
      "data": null
    }
    ```

### 2.5 IP 白名单策略

- 对接方提供**固定出口 IP** 列表。
- 平台配置白名单，仅允许白名单 IP 调用 API。
- 白名单不通过接口暴露，由运维/配置系统管理。

### 2.6 事件推送机制设计

- **推送方式**：HTTP `POST`，`Content-Type: application/json`
- **推送目标**：对接方在平台配置的回调 URL
- **签名机制（推荐）**：
  - Header：
    - `X-Signature`: 请求体签名（如 `HMAC-SHA256(body + timestamp, secret)`）
    - `X-Timestamp`: 时间戳（毫秒）
    - `X-Nonce`: 随机数
  - 对接方需按约定算法验签。
- **重试机制**：
  - 对接方返回 HTTP 2xx 视为成功。
  - 非 2xx 或超时视为失败，按退避策略重试，例如：
    - 第 1 次失败：5 秒后重试
    - 第 2 次失败：30 秒后重试
    - 第 3 次失败：5 分钟后重试
  - 超过最大重试次数（如 5 次）则标记为失败，可提供查询/补单接口。
- **幂等性**：
  - 每次推送携带 `eventId`，对接方需按 `eventId` 做幂等处理。

---

## 三、通用约定

### 3.1 基础信息

- **Base URL 示例**：
  - `https://api.ankcoffee.com/api/v1`
- **字符编码**：`UTF-8`
- **时间格式**：`yyyy-MM-dd HH:mm:ss`（或 ISO8601：`yyyy-MM-dd'T'HH:mm:ssZ`）
- **货币单位**：统一为「分」（整数），字段命名如 `amount`, `price`, `originalPrice` 等。

### 3.2 公共请求头

- `Authorization`：`Bearer <access_token>`（或其他认证方式）
- `Content-Type`：`application/json; charset=utf-8`
- `X-Request-Id`（可选）：请求唯一 ID，由调用方生成，平台透传到响应中。
- `X-Client-Id`（可选）：调用方应用 ID。

### 3.3 通用响应结构

```json
{
  "code": "0",
  "message": "ok",
  "traceId": "a1b2c3d4",
  "data": { }
}
```

- **字段说明**：
  - `code`：业务状态码，`0` 表示成功，非 0 表示失败。
  - `message`：错误或提示信息。
  - `traceId`：链路追踪 ID，便于排查问题。
  - `data`：业务数据，类型依据不同接口而定。

### 3.4 分页约定

- **请求参数**：
  - `pageNo`：页码，从 1 开始
  - `pageSize`：每页数量（建议默认 20，最大 100）
- **响应格式**：
  ```json
  {
    "code": "0",
    "message": "ok",
    "traceId": "xxx",
    "data": {
      "pageNo": 1,
      "pageSize": 20,
      "totalCount": 100,
      "totalPages": 5,
      "items": [ ... ]
    }
  }
  ```

### 3.5 全局错误码示例

| 错误码  | 含义                         |
|--------|------------------------------|
| 0      | 成功                         |
| 400001 | 请求参数错误                 |
| 400002 | 签名校验失败                 |
| 401001 | 未认证或 Token 失效         |
| 401002 | 无访问权限                   |
| 403001 | IP 不在白名单内             |
| 404001 | 资源不存在                   |
| 409001 | 幂等冲突/重复请求           |
| 409101 | 订单价格不一致，需重新下单 |
| 409102 | 订单状态不允许当前操作     |
| 429000 | 请求过于频繁（限流）       |
| 500000 | 系统内部错误                 |
| 500100 | 下游服务调用失败             |

各具体接口在“错误码说明”中可引用上述编码，并补充业务专用错误。

---

## 四、用户模块 API

### 4.1 同步用户（单个/批量）

- **功能描述**：同步员工/用户数据；以手机号为唯一键。若不存在则创建；存在则更新。

- **接口路径**：`/api/v1/users/sync`
- **方法**：`POST`
- **请求体类型**：`application/json`

#### 4.1.1 请求参数

- **Body 示例结构**：

```json
{
  "users": [
    {
      "mobile": "13800000000",
      "userName": "张三",
      "employeeNo": "E001",
      "email": "test@example.com",
      "status": "ACTIVE",
      "storeCode": "STORE001",
      "extra": {
        "position": "店员"
      }
    }
  ]
}
```

- **字段说明**：

| 字段名           | 类型     | 必填 | 说明                          |
|------------------|----------|------|-------------------------------|
| `users`          | array    | 是   | 用户列表                      |
| `users[].mobile` | string   | 是   | 手机号，唯一键                |
| `users[].userName` | string | 是   | 用户姓名                      |
| `users[].employeeNo` | string | 否 | 员工编号                      |
| `users[].email`  | string   | 否   | 邮箱                          |
| `users[].status` | string   | 否   | 状态：`ACTIVE`/`DISABLED`    |
| `users[].storeCode` | string| 否   | 所属门店编码                  |
| `users[].extra`  | object   | 否   | 扩展字段                       |

#### 4.1.2 响应格式

```json
{
  "code": "0",
  "message": "ok",
  "traceId": "xxx",
  "data": {
    "successCount": 1,
    "failCount": 0,
    "failList": []
  }
}
```

- `data.successCount`：成功数量
- `data.failList`：失败记录列表，包含 `mobile` 和 `reason`

#### 4.1.3 错误码说明

- `400001`：请求参数错误（如手机号为空或格式不正确）
- `500000`：系统内部错误

---

### 4.2 获取用户信息

- **功能描述**：根据手机号（唯一键）获取用户详情。

- **接口路径**：`/api/v1/users/{mobile}`
- **方法**：`GET`

#### 4.2.1 请求参数

- **Path 参数**：

| 字段    | 类型   | 必填 | 说明   |
|---------|--------|------|--------|
| mobile  | string | 是   | 手机号 |

#### 4.2.2 响应格式

```json
{
  "code": "0",
  "message": "ok",
  "traceId": "xxx",
  "data": {
    "userId": "U123",
    "mobile": "13800000000",
    "userName": "张三",
    "employeeNo": "E001",
    "status": "ACTIVE",
    "storeCode": "STORE001",
    "registerTime": "2024-01-01 10:00:00",
    "extra": {}
  }
}
```

#### 4.2.3 错误码说明

- `404001`：用户不存在
- 其他通用错误码同全局定义

---

### 4.3 获取用户优惠券列表

- **功能描述**：获取指定用户的优惠券列表，支持状态筛选、分页。

- **接口路径**：`/api/v1/users/{userId}/coupons`
- **方法**：`GET`

#### 4.3.1 请求参数

- **Path 参数**：

| 字段    | 类型   | 必填 | 说明       |
|---------|--------|------|------------|
| userId  | string | 是   | 用户 ID    |

- **Query 参数**：

| 字段名      | 类型   | 必填 | 说明                                             |
|-------------|--------|------|--------------------------------------------------|
| status      | string | 否   | 优惠券状态，等值筛选：`AVAILABLE`/`USED`/`EXPIRED` 等 |
| pageNo      | int    | 否   | 页码，默认 1                                     |
| pageSize    | int    | 否   | 每页数量，默认 20，最大 100                      |

#### 4.3.2 响应格式

```json
{
  "code": "0",
  "message": "ok",
  "traceId": "xxx",
  "data": {
    "pageNo": 1,
    "pageSize": 20,
    "totalCount": 100,
    "totalPages": 5,
    "items": [
      {
        "couponId": "C123",
        "couponCode": "ABC123",
        "name": "满20减5",
        "status": "AVAILABLE",
        "discountAmount": 500,
        "minOrderAmount": 2000,
        "validStartTime": "2024-01-01 00:00:00",
        "validEndTime": "2024-01-31 23:59:59"
      }
    ]
  }
}
```

#### 4.3.3 错误码说明

- `404001`：用户不存在
- 其他通用错误码同全局定义

---

## 五、订单模块 API

### 5.1 预创建订单（实时算价）

- **功能描述**：不落订单，仅进行价格试算；入参与创建订单一致，出参需返回订单用户适用优惠券列表。

- **接口路径**：`/api/v1/orders/pre-create`
- **方法**：`POST`

#### 5.1.1 请求参数

- **Body 示例结构**：

```json
{
  "userId": "U123",
  "storeCode": "STORE001",
  "items": [
    {
      "skuId": "SKU001",
      "goodsId": "G001",
      "quantity": 2,
      "unitPrice": 1800
    }
  ],
  "useCouponId": "C123",
  "clientOrderNo": "EXT202401010001"
}
```

- **字段说明**：

| 字段名               | 类型   | 必填 | 说明                                                         |
|----------------------|--------|------|--------------------------------------------------------------|
| `userId`             | string | 是   | 用户 ID                                                      |
| `storeCode`          | string | 是   | 门店编码                                                     |
| `items`              | array  | 是   | 订单商品明细                                                |
| `items[].skuId`      | string | 是   | SKU 编号                                                     |
| `items[].goodsId`    | string | 否   | 商品编号                                                     |
| `items[].quantity`   | int    | 是   | 数量                                                         |
| `items[].unitPrice`  | int    | 否   | 客户侧传入的单价（分），可用于校验或留空由平台价格为准       |
| `useCouponId`        | string | 否   | 指定使用的优惠券 ID，不传则自动匹配最优券                   |
| `clientOrderNo`      | string | 否   | 外部订单号，用于关联                                         |

#### 5.1.2 响应格式

```json
{
  "code": "0",
  "message": "ok",
  "traceId": "xxx",
  "data": {
    "calcOrderAmount": 3600,
    "discountAmount": 500,
    "payableAmount": 3100,
    "availableCoupons": [
      {
        "couponId": "C123",
        "name": "满20减5",
        "discountAmount": 500,
        "selected": true
      }
    ]
  }
}
```

#### 5.1.3 错误码说明

- `400001`：参数错误（商品为空等）
- `404001`：用户或商品不存在
- `500000`：算价内部错误

---

### 5.2 创建订单

- **功能描述**：实际创建订单；根据营销活动和优惠券计算最优价格；需要对比预创建订单总价一致性。

- **接口路径**：`/api/v1/orders`
- **方法**：`POST`

#### 5.2.1 请求参数

```json
{
  "userId": "U123",
  "storeCode": "STORE001",
  "items": [
    {
      "skuId": "SKU001",
      "goodsId": "G001",
      "quantity": 2
    }
  ],
  "useCouponId": "C123",
  "clientOrderNo": "EXT202401010001",
  "preCalcOrderAmount": 3100
}
```

- **字段说明**：

| 字段名                 | 类型   | 必填 | 说明                                                         |
|------------------------|--------|------|--------------------------------------------------------------|
| `userId`               | string | 是   | 用户 ID                                                      |
| `storeCode`            | string | 是   | 门店编码                                                     |
| `items`                | array  | 是   | 订单商品列表                                                |
| `items[].skuId`        | string | 是   | SKU 编号                                                     |
| `items[].goodsId`      | string | 否   | 商品编号                                                     |
| `items[].quantity`     | int    | 是   | 数量                                                         |
| `useCouponId`          | string | 否   | 指定使用的优惠券 ID                                         |
| `clientOrderNo`        | string | 否   | 外部订单号                                                  |
| `preCalcOrderAmount`   | int    | 是   | 预创建订单的总订单价格（下单前客户端保存的金额，用于校验）   |

#### 5.2.2 响应格式

```json
{
  "code": "0",
  "message": "ok",
  "traceId": "xxx",
  "data": {
    "orderNo": "O202401010001",
    "orderStatus": "CREATED",
    "orderAmount": 3600,
    "discountAmount": 500,
    "payableAmount": 3100,
    "payStatus": "UNPAID",
    "estimatedWaitTime": 600,
    "aheadCups": 5
  }
}
```

#### 5.2.3 错误码说明

- `409101`：订单价格不一致（需求中特殊错误：“商品更新，请重新下单”）
  - `message`：`"商品信息已更新，请重新下单"`
- `404001`：用户/商品不存在
- `500000`：系统内部错误

---

### 5.3 取消订单

- **功能描述**：取消订单；若订单已支付，需自动触发退款逻辑。

- **接口路径**：`/api/v1/orders/{orderNo}/cancel`
- **方法**：`POST`

#### 5.3.1 请求参数

- **Path 参数**：

| 字段名   | 类型   | 必填 | 说明   |
|----------|--------|------|--------|
| orderNo  | string | 是   | 订单号 |

- **Body（可选）**：

```json
{
  "reason": "用户主动取消"
}
```

#### 5.3.2 响应格式

```json
{
  "code": "0",
  "message": "ok",
  "traceId": "xxx",
  "data": {
    "orderNo": "O202401010001",
    "orderStatus": "CANCELLED",
    "refundStatus": "SUCCESS" 
  }
}
```

#### 5.3.3 错误码说明

- `404001`：订单不存在
- `409102`：订单状态不允许取消（如已完成/已取消）
- `500000`：系统内部错误

> 注：取消已支付订单成功后，需触发“订单退款事件回调”。

---

### 5.4 支付订单

- **功能描述**：完成订单支付；支付成功后发送订单支付成功事件回调。

- **接口路径**：`/api/v1/orders/{orderNo}/pay`
- **方法**：`POST`

#### 5.4.1 请求参数

```json
{
  "payChannel": "WECHAT",
  "payAmount": 3100,
  "payTime": "2024-01-01 10:10:10",
  "transactionId": "WX2024XXXX",
  "clientIp": "1.2.3.4"
}
```

- **字段说明**：

| 字段名          | 类型   | 必填 | 说明                         |
|-----------------|--------|------|------------------------------|
| `payChannel`    | string | 是   | 支付渠道：`WECHAT`/`ALIPAY` 等 |
| `payAmount`     | int    | 是   | 实付金额（分）               |
| `payTime`       | string | 是   | 支付时间                     |
| `transactionId` | string | 是   | 第三方支付流水号             |
| `clientIp`      | string | 否   | 客户端 IP                    |

#### 5.4.2 响应格式

```json
{
  "code": "0",
  "message": "ok",
  "traceId": "xxx",
  "data": {
    "orderNo": "O202401010001",
    "orderStatus": "PAID",
    "payStatus": "SUCCESS"
  }
}
```

#### 5.4.3 错误码说明

- `404001`：订单不存在
- `409102`：订单状态不允许支付
- `400001`：支付金额与应付金额不一致
- 其他通用错误码

> 注：支付成功后触发“订单支付成功事件回调”和“支付状态变更回调”。

---

### 5.5 订单退款

- **功能描述**：触发退款逻辑，退款成功修改订单支付状态为已退款，并发送订单退款事件回调。

- **接口路径**：`/api/v1/orders/{orderNo}/refund`
- **方法**：`POST`

#### 5.5.1 请求参数

```json
{
  "refundAmount": 3100,
  "refundReason": "用户申请退款",
  "clientRefundNo": "RF2024XXXX"
}
```

- **字段说明**：

| 字段名           | 类型   | 必填 | 说明         |
|------------------|--------|------|--------------|
| `refundAmount`   | int    | 是   | 退款金额（分）|
| `refundReason`   | string | 否   | 退款原因     |
| `clientRefundNo` | string | 否   | 外部退款单号 |

#### 5.5.2 响应格式

```json
{
  "code": "0",
  "message": "ok",
  "traceId": "xxx",
  "data": {
    "orderNo": "O202401010001",
    "refundStatus": "SUCCESS"
  }
}
```

#### 5.5.3 错误码说明

- `404001`：订单不存在
- `409102`：当前状态不允许退款
- `400001`：退款金额非法
- `500000`：退款失败

---

### 5.6 订单详情查询

- **功能描述**：查询订单详情，包括订单与订单商品信息、预计等待时间、前方杯数。

- **接口路径**：`/api/v1/orders/{orderNo}`
- **方法**：`GET`

#### 5.6.1 请求参数

- Path 参数同上。

#### 5.6.2 响应格式

```json
{
  "code": "0",
  "message": "ok",
  "traceId": "xxx",
  "data": {
    "orderNo": "O202401010001",
    "clientOrderNo": "EXT202401010001",
    "userId": "U123",
    "storeCode": "STORE001",
    "orderStatus": "PAID",
    "payStatus": "SUCCESS",
    "orderAmount": 3600,
    "discountAmount": 500,
    "payableAmount": 3100,
    "payTime": "2024-01-01 10:10:10",
    "estimatedWaitTime": 600,
    "aheadCups": 5,
    "items": [
      {
        "skuId": "SKU001",
        "goodsId": "G001",
        "goodsName": "拿铁咖啡",
        "quantity": 2,
        "unitPrice": 1800,
        "salePrice": 1550
      }
    ]
  }
}
```

#### 5.6.3 错误码说明

- `404001`：订单不存在

---

### 5.7 订单列表查询

- **功能描述**：支持按用户、商品、订单状态、支付状态、下单时间等多条件 AND/OR 查询，支持分页；条件支持等于、模糊、包含、大于、小于。

- **接口路径**：`/api/v1/orders`
- **方法**：`GET`

#### 5.7.1 请求参数（Query）

为保证 REST 友好与灵活性，建议采用：

- 单条件参数 + 可选高级查询结构，比如：

| 字段                | 类型   | 必填 | 说明                                           |
|---------------------|--------|------|------------------------------------------------|
| userId              | string | 否   | 等于查询                                       |
| mobile              | string | 否   | 用户手机号（模糊：`like`）                     |
| goodsId             | string | 否   | 商品 ID                                       |
| orderStatus         | string | 否   | 订单状态                                       |
| payStatus           | string | 否   | 支付状态                                       |
| orderTimeStart      | string | 否   | 下单时间起                                     |
| orderTimeEnd        | string | 否   | 下单时间止                                     |
| searchLogic         | string | 否   | 查询逻辑：`AND` / `OR`，默认 `AND`             |
| pageNo              | int    | 否   | 页码                                           |
| pageSize            | int    | 否   | 每页数量                                       |

#### 5.7.2 响应格式

与分页通用格式一致，`items` 中为简化订单字段列表。

---

### 5.8 订单评价（NPS 打分）

#### 5.8.1 提交订单评价

- **功能描述**：对订单进行 NPS（0–10 分）评价。

- **接口路径**：`/api/v1/orders/{orderNo}/rating`
- **方法**：`POST`

- **请求参数**：

```json
{
  "score": 8,
  "comment": "出餐很快，味道不错"
}
```

| 字段名   | 类型   | 必填 | 说明        |
|----------|--------|------|-------------|
| `score`  | int    | 是   | 0–10 分     |
| `comment`| string | 否   | 评价内容    |

- **响应**：

```json
{
  "code": "0",
  "message": "ok",
  "traceId": "xxx",
  "data": {}
}
```

- **错误码**：

- `404001`：订单不存在
- `400001`：评分不在 0–10 范围

#### 5.8.2 订单评价拉取

- **功能描述**：按时间范围、商品 sku 范围筛选订单评价，支持分页。

- **接口路径**：`/api/v1/order-ratings`
- **方法**：`GET`

- **请求参数（Query）**：

| 字段名       | 类型   | 必填 | 说明                        |
|--------------|--------|------|-----------------------------|
| startTime    | string | 否   | 评价时间起                  |
| endTime      | string | 否   | 评价时间止                  |
| skuId        | string | 否   | 商品 SKU                    |
| storeCode    | string | 否   | 门店编码                    |
| pageNo       | int    | 否   | 页码                        |
| pageSize     | int    | 否   | 每页数量                    |

- **响应**：分页结构，`items` 内含 `orderNo`, `score`, `comment`, `skuId`, `createTime` 等。

---

## 六、商品模块 API

### 6.1 商品基础配置（创建/更新）

> 注：若该模块为内部配置接口，可仅对内部系统开放；OpenAPI 可根据业务实际决定是否开放对第三方。

- **接口路径**：`/api/v1/products`
- **方法**：`POST`（创建），`PUT /api/v1/products/{goodsId}`（更新，可选）

- **请求参数（创建示例）**：

```json
{
  "goodsId": "G001",
  "name": "拿铁咖啡",
  "code": "LATTE",
  "description": "经典拿铁",
  "imageUrl": "https://xxx",
  "basePrice": 1800,
  "stock": 1000,
  "status": "ON_SHELF"
}
```

| 字段名       | 类型   | 必填 | 说明                   |
|--------------|--------|------|------------------------|
| `goodsId`    | string | 是   | 商品唯一标识           |
| `name`       | string | 是   | 商品名称               |
| `code`       | string | 否   | 商品编码               |
| `description`| string | 否   | 描述                   |
| `imageUrl`   | string | 否   | 图片 URL               |
| `basePrice`  | int    | 是   | 基础价格（原价，分）   |
| `stock`      | int    | 否   | 库存                   |
| `status`     | string | 否   | 状态：`ON_SHELF`等     |

- **响应**：返回创建/更新后的商品基础信息。

---

### 6.2 多规格商品配置

- **功能描述**：配置商品规格（如杯型、温度、甜度）及规格价格。

- **接口路径**：`/api/v1/products/{goodsId}/skus`
- **方法**：`POST`（批量配置）

- **请求参数**：

```json
{
  "skus": [
    {
      "skuId": "SKU001",
      "specs": {
        "cupSize": "大杯",
        "temperature": "热",
        "sweetness": "半糖"
      },
      "price": 2000,
      "stock": 100,
      "status": "ON_SHELF"
    }
  ]
}
```

| 字段名              | 类型   | 必填 | 说明                   |
|---------------------|--------|------|------------------------|
| `skus`              | array  | 是   | SKU 列表               |
| `skus[].skuId`      | string | 是   | SKU ID                 |
| `skus[].specs`      | object | 是   | 规格键值对             |
| `skus[].price`      | int    | 是   | 规格价格（分）         |
| `skus[].stock`      | int    | 否   | 库存                   |
| `skus[].status`     | string | 否   | 状态：`ON_SHELF`等     |

- **响应**：成功/失败列表。

---

### 6.3 分页条件查询商品基础信息

- **功能描述**：支持分页、多条件 AND/OR 查询，条件支持等于、模糊、包含、大于、小于。

- **接口路径**：`/api/v1/products`
- **方法**：`GET`

- **请求参数（Query）**：

| 字段名          | 类型   | 必填 | 说明                             |
|-----------------|--------|------|----------------------------------|
| name            | string | 否   | 名称（模糊查询）                 |
| code            | string | 否   | 编码（等值）                     |
| status          | string | 否   | 状态                             |
| priceMin        | int    | 否   | 价格下限（分，大于等于）         |
| priceMax        | int    | 否   | 价格上限（分，小于等于）         |
| searchLogic     | string | 否   | 查询逻辑：`AND`/`OR`            |
| pageNo          | int    | 否   | 页码                             |
| pageSize        | int    | 否   | 每页数量                         |

- **响应**：分页结构，`items` 为商品基础信息列表。

---

### 6.4 C 端商品列表（支持动态价格计算）

- **功能描述**：获取 C 端可点单商品列表（已上架，含所有规格信息），需返回动态价格（根据营销活动与用户优惠券计算）和划线价格（原价）。

- **接口路径**：`/api/v1/products/c-end`
- **方法**：`GET`

- **请求参数（Query）**：

| 字段名    | 类型   | 必填 | 说明                   |
|-----------|--------|------|------------------------|
| userId    | string | 否   | 当前用户 ID，用于计算个性化价格 |
| storeCode | string | 是   | 门店编码               |
| pageNo    | int    | 否   | 页码                   |
| pageSize  | int    | 否   | 每页数量               |

- **响应示例**：

```json
{
  "code": "0",
  "message": "ok",
  "traceId": "xxx",
  "data": {
    "pageNo": 1,
    "pageSize": 20,
    "totalCount": 100,
    "totalPages": 5,
    "items": [
      {
        "goodsId": "G001",
        "name": "拿铁咖啡",
        "description": "经典拿铁",
        "imageUrl": "https://xxx",
        "status": "ON_SHELF",
        "skus": [
          {
            "skuId": "SKU001",
            "specs": {
              "cupSize": "大杯",
              "temperature": "热",
              "sweetness": "半糖"
            },
            "dynamicPrice": 1550,
            "originalPrice": 1800,
            "stock": 100
          }
        ]
      }
    ]
  }
}
```

---

## 七、事件回调模块（Webhook）设计

> 以下接口为 **对接方（合作方）需要提供的回调接口规范**，平台会向这些 URL 发送事件通知。

### 7.1 通用回调约定

- **请求方法**：`POST`
- **Content-Type**：`application/json`
- **认证**：
  - 建议使用签名头：`X-Signature`, `X-Timestamp`, `X-Nonce`
- **通用字段**：
  - `eventId`：事件唯一 ID
  - `eventType`：事件类型，如 `ORDER_STATUS_CHANGED`
  - `eventTime`：事件发生时间
  - `data`：业务数据对象

- **响应要求**：
  - HTTP 状态码 2xx 视为成功。
  - Body 可简单返回：
    ```json
    {
      "code": "0",
      "message": "ok"
    }
    ```

#### 7.1.1 回调认证与签名机制

- **密钥分配**：
  - 平台为每个对接应用分配唯一的 `appId` 与 `appSecret`。
  - `appSecret` 仅用于服务端签名和验签，不在客户端、前端或日志中明文展示。
  - 建议支持密钥轮换（如：`appSecret` 与 `appSecretBackup`），并在控制台中展示当前生效版本。

- **请求头约定**：平台向合作方回调时统一携带以下请求头：
  - `X-App-Id`：应用标识，对应分配的 `appId`
  - `X-Timestamp`：时间戳（毫秒），例如：`1704094210000`
  - `X-Nonce`：随机字符串（建议长度 16–32，仅包含字母数字）
  - `X-Signature`：签名值（见下文算法说明）
  - `X-Signature-Method`：签名算法标识，例如：`HMAC-SHA256`
  - `X-Signature-Version`：签名版本，例如：`v1`

- **签名算法约定（示例方案）**：
  1. 定义签名原文 `stringToSign`（顺序固定）：

     ```text
     stringToSign = appId + "\n" + timestamp + "\n" + nonce + "\n" + body
     ```

     - `appId`：同 `X-App-Id`
     - `timestamp`：同 `X-Timestamp`
     - `nonce`：同 `X-Nonce`
     - `body`：HTTP 请求体原始字符串（UTF-8 编码的 JSON 文本，空体则使用空字符串）

  2. 使用 HMAC-SHA256 计算摘要：

     ```text
     signature = HMAC-SHA256(stringToSign, appSecret)
     ```

  3. 将结果用 Base64 编码，放入请求头 `X-Signature`。

- **验签流程（合作方侧）**：
  1. 根据 `X-App-Id` 在本地配置中查找对应的 `appSecret`，若未找到则直接拒绝。
  2. 校验 `X-Timestamp` 与当前时间差是否在允许的时间窗口内（例如 ±5 分钟），超出则拒绝（防止重放）。
  3. 校验 `X-Nonce` 是否在短期内已使用过（可存入缓存/数据库），若已使用则拒绝（防止重放）。
  4. 使用相同规则拼接 `stringToSign`，用本地 `appSecret` 计算本地签名 `localSignature`。
  5. 使用常量时间比较方式对比 `localSignature` 与头部 `X-Signature`，不一致则拒绝。
  6. 验签通过后再进入业务处理逻辑。

- **错误处理建议**：
  - 验签失败建议返回 HTTP 状态码 `401` 或 `403`，响应体可简单返回：

    ```json
    {
      "code": "401002",
      "message": "签名校验失败"
    }
    ```

  - 时间戳过期、重复 `nonce` 等均可统一归类为签名校验失败。

---

### 7.2 订单状态变更回调

- **建议路径（合作方侧）**：`/api/v1/webhook/order/status`
- **方法**：`POST`

#### 7.2.1 请求体示例

```json
{
  "eventId": "EVT202401010001",
  "eventType": "ORDER_STATUS_CHANGED",
  "eventTime": "2024-01-01 10:10:10",
  "data": {
    "orderNo": "O202401010001",
    "clientOrderNo": "EXT202401010001",
    "userId": "U123",
    "storeCode": "STORE001",
    "oldStatus": "CREATED",
    "newStatus": "PAID"
  }
}
```

- 可覆盖：创建订单、取消订单、完成订单等所有状态变更。

---

### 7.3 可取餐状态通知回调

- **路径**：`/api/v1/webhook/order/ready`
- **方法**：`POST`

#### 7.3.1 请求体示例

```json
{
  "eventId": "EVT202401010002",
  "eventType": "ORDER_READY",
  "eventTime": "2024-01-01 10:20:00",
  "data": {
    "orderNo": "O202401010001",
    "userId": "U123",
    "storeCode": "STORE001",
    "pickupCode": "A001",
    "items": [
      {
        "skuId": "SKU001",
        "goodsName": "拿铁咖啡",
        "quantity": 2
      }
    ]
  }
}
```

---

### 7.4 订单支付状态变更回调

- **路径**：`/api/v1/webhook/payment/status`
- **方法**：`POST`

#### 7.4.1 请求体示例

```json
{
  "eventId": "EVT202401010003",
  "eventType": "PAYMENT_STATUS_CHANGED",
  "eventTime": "2024-01-01 10:10:15",
  "data": {
    "orderNo": "O202401010001",
    "userId": "U123",
    "payStatus": "SUCCESS",
    "payChannel": "WECHAT",
    "payAmount": 3100,
    "refundStatus": "NONE"
  }
}
```

- `payStatus` 取值示例：`SUCCESS`, `REFUNDING`, `REFUNDED`, `FAILED`
- 对应需求：支付成功、申请退款、退款成功等状态。

---

### 7.5 用户优惠券事件回调

- **路径**：`/api/v1/webhook/coupon/event`
- **方法**：`POST`

#### 7.5.1 请求体示例

```json
{
  "eventId": "EVT202401010004",
  "eventType": "COUPON_EVENT",
  "eventTime": "2024-01-01 11:00:00",
  "data": {
    "userId": "U123",
    "couponId": "C123",
    "couponCode": "ABC123",
    "event": "RECEIVED",
    "orderNo": "O202401010001",
    "occurTime": "2024-01-01 11:00:00"
  }
}
```

- `data.event` 取值：
  - `RECEIVED`：用户获取优惠券
  - `USED`：用户优惠券核销
  - `EXPIRED`：用户优惠券过期
  - `REVOKED`：用户优惠券被撤回

---

## 八、附录：错误码与状态值建议

- **订单状态**：`CREATED` / `PAID` / `MAKING` / `READY` / `COMPLETED` / `CANCELLED` 等。
- **支付状态**：`UNPAID` / `SUCCESS` / `REFUNDING` / `REFUNDED` / `FAILED`。
- **优惠券状态**：`AVAILABLE` / `USED` / `EXPIRED` / `FROZEN` / `REVOKED`。
- **商品状态**：`ON_SHELF` / `OFF_SHELF` / `DELETED`。

---

如果你需要，我可以在下一步把上述内容整理成更接近 OpenAPI 3.0 的 `yaml` 结构草稿，方便直接导入 Swagger 工具。