package com.fuint.openapi.v1.marketing.coupon.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fuint.common.enums.CouponExpireTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 优惠券更新请求VO
 *
 * @author mjw
 * @since 2026/1/17
 */
@Data
@ApiModel(value = "优惠券更新请求VO")
public class MtCouponUpdateReqVO {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "优惠券ID不能为空")
    @ApiModelProperty(value = "优惠券ID", required = true, example = "1")
    private Integer id;

    @NotNull(message = "分组ID不能为空")
    @ApiModelProperty(value = "分组ID", required = true, example = "1")
    private Integer groupId;

    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;

    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;

    @NotBlank(message = "优惠券名称不能为空")
    @ApiModelProperty(value = "券名称", required = true, example = "满100减20优惠券")
    private String name;

    @ApiModelProperty(value = "是否允许转赠：0不允许，1允许", example = "1")
    private Integer isGive;

    @ApiModelProperty(value = "获得卡券所消耗积分", example = "0")
    private Integer point;

    @NotBlank(message = "适用商品不能为空")
    @ApiModelProperty(value = "适用商品：allGoods全部商品、partGoods部分商品", example = "allGoods")
    private String applyGoods;

    @ApiModelProperty(value = "适用商品ID列表")
    private List<Long> goodsList;

    @ApiModelProperty(value = "领取码", example = "")
    private String receiveCode;

    @ApiModelProperty(value = "使用专项", example = "")
    private String useFor;

    @NotNull(message = "过期类型不能为空")
    @ApiModelProperty(value = "过期类型：FIX固定期限、FLEX领取后F生效", required = true, example = "FIX")
    private CouponExpireTypeEnum expireType;

    @ApiModelProperty(value = "有效天数（expireType=FLEX时必填）", example = "30")
    private Integer expireTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "开始有效期（expireType=FIX时必填）", example = "2026-01-01 00:00:00")
    private Date beginTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "结束有效期（expireType=FIX时必填）", example = "2026-12-31 23:59:59")
    private Date endTime;

    @NotNull(message = "优惠金额不能为空")
    @ApiModelProperty(value = "面额/固定金额", required = true, example = "20.00")
    private BigDecimal amount;

    @NotBlank(message = "发放方式不能为空")
    @ApiModelProperty(value = "发放方式：front前台领取、backend后台发放、offline线下发放", required = true, example = "backend")
    private String sendWay;

//    @ApiModelProperty(value = "每次发放数量", example = "1")
//    private Integer sendNum;

    @ApiModelProperty(value = "发行总数量", example = "1000")
    private Integer total;

    @ApiModelProperty(value = "每人最多拥有数量", example = "1")
    private Integer limitNum;

    @ApiModelProperty(value = "不可用日期，逗号隔开。周末：weekend；其他：2026-01-01_2026-01-07", example = "")
    private String exceptTime;

    @ApiModelProperty(value = "适用店铺ID")
    private List<Integer> storeIds;

    @ApiModelProperty(value = "适用会员等级")
    private List<Integer> gradeIds;

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
