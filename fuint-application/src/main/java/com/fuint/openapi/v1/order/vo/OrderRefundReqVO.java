package com.fuint.openapi.v1.order.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 订单退款请求VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "订单退款请求VO")
public class OrderRefundReqVO {

    @NotNull(message = "订单ID不能为空")
    @ApiModelProperty(value = "订单ID", required = true, example = "1")
    private Integer orderId;

    @ApiModelProperty(value = "用户ID（用于权限验证）", example = "1")
    private Integer userId;

    @NotNull(message = "退款金额不能为空")
    @ApiModelProperty(value = "退款金额", required = true, example = "10.00")
    private BigDecimal amount;

    @ApiModelProperty(value = "退款原因", example = "商品缺货")
    private String remark;
}
