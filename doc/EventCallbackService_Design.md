# 事件回调服务设计与实现说明

## 1. 概述
本服务 (`EventCallbackService`) 实现了安克点餐系统接收来自咖啡系统的业务事件回调。
实现严格遵循 `咖啡系统事件回调接入文档.md` 的规范。

## 2. 核心类说明

### 2.1 EventCallbackService.java
- **位置**: `com.fuint.openapi.service.EventCallbackService`
- **职责**: 
  - 接收回调请求
  - 签名验证 (HMAC-SHA256)
  - 幂等性检查 (基于 `eventId`)
  - 业务分发与处理
  - 回调日志记录 (`mt_webhook_log`)

### 2.2 TakeStatusEnum.java
- **位置**: `com.fuint.common.enums.TakeStatusEnum`
- **职责**: 定义订单取餐状态，并提供状态流转检查。
- **状态列表**:
  - `PENDING`: 待确认
  - `CONFIRMED`: 已确认
  - `PROCESSING`: 制作中
  - `READY`: 可取餐
  - `COMPLETED`: 已完成
  - `CANCELLED`: 已取消

## 3. 业务处理逻辑

### 3.1 签名验证
- 从 Header 获取 `X-Access-Key`，查询对应的 `AppSecret`。
- 拼接 `method + "\n" + path + "\n" + timestamp + "\n" + nonce`。
- 使用 HMAC-SHA256 算法计算签名并比对。
- **注意**: 时间戳有效期限制为 5 分钟。

### 3.2 幂等性
- 使用 `mt_webhook_log` 表记录所有接收到的 `eventId`。
- 如果相同的 `eventId` 且状态为成功 (status=1) 再次请求，直接返回成功，不重复执行业务逻辑。

### 3.3 状态变更处理 (ORDER_TAKE_STATUS_CHANGE)
- 校验状态枚举值的合法性。
- 校验状态流转的合法性 (例如：不可从 `READY` 变更为 `PENDING`)。
- 更新 `MtOrder` 表的 `takeStatus` 字段。

### 3.4 异常处理
- 签名失败返回 `401`。
- 参数错误返回 `00400`。
- 系统异常返回 `500`，并记录错误日志。

## 4. 数据库变更
- 本次重构主要复用现有表 `mt_order`, `mt_user_coupon`, `mt_webhook_log`。
- `mt_webhook_log` 用于记录入站 (Inbound) 回调日志。

## 5. 待办事项
- 需确保 `mt_order` 表包含 `take_status` 字段 (代码中已假定存在)。
- 需确保 `mt_webhook_log` 表结构支持较长的 `request_body`。
