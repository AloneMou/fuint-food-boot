package com.fuint.openapi.v1.order.vo;

import com.fuint.common.enums.OrderTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 订单预创建请求VO
 * <p>
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
    private OrderTypeEnum type;

    @NotEmpty(message = "订单商品列表不能为空")
    @ApiModelProperty(value = "订单商品列表")
    private List<OrderGoodsItemVO> items;

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
