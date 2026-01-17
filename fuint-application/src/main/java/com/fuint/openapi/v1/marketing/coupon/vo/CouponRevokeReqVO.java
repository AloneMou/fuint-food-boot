package com.fuint.openapi.v1.marketing.coupon.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 撤销券请求VO
 *
 * @author mjw
 * @since 2026/1/17
 */
@Data
@ApiModel(value = "撤销券请求VO")
public class CouponRevokeReqVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "优惠券ID不能为空")
    @ApiModelProperty(value = "优惠券ID", required = true, example = "1")
    private Integer couponId;

    @NotBlank(message = "批次号不能为空")
    @ApiModelProperty(value = "批次号（发券时返回的UUID）", required = true, example = "a1b2c3d4e5f6g7h8i9j0")
    private String uuid;

    @ApiModelProperty(value = "操作人", hidden = true)
    private String operator;
}
