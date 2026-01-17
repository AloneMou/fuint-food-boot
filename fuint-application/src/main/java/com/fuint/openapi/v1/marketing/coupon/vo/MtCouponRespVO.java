package com.fuint.openapi.v1.marketing.coupon.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 优惠券响应VO
 *
 * @author mjw
 * @since 2026/1/17
 */
@Data
@ApiModel(value = "优惠券响应VO")
public class MtCouponRespVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "优惠券ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "分组ID", example = "1")
    private Integer groupId;

    @ApiModelProperty(value = "分组名称", example = "活动券组")
    private String groupName;

    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;

    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;

    @ApiModelProperty(value = "券类型", example = "C")
    private String type;

    @ApiModelProperty(value = "券名称", example = "满100减20优惠券")
    private String name;

    @ApiModelProperty(value = "是否允许转赠", example = "true")
    private Boolean isGive;

    @ApiModelProperty(value = "获得卡券所消耗积分", example = "0")
    private Integer point;

    @ApiModelProperty(value = "适用商品", example = "allGoods")
    private String applyGoods;

    @ApiModelProperty(value = "适用商品列表")
    private List<CouponGoodsItemVO> goodsList;

    @ApiModelProperty(value = "领取码", example = "")
    private String receiveCode;

    @ApiModelProperty(value = "使用专项", example = "")
    private String useFor;

    @ApiModelProperty(value = "过期类型", example = "FIX")
    private String expireType;

    @ApiModelProperty(value = "有效天数", example = "30")
    private Integer expireTime;

    @ApiModelProperty(value = "开始有效期", example = "2026-01-01 00:00:00")
    private Date beginTime;

    @ApiModelProperty(value = "结束有效期", example = "2026-12-31 23:59:59")
    private Date endTime;

    @ApiModelProperty(value = "面额", example = "20.00")
    private BigDecimal amount;

    @ApiModelProperty(value = "优惠费率", example = "0")
    private Integer discountRate;

    @ApiModelProperty(value = "最大优惠金额", example = "50.00")
    private BigDecimal maxDiscountAmount;

    @ApiModelProperty(value = "发放方式", example = "backend")
    private String sendWay;

    @ApiModelProperty(value = "每次发放数量", example = "1")
    private Integer sendNum;

    @ApiModelProperty(value = "发行总数量", example = "1000")
    private Integer total;

    @ApiModelProperty(value = "每人最多拥有数量", example = "1")
    private Integer limitNum;

    @ApiModelProperty(value = "不可用日期", example = "")
    private String exceptTime;

    @ApiModelProperty(value = "适用店铺ID", example = "1,2,3")
    private String storeIds;

    @ApiModelProperty(value = "适用会员等级", example = "1,2")
    private String gradeIds;

    @ApiModelProperty(value = "描述信息", example = "满100元可用")
    private String description;

    @ApiModelProperty(value = "效果图片", example = "/uploads/coupon/001.jpg")
    private String image;

    @ApiModelProperty(value = "后台备注", example = "活动专用券")
    private String remarks;

    @ApiModelProperty(value = "获取券的规则", example = "")
    private String inRule;

    @ApiModelProperty(value = "核销规则", example = "100")
    private String outRule;

    @ApiModelProperty(value = "创建时间", example = "2026-01-17 10:00:00")
    private Date createTime;

    @ApiModelProperty(value = "更新时间", example = "2026-01-17 10:00:00")
    private Date updateTime;

    @ApiModelProperty(value = "最后操作人", example = "admin")
    private String operator;

    @ApiModelProperty(value = "状态", example = "A")
    private String status;

    @ApiModelProperty(value = "已发放数量", example = "100")
    private Integer sentNum;

    @ApiModelProperty(value = "剩余数量", example = "900")
    private Integer leftNum;
}
