package com.fuint.openapi.v1.marketing.group.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 更新优惠券分组请求VO
 *
 * @author mjw
 * @since 2026/1/17
 */
@Data
@ApiModel(value = "更新优惠券分组请求", description = "更新优惠券分组的请求参数")
public class CouponGroupUpdateReqVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "分组ID", required = true, example = "1")
    @NotNull(message = "分组ID不能为空")
    private Integer id;

    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;

    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;

    @NotBlank(message = "分组名称不能为空")
    @ApiModelProperty(value = "分组名称", example = "春节优惠券套餐")
    private String name;

    @ApiModelProperty(value = "分组描述", required = false, example = "春节特惠优惠券组合")
    private String description;
}
