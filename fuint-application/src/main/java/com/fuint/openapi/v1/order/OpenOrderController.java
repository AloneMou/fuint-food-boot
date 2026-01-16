package com.fuint.openapi.v1.order;

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
import com.fuint.openapi.v1.order.vo.*;
import com.fuint.repository.mapper.MtOrderGoodsMapper;
import com.fuint.repository.mapper.MtUserActionMapper;
import com.fuint.repository.model.*;
import com.fuint.utils.StringUtil;
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
            return CommonResult.error(404, "用户不存在");
        }

        // 设置默认值
        Integer merchantId = reqVO.getMerchantId() != null ? reqVO.getMerchantId() : 1;
        Integer storeId = reqVO.getStoreId() != null ? reqVO.getStoreId() : 0;
        String orderMode = StringUtil.isNotEmpty(reqVO.getOrderMode()) ? reqVO.getOrderMode() : OrderModeEnum.ONESELF.getKey();
        String platform = StringUtil.isNotEmpty(reqVO.getPlatform()) ? reqVO.getPlatform() : "MP-WEIXIN";
        Integer userCouponId = reqVO.getUserCouponId() != null ? reqVO.getUserCouponId() : 0;
        Integer usePoint = reqVO.getUsePoint() != null ? reqVO.getUsePoint() : 0;

        // 构建购物车列表
        List<MtCart> cartList = new ArrayList<>();

        // 从购物车ID获取
//        if (StringUtil.isNotEmpty(reqVO.getCartIds())) {
//            Map<String, Object> params = new java.util.HashMap<>();
//            params.put("status", StatusEnum.ENABLED.getKey());
//            params.put("ids", reqVO.getCartIds());
//            cartList = cartService.queryCartListByParams(params);
//            if (cartList.isEmpty()) {
//                return CommonResult.error(400, "购物车商品不存在");
//            }
//        }
        // 从商品列表获取
//        else
        if (reqVO.getItems() != null && !reqVO.getItems().isEmpty()) {
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
        // 从立即购买参数获取
//        else if (reqVO.getGoodsId() != null && reqVO.getGoodsId() > 0) {
//            MtCart cart = new MtCart();
//            cart.setGoodsId(reqVO.getGoodsId());
//            cart.setSkuId(reqVO.getSkuId() != null ? reqVO.getSkuId() : 0);
//            cart.setNum(reqVO.getBuyNum() != null ? reqVO.getBuyNum() : 1);
//            cart.setUserId(reqVO.getUserId());
//            cart.setStatus(StatusEnum.ENABLED.getKey());
//            cart.setId(0);
//            cartList.add(cart);
//        } else {
//            return CommonResult.error(400, "请提供购物商品信息");
//        }

        // 调用订单预创建服务
        Map<String, Object> preCreateResult = orderService.preCreateOrder(
                merchantId,
                reqVO.getUserId(),
                cartList,
                userCouponId,
                usePoint,
                platform,
                orderMode,
                storeId
        );

        // 构建响应VO
        OrderPreCreateRespVO respVO = new OrderPreCreateRespVO();
        respVO.setTotalAmount((BigDecimal) preCreateResult.get("totalAmount"));
        respVO.setDiscountAmount((BigDecimal) preCreateResult.get("discountAmount"));
        respVO.setPointAmount((BigDecimal) preCreateResult.get("pointAmount"));
        respVO.setDeliveryFee((BigDecimal) preCreateResult.get("deliveryFee"));
        respVO.setPayableAmount((BigDecimal) preCreateResult.get("payableAmount"));
        respVO.setUsePoint((Integer) preCreateResult.get("usePoint"));
        respVO.setAvailablePoint((Integer) preCreateResult.get("availablePoint"));
        respVO.setSelectedCouponId((Integer) preCreateResult.get("selectedCouponId"));
        respVO.setCalculateTime((Date) preCreateResult.get("calculateTime"));

        // 转换优惠券列表
        List<Map<String, Object>> availableCouponsMap = (List<Map<String, Object>>) preCreateResult.get("availableCoupons");
        List<AvailableCouponVO> availableCoupons = new ArrayList<>();
        if (availableCouponsMap != null) {
            for (Map<String, Object> couponMap : availableCouponsMap) {
                AvailableCouponVO couponVO = new AvailableCouponVO();
                couponVO.setUserCouponId((Integer) couponMap.get("userCouponId"));
                couponVO.setCouponId((Integer) couponMap.get("couponId"));
                couponVO.setCouponName((String) couponMap.get("couponName"));
                couponVO.setCouponType((String) couponMap.get("couponType"));
                couponVO.setDiscountAmount((BigDecimal) couponMap.get("discountAmount"));
                couponVO.setUsable((String) couponMap.get("usable"));
                couponVO.setDescription((String) couponMap.get("description"));
                couponVO.setSelected((Boolean) couponMap.get("selected"));
                if (couponMap.get("balance") != null) {
                    couponVO.setBalance((BigDecimal) couponMap.get("balance"));
                }
                availableCoupons.add(couponVO);
            }
        }
        respVO.setAvailableCoupons(availableCoupons);

        // 转换商品列表
        List<ResCartDto> goodsListDto = (List<ResCartDto>) preCreateResult.get("goodsList");
        List<OrderGoodsDetailVO> goodsList = new ArrayList<>();
        String basePath = settingService.getUploadBasePath();
        if (goodsListDto != null) {
            for (ResCartDto cartDto : goodsListDto) {
                OrderGoodsDetailVO goodsVO = new OrderGoodsDetailVO();
                goodsVO.setGoodsId(cartDto.getGoodsId());
                goodsVO.setSkuId(cartDto.getSkuId());
                goodsVO.setQuantity(cartDto.getNum());

                MtGoods goodsInfo = cartDto.getGoodsInfo();
                if (goodsInfo != null) {
                    goodsVO.setGoodsName(goodsInfo.getName());
                    goodsVO.setPrice(goodsInfo.getPrice());
                    goodsVO.setLinePrice(goodsInfo.getLinePrice());

                    String logo = goodsInfo.getLogo();
                    if (StringUtil.isNotEmpty(logo) && !logo.startsWith("http")) {
                        logo = basePath + logo;
                    }
                    goodsVO.setGoodsImage(logo);

                    BigDecimal subtotal = goodsInfo.getPrice().multiply(new BigDecimal(cartDto.getNum()));
                    goodsVO.setSubtotal(subtotal);
                }

                goodsList.add(goodsVO);
            }
        }
        respVO.setGoodsList(goodsList);

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
        String orderMode = StringUtil.isNotEmpty(reqVO.getOrderMode()) ? reqVO.getOrderMode() : OrderModeEnum.ONESELF.getKey();
        String platform = StringUtil.isNotEmpty(reqVO.getPlatform()) ? reqVO.getPlatform() : "MP-WEIXIN";
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

}
