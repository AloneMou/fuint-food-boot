package com.fuint.openapi.v1.marketing.coupon.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 发券请求VO
 *
 * @author mjw
 * @since 2026/1/17
 */
@Data
@ApiModel(value = "发券请求VO")
public class CouponSendReqVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "优惠券ID不能为空")
    @ApiModelProperty(value = "优惠券ID", required = true, example = "1")
    private Integer couponId;

    @ApiModelProperty(value = "发放对象：single单个用户、batch批量用户、group会员分组")
    @NotNull(message = "发放对象不能为空")
    private String sendObject;

    @ApiModelProperty(value = "单个用户ID（sendObject=single时必填）", example = "100")
    private Integer userId;

    @ApiModelProperty(value = "用户手机号（sendObject=single时可选）", example = "13800138000")
    private String mobile;

    @ApiModelProperty(value = "批量用户ID列表（sendObject=batch时必填）")
    private List<Integer> userIds;

    @ApiModelProperty(value = "会员分组ID（sendObject=group时必填）", example = "1")
    private Integer groupId;

    @NotNull(message = "发放数量不能为空")
    @ApiModelProperty(value = "每人发放数量", required = true, example = "1")
    private Integer num;

//    @ApiModelProperty(value = "是否发送消息通知", example = "false")
//    private Boolean sendMessage;

    @ApiModelProperty(value = "操作人", hidden = true)
    private String operator;
}
