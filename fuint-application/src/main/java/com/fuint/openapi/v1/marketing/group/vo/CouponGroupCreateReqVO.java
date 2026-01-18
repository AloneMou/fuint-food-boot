package com.fuint.openapi.v1.marketing.group.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 创建优惠券分组请求VO
 *
 * @author mjw
 * @since 2026/1/17
 */
@Data
@ApiModel(value = "创建优惠券分组请求", description = "创建优惠券分组的请求参数")
public class CouponGroupCreateReqVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商户ID", required = true, example = "1")
    @NotNull(message = "商户ID不能为空")
    private Integer merchantId;

    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;

    @ApiModelProperty(value = "分组名称", required = true, example = "春节优惠券套餐")
    @NotBlank(message = "分组名称不能为空")
    private String name;

    @ApiModelProperty(value = "分组描述", example = "春节特惠优惠券组合")
    private String description;
}
