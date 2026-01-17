# AI IDE 规则（面向本仓库）

本规则用于指导 AI 在本仓库内进行代码检索、修改与新增时保持一致性，避免破坏既有架构与编码风格。

## 目标

- 优先遵循现有代码风格与模块边界
- 不混用不同模块的返回模型与异常体系
- 优先复用已有工具、DTO/VO、枚举与分页模型
- 改动可验证：能编译、能测试（至少跑相关测试集）

## 基本工作流

- 先定位：从入口（Controller）→ Service → Mapper/Model，理解现有实现再改
- 先查现有模式：同目录找相似接口/相似业务的实现方式，然后按同样结构扩展
- 小步提交：一次改动聚焦一个目标，避免跨模块大范围重构

## 分层与目录约束

- Controller：只做参数解析、鉴权/登录态校验、调用 Service、拼装返回
- Service：承载业务逻辑与事务边界
- Mapper/Model：只做数据访问与实体映射

常用包路径：

- `com.fuint.module.*.controller`：管理端/会员端/商户端 API
- `com.fuint.openapi.v1.*`：OpenAPI v1
- `com.fuint.common.service.*` 与 `com.fuint.common.service.impl.*`：业务服务
- `com.fuint.repository.mapper.*` 与 `com.fuint.repository.model.*`：数据层

## 返回结构与异常体系（禁止混用）

### 管理端/会员端/商户端（旧风格）

- Controller 继承 [BaseController.java](file:///d:/Project/Aite/Foot-Fuint-Backend-master/fuint-framework/fuint-common/src/main/java/com/fuint/framework/web/BaseController.java)
- 返回 [ResponseObject.java](file:///d:/Project/Aite/Foot-Fuint-Backend-master/fuint-framework/fuint-common/src/main/java/com/fuint/framework/web/ResponseObject.java)
- 常见错误返回：`getFailureResult(code)` 或 `getFailureResult(code, message)`
- 业务异常：`BusinessCheckException`

### OpenAPI v1（新风格）

- Controller 返回 [CommonResult.java](file:///d:/Project/Aite/Foot-Fuint-Backend-master/fuint-framework/fuint-common/src/main/java/com/fuint/framework/pojo/CommonResult.java)
- 结合校验与网关能力：`@Validated`、`@Valid`、`@ApiSignature`、`@RateLimiter`
- 错误码优先使用 `*ErrorCodeConstants` + `CommonResult.error(...)`（示例：[OpenCateController.java](file:///d:/Project/Aite/Foot-Fuint-Backend-master/fuint-application/src/main/java/com/fuint/openapi/v1/goods/cate/OpenCateController.java)）

## 代码风格与命名

- 类命名：Controller/Service/ServiceImpl/Mapper/Model 按现有命名保持一致
- 依赖注入：优先使用 Lombok `@AllArgsConstructor` 或 `@Resource`，与所在目录一致
- 状态值：优先用 `StatusEnum`、`YesOrNoEnum` 等枚举，避免硬编码

## 数据访问与分页

- MyBatis-Plus：优先 `LambdaQueryWrapper` / `Wrappers` / `BaseMapper`
- XML：仅在已有同类 Mapper 使用 XML 时新增；namespace 必须与 Mapper 全限定名一致
- 分页：优先复用 `PaginationRequest` / `PaginationResponse`

## 安全与合规

- 不在日志与返回中输出敏感信息（口令、密钥、Token、支付证书路径等）
- 处理用户输入时，复用现有的输入清洗方式（例如 `CommonUtil.replaceXSS(...)` 在部分 Controller 已使用）

## 验证与回归

- 修改业务逻辑后，至少执行相关测试；订单 OpenAPI 可参考：[QUICKSTART-TEST.md](file:///d:/Project/Aite/Foot-Fuint-Backend-master/QUICKSTART-TEST.md)
- 无现成测试时，优先在现有测试框架中补充最小覆盖用例，再运行 `mvn -f fuint-application/pom.xml test`

