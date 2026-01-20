package com.fuint.openapi.v1.marketing.coupon.vo;

import com.fuint.common.enums.CouponExpireTypeEnum;
import com.fuint.framework.pojo.PageParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 优惠券分页查询请求VO
 *
 * @author mjw
 * @since 2026/1/17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "优惠券分页查询请求VO")
public class MtCouponPageReqVO extends PageParams implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "优惠券ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "分组ID", example = "1")
    private Integer groupId;

    @ApiModelProperty(value = "商户ID", example = "1")
    private Integer merchantId;

    @ApiModelProperty(value = "店铺ID", example = "1")
    private Integer storeId;

    @ApiModelProperty(value = "券名称（模糊查询）", example = "满减券")
    private String name;

    @ApiModelProperty(value = "券类型：C优惠券；P储值卡；T计次卡", example = "C")
    private String type;

    @ApiModelProperty(value = "商品ID", example = "1")
    private Integer goodsId;

    @ApiModelProperty(value = "过期类型：FIX固定期限、FLEX领取后F生效", required = true, example = "FIX")
    private CouponExpireTypeEnum expireType;

    @ApiModelProperty(value = "优惠券ID列表", example = "[1,2,3]", hidden = true)
    private List<Integer> couponIds;
}
