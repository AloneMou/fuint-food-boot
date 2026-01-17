# Java 代码规范（基于现有代码）

## 总体约定

- 基础包名以 `com.fuint` 为根（入口类位于 `com.fuint`）
- 业务分层清晰：Controller / Service / Repository（Mapper+Model）
- 统一使用 Lombok 简化实体与构造注入（如 `@Getter/@Setter`、`@AllArgsConstructor`）

## 包结构约定

- 管理端接口：`com.fuint.module.backendApi.controller`
- 会员端接口：`com.fuint.module.clientApi.controller`
- 商户端接口：`com.fuint.module.merchantApi.controller`
- OpenAPI v1：`com.fuint.openapi.v1`
- Service 接口：`com.fuint.common.service`
- Service 实现：`com.fuint.common.service.impl`
- Mapper：`com.fuint.repository.mapper`
- Model：`com.fuint.repository.model`

## Controller 规范

### 统一返回

- 管理端/会员端/商户端：Controller 继承 [BaseController](file:///d:/Project/Aite/Foot-Fuint-Backend-master/fuint-framework/fuint-common/src/main/java/com/fuint/framework/web/BaseController.java)
  - 成功返回：`getSuccessResult(...)`
  - 失败返回：`getFailureResult(code)` 或 `getFailureResult(code, message)`
  - 返回结构：[ResponseObject](file:///d:/Project/Aite/Foot-Fuint-Backend-master/fuint-framework/fuint-common/src/main/java/com/fuint/framework/web/ResponseObject.java)
- OpenAPI v1：返回 [CommonResult](file:///d:/Project/Aite/Foot-Fuint-Backend-master/fuint-framework/fuint-common/src/main/java/com/fuint/framework/pojo/CommonResult.java)
  - 常见：`CommonResult.success(data)`、`CommonResult.error(errorCode)`

建议保持同一模块内返回风格一致，不要混用两套返回结构。

### 注解与命名

- REST 接口使用 `@RestController` + `@RequestMapping`
- Swagger 使用 `@Api`、`@ApiOperation`、`@ApiParam`（示例：[BackendAppController.java](file:///d:/Project/Aite/Foot-Fuint-Backend-master/fuint-application/src/main/java/com/fuint/module/backendApi/controller/BackendAppController.java)、[OpenCateController.java](file:///d:/Project/Aite/Foot-Fuint-Backend-master/fuint-application/src/main/java/com/fuint/openapi/v1/goods/cate/OpenCateController.java)）
- OpenAPI v1 常见增加 `@Validated`、`@Valid` 校验，以及签名/限流注解

### 登录态/权限

现有模块常用：

- 通过请求头 `Access-Token`，用 `TokenUtil` 解析登录信息
- 管理端权限常见 `@PreAuthorize(...)`

## Service 规范

- 实现类命名：`XxxServiceImpl`
- 常见继承：`ServiceImpl<XxxMapper, XxxModel>` 并实现 `XxxService`
- 事务：写操作使用 `@Transactional(rollbackFor = Exception.class)`（示例：[AddressServiceImpl.java](file:///d:/Project/Aite/Foot-Fuint-Backend-master/fuint-application/src/main/java/com/fuint/common/service/impl/AddressServiceImpl.java)）

## Repository（MyBatis-Plus）规范

- Mapper 命名：`MtXxxMapper`，继承 `BaseMapper<MtXxx>`
- XML 自定义 SQL 放在 `resources/mapper/`，namespace 与 Mapper 全限定名一致（示例：[MtMessageMapper.xml](file:///d:/Project/Aite/Foot-Fuint-Backend-master/fuint-application/src/main/resources/mapper/MtMessageMapper.xml)）
- Model 命名：`MtXxx`
  - `@TableName("mt_xxx")`
  - `@TableId(value = "ID", type = IdType.AUTO)` 等
  - Lombok `@Getter/@Setter`（示例：[MtMessage.java](file:///d:/Project/Aite/Foot-Fuint-Backend-master/fuint-application/src/main/java/com/fuint/repository/model/MtMessage.java)）

## 异常与错误码

- 业务检查异常：`BusinessCheckException`（定义：[BusinessCheckException.java](file:///d:/Project/Aite/Foot-Fuint-Backend-master/fuint-framework/fuint-common/src/main/java/com/fuint/framework/exception/BusinessCheckException.java)）
- 全局异常处理：`@RestControllerAdvice`（实现：[GlobalExceptionHandler.java](file:///d:/Project/Aite/Foot-Fuint-Backend-master/fuint-framework/fuint-common/src/main/java/com/fuint/framework/exception/GlobalExceptionHandler.java)）
- 错误码文案来源：
  - `PropertiesUtil.getResponseErrorMessageByCode(code)`（实现：[PropertiesUtil.java](file:///d:/Project/Aite/Foot-Fuint-Backend-master/fuint-utils/src/main/java/com/fuint/utils/PropertiesUtil.java)）
  - 资源文件：[message_zh_CN.properties](file:///d:/Project/Aite/Foot-Fuint-Backend-master/fuint-application/src/main/resources/international/message_zh_CN.properties)

## DTO / Param / Enum

- DTO：`com.fuint.common.dto.*`（接口输出与内部传输对象）
- Param：`com.fuint.common.param.*`（接口入参对象）
- Enum：`com.fuint.common.enums.*`（状态、类型等，常见 `getKey()` 返回数据库/接口值）

