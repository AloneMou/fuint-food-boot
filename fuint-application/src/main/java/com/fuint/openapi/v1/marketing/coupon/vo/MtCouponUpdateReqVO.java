package com.fuint.openapi.v1.marketing.coupon.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * 优惠券更新请求VO
 *
 * @author mjw
 * @since 2026/1/17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "优惠券更新请求VO")
public class MtCouponUpdateReqVO extends MtCouponCreateReqVO {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "优惠券ID不能为空")
    @ApiModelProperty(value = "优惠券ID", required = true, example = "1")
    private Integer id;
}
