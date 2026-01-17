package com.fuint.openapi.v1.marketing.coupon.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券创建请求VO
 *
 * @author mjw
 * @since 2026/1/17
 */
@Data
@ApiModel(value = "优惠券创建请求VO")
public class MtCouponCreateReqVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "分组ID不能为空")
    @ApiModelProperty(value = "分组ID", required = true, example = "1")
    private Integer groupId;

    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;

    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;

    @NotBlank(message = "优惠券类型不能为空")
    @ApiModelProperty(value = "券类型：C优惠券；P储值卡；T计次卡", required = true, example = "C")
    private String type;

    @NotBlank(message = "优惠券名称不能为空")
    @ApiModelProperty(value = "券名称", required = true, example = "满100减20优惠券")
    private String name;

    @ApiModelProperty(value = "是否允许转赠：0不允许，1允许", example = "1")
    private Integer isGive;

    @ApiModelProperty(value = "获得卡券所消耗积分", example = "0")
    private Integer point;

    @ApiModelProperty(value = "适用商品：allGoods全部商品、partGoods部分商品", example = "allGoods")
    private String applyGoods;

    @ApiModelProperty(value = "适用商品ID列表")
    private List<CouponGoodsItemVO> goodsList;

    @ApiModelProperty(value = "领取码", example = "")
    private String receiveCode;

    @ApiModelProperty(value = "使用专项", example = "")
    private String useFor;

    @NotBlank(message = "过期类型不能为空")
    @ApiModelProperty(value = "过期类型：FIX固定期限、FLEX领取后生效", required = true, example = "FIX")
    private String expireType;

    @ApiModelProperty(value = "有效天数（expireType=FLEX时必填）", example = "30")
    private Integer expireTime;

    @ApiModelProperty(value = "开始有效期（expireType=FIX时必填）", example = "2026-01-01 00:00:00")
    private String beginTime;

    @ApiModelProperty(value = "结束有效期（expireType=FIX时必填）", example = "2026-12-31 23:59:59")
    private String endTime;

    @NotNull(message = "优惠金额不能为空")
    @ApiModelProperty(value = "面额/固定金额", required = true, example = "20.00")
    private BigDecimal amount;

    @ApiModelProperty(value = "优惠费率（0-100，100表示全免）", example = "0")
    private Integer discountRate;

    @ApiModelProperty(value = "最大优惠金额", example = "50.00")
    private BigDecimal maxDiscountAmount;

    @NotBlank(message = "发放方式不能为空")
    @ApiModelProperty(value = "发放方式：front前台领取、backend后台发放、offline线下发放", required = true, example = "backend")
    private String sendWay;

    @ApiModelProperty(value = "每次发放数量", example = "1")
    private Integer sendNum;

    @ApiModelProperty(value = "发行总数量", example = "1000")
    private Integer total;

    @ApiModelProperty(value = "每人最多拥有数量", example = "1")
    private Integer limitNum;

    @ApiModelProperty(value = "不可用日期，逗号隔开。周末：weekend；其他：2026-01-01_2026-01-07", example = "")
    private String exceptTime;

    @ApiModelProperty(value = "适用店铺ID，逗号分隔", example = "1,2,3")
    private String storeIds;

    @ApiModelProperty(value = "适用会员等级，逗号分隔", example = "1,2")
    private String gradeIds;

    @ApiModelProperty(value = "描述信息", example = "满100元可用")
    private String description;

    @ApiModelProperty(value = "效果图片", example = "/uploads/coupon/001.jpg")
    private String image;

    @ApiModelProperty(value = "后台备注", example = "活动专用券")
    private String remarks;

    @ApiModelProperty(value = "获取券的规则（储值卡）", example = "")
    private String inRule;

    @ApiModelProperty(value = "核销规则（使用门槛）", example = "100")
    private String outRule;

    @ApiModelProperty(value = "状态：A正常、D删除", example = "A")
    private String status;

    @ApiModelProperty(value = "操作人", hidden = true)
    private String operator;
}
