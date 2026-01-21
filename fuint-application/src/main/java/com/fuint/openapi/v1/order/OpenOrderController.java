package com.fuint.openapi.v1.order;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.ratelimiter.core.annotation.RateLimiter;
import cn.iocoder.yudao.framework.ratelimiter.core.keyresolver.impl.ClientIpRateLimiterKeyResolver;
import cn.iocoder.yudao.framework.signature.core.annotation.ApiSignature;
import com.fuint.common.dto.UserOrderDto;
import com.fuint.common.enums.OrderModeEnum;
import com.fuint.common.enums.StatusEnum;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pojo.CommonResult;
import com.fuint.framework.pojo.PageResult;
import com.fuint.framework.web.BaseController;
import com.fuint.openapi.service.OpenApiOrderService;
import com.fuint.openapi.v1.order.vo.*;
import com.fuint.repository.model.MtCart;
import com.fuint.repository.model.MtUserAction;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

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

    @Resource
    private OpenApiOrderService openApiOrderService;

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

        // 设置默认值
        String orderMode = StringUtils.isNotEmpty(reqVO.getOrderMode()) ? reqVO.getOrderMode() : OrderModeEnum.ONESELF.getKey();
        String platform = StringUtils.isNotEmpty(reqVO.getPlatform()) ? reqVO.getPlatform() : "MP-WEIXIN";
        Integer merchantId = reqVO.getMerchantId() != null ? reqVO.getMerchantId() : 1;
        Integer storeId = reqVO.getStoreId() != null ? reqVO.getStoreId() : 0;
        Integer userCouponId = reqVO.getUserCouponId() != null ? reqVO.getUserCouponId() : 0;
        Integer usePoint = reqVO.getUsePoint() != null ? reqVO.getUsePoint() : 0;

        OrderPreCreateRespVO result = openApiOrderService.preCreateOrder(
                merchantId,
                reqVO.getUserId(),
                cartList,
                userCouponId,
                usePoint,
                platform,
                orderMode,
                storeId
        );
        return CommonResult.success(result);
    }
    
    // Re-writing preCreateOrder to include conversion
    
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
        UserOrderDto result = openApiOrderService.createOrder(reqVO);
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
        Boolean result = openApiOrderService.cancelOrder(reqVO.getOrderId(), reqVO.getRemark(), reqVO.getUserId());
        return CommonResult.success(result);
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
        Boolean result = openApiOrderService.payOrder(reqVO);
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
        Boolean result = openApiOrderService.refundOrder(reqVO);
        return CommonResult.success(result);
    }

    /**
     * 获取订单详情
     *
     * @param reqVO 订单详情请求参数
     * @return 订单详情
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "订单详情", notes = "包含订单与订单商品所有信息，预计等待时间，前有多少杯咖啡")
    @GetMapping(value = "/detail")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<OrderDetailRespVO> getOrderDetail(@Valid OrderDetailReqVO reqVO) throws BusinessCheckException {
        OrderDetailRespVO result = openApiOrderService.getOrderDetail(reqVO);
        return CommonResult.success(result);
    }

    /**
     * 订单列表
     *
     * @param reqVO 查询参数
     * @return 订单列表
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "订单列表", notes = "支持多条件分页查询，使用MyBatis Plus优化性能")
    @GetMapping(value = "/list")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<PageResult<UserOrderDto>> getOrderList(@Valid OrderListReqVO reqVO) throws BusinessCheckException {
        PageResult<UserOrderDto> result = openApiOrderService.getOrderList(reqVO);
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
        Boolean result = openApiOrderService.evaluateOrder(reqVO);
        return CommonResult.success(result);
    }

    /**
     * 订单评价拉取
     *
     * @param reqVO 查询参数
     * @return 评价列表
     */
    @ApiOperation(value = "订单评价拉取", notes = "支持分页拉取，时间范围筛选，商品SKU范围筛选，使用MyBatis Plus优化")
    @GetMapping(value = "/evaluations")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<PageResult<MtUserAction>> getEvaluations(@Valid EvaluationPageReqVO reqVO) {
        PageResult<MtUserAction> result = openApiOrderService.getEvaluations(reqVO);
        return CommonResult.success(result);
    }

    /**
     * 标记订单可取餐
     *
     * @param reqVO 标记订单可取餐请求参数
     * @return 操作结果
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "标记订单可取餐", notes = "标记订单商品可取餐，并发送可取餐状态通知回调")
    @PostMapping(value = "/ready")
    @ApiSignature
    @RateLimiter(keyResolver = ClientIpRateLimiterKeyResolver.class)
    public CommonResult<Boolean> markOrderReady(@Valid @RequestBody OrderReadyReqVO reqVO) throws BusinessCheckException {
        Boolean result = openApiOrderService.markOrderReady(reqVO);
        return CommonResult.success(result);
    }
}
