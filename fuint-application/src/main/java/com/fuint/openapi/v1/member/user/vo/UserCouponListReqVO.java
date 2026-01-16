package com.fuint.openapi.v1.member.user.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 用户优惠券列表查询请求VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "用户优惠券列表查询请求VO")
public class UserCouponListReqVO {

    @NotNull(message = "用户ID不能为空")
    @ApiModelProperty(value = "用户ID", required = true, example = "1")
    private Integer userId;

    @ApiModelProperty(value = "优惠券状态（精确匹配）：A-未使用；B-已使用；C-已过期；D-已删除；E-未领取", example = "A")
    private String status;

    @ApiModelProperty(value = "当前页码", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @ApiModelProperty(value = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量最小为1")
    private Integer pageSize = 10;
}
