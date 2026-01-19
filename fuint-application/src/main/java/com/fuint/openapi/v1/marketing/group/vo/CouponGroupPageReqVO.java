package com.fuint.openapi.v1.marketing.group.vo;

import com.fuint.framework.pojo.PageParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 优惠券分组分页查询请求VO
 *
 * @author mjw
 * @since 2026/1/17
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "优惠券分组分页查询请求", description = "优惠券分组列表查询的请求参数")
public class CouponGroupPageReqVO extends PageParams implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;

    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;

    @ApiModelProperty(value = "分组名称", example = "春节优惠券")
    private String name;
}
