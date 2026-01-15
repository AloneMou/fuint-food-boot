package com.fuint.openapi.v1.order.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单预创建请求VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "订单预创建请求VO")
public class OrderPreCreateReqVO {

    @NotNull(message = "用户ID不能为空")
    @ApiModelProperty(value = "用户ID", required = true, example = "1")
    private Integer userId;

    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;

    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;

    @ApiModelProperty(value = "订单类型：googs-商品订单；payment-付款订单", example = "googs")
    private String type;

    @ApiModelProperty(value = "订单商品列表")
    private List<OrderGoodsItemVO> items;

    @ApiModelProperty(value = "购物车ID列表，逗号分隔", example = "1,2,3")
    private String cartIds;

    @ApiModelProperty(value = "商品ID（立即购买）", example = "1")
    private Integer goodsId;

    @ApiModelProperty(value = "商品SKU ID（立即购买）", example = "1")
    private Integer skuId;

    @ApiModelProperty(value = "购买数量（立即购买）", example = "1")
    private Integer buyNum;

    @ApiModelProperty(value = "用户优惠券ID（指定使用的优惠券）", example = "1")
    private Integer userCouponId;

    @ApiModelProperty(value = "使用积分数量", example = "100")
    private Integer usePoint;

    @ApiModelProperty(value = "订单模式：express-配送；oneself-自取", example = "oneself")
    private String orderMode;

    @ApiModelProperty(value = "备注", example = "少糖")
    private String remark;

    @ApiModelProperty(value = "桌台ID", example = "1")
    private Integer tableId;

    @ApiModelProperty(value = "平台：H5、MP-WEIXIN等", example = "MP-WEIXIN")
    private String platform;
}
