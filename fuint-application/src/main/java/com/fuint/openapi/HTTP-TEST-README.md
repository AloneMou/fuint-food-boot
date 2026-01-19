# OpenAPI HTTP 测试文件使用说明

## 概述
本目录包含 openapi 包下所有接口的 HTTP 测试文件，用于快速测试和调试 OpenAPI 接口。

## 测试文件列表

| 文件名 | 说明 | 对应 Controller |
|--------|------|----------------|
| OpenOrderController.http | 订单相关接口测试 | OpenOrderController |
| OpenGoodsController.http | 商品管理接口测试 | OpenGoodsController |
| OpenUserController.http | 员工管理接口测试 | OpenUserController |
| OpenCouponController.http | 优惠券管理接口测试 | OpenCouponController |
| OpenUserCouponController.http | 会员优惠券接口测试 | OpenUserCouponController |
| OpenCateController.http | 商品分类接口测试 | OpenCateController |
| OpenMemberGroupController.http | 会员分组接口测试 | OpenMemberGroupController |
| OpenCouponGroupController.http | 优惠券分组接口测试 | OpenCouponGroupController |

## 使用方法

### 方法一：IntelliJ IDEA 内置 HTTP Client
1. 在 IDEA 中打开任意 .http 文件
2. 点击请求左侧的绿色三角形运行按钮，或使用快捷键 `Ctrl + Enter` (Windows/Linux) / `Cmd + Enter` (Mac)
3. 查看响应结果

### 方法二：使用 VS Code REST Client 插件
1. 安装 REST Client 插件
2. 打开 .http 文件
3. 点击请求上方的 "Send Request" 链接

### 方法三：使用 curl 命令
复制 .http 文件中的请求内容，在终端中运行 curl 命令

### 方法四：使用 Postman 或其他 API 测试工具
1. 打开 Postman
2. 创建新请求
3. 复制 .http 文件中的 URL、方法、请求头和请求体
4. 发送请求

## 配置说明

每个 .http 文件开头都定义了两个变量：
```
@baseUrl = http://localhost:8080
@contentType = application/json
```

如果需要修改服务器地址，请修改 `@baseUrl` 的值。

## 通用请求头

所有 POST/PUT 请求都包含以下请求头：
```
Content-Type: {{contentType}}
```

## 接口说明

### 订单相关接口 (OpenOrderController.http)
- 订单预创建（实时算价）
- 创建订单
- 取消订单
- 支付订单
- 订单退款
- 获取订单详情
- 订单列表
- 订单评价
- 订单评价拉取
- 标记订单可取餐

### 商品管理接口 (OpenGoodsController.http)
- 创建商品
- 更新商品
- 删除商品
- 获取商品详情
- 分页查询商品列表
- 获取所有启用的商品列表
- 更新商品状态
- C端商品列表（支持动态价格计算）

### 员工管理接口 (OpenUserController.http)
- 单个员工数据同步
- 批量员工数据同步
- 获取员工详情
- 分页查询员工列表

### 优惠券管理接口 (OpenCouponController.http)
- 创建优惠券
- 更新优惠券
- 分页查询优惠券列表
- 获取优惠券详情
- 发放优惠券（单个用户）
- 发放优惠券（批量用户）
- 发放优惠券（会员分组）
- 撤销优惠券

### 会员优惠券接口 (OpenUserCouponController.http)
- 分页查询用户优惠券列表
- 获取用户优惠券详情

### 商品分类接口 (OpenCateController.http)
- 创建商品分类
- 更新商品分类
- 删除商品分类
- 获取商品分类详情
- 分页查询商品分类列表
- 获取所有启用的商品分类列表
- 更新商品分类状态

### 会员分组接口 (OpenMemberGroupController.http)
- 创建会员分组
- 更新会员分组
- 删除会员分组
- 获取会员分组详情
- 分页查询会员分组列表
- 获取所有启用的会员分组列表
- 更新会员分组状态

### 优惠券分组接口 (OpenCouponGroupController.http)
- 创建优惠券分组
- 更新优惠券分组
- 删除优惠券分组
- 获取优惠券分组详情
- 分页查询优惠券分组列表

## 注意事项

1. 所有接口都需要 API 签名验证（@ApiSignature 注解），测试时需要配置正确的签名
2. 部分接口需要用户认证，请确保已登录或提供有效的用户令牌
3. 请求中的参数值为示例值，实际测试时请根据需要修改
4. 删除操作为逻辑删除，不会物理删除数据
5. 分页查询默认每页 10 条，可通过 pageSize 参数调整

## 常见问题

### Q: 如何修改服务器地址？
A: 修改每个 .http 文件顶部的 `@baseUrl` 变量值即可。

### Q: 如何处理签名认证？
A: 需要配置 API 签名相关的配置文件，具体请参考项目的 API 签名文档。

### Q: 测试失败怎么办？
A: 检查以下几点：
   - 服务是否正常启动
   - baseUrl 是否正确
   - 请求参数是否完整
   - 是否有正确的签名认证

## 更多帮助

如有问题，请参考项目文档或联系开发团队。
