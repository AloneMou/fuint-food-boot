package com.fuint.openapi.v1.marketing.group.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 优惠券分组响应VO
 *
 * @author mjw
 * @since 2026/1/17
 */
@Data
@ApiModel(value = "优惠券分组响应", description = "优惠券分组的响应数据")
public class CouponGroupRespVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "分组ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;

    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;

    @ApiModelProperty(value = "分组名称", example = "春节优惠券套餐")
    private String name;

    @ApiModelProperty(value = "价值金额", example = "100.00")
    private BigDecimal money;

    @ApiModelProperty(value = "券种类数量", example = "5")
    private Integer num;

    @ApiModelProperty(value = "发行数量", example = "1000")
    private Integer total;

    @ApiModelProperty(value = "分组描述", example = "春节特惠优惠券组合")
    private String description;

    @ApiModelProperty(value = "创建时间", example = "2026-01-17 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "更新时间", example = "2026-01-17 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty(value = "操作人", example = "admin")
    private String operator;

    @ApiModelProperty(value = "状态", example = "A", notes = "A：正常；D：删除")
    private String status;

    @ApiModelProperty(value = "已发放套数", example = "500")
    private Integer sendNum;

    @ApiModelProperty(value = "券种类数量", example = "3")
    private Integer couponNum;

}
