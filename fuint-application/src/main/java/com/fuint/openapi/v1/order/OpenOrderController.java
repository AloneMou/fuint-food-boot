package com.fuint.openapi.v1.order;

import com.fuint.common.dto.ResCartDto;
import com.fuint.common.enums.OrderModeEnum;
import com.fuint.common.enums.OrderTypeEnum;
import com.fuint.common.enums.StatusEnum;
import com.fuint.common.service.*;
import com.fuint.framework.exception.BusinessCheckException;
import com.fuint.framework.pojo.CommonResult;
import com.fuint.framework.web.BaseController;
import com.fuint.openapi.v1.order.vo.*;
import com.fuint.repository.model.MtCart;
import com.fuint.repository.model.MtGoods;
import com.fuint.repository.model.MtUser;
import com.fuint.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * OpenAPI订单相关接口
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Validated
@Api(tags = "OpenApi-订单相关接口")
@RestController
@RequestMapping(value = "/api/v1/order")
public class OpenOrderController extends BaseController {

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

    /**
     * 订单预创建（实时算价）
     *
     * @param reqVO 预创建请求参数
     * @return 订单预创建结果
     * @throws BusinessCheckException 业务异常
     */
    @ApiOperation(value = "订单预创建（实时算价）", notes = "不实际创建订单，仅进行价格试算和优惠券匹配")
    @PostMapping(value = "/pre-create")
    public CommonResult<OrderPreCreateRespVO> preCreateOrder(@Valid @RequestBody OrderPreCreateReqVO reqVO) throws BusinessCheckException {
        
        // 验证用户是否存在
        MtUser userInfo = memberService.queryMemberById(reqVO.getUserId());
        if (userInfo == null) {
            return CommonResult.error(404, "用户不存在");
        }

        // 设置默认值
        Integer merchantId = reqVO.getMerchantId() != null ? reqVO.getMerchantId() : 1;
        Integer storeId = reqVO.getStoreId() != null ? reqVO.getStoreId() : 0;
        String orderType = StringUtil.isNotEmpty(reqVO.getType()) ? reqVO.getType() : OrderTypeEnum.GOOGS.getKey();
        String orderMode = StringUtil.isNotEmpty(reqVO.getOrderMode()) ? reqVO.getOrderMode() : OrderModeEnum.ONESELF.getKey();
        String platform = StringUtil.isNotEmpty(reqVO.getPlatform()) ? reqVO.getPlatform() : "MP-WEIXIN";
        Integer userCouponId = reqVO.getUserCouponId() != null ? reqVO.getUserCouponId() : 0;
        Integer usePoint = reqVO.getUsePoint() != null ? reqVO.getUsePoint() : 0;

        // 构建购物车列表
        List<MtCart> cartList = new ArrayList<>();

        // 从购物车ID获取
        if (StringUtil.isNotEmpty(reqVO.getCartIds())) {
            Map<String, Object> params = new java.util.HashMap<>();
            params.put("status", StatusEnum.ENABLED.getKey());
            params.put("ids", reqVO.getCartIds());
            cartList = cartService.queryCartListByParams(params);
            if (cartList.isEmpty()) {
                return CommonResult.error(400, "购物车商品不存在");
            }
        }
        // 从商品列表获取
        else if (reqVO.getItems() != null && !reqVO.getItems().isEmpty()) {
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
        else if (reqVO.getGoodsId() != null && reqVO.getGoodsId() > 0) {
            MtCart cart = new MtCart();
            cart.setGoodsId(reqVO.getGoodsId());
            cart.setSkuId(reqVO.getSkuId() != null ? reqVO.getSkuId() : 0);
            cart.setNum(reqVO.getBuyNum() != null ? reqVO.getBuyNum() : 1);
            cart.setUserId(reqVO.getUserId());
            cart.setStatus(StatusEnum.ENABLED.getKey());
            cart.setId(0);
            cartList.add(cart);
        } else {
            return CommonResult.error(400, "请提供购物商品信息");
        }

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
}
