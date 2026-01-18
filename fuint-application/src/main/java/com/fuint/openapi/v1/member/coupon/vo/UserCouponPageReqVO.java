package com.fuint.openapi.v1.member.coupon.vo;

import com.fuint.framework.pojo.PageParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * 用户优惠券列表查询请求VO
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "用户优惠券列表查询请求VO")
public class UserCouponPageReqVO extends PageParams {

    @NotNull(message = "用户ID不能为空")
    @ApiModelProperty(value = "用户ID", required = true, example = "1")
    private Integer userId;

    @ApiModelProperty(value = "优惠券状态（精确匹配）：A-未使用；B-已使用；C-已过期；D-已删除", example = "A")
    private String status;

}
