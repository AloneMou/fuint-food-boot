package com.fuint.openapi.v1.goods.comment.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 评价响应VO
 */
@Data
public class CommentRespVO {

    @ApiModelProperty(value = "评价ID")
    private Integer id;

    @ApiModelProperty(value = "商户ID")
    private Integer merchantId;

    @ApiModelProperty(value = "店铺ID")
    private Integer storeId;

    @ApiModelProperty(value = "店铺名称")
    private String storeName;

    @ApiModelProperty(value = "订单ID")
    private Integer orderId;

    @ApiModelProperty(value = "订单编号")
    private String orderSn;

    @ApiModelProperty(value = "商品ID")
    private Integer goodsId;

    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @ApiModelProperty(value = "商品图片")
    private String goodsImage;

    @ApiModelProperty(value = "SKU ID")
    private Integer skuId;

    @ApiModelProperty(value = "SKU规格信息")
    private String skuSpec;

    @ApiModelProperty(value = "用户ID")
    private Integer userId;

    @ApiModelProperty(value = "用户昵称")
    private String userName;

    @ApiModelProperty(value = "用户头像")
    private String userAvatar;

    @ApiModelProperty(value = "评价类型: 1-商品评价 2-订单NPS评价")
    private Integer commentType;

    @ApiModelProperty(value = "评分(1-5星或0-10分)")
    private Integer score;

    @ApiModelProperty(value = "评价内容")
    private String content;

    @ApiModelProperty(value = "评价图片列表")
    private List<String> images;

    @ApiModelProperty(value = "商家回复内容")
    private String replyContent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "商家回复时间")
    private Date replyTime;

    @ApiModelProperty(value = "是否匿名评价 Y-是 N-否")
    private String isAnonymous;

    @ApiModelProperty(value = "是否显示 Y-显示 N-隐藏")
    private String isShow;

    @ApiModelProperty(value = "点赞数")
    private Integer likeCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "状态 A-正常 D-删除")
    private String status;

}
