package com.fuint.openapi.v1.member.coupon.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户优惠券分页响应VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "用户优惠券分页响应VO")
public class UserCouponPageRespVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "总记录数", example = "100")
    private Long totalElements;

    @ApiModelProperty(value = "总页数", example = "10")
    private Integer totalPages;

    @ApiModelProperty(value = "当前页码", example = "1")
    private Integer currentPage;

    @ApiModelProperty(value = "每页大小", example = "10")
    private Integer pageSize;

    @ApiModelProperty(value = "用户优惠券列表")
    private List<UserCouponRespVO> content;
}
