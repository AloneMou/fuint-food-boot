package com.fuint.openapi.v1.marketing.group.vo;

import com.fuint.framework.pojo.PageParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 优惠券分组分页查询请求VO
 *
 * @author mjw
 * @since 2026/1/17
 */
@Data
@ApiModel(value = "优惠券分组分页查询请求", description = "优惠券分组列表查询的请求参数")
public class CouponGroupPageReqVO extends PageParams implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "页码", required = false, example = "1")
    private Integer pageNo;

    @ApiModelProperty(value = "每页数量", required = false, example = "20")
    private Integer pageSize;

    @ApiModelProperty(value = "商户ID", required = false, example = "1")
    private Integer merchantId;

    @ApiModelProperty(value = "店铺ID", required = false, example = "1")
    private Integer storeId;

    @ApiModelProperty(value = "分组名称", required = false, example = "春节优惠券")
    private String name;

    @ApiModelProperty(value = "状态", required = false, example = "A")
    private String status;

}
