package com.fuint.openapi.v1.member.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户优惠券响应VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "用户优惠券响应VO")
public class UserCouponRespVO {

    @ApiModelProperty(value = "用户优惠券ID", example = "1")
    private Integer userCouponId;

    @ApiModelProperty(value = "优惠券ID", example = "1")
    private Integer couponId;

    @ApiModelProperty(value = "优惠券名称", example = "满20减5")
    private String couponName;

    @ApiModelProperty(value = "优惠券类型：coupon-优惠券；prestore-储值卡；timer-计次卡", example = "coupon")
    private String couponType;

    @ApiModelProperty(value = "优惠金额（元）", example = "5.00")
    private BigDecimal amount;

    @ApiModelProperty(value = "储值卡余额（元）", example = "100.00")
    private BigDecimal balance;

    @ApiModelProperty(value = "优惠券编码", example = "ABC123")
    private String code;

    @ApiModelProperty(value = "状态：A-未使用；B-已使用；C-已过期；D-已删除；E-未领取", example = "A")
    private String status;

    @ApiModelProperty(value = "使用门槛说明", example = "满20元可用")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "有效期开始时间")
    private Date effectiveStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "有效期结束时间")
    private Date effectiveEndTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "使用时间")
    private Date usedTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
