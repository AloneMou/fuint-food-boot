package com.fuint.openapi.v1.marketing.coupon.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 优惠券分页响应VO
 *
 * @author mjw
 * @since 2026/1/17
 */
@Data
@ApiModel(value = "优惠券分页响应VO")
public class MtCouponPageRespVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "总记录数", example = "100")
    private Long totalElements;

    @ApiModelProperty(value = "总页数", example = "10")
    private Integer totalPages;

    @ApiModelProperty(value = "当前页码", example = "1")
    private Integer currentPage;

    @ApiModelProperty(value = "每页大小", example = "10")
    private Integer pageSize;

    @ApiModelProperty(value = "优惠券列表")
    private List<MtCouponRespVO> content;
}
