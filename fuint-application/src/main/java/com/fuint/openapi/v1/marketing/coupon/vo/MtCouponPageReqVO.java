package com.fuint.openapi.v1.marketing.coupon.vo;

import com.fuint.framework.pojo.PageParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 优惠券分页查询请求VO
 *
 * @author mjw
 * @since 2026/1/17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "优惠券分页查询请求VO")
public class MtCouponPageReqVO extends PageParams implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "优惠券ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "分组ID", example = "1")
    private Integer groupId;

    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;

    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;

    @ApiModelProperty(value = "券名称（模糊查询）", example = "满减券")
    private String name;

    @ApiModelProperty(value = "券类型：C优惠券；P储值卡；T计次卡", example = "C")
    private String type;

    @ApiModelProperty(value = "状态：A正常、D删除", example = "A")
    private String status;
}
