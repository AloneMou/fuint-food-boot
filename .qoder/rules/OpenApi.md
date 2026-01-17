---
trigger: always_on
---

# OpenAPI 开发规范

## 一、设计原则

1.独立开发OpenAPI功能：不用担心影响后台管理系统
2.针对性优化：可以根据OpenAPI的特殊需求进行定制优化
3.安全隔离：前台API和后台管理完全分离，提高安全性

## 二、Controller 编写规范

### 2.1 基础结构

- Controller 类必须继承 `BaseController`
- 使用 `@RestController` 和 `@RequestMapping` 注解
- 使用 `@Api` 注解标注 API 分组，tags 格式为 "OpenApi-{模块}相关接口"
- 使用 `@Validated` 注解启用参数验证

示例：
```java
@Validated
@Api(tags = "OpenApi-商品分类相关接口")
@RestController
@RequestMapping(value = "/api/v1/goods/cate")
public class OpenCateController extends BaseController {
    @Resource
    private CateService cateService;
}
```

### 2.2 安全注解

所有 OpenAPI 接口必须添加以下安全注解：

- `@ApiSignature`：API 签名验证
- `@RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)`：基于客户端 IP 的限流

示例：
```java
@PostMapping(value = "/create")
@ApiSignature
@RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
public CommonResult<Integer> createCate(@Valid @RequestBody MtGoodsCateCreateReqVO cateCreateReqVO) {
}
```

### 2.3 API 文档注解

- 使用 `@ApiOperation` 描述接口功能，包含 value 和 notes
- 使用 `@ApiParam` 描述路径变量和请求参数
- 对于 @RequestBody 参数，在 VO 类中使用 `@ApiModelProperty` 描述字段

示例：
```java
@ApiOperation(value = "创建商品分类", notes = "创建一个新的商品分类")
@PostMapping(value = "/create")
public CommonResult<Integer> createCate(@Valid @RequestBody MtGoodsCateCreateReqVO cateCreateReqVO) {
}

@ApiOperation(value = "获取商品分类详情", notes = "根据ID获取商品分类详细信息")
@GetMapping(value = "/detail/{id}")
public CommonResult<MtGoodsCateRespVO> getCateDetail(
    @ApiParam(value = "分类ID", required = true, example = "1")
    @PathVariable("id") Integer id) {
}
```

### 2.4 URL 规范

- URL 格式：`/api/v1/{模块}/{实体}/{操作}`
- RESTful 风格：
  - POST /create：创建
  - PUT /update：更新
  - DELETE /delete/{id}：删除
  - GET /detail/{id}：查询单个
  - GET /page：分页查询
  - GET /list：列表查询

### 2.5 响应格式

统一使用 `CommonResult<T>` 作为返回类型：

```java
return CommonResult.success(data);
return CommonResult.error(errorCode);
return CommonResult.error(code, message);
```

## 三、VO 编写规范

### 3.1 命名规范

- 请求 VO：`{Entity}ReqVO`、`{Entity}CreateReqVO`、`{Entity}UpdateReqVO`、`{Entity}PageReqVO`
- 响应 VO：`{Entity}RespVO`、`{Entity}PageRespVO`、`{Entity}ListRespVO`

### 3.2 基础注解

所有 VO 类必须使用：
- `@Data`：Lombok 自动生成 getter/setter
- `@ApiModelProperty`：描述字段信息

示例：
```java
@Data
public class MtGoodsCateCreateReqVO {

    @NotBlank(message = "分类名称不能为空")
    @ApiModelProperty(value = "分类名称", required = true)
    private String name;

    @ApiModelProperty(value = "LOGO地址")
    private String logo;
}
```

### 3.3 验证注解

根据需要使用 javax.validation 注解：
- `@NotBlank`：字符串不能为空
- `@NotNull`：对象不能为空
- `@Min` / `@Max`：数值范围
- `@Pattern`：正则表达式验证

### 3.4 分页 VO 规范

分页请求 VO 必须包含：
- `page`：页码
- `pageSize`：每页大小

分页响应 VO 必须包含：
- `list`：数据列表
- `total`：总数
- `totalPages`：总页数
- `currentPage`：当前页
- `pageSize`：每页大小

## 四、错误码规范

### 4.1 错误码定义

在 `{Module}ErrorCodeConstants` 接口中定义错误码：

```java
public interface GoodsCateErrorCodeConstants {
    ErrorCode GOODS_CATE_NOT_FOUND = new ErrorCode(100_3_001, "商品分类不存在");
}
```

### 4.2 错误码格式

- 格式：`{模块}_{实体}_{错误类型}`
- 模块号：100_x_yyy（x 为模块编号，y 为具体错误）
- 示例：
  - 100_3_001：商品分类不存在
  - 100_2_012：用户批量同步失败

### 4.3 错误码使用

```java
import static com.fuint.openapi.enums.GoodsCateErrorCodeConstants.GOODS_CATE_NOT_FOUND;

if (existCate == null) {
    return CommonResult.error(GOODS_CATE_NOT_FOUND);
}
```

## 五、业务逻辑规范

### 5.1 默认值处理

在 Controller 中处理可选参数的默认值：

```java
Integer merchantId = reqVO.getMerchantId() != null ? reqVO.getMerchantId() : 1;
Integer storeId = reqVO.getStoreId() != null ? reqVO.getStoreId() : 0;
```

### 5.2 数据验证

- 先验证数据是否存在
- 再进行业务操作

```java
MtGoodsCate existCate = cateService.queryCateById(id);
if (existCate == null) {
    return CommonResult.error(GOODS_CATE_NOT_FOUND);
}
```

### 5.3 对象转换

使用 `BeanUtils.toBean()` 进行 VO 到 Entity 的转换：

```java
MtGoodsCate mtGoodsCate = BeanUtils.toBean(cateCreateReqVO, MtGoodsCate.class);
```

### 5.4 操作标识

OpenAPI 操作时，设置 operator 为 "openapi"：

```java
mtCate.setOperator("openapi");
```

## 六、分页查询规范

### 6.1 分页请求构建

```java
PaginationRequest paginationRequest = new PaginationRequest();
paginationRequest.setCurrentPage(pageReqVO.getPage());
paginationRequest.setPageSize(pageReqVO.getPageSize());
Map<String, Object> params = new HashMap<>();
params.put("name", pageReqVO.getName());
paginationRequest.setSearchParams(params);
```

### 6.2 分页响应构建

```java
PaginationResponse<GoodsCateDto> paginationResponse = cateService.queryCateListByPagination(paginationRequest);

List<MtGoodsCateRespVO> list = paginationResponse.getContent().stream()
    .map(dto -> {
        MtGoodsCateRespVO vo = new MtGoodsCateRespVO();
        BeanUtils.copyProperties(dto, vo);
        return vo;
    })
    .collect(Collectors.toList());

respVO.setList(list);
respVO.setTotal(paginationResponse.getTotalElements());
respVO.setTotalPages(paginationResponse.getTotalPages());
respVO.setCurrentPage(pageReqVO.getPage());
respVO.setPageSize(pageReqVO.getPageSize());
```

## 七、异常处理

### 7.1 异常捕获

在 Controller 方法中捕获业务异常并返回错误信息：

```java
try {
    MtUserSyncRespVO result = syncSingleUser(syncReqVO);
    return CommonResult.success(result);
} catch (BusinessCheckException e) {
    return CommonResult.error(100_2_012, e.getMessage());
} catch (Exception e) {
    return CommonResult.error(500, "同步员工数据失败: " + e.getMessage());
}
```

### 7.2 方法签名

Controller 方法可以声明 `throws BusinessCheckException`，由全局异常处理器处理：

```java
public CommonResult<Integer> createCate(@Valid @RequestBody MtGoodsCateCreateReqVO cateCreateReqVO) throws BusinessCheckException {
}
```

## 八、Service 调用规范

### 8.1 依赖注入

使用 `@Resource` 注入 Service：

```java
@Resource
private CateService cateService;
```

### 8.2 方法命名

Service 方法命名规范：
- add{Entity}：添加
- update{Entity}：更新
- delete{Entity}：删除
- query{Entity}ById：根据 ID 查询
- query{Entity}ListByParams：根据参数查询列表
- query{Entity}ListByPagination：分页查询

## 九、批量操作规范

### 9.1 批量同步限制

批量操作建议限制数量，避免性能问题：

```java
if (users.size() > 100) {
    return CommonResult.error(UserErrorCodeConstants.USER_BATCH_SYNC_EXCEED_LIMIT);
}
```

### 9.2 批量结果统计

批量操作返回成功/失败统计：

```java
MtUserBatchSyncRespVO response = new MtUserBatchSyncRespVO();
response.setSuccessCount(successCount);
response.setFailureCount(failureCount);
response.setTotalCount(users.size());
response.setResults(results);
```

## 十、事件回调规范

对于订单等关键业务，操作后触发事件回调：

```java
eventCallbackService.sendOrderStatusChangedCallback(order, oldStatus, OrderStatusEnum.CREATED.getKey());
eventCallbackService.sendPaymentStatusChangedCallback(order, "SUCCESS");
```

## 十一、数据脱敏规范

对于敏感数据（如手机号），在响应 VO 中进行脱敏处理：

```java
String phone = mtUser.getMobile();
respVO.setMobile(phone);
```

## 十二、图片路径处理

对于图片路径，判断是否需要添加完整 URL：

```java
String basePath = settingService.getUploadBasePath();
String logo = goodsInfo.getLogo();
if (StringUtil.isNotEmpty(logo) && !logo.startsWith("http")) {
    logo = basePath + logo;
}
```