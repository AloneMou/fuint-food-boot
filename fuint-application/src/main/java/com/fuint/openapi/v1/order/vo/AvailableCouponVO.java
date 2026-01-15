package com.fuint.openapi.v1.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 可用优惠券VO
 *
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Data
@ApiModel(value = "可用优惠券VO")
public class AvailableCouponVO {

    @ApiModelProperty(value = "用户优惠券ID", example = "1")
    private Integer userCouponId;

    @ApiModelProperty(value = "优惠券ID", example = "1")
    private Integer couponId;

    @ApiModelProperty(value = "优惠券名称", example = "满20减5")
    private String couponName;

    @ApiModelProperty(value = "优惠券类型：coupon-优惠券；prestore-储值卡；timer-计次卡", example = "coupon")
    private String couponType;

    @ApiModelProperty(value = "优惠金额（元）", example = "5.00")
    private BigDecimal discountAmount;

    @ApiModelProperty(value = "使用状态：A-可用；N-不可用", example = "A")
    private String usable;

    @ApiModelProperty(value = "不可用原因", example = "订单金额不满足使用条件")
    private String unusableReason;

    @ApiModelProperty(value = "优惠券说明", example = "满20元可用")
    private String description;

    @ApiModelProperty(value = "有效期开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date effectiveStartTime;

    @ApiModelProperty(value = "有效期结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date effectiveEndTime;

    @ApiModelProperty(value = "是否选中", example = "true")
    private Boolean selected;

    @ApiModelProperty(value = "储值卡余额（元）", example = "100.00")
    private BigDecimal balance;
}
