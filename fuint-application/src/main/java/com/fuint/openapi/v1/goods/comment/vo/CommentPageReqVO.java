package com.fuint.openapi.v1.goods.comment.vo;

import com.fuint.framework.pojo.PageParams;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 评价分页查询请求VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentPageReqVO extends PageParams {

    @ApiModelProperty(value = "商户ID")
    private Integer merchantId;

    @ApiModelProperty(value = "店铺ID")
    private Integer storeId;

    @ApiModelProperty(value = "商品ID")
    private Integer goodsId;

    @ApiModelProperty(value = "订单ID")
    private Integer orderId;

    @ApiModelProperty(value = "用户ID")
    private Integer userId;

    @ApiModelProperty(value = "评价类型: 1-商品评价 2-订单NPS评价")
    private Integer commentType;

    @ApiModelProperty(value = "评分(1-5或0-10)")
    private Integer score;

    @ApiModelProperty(value = "是否显示 Y/N")
    private String isShow;

    @ApiModelProperty(value = "状态")
    private String status;

    @ApiModelProperty(value = "开始时间")
    private Date startTime;

    @ApiModelProperty(value = "结束时间")
    private Date endTime;

}
