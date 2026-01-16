package com.fuint.openapi.v1.member.user.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 用户优惠券列表分页响应VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "用户优惠券列表分页响应VO")
public class UserCouponListPageRespVO {

    @ApiModelProperty(value = "当前页码", example = "1")
    private Integer page;

    @ApiModelProperty(value = "每页数量", example = "10")
    private Integer pageSize;

    @ApiModelProperty(value = "总记录数", example = "100")
    private Long total;

    @ApiModelProperty(value = "总页数", example = "10")
    private Integer totalPages;

    @ApiModelProperty(value = "优惠券列表")
    private List<UserCouponRespVO> list;
}
