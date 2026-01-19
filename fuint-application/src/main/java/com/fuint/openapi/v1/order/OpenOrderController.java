package com.fuint.openapi.v1.order;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.ClientIpRateLimiterKeyResolver;
import cn.iocoder.yudao.framework.signature.core.annotation.ApiSignature;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fuint.common.dto.AccountInfo;
import com.fuint.common.dto.OrderDto;
import com.fuint.common.dto.ResCartDto;
import com.fuint.common.dto.UserOrderDto;
import com.fuint.common.enums.*;
import com.fuint.common.param.OrderListParam;
import com.fuint.common.service.*;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pagination.PaginationResponse;
import com.fuint.framework.pojo.CommonResult;
import com.fuint.framework.web.BaseController;
import com.fuint.openapi.service.OpenApiOrderService;
import com.fuint.openapi.v1.order.vo.*;
import com.fuint.repository.mapper.MtOrderGoodsMapper;
import com.fuint.repository.mapper.MtUserActionMapper;
import com.fuint.repository.model.*;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.fuint.openapi.enums.OrderErrorCodeConstants.GOODS_NOT_BELONG_TO_STORE;
import static com.fuint.openapi.enums.OrderErrorCodeConstants.GOODS_NOT_EMPTY;
import static com.fuint.openapi.enums.UserErrorCodeConstants.USER_NOT_FOUND;

/**
 * OpenAPI订单相关接口
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Validated
@Api(tags = "OpenApi-订单相关接口")
@RestController
@RequestMapping(value = "/api/v1/order")
public class OpenOrderController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(OpenOrderController.class);

    @Resource
    private OrderService orderService;

    @Resource
    private OpenApiOrderService openApiOrderService;

    @Resource
    private MemberService memberService;

    @Resource
    private CartService cartService;

    @Resource
    private GoodsService goodsService;

    @Resource
    private SettingService settingService;

    @Resource
    private RefundService refundService;

    @Resource
    private MtUserActionMapper mtUserActionMapper;

    @Resource
    private MtOrderGoodsMapper mtOrderGoodsMapper;

    @Resource
    private com.fuint.openapi.service.EventCallbackService eventCallbackService;

    @Resource
    private UserGradeService userGradeService;

    /**
     * 订单预创建（实时算价）
     *
     * @param reqVO 预创建请求参数
     * @return 订单预创建结果
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "订单预创建（实时算价）", notes = "不实际创建订单，仅进行价格试算和优惠券匹配")
    @PostMapping(value = "/pre-create")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<OrderPreCreateRespVO> preCreateOrder(@Valid @RequestBody OrderPreCreateReqVO reqVO) throws BusinessCheckException {
        // 验证用户是否存在
        MtUser userInfo = memberService.queryMemberById(reqVO.getUserId());
        if (userInfo == null) {
            return CommonResult.error(USER_NOT_FOUND);
        }
        if (CollUtil.isEmpty(reqVO.getItems())) {
            return CommonResult.error(GOODS_NOT_EMPTY);
        }
        // 设置默认值
        Integer merchantId = reqVO.getMerchantId() != null ? reqVO.getMerchantId() : 1;
        Integer storeId = reqVO.getStoreId() != null ? reqVO.getStoreId() : 0;
        String orderMode = StringUtils.isNotEmpty(reqVO.getOrderMode()) ? reqVO.getOrderMode() : OrderModeEnum.ONESELF.getKey();
        String platform = StringUtils.isNotEmpty(reqVO.getPlatform()) ? reqVO.getPlatform() : "MP-WEIXIN";
        Integer userCouponId = reqVO.getUserCouponId() != null ? reqVO.getUserCouponId() : 0;
        Integer usePoint = reqVO.getUsePoint() != null ? reqVO.getUsePoint() : 0;

        // 系统配置检查：检查交易功能是否关闭
        MtSetting config = settingService.querySettingByName(merchantId, storeId, SettingTypeEnum.ORDER.getKey(), OrderSettingEnum.IS_CLOSE.getKey());
        if (config != null && config.getValue().equals(YesOrNoEnum.TRUE.getKey())) {
            return CommonResult.error(403, "系统已关闭交易功能，请稍后再试！");
        }

        // 构建购物车列表
        List<MtCart> cartList = new ArrayList<>();
        if (CollUtil.isNotEmpty(reqVO.getItems())) {
            for (OrderGoodsItemVO item : reqVO.getItems()) {
                MtCart cart = new MtCart();
                cart.setGoodsId(item.getGoodsId());
                cart.setSkuId(item.getSkuId() != null ? item.getSkuId() : 0);
                cart.setNum(item.getQuantity());
                cart.setUserId(reqVO.getUserId());
                cart.setStatus(StatusEnum.ENABLED.getKey());
                cart.setId(0);
                cartList.add(cart);
            }
        }
        
        // 验证商品是否属于公共商品或当前门店
        if (CollUtil.isNotEmpty(cartList)) {
            for (MtCart cart : cartList) {
                try {
                    MtGoods goodsInfo = goodsService.queryGoodsById(cart.getGoodsId());
                    if (goodsInfo != null) {
                        Integer goodsStoreId = goodsInfo.getStoreId();
                        // 如果当前门店ID为0（公共订单），商品必须是公共商品（storeId = 0）
                        // 如果当前门店ID > 0（指定门店），商品必须是公共商品（storeId = 0）或属于当前门店（storeId = storeId）
                        if (goodsStoreId == null) {
                            return CommonResult.error(GOODS_NOT_BELONG_TO_STORE);
                        }
                        if (storeId == 0) {
                            // 公共订单，商品必须是公共商品
                            if (goodsStoreId != 0) {
                                return CommonResult.error(GOODS_NOT_BELONG_TO_STORE);
                            }
                        } else {
                            // 指定门店订单，商品必须是公共商品或属于该门店
                            if (goodsStoreId != 0 && !goodsStoreId.equals(storeId)) {
                                return CommonResult.error(GOODS_NOT_BELONG_TO_STORE);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("验证商品归属失败: goodsId={}, error={}", cart.getGoodsId(), e.getMessage());
                    return CommonResult.error(GOODS_NOT_BELONG_TO_STORE);
                }
            }
        }
        
        // 调用订单预创建服务
        Map<String, Object> preCreateResult = openApiOrderService.preCreateOrder(
                merchantId,
                reqVO.getUserId(),
                cartList,
                userCouponId,
                usePoint,
                platform,
                orderMode,
                storeId
        );

        // 订单起送费检查（针对配送订单）
        if (orderMode.equals(OrderModeEnum.EXPRESS.getKey())) {
            MtSetting delivery = settingService.querySettingByName(merchantId, SettingTypeEnum.ORDER.getKey(), OrderSettingEnum.DELIVERY_MIN_AMOUNT.getKey());
            if (delivery != null && StringUtils.isNotEmpty(delivery.getValue())) {
                BigDecimal deliveryMinAmount = new BigDecimal(delivery.getValue());
                BigDecimal totalAmount = getBigDecimalValue(preCreateResult.get("totalAmount"));
                if (deliveryMinAmount.compareTo(BigDecimal.ZERO) > 0 && deliveryMinAmount.compareTo(totalAmount) > 0) {
                    return CommonResult.error(400, "订单起送金额：" + deliveryMinAmount + "元");
                }
            }
        }

        // 构建响应VO，使用安全的方法获取值并设置默认值
        OrderPreCreateRespVO respVO = new OrderPreCreateRespVO();
        respVO.setTotalAmount(getBigDecimalValue(preCreateResult.get("totalAmount")));
        respVO.setDiscountAmount(getBigDecimalValue(preCreateResult.get("discountAmount")));
        respVO.setPointAmount(getBigDecimalValue(preCreateResult.get("pointAmount")));
        respVO.setMemberDiscountAmount(getBigDecimalValue(preCreateResult.get("memberDiscountAmount")));
        respVO.setDeliveryFee(getBigDecimalValue(preCreateResult.get("deliveryFee")));
        respVO.setPayableAmount(getBigDecimalValue(preCreateResult.get("payableAmount")));
        respVO.setUsePoint(getIntegerValue(preCreateResult.get("usePoint")));
        respVO.setAvailablePoint(getIntegerValue(preCreateResult.get("availablePoint")));
        respVO.setSelectedCouponId(getIntegerValue(preCreateResult.get("selectedCouponId")));
        respVO.setCalculateTime(getDateValue(preCreateResult.get("calculateTime")));
        respVO.setOrderMode(orderMode);
        respVO.setStoreId(storeId);

        // 转换优惠券列表
        List<Map<String, Object>> availableCouponsMap = (List<Map<String, Object>>) preCreateResult.get("availableCoupons");
        List<AvailableCouponVO> availableCoupons = convertAvailableCoupons(availableCouponsMap);
        respVO.setAvailableCoupons(availableCoupons);

        // 转换商品列表
        List<ResCartDto> goodsListDto = (List<ResCartDto>) preCreateResult.get("goodsList");
        List<OrderGoodsDetailVO> goodsList = convertGoodsList(goodsListDto, reqVO.getUserId(), merchantId);
        respVO.setGoodsList(goodsList);

        // 计算商品总数量
        int totalQuantity = goodsList.stream()
                .mapToInt(goods -> goods.getQuantity() != null ? goods.getQuantity() : 0)
                .sum();
        respVO.setTotalQuantity(totalQuantity);

        return CommonResult.success(respVO);
    }

    /**
     * 创建订单
     *
     * @param reqVO 订单创建请求参数
     * @return 订单创建结果
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "创建订单", notes = "验证价格并创建订单")
    @PostMapping(value = "/create")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<UserOrderDto> createOrder(@Valid @RequestBody OrderCreateReqVO reqVO) throws BusinessCheckException {
        // 1. 验证用户是否存在
        MtUser userInfo = memberService.queryMemberById(reqVO.getUserId());
        if (userInfo == null) {
            return CommonResult.error(404, "用户不存在");
        }

        // 2. 模拟预创建计算价格
        List<MtCart> cartList = new ArrayList<>();
        if (reqVO.getItems() != null && !reqVO.getItems().isEmpty()) {
            for (OrderGoodsItemVO item : reqVO.getItems()) {
                MtCart cart = new MtCart();
                cart.setGoodsId(item.getGoodsId());
                cart.setSkuId(item.getSkuId() != null ? item.getSkuId() : 0);
                cart.setNum(item.getQuantity());
                cart.setUserId(reqVO.getUserId());
                cart.setStatus(StatusEnum.ENABLED.getKey());
                cartList.add(cart);
            }
        }

        Integer merchantId = reqVO.getMerchantId() != null ? reqVO.getMerchantId() : 1;
        Integer storeId = reqVO.getStoreId() != null ? reqVO.getStoreId() : 0;
        String orderMode = StringUtils.isNotEmpty(reqVO.getOrderMode()) ? reqVO.getOrderMode() : OrderModeEnum.ONESELF.getKey();
        String platform = StringUtils.isNotEmpty(reqVO.getPlatform()) ? reqVO.getPlatform() : "MP-WEIXIN";
        Integer userCouponId = reqVO.getUserCouponId() != null ? reqVO.getUserCouponId() : 0;
        Integer usePoint = reqVO.getUsePoint() != null ? reqVO.getUsePoint() : 0;

        Map<String, Object> preCreateResult = orderService.preCreateOrder(
                merchantId, reqVO.getUserId(), cartList, userCouponId, usePoint, platform, orderMode, storeId
        );

        BigDecimal calculatedPayableAmount = (BigDecimal) preCreateResult.get("payableAmount");

        // 3. 校验价格一致性
        if (reqVO.getPreTotalAmount().compareTo(calculatedPayableAmount) != 0) {
            return CommonResult.error(400, "商品更新,请重新下单");
        }

        // 4. 实际创建订单
        OrderDto orderDto = new OrderDto();
        orderDto.setUserId(reqVO.getUserId());
        orderDto.setMerchantId(merchantId);
        orderDto.setStoreId(storeId);
        orderDto.setOrderMode(orderMode);
        orderDto.setPlatform(platform);
        orderDto.setCouponId(userCouponId);
        orderDto.setUsePoint(usePoint);
        orderDto.setRemark(reqVO.getRemark());
        orderDto.setTableId(reqVO.getTableId());
        orderDto.setType(reqVO.getType() != null ? reqVO.getType().getKey() : OrderTypeEnum.GOODS.getKey());
        orderDto.setPayType(reqVO.getPayType() != null ? reqVO.getPayType() : PayTypeEnum.JSAPI.getKey());
        orderDto.setIsVisitor(YesOrNoEnum.NO.getKey());

        MtOrder order = orderService.saveOrder(orderDto);
        UserOrderDto result = orderService.getOrderById(order.getId());

        // 发送订单创建回调
        eventCallbackService.sendOrderStatusChangedCallback(order, null, OrderStatusEnum.CREATED.getKey());

        return CommonResult.success(result);
    }

    /**
     * 取消订单
     *
     * @param reqVO 取消订单请求参数
     * @return 操作结果
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "取消订单", notes = "取消订单，若已支付则自动退款")
    @PostMapping(value = "/cancel")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Boolean> cancelOrder(@Valid @RequestBody OrderCancelReqVO reqVO) throws BusinessCheckException {
        MtOrder order = orderService.getOrderInfo(reqVO.getOrderId());
        if (order == null) {
            return CommonResult.error(404, "订单不存在");
        }

        String oldStatus = order.getStatus();

        // 如果已支付，执行退款
        if (order.getPayStatus().equals(PayStatusEnum.SUCCESS.getKey())) {
            AccountInfo accountInfo = new AccountInfo();
            accountInfo.setAccountName("OpenApi-System");
            refundService.doRefund(order.getId(), order.getPayAmount().toString(), "订单取消自动退款", accountInfo);
        }

        orderService.cancelOrder(reqVO.getOrderId(), reqVO.getRemark());

        // 获取更新后的订单信息
        MtOrder updatedOrder = orderService.getOrderInfo(reqVO.getOrderId());
        // 发送订单取消回调
        eventCallbackService.sendOrderStatusChangedCallback(updatedOrder, oldStatus, OrderStatusEnum.CANCEL.getKey());

        return CommonResult.success(true);
    }

    /**
     * 支付订单
     *
     * @param reqVO 支付请求参数
     * @return 操作结果
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "支付订单", notes = "支付成功，并发送订单支付成功事件回调")
    @PostMapping(value = "/pay")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Boolean> payOrder(@Valid @RequestBody OrderPayReqVO reqVO) throws BusinessCheckException {
        MtOrder order = orderService.getOrderInfo(reqVO.getOrderId());
        if (order == null) {
            return CommonResult.error(404, "订单不存在");
        }

        // 设置为已支付
        Boolean result = orderService.setOrderPayed(reqVO.getOrderId(), reqVO.getPayAmount());

        if (result) {
            // 获取更新后的订单信息
            MtOrder updatedOrder = orderService.getOrderInfo(reqVO.getOrderId());

            // 发送订单支付成功事件回调
            eventCallbackService.sendPaymentStatusChangedCallback(updatedOrder, "SUCCESS");

            // 发送订单状态变更回调
            eventCallbackService.sendOrderStatusChangedCallback(updatedOrder, order.getStatus(), OrderStatusEnum.PAID.getKey());
        }

        return CommonResult.success(result);
    }

    /**
     * 订单退款
     *
     * @param reqVO 退款请求参数
     * @return 操作结果
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "订单退款", notes = "触发退款逻辑,退款成功修改订单支付状态为已退款")
    @PostMapping(value = "/refund")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Boolean> refundOrder(@Valid @RequestBody OrderRefundReqVO reqVO) throws BusinessCheckException {
        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setAccountName("OpenApi-System");

        // 发送申请退款回调
        MtOrder order = orderService.getOrderInfo(reqVO.getOrderId());
        eventCallbackService.sendPaymentStatusChangedCallback(order, "REFUNDING");

        Boolean result = refundService.doRefund(reqVO.getOrderId(), reqVO.getAmount().toString(), reqVO.getRemark(), accountInfo);

        if (result) {
            // 获取更新后的订单信息
            MtOrder updatedOrder = orderService.getOrderInfo(reqVO.getOrderId());
            // 发送订单退款成功事件回调
            eventCallbackService.sendPaymentStatusChangedCallback(updatedOrder, "REFUNDED");
        }

        return CommonResult.success(result);
    }

    /**
     * 获取订单详情
     *
     * @param id 订单ID
     * @return 订单详情
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "订单详情", notes = "包含订单与订单商品所有信息，预计等待时间，前有多少杯咖啡")
    @GetMapping(value = "/detail/{id}")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Map<String, Object>> getOrderDetail(@PathVariable("id") Integer id) throws BusinessCheckException {
        UserOrderDto orderDto = orderService.getOrderById(id);
        if (orderDto == null) {
            return CommonResult.error(404, "订单不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("order", orderDto);

        // 计算队列信息
        // 假设状态为已支付（待制作/制作中）的订单在排队
        LambdaQueryWrapper<MtOrder> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(MtOrder::getStatus, OrderStatusEnum.PAID.getKey());
        queryWrapper.lt(MtOrder::getId, id); // 在当前订单之前的
        List<MtOrder> queueOrders = orderService.list(queryWrapper);

        int coffeeCount = 0;
        for (MtOrder qOrder : queueOrders) {
            Map<String, Object> params = new HashMap<>();
            params.put("ORDER_ID", qOrder.getId());
            List<MtOrderGoods> goodsList = mtOrderGoodsMapper.selectByMap(params);
            for (MtOrderGoods goods : goodsList) {
                coffeeCount += goods.getNum();
            }
        }

        result.put("queueCount", coffeeCount);
        result.put("estimatedWaitTime", coffeeCount * 5); // 假设每杯5分钟

        return CommonResult.success(result);
    }

    /**
     * 订单列表
     *
     * @param reqVO 查询参数
     * @return 订单列表
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "订单列表", notes = "支持多条件分页查询")
    @GetMapping(value = "/list")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<PaginationResponse<UserOrderDto>> getOrderList(@Valid OrderListReqVO reqVO) throws BusinessCheckException {
        OrderListParam param = new OrderListParam();
        param.setUserId(reqVO.getUserId());
        param.setMerchantId(reqVO.getMerchantId());
        param.setStoreId(reqVO.getStoreId());
        param.setStatus(reqVO.getStatus());
        param.setPayStatus(reqVO.getPayStatus());
        param.setStartTime(reqVO.getStartTime());
        param.setEndTime(reqVO.getEndTime());
        param.setPage(reqVO.getPage());
        param.setPageSize(reqVO.getPageSize());

        // 针对商品名称模糊查询，OrderListParam可能不支持，这里如果需要可以自行实现Lambda查询
        PaginationResponse<UserOrderDto> result = orderService.getUserOrderList(param);

        return CommonResult.success(result);
    }

    /**
     * 订单评价
     *
     * @param reqVO 评价请求参数
     * @return 操作结果
     */
    @ApiOperation(value = "订单维度评价", notes = "支持订单维度NPS打分评价（0-10分）")
    @PostMapping(value = "/evaluate")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Boolean> evaluateOrder(@Valid @RequestBody OrderEvaluateReqVO reqVO) {
        MtUserAction action = new MtUserAction();
        action.setUserId(0); // 暂时未知，或者从订单获取

        MtOrder order = orderService.getById(reqVO.getOrderId());
        if (order != null) {
            action.setUserId(order.getUserId());
            action.setMerchantId(order.getMerchantId());
            action.setStoreId(order.getStoreId());
        }

        action.setAction("NPS_EVALUATION");
        action.setDescription(reqVO.getComment());
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", reqVO.getOrderId());
        params.put("score", reqVO.getScore());
        action.setParam(JSON.toJSONString(params));
        action.setCreateTime(new Date());
        action.setUpdateTime(new Date());
        action.setStatus(StatusEnum.ENABLED.getKey());

        mtUserActionMapper.insert(action);

        return CommonResult.success(true);
    }

    /**
     * 订单评价拉取
     *
     * @param reqVO 查询参数
     * @return 评价列表
     */
    @ApiOperation(value = "订单评价拉取", notes = "支持分页拉取，时间范围筛选，商品sku范围筛选")
    @GetMapping(value = "/evaluations")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<PaginationResponse<MtUserAction>> getEvaluations(@Valid EvaluationListReqVO reqVO) {
        PageHelper.startPage(reqVO.getPage(), reqVO.getPageSize());

        LambdaQueryWrapper<MtUserAction> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(MtUserAction::getAction, "NPS_EVALUATION");

        if (StringUtils.isNotEmpty(reqVO.getStartTime())) {
            queryWrapper.ge(MtUserAction::getCreateTime, reqVO.getStartTime());
        }
        if (StringUtils.isNotEmpty(reqVO.getEndTime())) {
            queryWrapper.le(MtUserAction::getCreateTime, reqVO.getEndTime());
        }

        // SKU筛选逻辑：由于评价记录在Action的JSON参数里，需要解析或配合订单商品表
        // 这里简单实现，如果传了SKU，则先找出包含这些SKU的订单
        if (reqVO.getSkuIds() != null && !reqVO.getSkuIds().isEmpty()) {
            LambdaQueryWrapper<MtOrderGoods> ogWrapper = Wrappers.lambdaQuery();
            ogWrapper.in(MtOrderGoods::getSkuId, reqVO.getSkuIds());
            List<MtOrderGoods> ogList = mtOrderGoodsMapper.selectList(ogWrapper);
            List<Integer> orderIds = ogList.stream().map(MtOrderGoods::getOrderId).distinct().collect(Collectors.toList());
            if (orderIds.isEmpty()) {
                PageRequest pageRequest = PageRequest.of(reqVO.getPage() - 1, reqVO.getPageSize());
                org.springframework.data.domain.Page<MtUserAction> emptyPage = new PageImpl<>(new ArrayList<>(), pageRequest, 0);
                return CommonResult.success(new PaginationResponse<MtUserAction>(emptyPage, MtUserAction.class));
            }
            // 匹配Param中的orderId
            // 由于是JSON字符串，简单处理：
            queryWrapper.and(wrapper -> {
                for (int i = 0; i < orderIds.size(); i++) {
                    Integer id = orderIds.get(i);
                    if (i == 0) {
                        wrapper.like(MtUserAction::getParam, "\"orderId\":" + id);
                    } else {
                        wrapper.or().like(MtUserAction::getParam, "\"orderId\":" + id);
                    }
                }
                return wrapper;
            });
        }

        queryWrapper.orderByDesc(MtUserAction::getId);
        List<MtUserAction> list = mtUserActionMapper.selectList(queryWrapper);

        Page<MtUserAction> pageHelper = (Page<MtUserAction>) list;
        PageRequest pageRequest = PageRequest.of(reqVO.getPage() - 1, reqVO.getPageSize());
        org.springframework.data.domain.Page<MtUserAction> springPage = new PageImpl<>(list, pageRequest, pageHelper.getTotal());

        PaginationResponse<MtUserAction> response = new PaginationResponse<MtUserAction>(springPage, MtUserAction.class);
        response.setContent(list);

        return CommonResult.success(response);
    }

    /**
     * 标记订单可取餐
     *
     * @param orderId 订单ID
     * @return 操作结果
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "标记订单可取餐", notes = "标记订单商品可取餐，并发送可取餐状态通知回调")
    @PostMapping(value = "/ready/{orderId}")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Boolean> markOrderReady(
            @ApiParam(value = "订单ID", required = true, example = "1")
            @PathVariable("orderId") Integer orderId) throws BusinessCheckException {
        MtOrder order = orderService.getOrderInfo(orderId);
        if (order == null) {
            return CommonResult.error(404, "订单不存在");
        }

        // 更新订单状态为已发货（可取餐）
        OrderDto orderDto = new OrderDto();
        orderDto.setId(orderId);
        orderDto.setStatus(OrderStatusEnum.DELIVERED.getKey());
        orderService.updateOrder(orderDto);

        // 获取更新后的订单信息
        MtOrder updatedOrder = orderService.getOrderInfo(orderId);

        // 获取订单商品列表
        Map<String, Object> params = new HashMap<>();
        params.put("ORDER_ID", orderId);
        List<MtOrderGoods> goodsList = mtOrderGoodsMapper.selectByMap(params);
        List<Map<String, Object>> items = new ArrayList<>();
        for (MtOrderGoods orderGoods : goodsList) {
            Map<String, Object> item = new HashMap<>();
            item.put("skuId", orderGoods.getSkuId());
            item.put("quantity", orderGoods.getNum());

            // 通过goodsId查询商品信息获取商品名称
            try {
                MtGoods goodsInfo = goodsService.queryGoodsById(orderGoods.getGoodsId());
                if (goodsInfo != null) {
                    item.put("goodsName", goodsInfo.getName());
                } else {
                    item.put("goodsName", "");
                }
            } catch (Exception e) {
                log.warn("获取商品信息失败: goodsId={}, error={}", orderGoods.getGoodsId(), e.getMessage());
                item.put("goodsName", "");
            }

            items.add(item);
        }

        // 发送可取餐状态通知回调
        eventCallbackService.sendOrderReadyCallback(updatedOrder, items);

        return CommonResult.success(true);
    }

    /**
     * 安全获取BigDecimal值
     */
    private BigDecimal getBigDecimalValue(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            log.warn("转换BigDecimal失败: {}", value, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 安全获取Integer值
     */
    private Integer getIntegerValue(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            log.warn("转换Integer失败: {}", value, e);
            return 0;
        }
    }

    /**
     * 安全获取Date值
     */
    private Date getDateValue(Object value) {
        if (value == null) {
            return new Date();
        }
        if (value instanceof Date) {
            return (Date) value;
        }
        return new Date();
    }

    /**
     * 计算会员折扣金额
     * 会员折扣是在优惠券和积分抵扣之后应用的
     * 计算公式：会员折扣金额 = (总金额 - 优惠券金额 - 积分抵扣金额) * (1 - 会员折扣率)
     */
    private BigDecimal calculateMemberDiscountAmount(BigDecimal totalAmount, BigDecimal discountAmount,
                                                     BigDecimal pointAmount, BigDecimal deliveryFee,
                                                     BigDecimal payableAmount) {
        // 应用会员折扣前的金额 = 总金额 - 优惠券金额 - 积分抵扣金额
        BigDecimal beforeMemberDiscount = totalAmount.subtract(discountAmount).subtract(pointAmount);
        
        // 应用会员折扣后的金额 = 应付金额 - 配送费
        BigDecimal afterMemberDiscount = payableAmount.subtract(deliveryFee);
        
        // 会员折扣金额 = 折扣前金额 - 折扣后金额
        BigDecimal memberDiscount = beforeMemberDiscount.subtract(afterMemberDiscount);
        return memberDiscount.max(BigDecimal.ZERO);
    }

    /**
     * 转换可用优惠券列表
     */
    private List<AvailableCouponVO> convertAvailableCoupons(List<Map<String, Object>> availableCouponsMap) {
        List<AvailableCouponVO> availableCoupons = new ArrayList<>();
        if (availableCouponsMap == null || availableCouponsMap.isEmpty()) {
            return availableCoupons;
        }

        for (Map<String, Object> couponMap : availableCouponsMap) {
            AvailableCouponVO couponVO = new AvailableCouponVO();
            couponVO.setUserCouponId(getIntegerValue(couponMap.get("userCouponId")));
            couponVO.setCouponId(getIntegerValue(couponMap.get("couponId")));
            couponVO.setCouponName((String) couponMap.get("couponName"));
            couponVO.setCouponType((String) couponMap.get("couponType"));
            couponVO.setDiscountAmount(getBigDecimalValue(couponMap.get("discountAmount")));
            couponVO.setUsable((String) couponMap.get("usable"));
            couponVO.setDescription((String) couponMap.get("description"));
            couponVO.setSelected(Boolean.TRUE.equals(couponMap.get("selected")));
            
            // 设置余额（储值卡）
            if (couponMap.get("balance") != null) {
                couponVO.setBalance(getBigDecimalValue(couponMap.get("balance")));
            }

            // 解析有效期字符串为开始和结束时间
            String effectiveDate = (String) couponMap.get("effectiveDate");
            if (StringUtils.isNotEmpty(effectiveDate) && effectiveDate.contains("~")) {
                try {
                    String[] dates = effectiveDate.split("~");
                    if (dates.length == 2) {
                        Date startTime = parseDateFromString(dates[0].trim());
                        Date endTime = parseDateFromString(dates[1].trim());
                        couponVO.setEffectiveStartTime(startTime);
                        couponVO.setEffectiveEndTime(endTime);
                    }
                } catch (Exception e) {
                    log.warn("解析优惠券有效期失败: {}", effectiveDate, e);
                }
            }

            // 如果优惠券不可用，设置不可用原因
            if (!"A".equals(couponVO.getUsable())) {
                couponVO.setUnusableReason(getUnusableReason(couponVO));
            }

            availableCoupons.add(couponVO);
        }

        return availableCoupons;
    }

    /**
     * 从字符串解析日期
     */
    private Date parseDateFromString(String dateStr) {
        if (StringUtils.isEmpty(dateStr)) {
            return null;
        }
        try {
            // 尝试解析 "yyyy.MM.dd HH:mm" 格式
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy.MM.dd HH:mm");
            return sdf.parse(dateStr);
        } catch (Exception e) {
            try {
                // 尝试解析 "yyyy-MM-dd HH:mm:ss" 格式
                java.text.SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return sdf2.parse(dateStr);
            } catch (Exception e2) {
                log.warn("解析日期失败: {}", dateStr, e2);
                return null;
            }
        }
    }

    /**
     * 获取优惠券不可用原因
     */
    private String getUnusableReason(AvailableCouponVO couponVO) {
        if (couponVO == null) {
            return "优惠券不可用";
        }
        // 根据优惠券状态判断不可用原因
        if (StringUtils.isEmpty(couponVO.getUsable()) || "N".equals(couponVO.getUsable())) {
            if (StringUtils.isNotEmpty(couponVO.getDescription()) && couponVO.getDescription().contains("满")) {
                return "订单金额不满足使用条件";
            }
            return "优惠券不可用";
        }
        return null;
    }

    /**
     * 转换商品列表
     */
    private List<OrderGoodsDetailVO> convertGoodsList(List<ResCartDto> goodsListDto, Integer userId, Integer merchantId) {
        List<OrderGoodsDetailVO> goodsList = new ArrayList<>();
        if (goodsListDto == null || goodsListDto.isEmpty()) {
            return goodsList;
        }

        String basePath = settingService.getUploadBasePath();
        
        // 获取会员折扣率
        BigDecimal memberDiscountRate = getMemberDiscountRate(userId, merchantId);

        for (ResCartDto cartDto : goodsListDto) {
            OrderGoodsDetailVO goodsVO = new OrderGoodsDetailVO();
            goodsVO.setGoodsId(cartDto.getGoodsId());
            goodsVO.setSkuId(cartDto.getSkuId() != null ? cartDto.getSkuId() : 0);
            goodsVO.setQuantity(cartDto.getNum() != null ? cartDto.getNum() : 0);
            goodsVO.setIsEffect(cartDto.getIsEffect() != null ? cartDto.getIsEffect() : true);
            goodsVO.setSpecList(cartDto.getSpecList());

            MtGoods goodsInfo = cartDto.getGoodsInfo();
            if (goodsInfo != null) {
                goodsVO.setGoodsName(goodsInfo.getName());
                goodsVO.setPrice(goodsInfo.getPrice() != null ? goodsInfo.getPrice() : BigDecimal.ZERO);
                goodsVO.setLinePrice(goodsInfo.getLinePrice() != null ? goodsInfo.getLinePrice() : BigDecimal.ZERO);
                
                // 处理商品图片
                String logo = goodsInfo.getLogo();
                if (StringUtils.isNotEmpty(logo) && !logo.startsWith("http")) {
                    logo = basePath + logo;
                }
                goodsVO.setGoodsImage(logo);

                // 计算小计（使用当前价格，已应用会员折扣）
                BigDecimal subtotal = goodsVO.getPrice().multiply(new BigDecimal(goodsVO.getQuantity()));
                goodsVO.setSubtotal(subtotal);

                // 计算会员折扣金额（单个商品）
                // 如果原价存在且大于当前价，说明有折扣
                if (goodsVO.getLinePrice() != null && goodsVO.getLinePrice().compareTo(goodsVO.getPrice()) > 0) {
                    BigDecimal originalSubtotal = goodsVO.getLinePrice().multiply(new BigDecimal(goodsVO.getQuantity()));
                    BigDecimal discountAmount = originalSubtotal.subtract(subtotal);
                    goodsVO.setMemberDiscount(discountAmount.max(BigDecimal.ZERO));
                } else {
                    // 如果会员折扣率小于1，说明有会员折扣，但商品可能没有原价
                    // 这种情况下，会员折扣会在总金额中体现，单个商品不单独计算
                    goodsVO.setMemberDiscount(BigDecimal.ZERO);
                }
            } else {
                // 如果商品信息为空，设置默认值
                goodsVO.setGoodsName("");
                goodsVO.setPrice(BigDecimal.ZERO);
                goodsVO.setLinePrice(BigDecimal.ZERO);
                goodsVO.setSubtotal(BigDecimal.ZERO);
                goodsVO.setMemberDiscount(BigDecimal.ZERO);
            }

            goodsList.add(goodsVO);
        }

        return goodsList;
    }

    /**
     * 获取会员折扣率
     */
    private BigDecimal getMemberDiscountRate(Integer userId, Integer merchantId) {
        try {
            MtUser userInfo = memberService.queryMemberById(userId);
            if (userInfo == null) {
                return BigDecimal.ONE;
            }

            MtUserGrade userGrade = userGradeService.queryUserGradeById(
                    merchantId,
                    userInfo.getGradeId() != null ? Integer.parseInt(userInfo.getGradeId()) : 1,
                    userId
            );

            if (userGrade != null && userGrade.getDiscount() != null && userGrade.getDiscount() > 0) {
                BigDecimal discount = BigDecimal.valueOf(userGrade.getDiscount())
                        .divide(new BigDecimal("10"), 2, BigDecimal.ROUND_HALF_UP);
                return discount.compareTo(BigDecimal.ZERO) > 0 ? discount : BigDecimal.ONE;
            }
        } catch (Exception e) {
            log.warn("获取会员折扣率失败: userId={}, merchantId={}", userId, merchantId, e);
        }
        return BigDecimal.ONE;
    }

}
