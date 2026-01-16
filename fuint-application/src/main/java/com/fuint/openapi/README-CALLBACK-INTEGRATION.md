# OpenAPI事件回调集成说明

## 概述

已创建统一的事件回调服务 `EventCallbackService`，用于管理所有OpenAPI事件回调。

## 已实现的事件回调

### 1. 订单状态变更回调
- **触发位置**: `OpenOrderController.createOrder()`, `OpenOrderController.cancelOrder()`
- **事件类型**: `ORDER_STATUS_CHANGED`
- **状态**: 创建订单、取消订单、支付后状态变更

### 2. 订单支付状态变更回调
- **触发位置**: `OpenOrderController.payOrder()`, `OpenOrderController.refundOrder()`
- **事件类型**: `PAYMENT_STATUS_CHANGED`
- **状态**: SUCCESS（支付成功）、REFUNDING（申请退款）、REFUNDED（退款成功）

### 3. 订单可取餐状态通知回调
- **触发位置**: `OpenOrderController.markOrderReady()`
- **事件类型**: `ORDER_READY`
- **说明**: 当订单商品可取餐时触发

### 4. 用户优惠券事件回调（待集成）
- **事件类型**: `COUPON_EVENT`
- **事件值**: RECEIVED（获取）、USED（核销）、EXPIRED（过期）、REVOKED（撤回）

## 如何在核心服务中集成优惠券事件回调

由于优惠券相关的核心服务方法（如 `CouponService.useCoupon()`, `UserCouponService.receiveCoupon()`）位于核心模块，建议通过以下方式集成：

### 方式1：在核心服务中注入EventCallbackService（推荐）

在以下方法中添加回调：

1. **优惠券领取** - `UserCouponServiceImpl.receiveCoupon()`
   ```java
   @Resource
   private EventCallbackService eventCallbackService;
   
   // 在领取成功后添加
   eventCallbackService.sendCouponEventCallback(userCoupon, "RECEIVED", null);
   ```

2. **优惠券核销** - `CouponServiceImpl.useCoupon()`
   ```java
   // 在核销成功后添加
   String orderNo = orderInfo != null ? orderInfo.getOrderSn() : null;
   eventCallbackService.sendCouponEventCallback(userCoupon, "USED", orderNo);
   ```

3. **优惠券过期** - `CouponExpireJob`（定时任务）
   ```java
   // 在标记过期后添加
   eventCallbackService.sendCouponEventCallback(userCoupon, "EXPIRED", null);
   ```

4. **优惠券撤回** - 在撤回方法中添加
   ```java
   eventCallbackService.sendCouponEventCallback(userCoupon, "REVOKED", null);
   ```

### 方式2：使用AOP切面（可选）

可以创建一个AOP切面，自动在相关方法执行后触发回调，但需要仔细设计以避免性能问题。

## 回调配置

回调地址需要在商户设置中配置：
- **设置类型**: ORDER
- **设置名称**: callback_url
- **设置值**: 回调URL地址

如果未配置回调地址，回调将自动跳过，不会影响业务流程。

## 回调格式

所有回调都遵循统一格式：

```json
{
  "eventId": "唯一事件ID",
  "eventType": "事件类型",
  "eventTime": "事件发生时间",
  "data": {
    // 业务数据
  }
}
```

具体的事件数据格式请参考 `EventCallbackService` 中的方法实现。
