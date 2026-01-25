package com.fuint.repository.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 商品评价表
 * <p>
 * Created by FSQ
 * CopyRight https://www.fuint.cn
 */
@Getter
@Setter
@TableName("mt_goods_comment")
@ApiModel(value = "MtGoodsComment对象", description = "商品评价表")
public class MtGoodsComment implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("自增ID")
    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("商户ID")
    private Integer merchantId;

    @ApiModelProperty("店铺ID")
    private Integer storeId;

    @ApiModelProperty("订单ID")
    private Integer orderId;

    @ApiModelProperty("商品ID")
    private Integer goodsId;

    @ApiModelProperty("SKU ID")
    private Integer skuId;

    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("评价类型: 1-商品评价 2-订单NPS评价 3-价格评价")
    private Integer commentType;

    @ApiModelProperty("评分(1-5星或0-10分)")
    private Integer score;

    @ApiModelProperty("评价内容")
    private String content;

    @ApiModelProperty("商家回复内容")
    private String replyContent;

    @ApiModelProperty("商家回复时间")
    private Date replyTime;

    @ApiModelProperty("是否匿名评价 Y-是 N-否")
    private String isAnonymous;

    @ApiModelProperty("是否显示 Y-显示 N-隐藏")
    private String isShow;

    @ApiModelProperty("点赞数")
    private Integer likeCount;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("最后操作人")
    private String operator;

    @ApiModelProperty("状态 A-正常 D-删除")
    private String status;

}
